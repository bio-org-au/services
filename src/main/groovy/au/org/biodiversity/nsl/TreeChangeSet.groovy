package au.org.biodiversity.nsl

import au.org.biodiversity.nsl.api.ValidationUtils
import groovy.sql.Sql

/**
 * User: pmcneil
 * Date: 16/10/18
 *
 *
 *
 */
class TreeChangeSet implements ValidationUtils {

    TreeVersion v1
    TreeVersion v2
    List<TreeVersionElement> added
    List<TreeVersionElement> removed
    List<List<TreeVersionElement>> modified  //List [current tve, previous tve]
    List<TreeVersionElement> all

    Boolean changed = false
    Boolean overflow = false

    TreeChangeSet(TreeVersion first,  TreeVersion second, Sql sql, Integer limit) {
        mustHave("version 1": first, "version 2": second)
        use(TreeReportUtils) {

            v1 = first
            v2 = second
            added = []
            removed = []
            modified = []

            List<Long> treeElementsNotInSecond = first.notIn(second, sql)
            List<Long> treeElementsNotInFirst = second.notIn(first, sql)

            if (treeElementsNotInSecond.empty && treeElementsNotInFirst.empty) {
                return
            }
            //limit null or 0 disables limit
            if (limit && (treeElementsNotInSecond.size() > limit || treeElementsNotInFirst.size() > limit)) {
                this.changed = true
                this.overflow = true
                return
            }

            modified = first.findModified(second, treeElementsNotInFirst)

            List<Long> treeElementsAddedToSecond = treeElementsNotInFirst - modified.collect { mod -> mod[0].treeElement.id }
            added = second.getTvesInVersion(treeElementsAddedToSecond)

            List<Long> treeElementsRemovedFromSecond = treeElementsNotInSecond - modified.collect { mod -> mod[1].treeElement.id }
            removed = first.getTvesInVersion(treeElementsRemovedFromSecond)

            this.changed = true
            this.overflow = false
            all = added + removed + modifiedResult
        }
    }

    Map toMap() {
        [v1: v1, v2: v2, added: added, removed: removed, modified: modified, changed: changed, overflow: overflow]
    }

    /**
     * get just the modified tree version element
     * @return
     */
    List<TreeVersionElement> getModifiedResult() {
        modified.collect {it[0]}
    }
}
