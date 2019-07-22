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

import org.apache.shiro.authc.UnknownAccountException

class ApiRealm {

    static authTokenClass = ApiKeyToken
    def configService

    @SuppressWarnings("GroovyUnusedDeclaration")
    List<String> authenticate(ApiKeyToken authToken) {
        log.info "trying API Realm login for ${authToken}"
        Map details = configService.apiAuth?.get(authToken.key) as Map
        if (details) {
            if (details.host) { //if host is set then ensure it matches
                if (details.host == authToken.host) {
                    return [details.application, 'api']
                }
                throw new UnknownAccountException("No account found for api user [${authToken.key}]")
            }
            return [details.application, 'api']
        }
        throw new UnknownAccountException("No account found for api user [${authToken.key}]")
    }

    private Map getDetailByPrincipal(String principal) {
        Map.Entry entry = configService.apiAuth?.find { k, v -> v.application == principal }
        return entry?.value as Map
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    Boolean hasRole(principal, String roleName) {
        Map details = getDetailByPrincipal(principal.toString())
        details?.roles && details.roles.contains(roleName)
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    Boolean hasAllRoles(principal, roles) {
        Map details = getDetailByPrincipal(principal.toString())
        details?.roles && details.roles.containsAll(roles)
    }

}