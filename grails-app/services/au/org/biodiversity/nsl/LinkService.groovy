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


import org.springframework.cache.CacheManager

class LinkService {
    def restCallService
    def grailsApplication
    def configService
    CacheManager grailsCacheManager

    private String lowerNameSpaceName
    private String preferredHost
    private AccessToken JWT

    String nameSpace() {
        if (!lowerNameSpaceName) {
            lowerNameSpaceName = configService.getNameSpaceName().toLowerCase()
        }
        return lowerNameSpaceName
    }

    List<Map> getLinksForObject(target) {
        try {
            List<Map> results = []
            if (target) {
                String url = getLinkServiceUrl(target, 'links', true)
                restCallService.json('get', url,
                        { List links ->
                            results = links
                        },
                        { Map data, List errors ->
                            log.error "Couldn't get links for $target. Errors: $errors"
                        },
                        { data ->
                            String link = addTargetLink(target)
                            if (link) {
                                results = [[link: link, resourceCount: 1, preferred: true]]
                            } else {
                                log.error "Links not found for $target, and couldn't be added."
                            }
                        },
                        { data ->
                            log.error "Something went wrong getting $url. Response: $data"
                        }
                )
            }
            return results
        } catch (RestCallException e) {
            log.error(e.message)
            return []
        }
    }

    String addTargetLink(target) {
        //noinspection GroovyAssignabilityCheck
        String identity = new TargetParam(target, nameSpace()).addIdentityParamString()
        return addIdentifier(identity, target.toString())
    }

    private String addIdentifier(String identity, String target) {
        String mapper = mapper(true)
        String newLink = null
        try {
            if (mapperAuth()) {
                String uri = "$mapper/api/add-identifier?$identity"
                restCallService.json('put', uri,
                        { data ->
                            newLink = data.uri
                        },
                        { Map data, List errors ->
                            log.error "Couldn't add link for $target. Errors: $errors"
                        },
                        { data ->
                            log.error "404 adding link: $uri. Response: $data"
                        },
                        { data ->
                            log.error "Something went wrong adding link: $uri. Response: $data"
                        }, JWT
                )
            } else {
                log.error "Not logged into mapper."
            }
        } catch (RestCallException e) {
            log.error "Error $e.message adding link for $target"
        }
        return newLink
    }

    /**
     * taxon identifier is a synthetic object, or a tag identifier and relates to many tree_version_elements. This creates
     * a taxon object type identifier of the form host/taxon/namespace/123456 where namespace is more a service identifier.
     * @param treeVersionElement
     * @return
     */
    String addTaxonIdentifier(TreeVersionElement treeVersionElement) {
        String identity = "nameSpace=${nameSpace()}&objectType=taxon&idNumber=${treeVersionElement.taxonId}"
        return addIdentifier(identity, "Taxon ID ${treeVersionElement.taxonId}")
    }

    Map bulkAddTargets(Collection<TreeVersionElement> targets) {
        List<Map> identities = targets.collect { target -> new TargetParam(target, nameSpace()).briefParamMap() }
        String mapper = mapper(true)
        Map result = [success: true]
        try {
            if (mapperAuth()) {
                String url = "$mapper/api/bulk-add-identifiers"
                String action = "Bulk add TreeVersionElements"
                restCallService.jsonPost([identifiers: identities], url,
                        { Map data ->
                            log.debug "$action. Response: $data"
                            result << data
                        },
                        { Map data, List errors ->
                            log.error "Couldn't $action. Errors: $errors"
                            result = [success: false, errors: errors]
                        },
                        { data ->
                            log.error "Couldn't $action. Not found response: $data"
                            result = [success: false, errors: ["Couldn't $action. Not found response: $data"]]
                        },
                        { data ->
                            log.error "Couldn't $action. Response: $data"
                            result = [success: false, errors: ["Couldn't $action. Response: $data"]]
                        }, JWT
                )
            } else {
                log.error "Not logged into mapper."
            }
        } catch (RestCallException e) {
            log.error e.message
            result = [success: false, errors: "Communication error with mapper."]
        }
        return result
    }

    Map bulkRemoveTargets(Collection<TreeVersionElement> targets) {
        List<Map> identities = targets.collect { target -> new TargetParam(target, nameSpace()).briefParamMap() }
        String mapper = mapper(true)
        Map result = [success: true]
        String url = "$mapper/api/bulk-remove-identifiers"
        String action = "Bulk remove TreeVersionElements"
        Map sendData = [identifiers: identities]
        return postIt(sendData, url, action, result)
    }

