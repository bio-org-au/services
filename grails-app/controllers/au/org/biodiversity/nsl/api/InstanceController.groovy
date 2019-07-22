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
import grails.transaction.Transactional
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authz.annotation.RequiresRoles
import org.grails.plugins.metrics.groovy.Timed

import java.sql.Timestamp

import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED

@Transactional
class InstanceController extends BaseApiController {

    def jsonRendererService
    def instanceService
    def treeService
    def linkService

    @SuppressWarnings("GroovyUnusedDeclaration")
    static responseFormats = ['json', 'xml', 'html']

    static allowedMethods = [
            delete: ["GET", "DELETE"]
    ]

    def index() {}

    @Timed()
    delete(Instance instance, String reason) {
        withTarget(instance) { ResultObject result, target ->
            if (request.method == 'DELETE') {
                SecurityUtils.subject.checkRole('admin')
                result << instanceService.deleteInstance(instance, reason)
                if (!result.ok) {
                    result.status = FORBIDDEN
                }
            } else if (request.method == 'GET') {
                result << instanceService.canDelete(instance, 'dummy reason')
            } else {
                result.status = METHOD_NOT_ALLOWED
            }
        }
    }

    def elementDataFromInstance(Long id) {
        ResultObject results = require('Instance id': id)

        handleResults(results) {
            Instance instance = got({ Instance.get(id) }, "Instance with id $id not found.") as Instance
            TaxonData taxonData = treeService.elementDataFromInstance(instance)
            if (taxonData) {
                results.payload = taxonData.asMap()
            } else {
                throw new ObjectNotFoundException("Couldn't get data for $instance.")
            }
        }
    }


    @RequiresRoles('treeBuilder')
    def editInstanceNote(Long id, String value) {
        InstanceNote note = InstanceNote.get(id)
        String user = treeService.authorizeTreeBuilder()
        if (note) {
            Name name = note.instance.name
            String nameLink = linkService.getPreferredLinkForObject(name) + "/api/apni-format"
            if (value) {
                note.value = value
                note.updatedAt = new Timestamp(System.currentTimeMillis())
                note.updatedBy = user
                note.save()
            } else {
                log.warn "Deleting instance note $note.id: $note.instanceNoteKey.name: $note.value"
                note.delete()
            }
            redirect(url: nameLink)
        } else {
            throw new ObjectNotFoundException("Couldn't get note with id $id.")
        }
    }
}
