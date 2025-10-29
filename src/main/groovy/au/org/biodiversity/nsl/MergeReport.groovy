package au.org.biodiversity.nsl

/**
 * User: pmcneil
 * Date: 17/10/18
 *
 */
/*
    NSL-5608
    We will not remove the code BUT I would like it clear that the functionality is on hold until
    we can find a use case for it. At this stage it can stay in the code base.
 */
class MergeReport {
    TreeVersion from
    TreeVersion to
    Boolean upToDate
    List<TveDiff> conflicts
    List<TveDiff> nonConflicts

    Map toMap() {
        [from: from, to: to, upToDate: upToDate, conflicts: conflicts, nonConflicts: nonConflicts]
    }

    def idDiffs() {
        Integer id = 1
        conflicts.each { it.id = id++ }
        nonConflicts.each { it.id = id++ }
        markParents()
    }

    private void markParents() {
        List<TveDiff> orderedToDiffs = (nonConflicts + conflicts)
                .findAll { it.toType == TveDiff.REMOVED || it.toType == TveDiff.ADDED }
                .sort { it.to?.namePath }

        while(orderedToDiffs.size()) {
            itrParent(orderedToDiffs)
            orderedToDiffs.remove(0)
        }
    }

    private static void itrParent(List<TveDiff>orderedToDiffs) {
        TveDiff parent = null
        for (tveDiff in orderedToDiffs) {
            if (parent && tveDiff.to?.namePath?.startsWith(parent.to?.namePath)) {
                tveDiff.parentId = parent.id
            } else {
                parent = tveDiff
            }
        }
    }
    /**
     * Sets useTo or useFrom on the diff with the id provided
     * @param diffId
     * @param toOrFrom "to" or "from"
     * @return true if it was set, false if diff not found of wrong word used
     */
    boolean setUse(Integer diffId, String toOrFrom) {
        TveDiff diff = (nonConflicts + conflicts).find { it.id == diffId }
        if (diff) {
            if (toOrFrom == 'to') {
                diff.setUseTo()
                return true
            }
            if (toOrFrom == 'from') {
                diff.setUseFrom()
                return true
            }
        }
        return false
    }

    List<TveDiff> getUseFrom() {
        (nonConflicts + conflicts).findAll { it.isUseFrom() }
    }

    List<TveDiff> getUseToType(int type) {
        (nonConflicts + conflicts).findAll { it.isUseTo() && it.toType == type }
    }

}
