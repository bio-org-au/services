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

package au.org.biodiversity.nsl.api

import grails.converters.JSON
import grails.converters.XML
import grails.core.GrailsApplication
import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.UnauthenticatedException
import org.grails.web.util.WebUtils
import org.springframework.http.HttpStatus

class ErrorController {

    GrailsApplication grailsApplication

    def index() {
        def e = exception ?: request.getAttribute('javax.servlet.error.exception')
        def err = e
        def status = request.getAttribute('javax.servlet.error.status_code') as int

        log.debug "Error controller: error is $err"

        while (e && !(e instanceof AuthorizationException)) {
            e = e.cause
        }

        // Redirect to the 'unauthorized' page if the cause was an
        // AuthorizationException.
        if (e instanceof AuthorizationException) {
            if (e instanceof UnauthenticatedException) {
                status = HttpStatus.UNAUTHORIZED.value()
            } else {
                status = HttpStatus.FORBIDDEN.value()
            }
        }

        request.setAttribute('javax.servlet.error.status_code', status)
        response.status = status

        withFormat {
            html {
                if (status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value()) {
                    redirect(controller: 'auth',
                            action: 'login',
                            params: [targetUri: request.forwardURI - request.contextPath])
                } else {
                    render(view: "/error", model: [exception: err])
                }
            }
            json {
                def response = jsonError(e, status)
                render response as JSON
            }
            xml {
                def response = jsonError(e, status)
                render response as XML
            }
        }
    }

    private Map jsonError(Exception exception, int status) {

        Map errorMap = [:]
        int statusCode = status
        errorMap.status = prettyPrintStatus(statusCode)
        errorMap.uri = (WebUtils.getForwardURI(request) ?: request.getAttribute('javax.servlet.error.request_uri'))

        if (exception) {
            def root = ExceptionUtils.getRootCause(exception)
            errorMap.exception = root?.getClass()?.name ?: exception.getClass().name
            errorMap.reason = exception.message

            if (root != null && root != exception && root.message != exception.message) {
                errorMap.CausedBy = root.message
            }
        }
        return errorMap
    }

    private static String prettyPrintStatus(int statusCode) {
        String httpStatusReason = HttpStatus.valueOf(statusCode).getReasonPhrase()
        "$statusCode: ${httpStatusReason}"
    }

}
