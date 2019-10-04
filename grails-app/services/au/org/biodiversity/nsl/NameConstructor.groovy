package au.org.biodiversity.nsl

import org.grails.encoder.Encoder

/**
 * User: pmcneil
 * Date: 16/05/18
 *
 */
trait NameConstructor {

    Encoder htmlEncoderInst

    /**
     * Construct a name according to the code implemented. This method returns the full marked up name and the simple
     * marked up name. full names have the author and simple names do not. Mark up should be as per the ICN name constructor
     * format
     *
     * @param name
     * @return Map [fullMarkedUpName: markedUpName, simpleMarkedUpName: markedUpName]
     */
    abstract ConstructedName constructName(Name name)

    abstract String constructAuthor(Name name)

    Encoder getHtmlEncoder() {
        if (!htmlEncoderInst) {
            htmlEncoderInst = codecLookup.lookupEncoder('HTML')
        }
        return htmlEncoderInst
    }

    String encodeHtml(String string) {
        if(string) {
            return htmlEncoder.encode(string)
        }
        return null
    }
}