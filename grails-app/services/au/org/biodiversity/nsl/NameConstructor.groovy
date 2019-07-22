package au.org.biodiversity.nsl

/**
 * User: pmcneil
 * Date: 16/05/18
 *
 */
interface NameConstructor {

    /**
     * Construct a name according to the code implemented. This method returns the full marked up name and the simple
     * marked up name. full names have the author and simple names do not. Mark up should be as per the ICN name constructor
     * format
     *
     * @param name
     * @return Map [fullMarkedUpName: markedUpName, simpleMarkedUpName: markedUpName]
     */
    Map constructName(Name name)

    String constructAuthor(Name name)
}