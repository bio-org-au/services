package au.org.biodiversity.nsl

import groovy.sql.Sql

class TreeReportUtils {
    /**
     * returns a list of element ids that are in version 1 but not version 2
     * @param version1
     * @param version2
     * @param sql
     * @return element ids not in version 2
     */
    static List<Long> notIn(TreeVersion version1, TreeVersion version2, Sql sql) {
        List<Long> treeElementIdsNotInVersion2 = []
        sql.eachRow('''    
  SELECT tree_element_id
   FROM tree_version_element
   WHERE tree_version_id = :v1
  EXCEPT
  SELECT tree_element_id
   FROM tree_version_element
   WHERE tree_version_id = :v2''', [v1: version1.id, v2: version2.id]) { row ->
            treeElementIdsNotInVersion2.add(row.tree_element_id as Long)
        }
        return treeElementIdsNotInVersion2
    }

}