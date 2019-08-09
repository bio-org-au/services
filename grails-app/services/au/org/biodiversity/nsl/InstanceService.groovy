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

import grails.gorm.transactions.Transactional
import org.apache.shiro.authz.annotation.RequiresRoles
import org.springframework.transaction.TransactionStatus

@Transactional
class InstanceService implements AsyncHelper {

    def treeService
    def linkService
    def auditService

    /**
     * Delete the instance
     *
     * check we can delete this instance
     * remove references to the instance from tree nodes
     * mark mapper links to this instance as deleted
     *
     * @param instance
     * @param reason
     * @return
     */
    @RequiresRoles('admin')
    Map deleteInstance(Instance instance, String reason) {
        Map canWeDelete = canDelete(instance, reason)
        if (canWeDelete.ok) {
            try {
                Name.withTransaction { TransactionStatus t ->
                    Map response = linkService.deleteInstanceLinks(instance, reason)
                    if (!response.success) {
                        List<String> errors = ["Error deleting link from the mapper"]
                        errors.addAll(response.errors as List<String>)
                        return [ok: false, errors: errors]
                    }

                    instance.refresh()
                    instance.delete()
                    t.flush()
                }
            } catch (e) {
                List<String> errors = [e.message]
                while (e.cause) {
                    e = e.cause
                    errors << e.message
                }
                return [ok: false, errors: errors]
            }
        }
        return canWeDelete
    }

    /**
     * Can we delete this instance.
     *
     * Check:
     * 1. no other instances cite this instance
     * 2. no other instances say they are citedBy this instance
     * 3. there are no Instance Notes for this instance
     * 4. this instance is not the parent of any other instance
     * 5. there are no external references for this reference
     * 6. there are no comments on this instance
     * 7. check this instance is not currently in APC
     *
     * @param instance
     * @return a map with ok and a list of error Strings
     */
    Map canDelete(Instance instance, String reason) {
        List<String> errors = []
        if (!reason) {
            errors << 'You need to supply a reason for deleting this instance.'
        }
        List<TreeVersionElement> currentTves = treeService.instanceInAnyCurrentTree(instance)
        if (currentTves.size()) {
            List<String> trees = currentTves.collect { tve ->
                if(tve.treeVersion.published) {
                    "Currently published tree $tve.treeVersion.tree.name"
                } else {
                    "Draft $tve.treeVersion.tree.name: $tve.treeVersion.draftName."
                }
            } as List<String>
            errors << "This instance is in ${trees.join(', ')}.".toString()
        }
        if (instance.instancesForCites) {
            errors << "There are ${instance.instancesForCites.size()} instances that cite this.".toString()
        }
        if (instance.instancesForCitedBy) {
            errors << "There are ${instance.instancesForCitedBy.size()} instances that say this cites it.".toString()
        }
        if (instance.instancesForParent) {
            errors << "This is a parent for ${instance.instancesForParent.size()} instances.".toString()
        }
        if (instance.instanceNotes) {
            errors << "There are ${instance.instanceNotes.size()} instances notes on this.".toString()
        }
        if (instance.comments) {
            errors << "There are ${instance.comments.size()} comments for this instance.".toString()
        }
        if (instance.resources) {
            String resources = (instance.resources.collect { it.resourceType.name } as Set).join('and ')
            if(instance.resources.size() == 1){
                errors << "There is a ${resources} resource for this instance.".toString()
            } else {
                errors << "There are ${resources} resources for this instance.".toString()
            }
        }

        if (errors.size() > 0) {
            return [ok: false, errors: errors]
        }
        return [ok: true]
    }

    List<Instance> findPrimaryInstance(Name name) {
        if (name) {
            return Instance.executeQuery("select i from Instance i where i.name = :name and i.instanceType.primaryInstance = true", [name: name])
        }
        return null
    }

    List<Instance> sortInstances(List<Instance> instances) {
        instances.sort { a, b ->
            //NSL-1827 use parent details on references where appropriate
            Integer aRefYear = ReferenceService.findReferenceYear(a.cites?.reference)
            Integer bRefYear = ReferenceService.findReferenceYear(b.cites?.reference)
            if (a.citedBy == b.citedBy) {
                if (a.instanceType.sortOrder == b.instanceType.sortOrder) {
                    if (aRefYear == bRefYear) {
                        if (a.reference == b.reference) {
                            if (a.page == b.page) {
                                return b.id <=> a.id
                            }
                            return b.page <=> a.page
                        }
                        return a.reference.citation <=> b.reference.citation
                    }
                    return (aRefYear) <=> (bRefYear)
                }
                return a.instanceType.sortOrder <=> b.instanceType.sortOrder
            }
            return compareReferences(a, b, 'citedBy')
        }
    }

