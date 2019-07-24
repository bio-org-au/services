package au.org.biodiversity.nsl


import org.grails.web.util.WebUtils

/**
 * Convert .format urls to format params
 */
class FormatInterceptor {

    int order = HIGHEST_PRECEDENCE+97

    FormatInterceptor() {
        matchAll()
    }

    boolean before() {
        //need the .format to get a good response in case of errors
        String requested = (WebUtils.getForwardURI(request) ?: request.getAttribute('javax.servlet.error.request_uri'))
        requested = requested.decodeURL()

        if (requested.endsWith('.json')) {
            params.format = 'json'
        }
        if (requested.endsWith('.xml')) {
            params.format = 'xml'
        }
        if (requested.endsWith('.html')) {
            params.format = 'html'
        }
        true
    }

    boolean after() {
        true
    }

    void afterView() {
        // no-op
    }
}
