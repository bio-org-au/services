package au.org.biodiversity.nsl

/**
 * User: pmcneil
 * Date: 1/02/17
 *
 */
class Diff {
    final String tableName
    final String fieldName
    final Object before
    final Object after

    Diff(String tableName, String fieldName, Object before, Object after) {
        this.tableName = tableName
        this.fieldName = fieldName
        this.before = before
        this.after = after
    }
    String toString() {
        return "$tableName.$fieldName: $before -> $after"
    }
}
