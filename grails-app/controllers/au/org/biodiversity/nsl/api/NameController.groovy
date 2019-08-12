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

import au.org.biodiversity.nsl.*
import grails.gorm.transactions.Transactional
import org.apache.shiro.SecurityUtils

import static org.springframework.http.HttpStatus.*

@Transactional
class NameController implements WithTarget {

    def nameConstructionService
    def jsonRendererService
    def nameService
    def apniFormatService
    def instanceService
    def flatViewService
    def treeService
    def linkService
    def configService

    @SuppressWarnings("GroovyUnusedDeclaration")
    static responseFormats = [
            index             : ['html'],
            nameStrings       : ['json', 'xml', 'html'],
            apniFormat        : ['html'],
            apcFormat         : ['html'],
            apniFormatEmbed   : ['html'],
            apcFormatEmbed    : ['html'],
            delete            : ['json', 'xml', 'html'],
            family            : ['json', 'xml', 'html'],
            branch            : ['json', 'xml', 'html'],
            nameUpdateEventUri: ['json', 'xml', 'html'],
            exportNslSimple   : ['json', 'xml', 'html'],
            apni              : ['json', 'xml', 'html'],
            apc               : ['json', 'xml', 'html'],
            taxonSearch       : ['json', 'xml', 'html']
    ]

    static allowedMethods = [
            nameStrings       : ["GET", "PUT"],
            apniFormat        : ["GET"],
            apcFormat         : ["GET"],
            apniFormatEmbed   : ["GET"],
            apcFormatEmbed    : ["GET"],
            delete            : ["GET", "DELETE"],
            family            : ["GET"],
            branch            : ["GET"],
            nameUpdateEventUri: ["PUT", "DELETE"],
            exportNslSimple   : ["GET"],
            apni              : ["GET"],
            apc               : ["GET"],
            taxonSearch       : ["GET", "POST"]
    ]

    
    def index() {
        redirect(uri: '/docs/main.html')
    }


    def apniFormat(Name name) {
        if (name) {
            if (params.embed) {
                forward(controller: 'apniFormat', action: 'name', id: name.id)
            } else {
                forward(controller: 'apniFormat', action: 'display', id: name.id)
            }
        } else {
            notFound('name')
        }
    }


    def apniFormatEmbed(Name name) {
        if (name) {
            forward(controller: 'apniFormat', action: 'name', id: name.id)
        } else {
            notFound('name')
        }
    }


    def apcFormat(Name name) {
        if (name) {
            if (params.embed) {
                forward(controller: 'apcFormat', action: 'name', id: name.id)
            } else {
                forward(controller: 'apcFormat', action: 'display', id: name.id)
            }
        } else {
            notFound('name')
        }
    }


    def apcFormatEmbed(Name name) {
        if (name) {
            forward(controller: 'apcFormat', action: 'name', id: name.id)
        } else {
            notFound('name')
        }
    }


    def nameStrings(Name name) {
        withTarget(name) { ResultObject result, target ->
            ConstructedName constructedName = nameConstructionService.constructName(name)
            result.result = [:]
            result.result.fullMarkedUpName = constructedName.fullMarkedUpName
            result.result.simpleMarkedUpName = constructedName.simpleMarkedUpName
            result.result.fullName = constructedName.plainFullName
            result.result.simpleName = constructedName.plainSimpleName
            result.result.sortName = nameConstructionService.makeSortName(name, result.result.simpleName as String)
            if (request.method == 'PUT') {
                SecurityUtils.subject.checkRole('admin')
                name.fullNameHtml = result.result.fullMarkedUpName
                name.fullName = result.result.fullName
                name.simpleName = result.result.simpleName
                name.simpleNameHtml = result.result.simpleMarkedUpName
                name.sortName = result.result.sortName
                name.save()
            }
        }
    }


    def delete(Name name, String reason) {
        withTarget(name) { ResultObject result, target ->
            if (request.method == 'DELETE') {
                SecurityUtils.subject.checkRole('admin')
                result << nameService.deleteName(name, reason)
                if (!result.ok) {
                    result.status = FORBIDDEN
                }
            } else if (request.method == 'GET') {
                result << nameService.canDelete(name, 'dummy reason')
            } else {
                result.status = METHOD_NOT_ALLOWED
            }
        }
    }


    def family(Name name) {
        withTarget(name) { ResultObject result, target ->
            if (name.family) {
                result << [familyName: name.family]
            } else {
                result << [error: 'Family name not found']
                result.status = NOT_FOUND
            }
        }
    }


    def branch(Name name) {
        withTarget(name) { ResultObject result, target ->
            List<TreeVersionElement> tvePath = treeService.getElementPath(treeService.findCurrentElementForName(name, treeService.getAcceptedTree()))
            List<Name> namesInBranch = tvePath.collect { it.treeElement.name }
            result << [branch: namesInBranch]
        }
    }


