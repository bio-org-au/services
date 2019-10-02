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

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject
import org.springframework.web.client.ResourceAccessException

/**
 * Handles making REST requests to a service.
 *
 * @author Peter McNeil
 */
class RestCallService {

    static transactional = false

    private RestBuilder rest = new RestBuilder(proxy: Proxy.NO_PROXY)

    /**
     * log into the mapper and store the JWT and refresh token
     * @return
     */
    AccessToken jwtLogin(String loginUrl, Map credentials, String refreshUrl) {
        RestResponse response = rest.post(loginUrl) {
            header 'Accept', "application/json"
            json(credentials)
        }
        if (response.status == 200) {
            Map resp = response.json as Map
            log.info "logged into mapper. $resp"
            return new AccessToken(resp.access_token as String, resp.refresh_token as String, refreshUrl)
        } else {
            log.error("Can't log into mapper, status: ${response.status}, ${response.json}")
            return null
        }
    }

    Boolean refreshLogin(AccessToken accessToken) {
        log.info "refreshing login to $accessToken.refreshUrl"
        Map data = [grantType: 'refresh_token', refreshToken: accessToken.refreshToken]
        RestResponse response = rest.post(accessToken.refreshUrl) {
            header 'Accept', "application/json"
            json(data)
        }
        if (response.status == 200) {
            accessToken.accessToken = response.json.accessToken as String
            log.info "refreshed JWT."
            return true
        }
        log.error "Refreshing JWT failed."
        return false
    }

    /**
     * Get data from a URL. If you're getting JSON data use the json method
     * @param uri
     * @return RestResponse
     */
    RestResponse nakedJsonGet(String uri) {
        log.debug "get ${uri}"
        return rest.get(uri) {
            header 'Accept', "application/json"
        }
    }

    /**
     * go a get request to the url ignoring the response completely. Log connection errors as information.
     * @param url
     */
    void blindJsonGet(String url) {
        log.debug "get ${url}"
        try {
            rest.get(url) {
                header 'Accept', "application/json"
            }
        } catch (Throwable e) {
            log.info "blind get to $url failed $e"
        }
    }

    /**
     * This does a JSON call to the url. It expects a JSON response back.
     *
     * It will look in the JSONObject data returned to see if there are any errors reported, and if there are errors it
     * will call the error closure. It will not check JSONArrays for errors.
     *
     * If there is no JSON response it will call the ok closure with any response text [text: response.text].
     *
     * The closures are called with a Map or List that represents the JSON response. JSON nulls are converted to JAVA null.
     *
     * On a 404 we call the notFound closure with any json data received, if no JSON data then we return a map
     * with the response text [text: response.text]. We also check any JSON response for errors and if we find
     * them we call the error closure with those errors.

     * On a !404 and !200 response we call the notOk closure with any json data received, if no JSON data then we return
     * a map with the response text [text: response.text]. We also check any JSON response for errors and if we find
     * them we call the error closure with those errors.
     *
     * @param url
     * @param ok ( Map data | List data )
     * @param notFound ( Map data | List data )
     * @param notOk ( Map data | List data )
     * @param error ( Map data , List errors )
     * @return void
     */
    def json(String method, String url, Closure ok, Closure error, Closure notFound, Closure notOk, AccessToken accessToken = null) {
        try {
            log.debug "$method json ${url}"
            RestResponse response = rest."$method"(url) {
                header 'Accept', "application/json"
                if (accessToken) {
                    header('Authorization', "Bearer ${accessToken.accessToken}")
                }
            }
            processResponse(response, ok, error, notFound, notOk)
        }
        catch (ResourceAccessException e) {
            log.error e.message
            throw new RestCallException("Unable to connect to the service at $url", e)
        }
    }

    def jsonPost(Map data, String url, Closure ok, Closure error, Closure notFound, Closure notOk, AccessToken accessToken = null) {
        try {
            log.debug "Post ${data.toMapString(200)} as json to ${url}"
            RestResponse response = postWithToken(url, accessToken, data)
            if(response.status == 401 && accessToken) {
                refreshLogin(accessToken)
                response = postWithToken(url, accessToken, data)
            }
            processResponse(response, ok, error, notFound, notOk)
        }
        catch (ResourceAccessException e) {
            log.error e.message
            throw new RestCallException("Unable to connect to the service at $url", e)
        }
    }

    private postWithToken(String url, AccessToken accessToken, Map data) {
        return rest.post(url) {
            header 'Accept', "application/json"
            if (accessToken) {
                header('Authorization', "Bearer ${accessToken.accessToken}")
            }
            json(data)
        }
    }

    private processResponse(RestResponse response, Closure ok, Closure error, Closure notFound, Closure notOk) {
        switch (response.status) {
            case 200:
                processJsonResponse(response, error, ok)
                break
            case 404:
                logResponseError(response)
                processJsonResponse(response, error, notFound)
                break
            default:
                logResponseError(response)
                processJsonResponse(response, error, notOk)
                break
        }
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private logResponseError(RestResponse response) {
        log.error "Got ${response.status}. headers: ${response.headers}, body: ${response.text}"
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private processJsonResponse(RestResponse response, Closure error, Closure worker) {
        if (response.json) {
            JSONElement json = response.json
            if (json instanceof JSONArray) {
                worker(convertJsonList(json as JSONArray))
            } else {
                Map jsonData = jsonObjectToMap(json as JSONObject)
                List errors = getJsonErrorMessages(jsonData)
                if (!errors.empty) {
                    error(jsonData, errors)
                }
                worker(jsonData)
            }
        } else {
            log.error "No JSON response: ${response.text}"
            worker(null)
        }
    }

    static List convertJsonList(JSONArray json) {
        return json.collect { thing ->
            if (thing instanceof JSONObject) {
                return jsonObjectToMap(thing)
            }
            if (thing instanceof JSONArray) {
                return convertJsonList(thing)
            }
            return thing
        }
    }

    static Map jsonObjectToMap(JSONObject object) {
        Map map = [:]
        object.keySet().each { String key ->
            map.put(key, intelligentType(object, key))
        }
        return map
    }

    private static def intelligentType(JSONObject jsonObject, String key) {
        if (jsonObject.isNull(key)) {
            return null
        }
        def thing = jsonObject.get(key)
        if (thing instanceof JSONArray) {
            convertJsonList(thing)
        }
        if (thing instanceof JSONObject) {
            jsonObjectToMap(thing)
        }
        if (key.endsWith('Id') && thing instanceof String) {
            if (thing.isEmpty()) {
                return null
            }
            if (thing.isLong()) {
                return thing.toLong()
            }
        }
        return thing
    }

    private static List<String> getJsonErrorMessages(Map response) {
        List<String> errors = []
        if (response.error) {
            errors.add(response.error as String)
        }
        if (response.errors) {
            if (response.errors instanceof String) {
                errors.add(response.errors as String)
            } else {
                errors.addAll(response.errors as List<String>)
            }
        }
        return errors
    }

}

class RestCallException extends Throwable {

    RestCallException(String message) {
        super(message)
    }

    RestCallException(String message, Throwable cause) {
        super(message, cause)
    }

}

class AccessToken {
    String accessToken
    String refreshToken
    String refreshUrl

    AccessToken(String accessToken, String refreshToken, String refreshUrl) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.refreshUrl = refreshUrl
    }
}