    static Integer compareReferences(Instance a1, Instance b1, String sortOn = null) {
        Instance a = (sortOn ? a1[sortOn] : a1) as Instance
        Instance b = (sortOn ? b1[sortOn] : b1) as Instance
        if (a && b) {
            if (a.reference.year == b.reference.year) {
                if (a.reference == b.reference) {
                    if (a.page == b.page) {
                        return b.id <=> a.id
                    }
                    return b.page <=> a.page
                }
                return a.reference.citation <=> b.reference.citation
            }
            return (a.reference.year) <=> (b.reference.year)
        }
        return a <=> b

    }

    /**
     * Get the instance note key by name. If create is provided and set true then the instance note key will be created
     * with default values (sort order = 100) and returned if it doesn't exist.
     * @param name
     * @param create - optional, default false
     * @return the key or null if not found and create is false.
     */
    InstanceNoteKey getInstanceNoteKey(String name, Boolean create = false) {
        if (!name) {
            //don't create or even look for a blank key
            return null
        }
        InstanceNoteKey key = InstanceNoteKey.findByName(name)
        if (key) {
            return key
        }
        if (create) {
            key = new InstanceNoteKey(
                    name: name,
                    sortOrder: 100,
                    descriptionHtml: "(description of <b>$name</b>)",
                    rdfId: name.toLowerCase().replaceAll(' ', '-')
            )
            key.save()
            return key
        }
        return null
    }

    def replaceXICSinInstanceNotes() {

        doAsync('Replace XICS in instance notes') {

            def count = InstanceNote.executeQuery("select count(note) from InstanceNote note where regex(value, '.*(\\~[a-zA-Z]|\\<[A-Z]|\\^).*') = true").first()
            log.debug "about to change $count Instance Notes"

            long changed = 0

            List<InstanceNote> notes = InstanceNote.executeQuery("select note from InstanceNote note where regex(value, '.*(\\~[a-zA-Z]|\\<[A-Z]|\\^).*') = true order by id")
            InstanceNote.withSession { session ->
                notes.each { InstanceNote note ->
                    String newValue = ApniFormatService.transformXicsToUTF8(note.value)
                    if (newValue != note.value) {
                        note.value = newValue
                        note.save()
                        changed++
                    }
                }
                session.flush()
                session.clear()
            }
            log.debug "changed $changed notes"
        }
    }

    def checkInstanceChanges(Instance instance) {
        //if this is a relationship instance we want to check if it's citedBy instance is on any tree and
        //create synonymy changed EventRecords
        if (instance.citedBy) {
            treeService.checkSynonymyUpdated(instance.citedBy)
        }
    }

    def checkInstanceCreated(Instance instance) {
        if(!instance.uri) {
            instance.uri = linkService.getPreferredLinkForObjectSansHost(instance)
            instance.save()
        }
        //if this is a relationship instance we want to check if it's citedBy instance is on any tree and
        //create synonymy changed EventRecords
        if (instance.citedBy) {
            treeService.checkSynonymyUpdated(instance.citedBy)
        }
    }

    /**
     * Check and update anything that may rely on this deleted instance ID e.g. trees.
     * Since the instance is deleted, it's hard to tell what it is. So we just get the treeService to check if this
     * instance id belongs to any current tree versions or is a synonym of any accepted tree element.
     * @param id
     * @return
     */
    def checkInstanceDelete(Long id) {
        Map instanceData = auditService.recoverDeletedInstanceData(id)
        if (instanceData) {
            treeService.checkUsageOfDeletedInstance(id, instanceData.cited_by_id as Long, (instanceData.updated_by ?: 'notification') as String)
        } else {
            log.error "Audit does not contain deleted instance $id. Check audit is working."
        }
    }

    def updateMissingUris() {
        Instance.findAllByUriIsNull().each { Instance instance ->
            instance.uri = linkService.getPreferredLinkForObjectSansHost(instance)
            instance.save()
        }
    }
}
