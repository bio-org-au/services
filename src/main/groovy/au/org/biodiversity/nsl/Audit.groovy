package au.org.biodiversity.nsl

import grails.converters.JSON
import grails.core.GrailsClass
import grails.core.GrailsDomainClass
import grails.util.GrailsClassUtils
import grails.util.Holders
import groovy.sql.GroovyResultSet
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.orm.hibernate.cfg.GrailsDomainBinder
import org.grails.web.json.JSONException

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import static java.time.format.DateTimeFormatter.*

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
    final def domainClass

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
        String clsName = 'au.org.biodiversity.nsl.' + snakeToCamel(table).capitalize()
        this.auditedClass = Class.forName(clsName)
//        this.domainClass = Holders.grailsApplication.domainClasses.find { it.fullName == clsName }
        def grailsDomainClassMappingContext = Holders.applicationContext.getBean("grailsDomainClassMappingContext")
        this.domainClass = grailsDomainClassMappingContext.getPersistentEntity(clsName)
        this.auditedObj = getTheAuditedObject()
        if (this.auditedObj instanceof Instance) {
            (this.auditedObj as Instance).instanceType  // load into memory to avoid LazyInitializationException
            (this.auditedObj as Instance).name  // load into memory to avoid LazyInitializationException
            (this.auditedObj as Instance).reference  // load into memory to avoid LazyInitializationException
        }
        if (this.auditedObj instanceof InstanceNote) {
            (this.auditedObj as InstanceNote).instanceNoteKey  // load into memory to avoid LazyInitializationException
            (this.auditedObj as InstanceNote).instance  // load into memory to avoid LazyInitializationException
            (this.auditedObj as InstanceNote).instance?.instanceType  // load into memory to avoid LazyInitializationException
            (this.auditedObj as InstanceNote).instance?.name  // load into memory to avoid LazyInitializationException
            (this.auditedObj as InstanceNote).instance?.reference  // load into memory to avoid LazyInitializationException
        }
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
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern('dd-MMM-yyyy hh:mm a')
    SimpleDateFormat sdf = new SimpleDateFormat('dd-MMM-yyyy hh:mm a')

    String when() {
        LocalDateTime upd = changedFields.updated_at ? LocalDateTime.parse(changedFields.updated_at.replace(" ", "T").replaceAll("Z", "").replaceAll('[\\-+][0-9][0-9]$', '')) : null
        upd ? upd.format(dtf): sdf.format(actionTimeStamp)
    }

    HashSet<String> getRelevantChangedFields() {
        if (!relevantChangedFields) {
            if (action == 'U') {
                relevantChangedFields = new HashSet(changedFields.keySet())
            } else {
                relevantChangedFields = new HashSet(rowData.keySet())
            }
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
                    if (action == 'I') {
                        diff << new Diff(table, key, null, lookupField(key, rowData[key]))
                    } else {
                        diff << new Diff(table, key, lookupField(key, rowData[key]), lookupField(key, changedFields[key]))
                    }
                }
            }
        } else {
            getRelevantRowData().each { String key ->
                if (rowData[key]) {
                    if (action == 'I') {
                        diff << new Diff(table, key, null, lookupField(key, rowData[key]))
                    } else {
                        diff << new Diff(table, key, lookupField(key, rowData[key]), lookupField(key, changedFields[key]))
                    }
                }
            }
        }
        return diff
    }

    Boolean isUpdateBeforeDelete() {
        action == 'U' && getRelevantChangedFields().size() == 0
    }

    private Object getTheAuditedObject() {
        if (action == 'D') {
            def session = Holders.grailsApplication.mainContext.sessionFactory.currentSession
            def columns = GrailsDomainBinder.getMapping(auditedClass).columns.entrySet().findAll { it.value.column }.collectEntries { [(it.value.column): it.key]}
            Object rtn = auditedClass.newInstance()
            DateTimeFormatter timestampFormatter = ofPattern('yyyy-MM-dd HH:mm:ssx')
            rowData.each { Map.Entry<String,Object> it ->
                if (it.value) {
                    String oCol = columns[it.key] ?: snakeToCamel(it.key)
//                    def prop = domainClass.persistentProperties.find { it.persistentProperty.name == oCol }?.persistentProperty
                    def prop = domainClass.getPropertyByName(oCol);
                    if (prop || oCol == 'id') {
                        if (prop?.type == Timestamp) {
                            String v = it.value.replaceAll('\\.[0-9]*', '')
                            rtn.setProperty(oCol, Timestamp.valubeOf(LocalDateTime.from(timestampFormatter.parse(v))))
                        } else if (oCol == 'id' || prop?.type == Long) {
                            rtn.setProperty(oCol, it.value as Long)
                        } else if (prop?.type == Integer) {
                            rtn.setProperty(oCol, it.value as Integer)
                        } else if (prop?.type == Boolean) {
                            rtn.setProperty(oCol, it.value == 'f' ? false : true)
                        } else {
                            rtn.setProperty(oCol, it.value)
                        }
                    } else if (it.key.endsWith('_id')) {
                        def dCol = it.key.substring(0, it.key.length() - 3)
                        oCol = columns[it.key] ?: snakeToCamel(dCol)
//                        prop = domainClass.persistentProperties.find { it.persistentProperty.name == oCol }?.persistentProperty
                        prop = domainClass.getPropertyByName(oCol);
                        def val = session.get(prop.type, it.value as Long)
                        rtn.setProperty(oCol, val)
                    }
                }
            }
            return rtn
        } else if (action != 'D') {
            return auditedClass.get(rowData.id as Long)
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
