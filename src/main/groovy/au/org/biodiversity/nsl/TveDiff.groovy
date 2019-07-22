package au.org.biodiversity.nsl

/**
 * User: pmcneil
 * Date: 16/10/18
 *
 */
class TveDiff {

    static final int ADDED = 1
    static final int REMOVED = 2
    static final int MODIFIED = 3
    static final int UNCHANGED = 0
    private static final String[] typeNames = ['Unchanged', 'Added','Removed','Modified']

    static final int USE_NONE = 0
    static final int USE_FROM = 1
    static final int USE_TO = 2


    final TreeVersionElement from //normally draft
    final TreeVersionElement to   //normally head
    final int fromType
    final int toType
    Integer id
    Integer parentId
    private int use = USE_NONE

    TveDiff(TreeVersionElement from, TreeVersionElement to, int fromType, int toType){
        this.from = from
        this.to = to
        this.fromType = fromType
        this.toType = toType
    }

    String getFromTypeString() {
        typeNames[fromType]
    }

    String getToTypeString() {
        typeNames[toType]
    }

    String toString() {
        "Tve diff ${from?.elementLink} $fromTypeString -> ${to?.elementLink} $toTypeString"
    }

    void setUseFrom() {
        use = USE_FROM
    }

    void setUseTo() {
        use = USE_TO
    }

    Boolean isUseFrom() {
        use == USE_FROM
    }

    Boolean isUseTo() {
        use == USE_TO
    }

}
