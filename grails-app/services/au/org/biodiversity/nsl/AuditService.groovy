package au.org.biodiversity.nsl

import groovy.sql.GroovyResultSet
import groovy.sql.GroovyRowResult
//import org.postgresql.jdbc.PgResultSet
import groovy.sql.Sql

import java.sql.Timestamp

class AuditService implements WithSql {

    ConfigService configService

    List<Audit> list(String userName, Timestamp from, Timestamp to, String filter) {
        String tableName
        String sessionUser = '%'
        userName = userName ?: '%'
        if (filter == 'all') {
            tableName = '%'
        } else if (['name', 'author', 'reference', 'instance', 'instance_note', 'tree_element', 'comment'].contains(filter)) {
            tableName = filter
        } else {
            return []
        }
        List rows = []
        withSql { Sql sql ->
            sql.eachRow("""
SELECT event_id, action_tstamp_tx, session_user_name, table_name, action, 
  hstore_to_json(row_data) AS rd, hstore_to_json(changed_fields) AS cf 
FROM audit.logged_actions 
WHERE action_tstamp_tx > :from 
 AND action_tstamp_tx < :to 
 AND table_name ILIKE :tableName
 AND  session_user_name ILIKE :sessionUser
 AND (changed_fields -> 'updated_by' LIKE :user OR (row_data -> 'updated_by' LIKE :user AND changed_fields -> 'updated_by' IS NULL))
 ORDER BY event_id DESC LIMIT 500""", [from: from, to: to, user: userName, tableName: tableName, sessionUser: sessionUser]) { GroovyResultSet row ->
                // log.debug(row.toString())
                rows.add(new Audit(row))
            }
        }
        return rows
    }

    /**
     * Returns a Map report in the form:
     * [userName : [thing : [created : total, updated: total], thing2 : ...], userName: ...]
     * @param from Timestamp
     * @param to Timestamp
     * @param things (optional list of things to get totals on)
     * @return
     */
    Map report(Timestamp from, Timestamp to, List<String> things = ['name', 'author', 'reference', 'instance', 'instance_note', 'tree_element', 'comment']) {
        Map userReport = [:]
        withSql { Sql sql ->
            sql.withTransaction {
                things.each { String thing ->
                    //created
                    String query = (thing == 'tree_element'
                            ? "select count(t) as count, t.updated_by as uname from $thing t where updated_at > :from and updated_at < :to group by updated_by"
                            : "select count(t) as count, t.created_by as uname from $thing t where created_at > :from and created_at < :to group by created_by")
                    sql.eachRow(query, [from: from, to: to]) { GroovyResultSet row ->
                        // log.debug row.toString()
                        userReport.get(row.uname, defaultUserThingReport(things)).get(thing).created = row.count
                    }
                    //updated
                    String q2 = "select count(t) as count, t.updated_by as uname from $thing t where updated_at > :from and updated_at < :to group by updated_by"
                    sql.eachRow(q2, [from: from, to: to]) { GroovyResultSet row ->
                        // log.debug row.toString()
                        userReport.get(row.uname, defaultUserThingReport(things)).get(thing).updated = row.count
                    }
                    //deleted
                    String q3 = """SELECT count(event_id) as count, row_data -> 'updated_by' as uname FROM audit.logged_actions
                WHERE action_tstamp_tx > :from
                AND action_tstamp_tx < :to
                AND action = 'D'
                AND table_name = '$thing'
                group by row_data -> 'updated_by'"""
                    sql.eachRow(q3, [from: from, to: to]) { GroovyResultSet row ->
                        // log.debug row.toString()
                        userReport.get(row.uname, defaultUserThingReport(things)).get(thing).deleted = row.count
                    }
                }
            }
        }
        userReport
    }

    Map recoverDeletedInstanceData(Long instanceId) {
        withSql { Sql sql ->
            GroovyRowResult rowResult = sql.firstRow('''SELECT (row_data -> 'reference_id') :: NUMERIC :: BIGINT as reference_id,
  (row_data -> 'cites_id') :: NUMERIC :: BIGINT as cites_id,
  (row_data -> 'created_at') as created_at,
  (row_data -> 'source_id_string') as source_id_string,
  (row_data -> 'updated_at') as updated_at,
  (row_data -> 'draft') :: BOOLEAN as draft,
  (row_data -> 'id') :: NUMERIC :: BIGINT as id,
  (row_data -> 'instance_type_id') :: NUMERIC :: BIGINT as instance_type_id,
  (row_data -> 'cited_by_id') :: NUMERIC :: BIGINT as cited_by_id,
  (row_data -> 'lock_version') :: NUMERIC :: BIGINT as lock_version,
  (row_data -> 'source_system') as source_system,
  (row_data -> 'name_id') :: NUMERIC :: BIGINT as name_id,
  (row_data -> 'created_by') as created_by,
  (row_data -> 'trash') :: BOOLEAN as trash,
  (row_data -> 'bhl_url') as bhl_url,
  (row_data -> 'namespace_id') :: NUMERIC :: BIGINT as namespace_id,
  (row_data -> 'parent_id') :: NUMERIC :: BIGINT as parent_id,
  (row_data -> 'nomenclatural_status') as nomenclatural_status,
  (row_data -> 'valid_record') :: BOOLEAN as valid_record,
  (row_data -> 'updated_by') as updated_by,
  (row_data -> 'page_qualifier') as page_qualifier,
  (row_data -> 'verbatim_name_string') as verbatim_name_string,
  (row_data -> 'page') as page,
  (row_data -> 'source_id') :: NUMERIC :: BIGINT as source_id
FROM audit.logged_actions
WHERE action = 'D\'
      AND table_name = 'instance\'
      and (row_data -> 'id') :: NUMERIC :: BIGINT = :instanceId
order by action_tstamp_tx desc;''', [instanceId: instanceId])
            return (rowResult && !rowResult.isEmpty()) ? new HashMap(rowResult as Map) : null
        } as Map
    }


    private static Map defaultUserThingReport(List<String> things) {
        Map defMap = [:]
        things.collect { String thing ->
            defMap.put(thing, [created: 0, updated: 0, deleted: 0])
        }
        return defMap
    }
}