    private Map postIt(LinkedHashMap<String, List<String>> sendData, String url, String action, Map result) {
        try {
            if (mapperAuth()) {
                restCallService.jsonPost(sendData, url,
                        { Map data ->
                            log.debug "$action. Response: $data"
                            result << data
                        },
                        { Map data, List errors ->
                            log.error "Couldn't $action. Errors: $errors"
                            result = [success: false, errors: errors]
                        },
                        { data ->
                            log.error "Couldn't $action. Not found response: $data"
                            result = [success: false, errors: ["Couldn't $action. Not found response: $data"]]
                        },
                        { data ->
                            log.error "Couldn't $action. Response: $data"
                            result = [success: false, errors: ["Couldn't $action. Response: $data"]]
                        }, JWT
                )
            } else {
                log.error "Not logged into mapper."
                result = [success: false, errors: ["Couldn't $action. Response: Not logged into mapper."]]
            }
        } catch (RestCallException e) {
            log.error e.message
            result = [success: false, errors: "Communication error with mapper."]
        }
        return result
    }

    String getPreferredLinkForObjectSansHost(Object target) {
        String link = getPreferredLinkForObject(target)
        if (link) {
            return link - "${getPreferredHost()}/"
        }
        return null
    }

    String getPreferredLinkForObject(Object target) {
        if (target == null) {
            log.warn "Can't get link for null object"
            return null
        }
        if (target instanceof Name && target.uri) {
            return "${getPreferredHost()}/${target.uri}"
        }
        if (target instanceof Instance && target.uri) {
            return "${getPreferredHost()}/${target.uri}"
        }
        String link = null
        try {
            String url = getLinkServiceUrl(target, 'preferred-link', true)

            restCallService.json('get', url,
                    { Map data ->
                        link = data.link
                    },
                    { Map data, List errors ->
                        log.error "Couldn't get links for $target. Errors: $errors"
                    },
                    {
                        link = addTargetLink(target)
                        if (!link) {
                            log.error "Link not found for $target, and couldn't be added."
                        }
                    },
                    { data ->
                        log.error "Something went wrong getting $url. Response: $data"
                    }
            )
        } catch (RestCallException e) {
            log.error "Error $e.message getting preferred link for $target"
        }
        return link
    }

    /**
     * Ask the mapper for the preferred host name and context path (sans protocol e.g. http://)
     * @return
     */

    String getPreferredHost() {
        if (!preferredHost) {
            String host = null
            try {
                String url = getInternalLinkServiceUrl('preferred-host')

                restCallService.json('get', url,
                        { Map data ->
                            host = data.host
                        },
                        { Map data, List errors ->
                            log.error "Couldn't get preferred host $errors"
                        },
                        {
                            log.error "Couldn't get preferred host: 404 not found"
                        },
                        { data ->
                            log.error "Something went wrong getting preferred host. Response: $data"
                        }
                )
            } catch (RestCallException e) {
                log.error "Error $e.message getting preferred host"
            }
            preferredHost = host
        }
        return preferredHost
    }

    /**
     * Get the domain object matching a URI based identifier. This method asks
     * the mapper for the mapper identity for a URI, and recover the object.
     *
     * It returns null if:-
     * - the uri is not known to the mapper,
     * - there are multiple ids for the uri,
     * - no object matches the id, namespace, and object type
     *
     * @param uri a uri perhaps known to the mapper
     * @return a domain object matching the object type returned by the mapper identity
     */

    Object getObjectForLink(String uri) {
        Map identity = getMapperIdentityForLink(uri)
        if (!identity) {
            return null
        }

        def domainObject = getDomainObjectFromIdentity(identity, uri)

        if (!domainObject) {
            log.error "Object for $identity not found"
        }

        return domainObject

    }

    /**
     * Find the domain object for a given Identity Map (triple) of:
     *
     *  nameSpace, objectType, idNumber
     *
     * @param identity
     * @return Domain object or null if not found
     */
    private static Object getDomainObjectFromIdentity(Map identity, String uri) {

        Long idNumber = identity.idNumber

        switch (identity.objectType) {
            case 'name':
                return Name.get(idNumber)
                break
            case 'author':
                return Author.get(idNumber)
                break
            case 'instance':
                return Instance.get(idNumber)
                break
            case 'reference':
                return Reference.get(idNumber)
                break
            case 'instanceNote':
                return InstanceNote.get(idNumber)
                break
            case 'tree':
                return Tree.get(idNumber)
                break
            case 'treeVersion':
                return TreeVersion.get(idNumber)
            case 'treeElement':
                return TreeVersionElement.get(uri)
            default:
                return null
        }

    }

