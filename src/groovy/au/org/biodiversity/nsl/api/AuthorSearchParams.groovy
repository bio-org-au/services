package au.org.biodiversity.nsl.api

import au.org.biodiversity.nsl.Author

/**
 * User: pmcneil
 * Date: 21/10/19
 *
 */
class AuthorSearchParams {
    String name
    String abbrev
    Integer max = 100
    Integer countFound = 0
    List<Author> results = []

    AuthorSearchParams(Map params) {
        println params
        name = params.name
        abbrev = params.abbrev
        max = params.max as Integer
    }
}
