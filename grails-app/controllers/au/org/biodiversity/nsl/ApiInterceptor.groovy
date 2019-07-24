package au.org.biodiversity.nsl

import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.subject.SimplePrincipalCollection

class ApiInterceptor {

    int order = HIGHEST_PRECEDENCE+98

    ApiInterceptor() {
        matchAll()
    }

    boolean before() {
        if (params.apiKey) {
            try {
                String apiKey = params.remove('apiKey')
                ApiKeyToken authToken = new ApiKeyToken(apiKey, null as char[], SecurityUtils.subject.host as String)
                Long start = System.currentTimeMillis()
                SecurityUtils.subject.login(authToken)

                // TODO: make a new permission "mayRunAsAnyUser"
                String runAs = params.remove('as')
                if (runAs) {
                    log.debug("${SecurityUtils.subject.principal} is running as ${runAs}")
                    SecurityUtils.subject.runAs(new SimplePrincipalCollection(runAs, ""))
                }

                log.debug "login took ${System.currentTimeMillis() - start}ms"
                return true
            } catch (AuthenticationException e) {
                log.info e.message
                redirect(controller: 'auth', action: 'unauthorized', params: [format: params.format])
                return false
            }
        }
        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
