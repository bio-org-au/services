/*
    Copyright 2015 Australian National Botanic Gardens

    This file is part of NSL services project.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy
    of the License at http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package au.org.biodiversity.nsl

import au.org.biodiversity.nsl.config.ApplicationUser
import groovy.transform.CompileStatic
import org.apache.shiro.authc.*
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.permission.WildcardPermissionResolver
import org.apache.shiro.grails.GrailsShiroRealm
import org.apache.shiro.grails.SimplifiedRealm

@CompileStatic
class ApiRealm implements SimplifiedRealm, GrailsShiroRealm {

    ConfigService configService

    private Map<String, ApplicationUser> appUsersByKey = null
    private Map<String, ApplicationUser> appUsersByPrincipal = [:]

    ApiRealm() {
        setTokenClass(ApiKeyToken)
        setPermissionResolver(new WildcardPermissionResolver())
    }

    void loadUsers() {
        if (!appUsersByKey) {
            appUsersByKey = configService.getApiAuth() ?: new HashMap<String, ApplicationUser>()
            appUsersByKey.each { k, v -> appUsersByPrincipal.put(v.application, v) }
        }
    }

    AuthenticationInfo authenticate(AuthenticationToken authToken) throws AuthenticationException {
        loadUsers()
        if (authToken instanceof ApiKeyToken) {
            log.info "trying API Realm login for ${authToken}"
            ApplicationUser details = appUsersByKey[authToken.key]
            if (details && (!details.host || details.host == authToken.host)) {
                return new SimpleAuthenticationInfo(details.application, authToken.key, "ApiRealm")
            }
            throw new UnknownAccountException("No account found for api user [${authToken.key}]")
        }
    }

    boolean hasRole(Object principal, String roleName) {
        ApplicationUser user = appUsersByPrincipal[principal.toString()]
        if (user) {
            return user.roles.contains(roleName)
        } else {
            return false
        }
    }

    boolean hasAllRoles(Object principal, Collection<String> roles) {
        ApplicationUser user = appUsersByPrincipal[principal.toString()]
        if (user) {
            return user.roles.containsAll(roles)
        } else {
            return false
        }
    }

    boolean isPermitted(Object principal, Permission requiredPermission) {
        ApplicationUser user = appUsersByPrincipal[principal.toString()]
        if (user) {
            return anyImplied(requiredPermission, user.permissions)
        } else {
            return false
        }
    }

    private boolean anyImplied(Permission requiredPermission, Collection<String> permStrings) {
        permStrings.find { String permString ->
            getPermissionResolver()
                    .resolvePermission(permString)
                    .implies(requiredPermission)
        } != null
    }


}