package au.org.biodiversity.nsl

/**
 * User: pmcneil
 * Date: 16/05/18
 *
 */
class UnsupportedNomCode extends Exception {
    UnsupportedNomCode() {
        super()
    }

    UnsupportedNomCode(String message) {
        super(message)
    }
}
