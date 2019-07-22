package au.org.biodiversity.nsl

/**
 * User: pmcneil
 * Date: 16/05/18
 *
 */
class NameConstructionUtils {

    static Name nameParent(Name name) {
        use(RankUtils) {
            if (name.nameRank.name == '[unranked]') {
                return firstMajorRankedParent(name)
            }
            Name next = name
            if (next.nameLowerThanRank('Genus') || next.nameRank.visibleInName) {
                NameRank parentRank = next.nameRank.parentRank
                int count = 9 //count to prevent recursive parents causing issues
                if (parentRank) {
                    while (next && count-- > 0 && next.nameLowerThanRank(parentRank)) {
                        // NSL-2696 don't use == here between NameRank objects because you may get a
                        // ...NameRank_$$_javassist_44 object which will fail the equals test.
                        // Comparing ids is quicker too.
                        if (next.parent && next.parent.nameRank.id == parentRank.id) {
                            return next.parent
                        }
                        next = next.parent
                    }
                }
            }
            return null
        }
    }

    static Name firstMajorRankedParent(Name name) {
        int count = 5
        while (name && name.parent && count-- > 0) {
            if (name.parent.nameRank.name != '[unranked]' && name.parent.nameRank.major) {
                return name.parent
            }
            name = name.parent
        }
        return null
    }

    static Name nameParentOfRank(Name name, String rankName) {
        Name parent = name.parent
        int count = 5
        while (count-- > 0) {
            if (parent.nameRank.name == rankName) {
                return parent
            }
            parent = parent.parent
        }
        return null
    }

    static String removeManuscript(String string) {
        string.replaceAll(/ (<manuscript>MS<\/manuscript>)/, '')
    }

    static String removeAuthors(String string) {
        string.replaceAll(/ (<authors>.*?<\/authors>)/, '')
    }
}
