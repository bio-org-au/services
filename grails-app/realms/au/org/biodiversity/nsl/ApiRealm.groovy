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
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authc.UnknownAccountException
import org.apache.shiro.authz.Permission
import org.apache.shiro.grails.GrailsShiroRealm
import org.apache.shiro.grails.SimplifiedRealm

class ApiRealm implements SimplifiedRealm {

    static authTokenClass = ApiKeyToken
    def configService

    AuthenticationInfo authenticate(AuthenticationToken authToken) throws AuthenticationException {
        if (authToken instanceof ApiKeyToken) {
            log.info "trying API Realm login for ${authToken}"
            ApplicationUser details = configService.apiAuth?.get(authToken.key)
            if (details && (!details.host || details.host == authToken.host)) {
                return new SimpleAuthenticationInfo(details.application, authToken.key, "ApiRealm")
            }
            throw new UnknownAccountException("No account found for api user [${authToken.key}]")
        }
    }

    private Map getDetailByPrincipal(String principal) {
        Map.Entry entry = configService.apiAuth?.find { k, v -> v.application == principal }
        return entry?.value as Map
    }

    boolean hasRole(Object principal, String roleName) {
        Map details = getDetailByPrincipal(principal.toString())
        details?.roles && details.roles.contains(roleName)
    }

    boolean hasAllRoles(Object principal, Collection<String> roles) {
        Map details = getDetailByPrincipal(principal.toString())
        details?.roles && details.roles.containsAll(roles)
    }

    boolean isPermitted(Object principal, Permission requiredPermission) {
    }

}