    /**
     * Ask the mapper for the single mapper identity for a URI. If the uri matches multiple identities,
     * this method returns null. This method also returns null if the uri is unknown to the mapper.
     * @param uri a uri known to the mapper
     * @return The Mapper Identity as a Map including nameSpace, objectType, and idNumber.
     */


    Map getMapperIdentityForLink(String uri) {
        Map identity = null
        try {
            String url = "${mapper(true)}/api/current-identity?uri=${URLEncoder.encode(uri, "UTF-8")}"

            restCallService.json('get', url,
                    { List identities ->
                        if (identities.size() == 1) {
                            Map ident = identities[0] as Map
                            identity = [objectType: ident.objectType, nameSpace: ident.nameSpace, idNumber: ident.idNumber, versionNumber: ident.versionNumber]
                        } else {
                            log.error "expected only 1 identity for $uri"
                        }
                    },
                    { Map data, List errors ->
                        log.error "Couldn't get Identity for $uri. Errors: $errors"
                    },
                    { data ->
                        log.error "Identity not found for $uri. Response: $data"
                    },
                    { data ->
                        log.error "Something went wrong getting $url. Response: $data"
                    }
            )
        } catch (RestCallException e) {
            log.error "Error $e.message getting mapper id for $uri"
            return null
        }
        return identity
    }

    String getLinkServiceUrl(target, String endPoint = 'links', Boolean internal = false) {
        String identity = new TargetParam(target, nameSpace()).identityUriString()
        if (identity) {
            String mapper = mapper(internal)
            String url = "${mapper}/api/${endPoint}/${identity}"
            return url
        }
        return null
    }

    private String getInternalLinkServiceUrl(String endPoint) {
        String mapper = mapper(true)
        String url = "${mapper}/api/${endPoint}"
        return url
    }

    private String mapper(Boolean internal) {
        return (internal ? configService.internalMapperURL : configService.publicMapperURL)
    }

    private Boolean mapperAuth() {
        if (JWT) {
            return true
        }
        String mapper = mapper(true)
        JWT = restCallService.jwtLogin("$mapper/api/login", configService.mapperCredentials, "$mapper/api/oauth/access_token")
        return JWT != null
    }

    Map deleteNameLinks(Name name, String reason) {
        String identity = new TargetParam(name, nameSpace()).identityParamString() + "&reason=${reason.encodeAsURL()}"
        evictAllCache(name)
        deleteTargetLinks(identity)
    }

    Map deleteInstanceLinks(Instance instance, String reason) {
        String identity = new TargetParam(instance, nameSpace()).identityParamString() + "&reason=${reason.encodeAsURL()}"
        evictAllCache(instance)
        deleteTargetLinks(identity)
    }

    Map deleteReferenceLinks(Reference reference, String reason) {
        String identity = new TargetParam(reference, nameSpace()).identityParamString() + "&reason=${reason.encodeAsURL()}"
        evictAllCache(reference)
        deleteTargetLinks(identity)
    }

    private Map deleteTargetLinks(String identity) {
        String mapper = mapper(true)
        Map result = [success: true]
        try {
            if (mapperAuth()) {
                String url = "$mapper/api/delete-identifier?$identity"
                restCallService.json('delete', url,
                        { data ->
                            log.debug "Deleted links for $identity. Response: $data"
                        },
                        { Map data, List errors ->
                            log.error "Couldn't delete $identity links. Errors: $errors"
                            result = [success: false, errors: errors]
                        },
                        { data ->
                            log.error "Couldn't delete $identity links. Not found response: $data"
                            result = [success: false, errors: ["Couldn't delete $identity links. Not found response: $data"]]
                        },
                        { data ->
                            log.error "Couldn't delete $identity links. Response: $data"
                            result = [success: false, errors: ["Couldn't delete $identity links. Response: $data"]]
                        }, JWT
                )
            } else {
                log.error "Not logged into mapper"
                result = [success: false, errors: "Not logged into mapper."]
            }
        } catch (RestCallException e) {
            log.error e.message
            result = [success: false, errors: "Communication error with mapper."]
        }
        return result
    }

    Map moveTargetLinks(Reference from, Reference to) {
        evictAllCache(from)
        evictAllCache(to)
        if (from && to) {
            Map paramsMap = new TargetParam(from, nameSpace()).paramMap('fromNameSpace', 'fromObjectType', 'fromIdNumber', 'fromVersionNumber') +
                    new TargetParam(to, nameSpace()).paramMap('toNameSpace', 'toObjectType', 'toIdNumber', 'toVersionNumber')
            moveTargetLinks(paramsMap)
        } else {
            return [success: false, errors: "Invalid targets $from, $to."]
        }
    }