    def apc(Name name) {
        withTarget(name) { ResultObject result, target ->
            TreeVersionElement tve = treeService.findCurrentElementForName(name, treeService.getAcceptedTree())
            if (tve == null) {
                result << ["inAPC"   : false,
                           excluded  : false,
                           operation : params.action,
                           "nsl-name": name?.id,
                           nameNs    : "",
                           nameId    : linkService.getPreferredLinkForObject(name),
                           taxonNs   : "",
                           taxonId   : null,
                           type      : ""
                ]
            } else {
                result << ["inAPC"   : true,
                           excluded  : tve.treeElement.excluded,
                           operation : params.action,
                           "nsl-name": name.id,
                           nameNs    : "${tve.treeVersion.tree.name}-name",
                           nameId    : linkService.getPreferredLinkForObject(name),
                           taxonNs   : "${tve.treeVersion.tree.name}-name",
                           taxonId   : tve.fullTaxonLink(),
                           type      : "${tve.treeVersion.tree.name}Concept"
                ]
            }
        }
    }


    def apni(Name name) {
        withTarget(name) { ResultObject result, target ->
            result << ["inAPNI"  : name != null,
                       operation : params.action,
                       "nsl-name": name.id,
                       nameNs    : "${configService.nameSpaceName}-name",
                       nameId    : linkService.getPreferredLinkForObject(name)]
        }
    }


    def nameUpdateEventUri(String uri) {
        if (request.method == 'PUT') {
            log.info "Adding $uri to event notification list"
            nameService.nameEventRegister(uri)
            respond(new ResultObject(text: "registered $uri"))
        } else if (request.method == 'DELETE') {
            log.info "Removing $uri from event notification list"
            nameService.nameEventUnregister(uri)
            respond(new ResultObject(text: "unregistered $uri"))
        }
    }

    /**
     * NSL-1171 look for an 'Acceptable' name to be used in eFlora by simple name
     *
     * @param name
     */

    def acceptableName(String name) {
        if (name) {
            List<String> status = ['legitimate', 'manuscript', 'nom. alt.', 'nom. cons.', 'nom. cons., nom. alt.', 'nom. cons., orth. cons.', 'nom. et typ. cons.', 'orth. cons.', 'typ. cons.']
            List<Name> names = Name.executeQuery('''
select n
from Name n
where (lower(n.fullName) like :query or lower(n.simpleName) like :query)
and n.instances.size > 0
and n.nameStatus.name in (:ns)
order by n.simpleName asc''',
                    [query: SearchService.tokenizeQueryString(name.toLowerCase()), ns: status], [max: 100])

            ResultObject result = new ResultObject([
                    action: params.action,
                    count : names.size(),
                    query : name,
                    names : names.collect { jsonRendererService.getBriefNameWithHtml(it) },
            ])
            return serviceRespond(result)
        } else {
            ResultObject result = new ResultObject([
                    status: NOT_FOUND,
                    action: params.action,
                    error : "${name ?: '(Blank)'} not found."
            ])
            serviceRespond(result)
        }
    }


    def findConcept(Name name, String term) {
        log.debug "search concepts for $term"
        withTarget(name) { ResultObject result, target ->
            List<String> terms = term.replaceAll('([,&])', '').split(' ')
            log.debug "terms are $terms"
            Integer highestRank = 0
            Instance match = null
            name.instances.each { Instance inst ->
                Integer rank = rank(inst.reference.citation, terms)
                if (rank > highestRank) {
                    highestRank = rank
                    match = inst
                }
            }
            result.matchedOn = term
            result.rank = highestRank
            result.instance = jsonRendererService.getBriefInstance(match)
        }
    }

    private static Integer rank(String target, List<String> terms) {
        terms.inject(0) { count, term ->
            target.contains(term) ? count + 1 : count
        } as Integer
    }


