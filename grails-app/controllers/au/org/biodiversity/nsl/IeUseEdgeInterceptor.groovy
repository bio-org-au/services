package au.org.biodiversity.nsl


import org.grails.web.util.WebUtils

/**
 * Convert .format urls to format params
 */
class IeUseEdgeInterceptor {

    int order = HIGHEST_PRECEDENCE+97

    IeUseEdgeInterceptor() {
        matchAll()
    }

    boolean before() {
        true
    }

    boolean after() {
        response.setHeader('X-UA-Compatible', 'IE=Edge')
        true
    }

    void afterView() {
        // no-op
    }
}
