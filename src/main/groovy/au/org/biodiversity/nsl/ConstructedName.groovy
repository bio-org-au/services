package au.org.biodiversity.nsl

import groovy.transform.CompileStatic

/**
 * User: pmcneil
 * Date: 7/8/19
 *
 */
@CompileStatic
class ConstructedName {
    String fullMarkedUpName
    String simpleMarkedUpName

    String getSimpleSansMS() {
        NameConstructionUtils.removeManuscript(simpleMarkedUpName)
    }

    String getFullSansMS() {
        NameConstructionUtils.removeManuscript(fullMarkedUpName)
    }

    String getPlainFullName() {
        NameConstructionService.stripMarkUp(fullMarkedUpName)
    }

    String getPlainSimpleName() {
        NameConstructionService.stripMarkUp(simpleMarkedUpName)
    }
}
