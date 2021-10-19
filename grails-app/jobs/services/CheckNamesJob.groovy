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

package services

import au.org.biodiversity.nsl.*
import groovy.util.logging.Slf4j
import org.springframework.transaction.support.DefaultTransactionStatus

@Slf4j
class CheckNamesJob {

    def nameService
    def referenceService
    def instanceService
    static concurrent = false
    static sessionRequired = true

    static triggers = {
        simple repeatInterval: 5000l // execute job once in 5 seconds
    }

    def execute() {
        def author_id
        def reference_id
        def name_id
        Name.withTransaction {
            List<Notification> notifications = Notification.list(max: 5000, sort: 'id')
            notifications.each { Notification note ->
                Name.withNewTransaction { DefaultTransactionStatus tx ->
                    switch (note.message) {
                        case 'name updated':
                            log.debug "Name $note.objectId updated"
                            Name name = Name.get(note.objectId)
                            if (name) {
                                name_id = name.id
                                nameService.nameUpdated(name, note)
                            } else {
                                log.debug "Name $note.objectId  doesn't exist "
                            }
                            break
                        case 'name created':
                            log.debug "Name $note.objectId created"
                            Name name = Name.get(note.objectId)
                            if (name) {
                                nameService.nameCreated(name, note)
                            } else {
                                log.debug "Name $note.objectId doesn't exist"
                            }
                            break
                        case 'name deleted':
                            log.info "Name $note.objectId was deleted."
                            break
                        case 'author updated':
                            log.debug "Author $note.objectId updated"
                            Author author = Author.findById(note.objectId)
                            if (author) {
                                author_id = author.id
                                nameService.authorUpdated(author, note)
                                referenceService.authorUpdated(author, note)
                            } else {
                                log.debug "Author $note.objectId  doesn't exist"
                            }
                            break
                        case 'author created':
                        case 'author deleted':
                            //NSL-1032 ignore for now, deleted authors can't have names
                            break
                        case 'reference updated':
                            log.debug "Reference $note.objectId updated"
                            Reference reference = Reference.get(note.objectId)
                            if (reference) {
                                reference_id = reference.id
                                referenceService.checkReferenceChanges(reference)
                            } else {
                                log.debug "Reference $note.objectId doesn't exist"
                            }
                            break
                        case 'reference created':
                        case 'reference deleted':
                            break
                        case 'instance updated':
                            log.debug "Instance $note.objectId updated"
                            Instance instance = Instance.get(note.objectId)
                            if (instance) {
                                instanceService.checkInstanceChanges(instance)
                            } else {
                                log.debug "Instance $note.objectId doesn't exist"
                            }
                            break
                        case 'instance created':
                            log.debug "Instance $note.objectId created"
                            Instance instance = Instance.get(note.objectId)
                            if (instance) {
                                instanceService.checkInstanceCreated(instance)
                            } else {
                                log.debug "Instance $note.objectId doesn't exist"
                            }
                            break
                        case 'instance deleted':
                            log.debug "Instance $note.objectId deleted."
                            instanceService.checkInstanceDelete(note.objectId)
                            break
                        default:
                            //probably caused by previous error. This note will be deleted
                            log.error "unhandled notification $note.message:$note.objectId"
                    }
                    note.delete()
                    tx.flush()
                }
            }
        }
        if (author_id)
            referenceService.updateSynonymyOnTreeForAuthor(author_id)

        if (reference_id)
            referenceService.updateSynonymyOnTreeForReference(reference_id)

        if (name_id)
            nameService.updateSynonymyOnTreeForName(name_id)
    }
}