    def apniConcepts(Name name, Boolean relationships) {
        if (relationships == null) {
            relationships = true
        }

        log.info "getting APNI concept for $name"
        withTarget(name) { ResultObject result, target ->
            Map nameModel = apniFormatService.getNameModel(name, null, false)
            result.name = jsonRendererService.getBriefNameWithHtml(name)
            if (nameModel.treeVersionElement != null) {
                    result.name.inAPC = true
                result.name.APCExcluded = nameModel.treeVersionElement.treeElement.excluded
            } else {
                result.name.inAPC = false
            }
            result.name.family = jsonRendererService.getBriefNameWithHtml(nameModel.familyName as Name)
            result.references = nameModel.references.collect { Reference reference ->
                Map refMap = jsonRendererService.getBriefReference(reference)
                refMap.citations = []
                //noinspection GroovyAssignabilityCheck
                List<Instance> sortedInstances = instanceService.sortInstances(nameModel.instancesByRef[reference] as List<Instance>)

                //noinspection GroovyAssignabilityCheck
                sortedInstances.eachWithIndex { Instance instance, Integer i ->

                    if (nameModel.apc?.taxonUriIdPart == instance.id.toString()) {
                        if (nameModel.apc.typeUriIdPart == 'ApcConcept') {
                            refMap.APCReference = true
                        } else {
                            refMap.APCExcludedReference = true
                        }
                    }

                    if (instance.instanceType.standalone) {

                        Map inst = jsonRendererService.brief(instance, [
                                page         : instance.page,
                                instanceType : instance.instanceType.name,
                                relationships: []
                        ])
                        if (relationships) {
                            if (instance.instancesForCitedBy) {
                                instanceService.sortInstances(instance.instancesForCitedBy.findAll {
                                    it.instanceType.synonym
                                } as List<Instance>).each { Instance synonym ->
                                    inst.relationships << jsonRendererService.brief(synonym, [
                                            page        : instancePage(synonym),
                                            relationship: "$synonym.instanceType.name: $synonym.name.fullNameHtml",
                                            name        : synonym.name.fullName
                                    ])
                                }

                                instanceService.sortInstances(instance.instancesForCitedBy.findAll {
                                    it.instanceType.misapplied
                                } as List<Instance>).each { Instance missapp ->
                                    String rel = "${missapp.instanceType.name.replaceAll('misapplied', 'misapplication')}" +
                                            " $missapp.cites.name.fullNameHtml" +
                                            " by ${missapp?.cites?.reference?.citationHtml}: ${missapp?.cites?.page ?: '-'}"

                                    inst.relationships << jsonRendererService.brief(missapp, [
                                            page        : instancePage(missapp),
                                            relationship: rel,
                                            name        : missapp.cites.name.fullName
                                    ])
                                }

                                instanceService.sortInstances(instance.instancesForCitedBy.findAll {
                                    (!it.instanceType.synonym && !it.instanceType.name.contains('misapplied'))
                                } as List<Instance>).each { Instance synonym ->
                                    inst.relationships << jsonRendererService.brief(synonym, [
                                            page        : instancePage(synonym),
                                            relationship: "$synonym.instanceType.name: $synonym.name.fullNameHtml",
                                            name        : synonym.name.fullName
                                    ])
                                }
                            }

                            if (instance.instanceType.misapplied) {
                                inst.relationships << jsonRendererService.brief(instance, [
                                        page        : instancePage(instance),
                                        relationship: "$instance.instanceType.name to: $instance.citedBy.name.fullNameHtml" +
                                                " by  ${instance?.cites?.reference?.citationHtml}: ${instance?.cites?.page ?: '-'}",
                                        name        : instance.citedBy.name.fullName
                                ])
                            }
                        }
                        refMap.citations << inst
                    }

                    if (instance.instanceType.synonym || instance.instanceType.unsourced) {
                        refMap.citations << jsonRendererService.brief(instance, [
                                instanceType: instance.instanceType.name,
                                page        : instance.citedBy.page,
                                relationship: "$instance.instanceType.name of $instance.citedBy.name.fullNameHtml",
                                name        : instance.citedBy.name.fullName
                        ])
                    }

                    refMap.notes = instance.instanceNotes.collect { InstanceNote instanceNote ->
                        [
                                instanceNoteKey : instanceNote.instanceNoteKey.name,
                                instanceNoteText: instanceNote.value
                        ]
                    }
                }

                return refMap
            }
        }
    }

    //TODO the taxon view needs to be re-written to make this work again
    //see NSL-1805
    def taxonSearch() {
        def json = request.JSON
        Map searchParams = params
        if (json) {
            searchParams = new LinkedHashMap(json as Map)
        }

        Map taxonRecords = flatViewService.taxonSearch(searchParams.q as String)

        ResultObject result = new ResultObject([records: taxonRecords])
        serviceRespond(result)
    }

    private static String instancePage(Instance instance) {
        if (instance.page) {
            return instance.page
        } else {
            if (instance.instanceType.citing && instance.citedBy.page) {
                if (instance.instanceType.name.contains('common')) {
                    return "~ $instance.citedBy.page"
                } else {
                    return instance.citedBy.page
                }
            } else {
                return '-'
            }
        }
    }

    private void notFound(String targetInfo) {
        ResultObject result = new ResultObject([
                action: params.action,
                error : "$targetInfo not found."
        ])
        //noinspection GroovyAssignabilityCheck
        respond(result, [view: '/common/serviceResult', model: [data: result], status: NOT_FOUND])
    }

}

