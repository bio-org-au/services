package au.org.biodiversity.nsl.api

/**
 * User: pmcneil
 * Date: 20/04/17
 *
 */
trait ValidationUtils {

    static mustHave(Map things) {
        things.each { k, v ->
            if (v == null) {
                throw new IllegalArgumentException("$k must not be null")
            }
        }
    }
}