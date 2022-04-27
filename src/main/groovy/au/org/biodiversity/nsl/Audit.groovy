package au.org.biodiversity.nsl

import grails.converters.JSON
import grails.util.GrailsClassUtils
import groovy.sql.GroovyResultSet
import org.grails.web.json.JSONException

import java.sql.Timestamp

/**
 * User: pmcneil
 * Date: 31/01/17
 *
 */
class Audit {

    final long eventId
    final String table
    final String action
    final String sessionUserName
    final Map rowData
    final Map changedFields
    final Timestamp actionTimeStamp
    final String updatedAtTimestamp
    final Object auditedObj
    final Class auditedClass

    private HashSet<String> relevantChangedFields
    private HashSet<String> relevantRowData

    Audit(GroovyResultSet row) {
        this.eventId = row.event_id
        this.actionTimeStamp = row.action_tstamp_tx
        this.table = row.table_name
        this.sessionUserName = row.session_user_name
        this.action = row.action
        this.rowData = JSON.parse((String) row.rd) as Map
        this.updatedAtTimestamp = this.rowData?.updated_at
        if (row.cf) {
            this.changedFields = JSON.parse((String) row.cf) as Map
        } else {
            this.changedFields = [:]
        }
        this.auditedClass = Class.forName('au.org.biodiversity.nsl.' + snakeToCamel(table).capitalize())
        this.auditedObj = getTheAuditedObject()
    }

    String toString() {
        String thing = auditedObj ? auditedObj.toString() : "$table ${rowData.id}"
        switch (action) {
            case 'I':
                "${eventId} ${when()}: ${updatedBy()} CREATED $thing: ${rowData}"
                break
            case 'D':
                "${eventId} ${when()}: ${updatedBy()}(?) DELETED $thing: ${rowData}"
                break
            case 'U':
                "${eventId} ${when()}: ${updatedBy()} CHANGED $thing: ${diffFields()}"
                break
            default:
                "${eventId} ${when()}: ${updatedBy()} $action $thing: ${diffFields()}"
        }
    }


    String updatedBy() {
        switch (action) {
            case 'I':
                rowData.created_by
                break
            case 'D':
                rowData.updated_by
                break
            case 'U':
                changedFields.updated_by ?: rowData.updated_by ?: 'Something'
                break
            default:
                changedFields.updated_by ?: rowData.updated_by ?: 'Something'
        }
    }

    String when() {
        changedFields.updated_at ?: actionTimeStamp.toString()
    }

    HashSet<String> getRelevantChangedFields() {
        if (!relevantChangedFields) {
            relevantChangedFields = new HashSet(changedFields.keySet())
            relevantChangedFields.removeAll(['lock_version', 'updated_by', 'updated_at'])
        }
        return relevantChangedFields
    }

    HashSet<String> getRelevantRowData() {
        if (!relevantRowData) {
            relevantRowData = new HashSet(rowData.keySet())
            relevantRowData.removeAll(['lock_version', 'updated_by', 'updated_at', 'trash', 'namespace', 'language', 'valid_record'])
        }
        return relevantRowData
    }


    String diffFields() {
        diffFieldList().join(', ')
    }

    List<String> diffFieldList() {
        List<String> diff = []
        fieldDiffs().each { Diff d ->
            diff << "$d.fieldName ${d.before} -> ${d.after}"
        }
        return diff
    }

    List<Diff> jsonSubDiffs(String tableName, String prefix, Map olds, Map news) {
        List<Diff> res = new ArrayList<>()
        Set keys = new HashSet<>()
        if (olds) {
            keys += olds.keySet()
        }
        if (news) {
            keys += news.keySet()
        }
        for (String it in keys) {
            Object o = olds instanceof Map && olds.containsKey(it) ? olds.get(it) : null
            Object n = news instanceof Map && news.containsKey(it) ? news.get(it) : null
            if (o instanceof Map || n instanceof Map) {
                res.addAll(jsonSubDiffs(tableName, prefix + '.' + it.replaceAll('\\.', ''), o as Map, n as Map))
            } else {
                if (o != n) {
                    res.add(new Diff(tableName, prefix + '.' + it.replaceAll('\\.', ''), o, n))
                }
            }
        }
        return res
    }

    List<Diff> jsonDiffs(String tableName, String prefix, Object olds, Object news) {
        try {
            Map oldm = JSON.parse(olds) as Map
            Map newm = JSON.parse(news) as Map
            return jsonSubDiffs(tableName, prefix, oldm, newm)
        } catch (JSONException) {
            return null
        }
    }

    List<Diff> fieldDiffs() {
        List<Diff> diff = []
        if (auditedObj) {
            getRelevantChangedFields().each { String key ->
                List<Diff> d = jsonDiffs(table, key, lookupField(key, rowData[key]), lookupField(key, changedFields[key]))
                if (d) {
                    diff.addAll(d)
                } else {
                    diff << new Diff(table, key, lookupField(key, rowData[key]), lookupField(key, changedFields[key]))
                }
            }
        } else {
            getRelevantRowData().each { String key ->
                if (rowData[key]) {
                    diff << new Diff(table, key, lookupField(key, rowData[key]), lookupField(key, changedFields[key]))
                }
            }
        }
        return diff
    }

    Boolean isUpdateBeforeDelete() {
        action == 'U' && getRelevantChangedFields().size() == 0
    }

    private Object getTheAuditedObject() {
        if (action != 'D') {
            auditedClass.get(rowData.id as Long)
        } else {
            return null
        }
    }

    def lookupField(String key, Object value) {
        //noinspection ChangeToOperator
        if (key.endsWith('_id') && auditedClass && !value.equals(null)) {
            //use the object to get the field and using it's class get the object
            String fieldName = snakeToCamelRemovingId(key)
            Class fieldClass = GrailsClassUtils.getPropertyType(auditedClass, fieldName)
            if (fieldClass) {
                fieldClass.get(value as Long) ?: "$value (deleted?)" //if no longer there return the value.
            } else {
                return value
            }
        } else {
            return value
        }
    }

    static String snakeToCamelRemovingId(String snake) {
        List<String> elements = []
        snake.split('_')[0..-2].eachWithIndex { String entry, int i ->
            elements.add(i ? entry.capitalize() : entry)
        }
        return elements.join('')
    }

    static String snakeToCamel(String snake) {
        List<String> elements = []
        snake.split('_').eachWithIndex { String entry, int i ->
            elements.add(i ? entry.capitalize() : entry)
        }
        return elements.join('')
    }
}
