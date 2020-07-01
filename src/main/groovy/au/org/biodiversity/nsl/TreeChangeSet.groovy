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
    List<TreeVersionElement> added = []
    List<TreeVersionElement> removed = []
    List<List<TreeVersionElement>> modified = [] //List [current tve, previous tve]
    List<TreeVersionElement> all = []

    Boolean changed = false
    Boolean overflow = false

    TreeChangeSet(TreeVersion first, TreeVersion second, Sql sql, Integer limit) {
        mustHave("version 1": first, "version 2": second)
        use(TreeReportUtils) {

            v1 = first
            v2 = second

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

            modified = first.findModified(second, treeElementsNotInFirst).sort { a, b -> a[0].namePath <=> b[0].namePath }

            List<Long> treeElementsAddedToSecond = treeElementsNotInFirst - modified.collect { mod -> mod[0].treeElement.id }
            added = second.getTvesInVersion(treeElementsAddedToSecond).sorted()

            List<Long> treeElementsRemovedFromSecond = treeElementsNotInSecond - modified.collect { mod -> mod[1].treeElement.id }
            removed = first.getTvesInVersion(treeElementsRemovedFromSecond).sorted()

            this.changed = true
            this.overflow = false
            all = (added + removed + modifiedResult).sorted()
        }
    }

    Map toMap() {
        [changeSet: this, v1: v1, v2: v2, all: all, added: added, removed: removed, modified: modified, changed: changed, overflow: overflow]
    }

    /**
     * get just the modified tree version element
     * @return
     */
    List<TreeVersionElement> getModifiedResult() {
        modified.collect { it[0] }
    }

    TreeVersionElement was(TreeVersionElement tve) {
        List<TreeVersionElement> mod = modified.find { it[0] == tve }
        if(mod) {
            return mod[1]
        }
        return null
    }
}
