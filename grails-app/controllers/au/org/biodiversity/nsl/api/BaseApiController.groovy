package au.org.biodiversity.nsl.api

import au.org.biodiversity.nsl.*
import grails.validation.ValidationException
import org.apache.commons.lang.NotImplementedException
import org.apache.shiro.authz.AuthorizationException
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import static org.springframework.http.HttpStatus.*

class BaseApiController implements WithTarget {

    def jsonRendererService
    def index() {}

    protected handleResults(ResultObject results, Closure response, Closure work) {
        if (results.ok) {
            try {
                work()
            } catch (ObjectExistsException exists) {
                results.ok = false
                results.fail(exists.message, CONFLICT)
            } catch (BadArgumentsException bad) {
                results.ok = false
                results.fail(bad.message, BAD_REQUEST)
            } catch (ValidationException invalid) {
                invalid.printStackTrace()
                log.error("Validation failed ${params.action} : $invalid.message")
                results.ok = false
                results.fail(invalid.message, INTERNAL_SERVER_ERROR)
            } catch (AuthorizationException authException) {
                results.ok = false
                results.fail("You are not authorised to ${params.action}. ${authException.message}", FORBIDDEN)
                log.warn("You are not authorised to do this. $results.\n ${authException.message}")
            } catch (NotImplementedException notImplementedException) {
                results.ok = false
                results.fail(notImplementedException.message, NOT_IMPLEMENTED)
                log.error("$notImplementedException.message : $results")
            } catch (ObjectNotFoundException notFound) {
                results.ok = false
                results.fail(notFound.message, NOT_FOUND)
                log.error("$notFound.message : $results")
            } catch (PublishedVersionException published) {
                results.ok = false
                results.fail(published.message, CONFLICT)
                log.error("$published.message : $results")
            }
        }
        response()
    }

    protected handleResults(ResultObject results, Closure work) {
        handleResults(results, { serviceRespond(results) }, work)
    }

    protected withJsonData(Object json, Boolean list, List<String> requiredKeys, Closure work) {
        ResultObject results = new ResultObject([action: params.action], jsonRendererService as JsonRendererService)
        results.ok = true
        if (!json) {
            results.ok = false
            results.fail("JSON paramerters not supplied. You must supply JSON parameters ${list ? 'as a list' : requiredKeys}.",
                    BAD_REQUEST)
            return serviceRespond(results)
        }
        if (list && !(json.class instanceof JSONArray)) {
            results.ok = false
            results.fail("JSON paramerters not supplied. You must supply JSON parameters as a list.", BAD_REQUEST)
            return serviceRespond(results)
        }
        if (list) {
            List data = RestCallService.convertJsonList(json as JSONArray)
            handleResults(results) {
                work(results, data)
            }
        } else {
            Map data = RestCallService.jsonObjectToMap(json as JSONObject)
            for (String key in requiredKeys) {
                if (data[key] == null) {
                    results.ok = false
                    results.fail("$key not supplied. You must supply $key.", BAD_REQUEST)
                }
            }
            handleResults(results) {
                work(results, data)
            }
        }
    }
}