    Map moveTargetLinks(Author from, Author to) {
        evictAllCache(from)
        evictAllCache(to)
        if (from && to) {
            Map paramsMap = new TargetParam(from, nameSpace()).paramMap('fromNameSpace', 'fromObjectType', 'fromIdNumber', 'fromVersionNumber') +
                    new TargetParam(to, nameSpace()).paramMap('toNameSpace', 'toObjectType', 'toIdNumber', 'toVersionNumber')
            moveTargetLinks(paramsMap)
        } else {
            return [success: false, errors: "Invalid targets $from, $to."]
        }
    }

    Map moveTargetLinks(Name from, Name to) {
        evictAllCache(from)
        evictAllCache(to)
        if (from && to) {
            Map paramsMap = new TargetParam(from, nameSpace()).paramMap('fromNameSpace', 'fromObjectType', 'fromIdNumber', 'fromVersionNumber') +
                    new TargetParam(to, nameSpace()).paramMap('toNameSpace', 'toObjectType', 'toIdNumber', 'toVersionNumber')
            moveTargetLinks(paramsMap)
        } else {
            return [success: false, errors: "Invalid targets $from, $to."]
        }
    }

    Map moveTargetLinks(Map paramsMap) {
        String mapper = mapper(true)
        Map result = [success: true]
        try {
            if (mapperAuth()) {
                String url = "$mapper/api/move-identity"

                restCallService.jsonPost(paramsMap, url,
                        { Map data ->
                            log.debug "Moved $paramsMap. Response: $data"
                        },
                        { Map data, List errors ->
                            log.error "Couldn't move $paramsMap. Errors: $errors"
                            result = [success: false, errors: errors]
                        },
                        { data ->
                            log.error "Couldn't move $paramsMap. Not found response: $data"
                            result = [success: false, errors: ["Couldn't move $paramsMap. Not found response: $data"]]
                        },
                        { data ->
                            log.error "Couldn't move $paramsMap. Response: $data"
                            result = [success: false, errors: ["Couldn't move $paramsMap. Response: $data"]]
                        }, JWT
                )
            } else {
                log.error "Not logged into mapper"
                result = [success: false, errors: "Not logged into mapper."]
            }
        } catch (RestCallException e) {
            log.error e.message
            result = [success: false, errors: "Communication error with mapper."]
        }
        return result
    }

    Map removeNameLink(Name name, String uri) {
        String identity = new TargetParam(name, nameSpace()).identityParamString()
        evictAllCache(name)
        removeTargetLink(identity, uri)
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    Map removeInstanceLink(Instance instance, String uri) {
        String identity = new TargetParam(instance, nameSpace()).identityParamString()
        evictAllCache(instance)
        removeTargetLink(identity, uri)
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    Map removeReferenceLink(Reference reference, String uri) {
        String identity = new TargetParam(reference, nameSpace()).identityParamString()
        evictAllCache(reference)
        removeTargetLink(identity, uri)
    }

    private Map removeTargetLink(String identity, String targetUri) {
        String mapper = mapper(true)
        String url = "$mapper/api/remove-identifier-from-uri?$identity" + "&uri=${targetUri.encodeAsURL()}"
        Map result = [success: true]
        String whatImDoing = "Remove link $targetUri from $identity"
        try {
            if (mapperAuth()) {
                restCallService.json('delete', url,
                        { Map data ->
                            log.debug "Removed link to $identity. Response: $data"
                        },
                        { Map data, List errors ->
                            log.error "Couldn't $whatImDoing. Errors: $errors"
                            result = [success: false, errors: errors]
                        },
                        { data ->
                            log.error "Couldn't $whatImDoing. Not found response: $data"
                            result = [success: false, errors: ["Couldn't $whatImDoing. Not found response: $data"]]
                        },
                        { data ->
                            log.error "Couldn't $whatImDoing. Response: $data"
                            result = [success: false, errors: ["Couldn't $whatImDoing. Response: $data"]]
                        }, JWT
                )
            } else {
                log.error "Not logged into mapper"
                result = [success: false, errors: "Not logged into mapper."]
            }
        } catch (RestCallException e) {
            log.error e.message
            result = [success: false, errors: "Communication error with mapper."]
        }
        return result
    }
}
