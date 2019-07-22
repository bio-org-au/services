package au.org.biodiversity.nsl

/**
 * User: pmcneil
 * Date: 1/02/17
 *
 */
class Diff {

    final String fieldName
    final Object before
    final Object after

    Diff(String fieldName, Object before, Object after) {
        this.fieldName = fieldName
        this.before = before
        this.after = after
    }

}
