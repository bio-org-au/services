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

import au.org.biodiversity.nsl.Author
import au.org.biodiversity.nsl.NameConstructionService
import au.org.biodiversity.nsl.RefAuthorRole
import au.org.biodiversity.nsl.Reference
import grails.gorm.transactions.Transactional
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authz.annotation.RequiresRoles

import static org.springframework.http.HttpStatus.*

@Transactional
class ReferenceController implements WithTarget {

    def referenceService
    def jsonRendererService

    static responseFormats = [
            index            : ['html'],
            citationStrings  : ['json', 'xml', 'html'],
            delete           : ['json', 'xml', 'html'],
            deduplicateMarked: ['json', 'xml', 'html'],
            move             : ['json', 'xml', 'html']
    ]

    static allowedMethods = [
            citationStrings  : ["GET", "PUT"],
            delete           : ["GET", "DELETE"],
            deduplicateMarked: ["DELETE"],
            move             : ["DELETE"]
    ]

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        def refs = Reference.list(params)
        respond(refs, [status: OK, view: '/common/serviceResult', model: [data: [max: params.max, references: refs]]])
    }


    def citationStrings(Reference reference) {
        withTarget(reference) { ResultObject result, target ->

            Author unknownAuthor = Author.findByName('-')
            RefAuthorRole editor = RefAuthorRole.findByName('Editor')

            String citationHtml = referenceService.generateReferenceCitation(reference, unknownAuthor, editor)
            result << [
                    result: [
                            citationHtml: citationHtml,
                            citation    : NameConstructionService.stripMarkUp(citationHtml)
                    ]
            ]

            if (request.method == 'PUT') {
                SecurityUtils.subject.checkRole('admin')
                referenceService.setCitation(target, result.result.citation, result.result.citationHtml)
            }
        }
    }

    
    def delete(Reference reference, String reason) {
        withTarget(reference) { ResultObject result, target ->
            if (request.method == 'DELETE') {
                SecurityUtils.subject.checkRole('admin')
                result << referenceService.deleteReference(reference, reason)
                if (!result.ok) {
                    results.status = FORBIDDEN
                }
            } else if (request.method == 'GET') {
                result << referenceService.canDelete(reference, 'dummy reason')
            } else {
                result.status = METHOD_NOT_ALLOWED
            }
        }
    }

    
    @RequiresRoles('admin')
    def move(Reference reference, Long target, String user) {
        Reference targetRef = null
        if (target) {
            targetRef = Reference.get(target)
        }

        withTargets(["source Reference": reference, "target Reference": targetRef]) { ResultObject result ->
            if (!user) {
                user = SecurityUtils.subject.principal.toString()
            }
            if (targetRef == reference) {
                result.status = BAD_REQUEST
                result.ok = false
                result.error("Source and target are the same, but shouldn't be.")
                return
            }
            result << referenceService.moveReference(reference, targetRef, user)
            if (!result.ok) {
                result.status = FORBIDDEN
            }
        }
    }

    
    @RequiresRoles('admin')
    def deduplicateMarked(String user) {
        if (!user) {
            user = SecurityUtils.subject.principal.toString()
        }
        ResultObject results = new ResultObject(referenceService.deduplicateMarked(user))
        //noinspection GroovyAssignabilityCheck

        respond(results, [status: OK, view: '/common/serviceResult', model: [data: results,]])
    }
}
