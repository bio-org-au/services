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

import grails.converters.JSON
import grails.converters.XML
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authz.annotation.RequiresRoles
import org.apache.shiro.web.util.SavedRequest
import org.apache.shiro.web.util.WebUtils
import org.grails.web.json.JSONObject
import org.springframework.http.HttpStatus

import javax.crypto.spec.SecretKeySpec
import java.security.Key

class AuthController {
    def grailsApplication
    def configService

    def index() {
        redirect(action: "login", params: params)
    }

    def login() {
        return [username: params.username, rememberMe: (params.rememberMe != null), targetUri: params.targetUri]
    }

    def signIn(String username, String password) {
        def authToken = new UsernamePasswordToken(username, password)

        // Support for "remember me"
        if (params.rememberMe) {
            authToken.rememberMe = true
        }

        // If a controller redirected to this page, redirect back
        // to it. Otherwise redirect to the root URI.
        String rootURI = configService.getServerUrl()
        def targetUri = params.targetUri ?: rootURI

        // Handle requests saved by Shiro filters.
        SavedRequest savedRequest = WebUtils.getSavedRequest(request)
        if (savedRequest) {
            targetUri = savedRequest.requestURI - request.contextPath
            if (savedRequest.queryString) targetUri = targetUri + '?' + savedRequest.queryString
        }

        try {
            // Perform the actual login. An AuthenticationException
            // will be thrown if the username is unrecognised or the
            // password is incorrect.
            SecurityUtils.subject.login(authToken)

            log.info "Redirecting to '${targetUri}'."
            redirect(uri: targetUri)
        }
        catch (AuthenticationException ex) {
            // Authentication failed, so display the appropriate message
            // on the login page.
            log.info "Authentication failure for user '${params.username}'. ($ex.message)"
            flash.message = message(code: "login.failed")

            // Keep the username and "remember me" setting so that the
            // user doesn't have to enter them again.
            def m = [username: params.username]
            if (params.rememberMe) {
                m["rememberMe"] = true
            }

            // Remember the target URI too.
            if (params.targetUri) {
                m["targetUri"] = params.targetUri
            }

            // Now redirect back to the login page.
            redirect(action: "login", params: m)
        }
    }

    /**
     * sign in and get a JSON web token back.
     *
     * todo At the moment this creates a useless session object which is probably not required. look at ApiSessionStorageEvaluator
     *
     * @param username
     * @param password
     * @return JSON object including the jwt
     */
    def signInJson(String username, String password) {
        def authToken = new UsernamePasswordToken(username, password)
        try {
            SecurityUtils.subject.login(authToken)

            Key key = new SecretKeySpec(configService.JWTSecret.getBytes('UTF-8'), 'plain text')
            String jwt = JsonWebTokenRealm.makeJWT(username, key)
            String refreshToken = JsonWebTokenRealm.makeRefreshJWT(username, key)

            JSON result = [
                    success     : true,
                    principal   : username,
                    jwt         : jwt,
                    refreshToken: refreshToken
            ] as JSON

            render result
        }
        catch (AuthenticationException ex) {
            log.info "Authentication failure for JWT user '${username}'. ($ex.message)"
            response.setStatus(401)
            def result = [success: false, principal: null]
            render result as JSON
        }
    }

    def reauth() {
        if (SecurityUtils.subject.authenticated) {
            String username = SecurityUtils.subject.principal.toString()
            log.info "${username} re-authenticating."
            Key key = new SecretKeySpec(configService.JWTSecret.getBytes('UTF-8'), 'plain text')
            String jwt = JsonWebTokenRealm.makeJWT(username, key)
            String refreshToken = JsonWebTokenRealm.makeRefreshJWT(username, key)

            JSON result = [
                    success     : true,
                    principal   : username,
                    jwt         : jwt,
                    refreshToken: refreshToken
            ] as JSON

            render result
        } else {
            log.info "Some unauthenticated dude called reauth!"
            render (status: HttpStatus.UNAUTHORIZED)
        }
    }

    def signOutJson() {
        log.info "${SecurityUtils.subject.principal.toString()} logging out."
        SecurityUtils.subject?.logout()
        webRequest.getCurrentRequest().session = null
        JSONObject result = [
                success  : true,
                principal: ''
        ] as JSONObject
        render result as JSON
    }

    /**
     * This method is used by the NSL editor. That is, the subject that calls this method is one we recognise as being
     * secure. This is setup in services-config.groovy . Note that no checking is done to see that the user actually exists.
     * todo is this actually used???
     */

    @RequiresRoles('may-fetch-jwt-for-any-username')
    getInfoJsonForUsername(String username) {
        JSON result = getJsonForPrincipal(username)
        render result
    }

    private JSON getJsonForPrincipal(String principal) {
        Key key = new SecretKeySpec(configService.JWTSecret.getBytes('UTF-8'), 'plain text')

        JSON result = [
                success  : true,
                principal: principal,
                jwt      : JsonWebTokenRealm.makeJWT(principal, key)
        ] as JSON

        return result
    }

    def signOut() {
        // Log the user out of the application.
        SecurityUtils.subject?.logout()
        webRequest.getCurrentRequest().session = null

        // For now, redirect back to the home page.
        redirect(url: '/')
    }

    def unauthorized() {
        withFormat {
            html {
                render(status: 401, text: "You do not have permission to do that.")
            }
            json {
                def error = mapError()
                render(contentType: "application/json", status: 401) {
                    error
                }
            }
            xml {
                def error = mapError()
                response.status = 401
                render error as XML
            }
        }
    }

    private Map mapError() {
        Map errorMap = [:]
        errorMap.status = prettyPrintStatus(401)
        errorMap.uri = (org.codehaus.groovy.grails.web.util.WebUtils.getForwardURI(request) ?: request.getAttribute('javax.servlet.error.request_uri'))
        errorMap.reason = "You do not have permission."
        return errorMap
    }

    private static String prettyPrintStatus(int statusCode) {
        String httpStatusReason = HttpStatus.valueOf(statusCode).getReasonPhrase()
        "$statusCode: ${httpStatusReason}"
    }

}
