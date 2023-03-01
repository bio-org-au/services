package au.org.biodiversity.nsl

import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.grails.LdapUser
import org.apache.shiro.subject.SimplePrincipalCollection
import org.springframework.beans.factory.annotation.Autowired

class ApiInterceptor {

    @Autowired
    LdapRealm ldapRealm
    int order = HIGHEST_PRECEDENCE+98

    ApiInterceptor() {
        matchAll()
    }

    boolean before() {
        if (params.apiKey) {
            try {
                String runAsUser = params.remove('as')
                String apiKey = params.remove('apiKey')
                log.debug "key: $apiKey as: $runAsUser"
                ApiKeyToken authToken = new ApiKeyToken(apiKey, null as char[], SecurityUtils.subject.host as String)
                Long start = System.currentTimeMillis()
                SecurityUtils.subject.login(authToken)

                if (runAsUser) {
                    log.debug("${SecurityUtils.subject.principal} is running as ${runAsUser}")
                    LdapUser ldapUser = ldapRealm.getLdapUser(runAsUser)
                    SecurityUtils.subject.runAs(new SimplePrincipalCollection(ldapUser, "LdapRealm"))
                }

                log.debug "login took ${System.currentTimeMillis() - start}ms"
                return true
            } catch (AuthenticationException e) {
                log.error "$e.message host: ${SecurityUtils.subject.host}"
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
