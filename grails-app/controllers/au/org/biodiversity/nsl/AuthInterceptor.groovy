package au.org.biodiversity.nsl

/**
 * Allow Auth Controller actions by default
 */
class AuthInterceptor {

    int order = HIGHEST_PRECEDENCE+100

    boolean before() {
        true
    }
    void afterView() {
        true
    }
}
