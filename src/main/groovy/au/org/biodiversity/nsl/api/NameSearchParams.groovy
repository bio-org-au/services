package au.org.biodiversity.nsl.api

import au.org.biodiversity.nsl.Name

/**
 * User: pmcneil
 * Date: 21/10/19
 *
 */
class NameSearchParams {
    String fullName
    String rankName
    Integer max = 50
    Integer countFound = 0
    List<Name> results = []

    NameSearchParams(Map params) {
        fullName = params.fullName
        rankName = params.rank
        max = params.max as Integer
    }
}
