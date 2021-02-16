package au.org.biodiversity.nsl

import groovy.sql.Sql
import groovy.util.logging.Slf4j

@Slf4j
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

    /**
     * Get TreeVersionElements by Id in a version
     * @param elementIds
     * @param version
     * @return
     */
    static List<TreeVersionElement> getTvesInVersion(TreeVersion version, List<Long> elementIds) {
        log.info "querying ${elementIds.size()} elements"
        if (elementIds.empty) {
            return []
        }
        return TreeVersionElement.executeQuery(
                'select tve from TreeVersionElement tve where treeVersion = :version and treeElement.id in :elementIds order by namePath',
                [version: version, elementIds: elementIds]
        )
    }

    static List<List<TreeVersionElement>> findModified(TreeVersion first, TreeVersion second, List<Long> treeElementsNotInFirst) {
        if (treeElementsNotInFirst.empty) {
            return []
        }
        // Takes the new tree elements and matches with the previous version tree_elements by the name.id.
        //should possibly use the tree_element.previous_element
        (TreeVersionElement.executeQuery('''
select tve, ptve 
    from TreeVersionElement tve, TreeVersionElement ptve
where tve.treeVersion = :version
    and ptve.treeVersion =:previousVersion
    and ptve.treeElement.nameId = tve.treeElement.nameId
    and tve.treeElement.id in :elementIds
    order by tve.namePath
''', [version: second, previousVersion: first, elementIds: treeElementsNotInFirst])) as List<List<TreeVersionElement>>
    }

    static List<TreeVersionElement> sorted(List<TreeVersionElement> tves) {
        tves.sort { a,b -> a.namePath <=> b.namePath }
    }
}