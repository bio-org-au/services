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

import au.org.biodiversity.nsl.JsonRendererService
import au.org.biodiversity.nsl.ObjectNotFoundException

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.NOT_FOUND

/**
 * User: pmcneil
 * Date: 18/06/15
 *
 * When using this trait you must have the jsonRendererService injected into your implementing code.
 */
trait WithTarget {

    def withTarget(Object target, Closure work) {
        withTarget(target, 'Object', work)
    }

    def withTarget(Object target, String targetInfo, Closure work) {
        assert jsonRendererService

        ResultObject result = new ResultObject([action: params.action], jsonRendererService as JsonRendererService)

        if (target) {
            result.briefObject(target)
            work(result, target)
        } else {
            result.error("$targetInfo not found.")
            result.status = NOT_FOUND
        }
        serviceRespond(result)
    }

    def withTargets(Map targets, Closure work) {
        assert jsonRendererService
        ResultObject result = new ResultObject([action: params.action], jsonRendererService as JsonRendererService)
        boolean ok = true
        for (key in targets.keySet()) {
            if (!targets[key]) {
                result.status = NOT_FOUND
                result.error("$key not found.")
                ok = false
            } else {
                result.briefObject(targets[key], key as String)
            }
        }
        if (ok) {
            work(result)
        } else {
            result.ok = false
        }
        serviceRespond(result)
    }

    ResultObject require(Map requirements) {
        assert jsonRendererService
        ResultObject result = new ResultObject([action: params.action], jsonRendererService as JsonRendererService)
        result.ok = true

        for (key in requirements.keySet()) {
            if (requirements[key] == null) {
                result.status = BAD_REQUEST
                result.error("$key not supplied. You must supply $key.")
                result.ok = false
            }
        }
        return result
    }

    ResultObject requireTarget(Object target, String errorMessage) {
        assert jsonRendererService

        ResultObject result = new ResultObject([action: params.action], jsonRendererService as JsonRendererService)
        result.ok = true

        if (!target) {
            result.error(errorMessage)
            result.status = NOT_FOUND
            result.ok = false
        }
        return result
    }

    void serviceRespond(ResultObject resultObject, String view = '/common/serviceResult') {
        log.debug "serviceRespond: result status is ${resultObject.status}"
        withFormat {
            html {
                render(view: view, model: [data: resultObject], status: resultObject.remove('status'))
            }
            json {
                respond(resultObject, status: resultObject.get('status'))
            }
            xml {
                respond(resultObject, status: resultObject.remove('status'))
            }
        }
    }

    static Object got(Closure c, String msg) {
        def result = c()
        if (!result) {
            throw new ObjectNotFoundException(msg)
        }
        return result
    }

}
