package au.org.biodiversity.nsl.api

import au.org.biodiversity.nsl.*
import org.apache.shiro.authz.AuthorizationException

import static org.springframework.http.HttpStatus.NOT_FOUND

class TreeElementController extends BaseApiController {

    def treeService
    def jsonRendererService
    def linkService

    static responseFormats = [
            placeElement           : ['json', 'html'],
            replaceElement         : ['json', 'html'],
            removeElement          : ['json', 'html'],
            editElementProfile     : ['json', 'html'],
            editElementStatus      : ['json', 'html'],
            elementDataFromInstance: ['json', 'html'],
            editComment            : ['html'],
            updateSynonymy         : ['json', 'html'],

    ]

    static allowedMethods = [
            placeElement           : ['PUT'],
            replaceElement         : ['PUT'],
            removeElement          : ['DELETE', 'POST'],
            editElementProfile     : ['POST'],
            editElementStatus      : ['POST'],
            elementDataFromInstance: ['GET'],
            editComment            : ['POST'],
            updateSynonymy         : ['POST']

    ]

    /**
     *
     * @param parentTaxonUri the URI or link of the parent treeVersionElement
     * @param instanceUri
     * @param excluded
     * @return
     */
    def placeElement() {
        withJsonData(request.JSON, false, ['parentElementUri', 'instanceUri', 'excluded']) { ResultObject results, Map data ->

            String parentTaxonUri = data.parentElementUri
            String instanceUri = data.instanceUri
            Boolean excluded = data.excluded
            Map profile = data.profile as Map
            TreeVersionElement treeVersionElement = TreeVersionElement.get(parentTaxonUri)
            if (treeVersionElement) {
                String userName = treeService.authorizeTreeOperation(treeVersionElement.treeVersion.tree)
                results.payload = treeService.placeTaxonUri(treeVersionElement, instanceUri, excluded, profile, userName)
            } else {
                results.ok = false
                results.fail("Parent taxon with id '$parentTaxonUri' not found", NOT_FOUND)
            }
        }
    }

    def placeTopElement() {
        withJsonData(request.JSON, false, ['versionId', 'instanceUri', 'excluded']) { ResultObject results, Map data ->
            log.debug "place top element $data"
            Long treeVersionId = data.versionId as Long
            String instanceUri = data.instanceUri
            Boolean excluded = data.excluded
            Map profile = data.profile as Map
            TreeVersion treeVersion = TreeVersion.get(treeVersionId)
            if (treeVersion) {
                String userName = treeService.authorizeTreeOperation(treeVersion.tree)
                results.payload = treeService.placeTaxonUri(treeVersion, instanceUri, excluded, profile, userName)
            } else {
                results.ok = false
                results.fail("Tree version with id '$treeVersionId' not found", NOT_FOUND)
            }
        }
    }

    def replaceElement() {
        withJsonData(request.JSON, false, ['currentElementUri', 'newParentElementUri', 'instanceUri']) { ResultObject results, Map data ->
            String instanceUri = data.instanceUri
            String currentElementUri = data.currentElementUri
            String newParentElementUri = data.newParentElementUri
            Boolean excluded = data.excluded ?: false
            Map profile = data.profile as Map

            TreeVersionElement currentElement = TreeVersionElement.get(currentElementUri)
            TreeVersionElement newParentElement = TreeVersionElement.get(newParentElementUri)

            if (currentElement && newParentElement && instanceUri) {
                String userName = treeService.authorizeTreeOperation(newParentElement.treeVersion.tree)
                results.payload = treeService.replaceTaxon(currentElement, newParentElement, instanceUri, excluded, profile, userName)
            } else {
                results.ok = false
                results.fail("Elements with ids $instanceUri, $currentElementUri, $newParentElementUri not found", NOT_FOUND)
            }
        }
    }

    def changeParentElement() {
        withJsonData(request.JSON, false, ['currentElementUri', 'newParentElementUri']) { ResultObject results, Map data ->
            String currentElementUri = data.currentElementUri
            String newParentElementUri = data.newParentElementUri

            TreeVersionElement currentElement = TreeVersionElement.get(currentElementUri)
            TreeVersionElement newParentElement = TreeVersionElement.get(newParentElementUri)

            if (currentElement && newParentElement) {
                String userName = treeService.authorizeTreeOperation(newParentElement.treeVersion.tree)
                results.payload = treeService.changeParentTaxon(currentElement, newParentElement, userName)
            } else {
                results.ok = false
                results.fail("Elements with ids $currentElementUri, $newParentElementUri not found", NOT_FOUND)
            }
        }
    }

    def removeElement() {
        withJsonData(request.JSON, false, ['taxonUri']) { ResultObject results, Map data ->
            String taxonUri = data.taxonUri
            TreeVersionElement treeVersionElement = TreeVersionElement.get(taxonUri)

            if (treeVersionElement) {
                treeService.authorizeTreeOperation(treeVersionElement.treeVersion.tree)
                Map result = treeService.removeTreeVersionElement(treeVersionElement)
                results.payload = [count: result.count, message: result.message]
            } else {
                results.ok = false
                results.fail("taxon with id $taxonUri not found", NOT_FOUND)
            }
        }
    }

    def editElementProfile() {
        withJsonData(request.JSON, false, ['taxonUri', 'profile']) { ResultObject results, Map data ->
            TreeVersionElement treeVersionElement = TreeVersionElement.get(data.taxonUri as String)
            if (!treeVersionElement) {
                throw new ObjectNotFoundException("Can't find taxon with URI $data.taxonUri")
            }
            String userName = treeService.authorizeTreeOperation(treeVersionElement.treeVersion.tree)
            Map profile = (data.profile.size() > 0 ? data.profile as Map : null)
            results.payload = treeService.editProfile(treeVersionElement, profile, userName)
            log.debug(results.payload.toString())
        }
    }

    /**
     * This is for errata edits only. It does an in place edit on the tree element directly, so it changes history.
     *
     * It needs post parameters
     * * taxonUri - the tree version element elementLink
     * * comment - the comment text
     * * reason - the reason for the change
     *
     * if comment text is blank the comment is removed.
     *
     * @return
     */
    def editComment(String taxonUri, String comment, String reason) {
        TreeVersionElement treeVersionElement = TreeVersionElement.get(taxonUri)
        if (!treeVersionElement) {
            throw new ObjectNotFoundException("Can't find taxon with URI $taxonUri")
        }
        String userName = treeService.authorizeTreeOperation(treeVersionElement.treeVersion.tree)
        treeService.minorEditComment(treeVersionElement, comment, reason, userName)
        redirect(url: "${treeVersionElement.treeElement.nameLink}/api/apni-format")
    }

    /**
     * This is for errata edits only. It does an in place edit on the tree element directly, so it changes history.
     *
     * It needs post parameters
     * * taxonUri - the tree version element elementLink
     * * distribution - the distribution text
     * * reason - the reason for the change
     *
     * if distribution text is blank it is removed.
     *
     * @return
     */
    def editDistribution(String taxonUri, String distribution, String reason) {
        TreeVersionElement treeVersionElement = TreeVersionElement.get(taxonUri)
        try {
            if (!treeVersionElement) {
                throw new ObjectNotFoundException("Can't find taxon with URI $taxonUri")
            }
            String userName = treeService.authorizeTreeOperation(treeVersionElement.treeVersion.tree)
            treeService.minorEditDistribution(treeVersionElement, distribution, reason, userName)
            redirect(url: "${treeVersionElement.treeElement.nameLink}/api/apni-format")
        } catch (AuthorizationException authException) {
            log.error(authException.message)
            flash.message = "You need to be logged in to edit."
            redirect(url: "${treeVersionElement.treeElement.nameLink}/api/apni-format")
        } catch (e) {
            log.error(e.message)
            flash.message = e.message
            if (treeVersionElement) {
                redirect(url: "${treeVersionElement.treeElement.nameLink}/api/apni-format")
            } else {
                redirect(controller: 'search', action: 'search')
            }
        }
    }

    def editElementStatus() {
        withJsonData(request.JSON, false, ['taxonUri', 'excluded']) { ResultObject results, Map data ->
            TreeVersionElement treeVersionElement = TreeVersionElement.get(data.taxonUri as String)
            if (!treeVersionElement) {
                throw new ObjectNotFoundException("Can't find taxon with URI $data.taxonUri")
            }
            String userName = treeService.authorizeTreeOperation(treeVersionElement.treeVersion.tree)
            results.payload = treeService.editExcluded(treeVersionElement, data.excluded as Boolean, userName)
        }
    }

    def elementDataFromInstance(String instanceUri) {
        ResultObject results = require('Instance URI': instanceUri)

        handleResults(results) {
            TaxonData taxonData = treeService.getInstanceDataByUri(instanceUri)
            if (taxonData) {
                results.payload = taxonData.asMap()
            } else {
                throw new ObjectNotFoundException("Instance with URI $instanceUri, is not in this shard.")
            }
        }
    }

    def updateSynonymyByInstance() {
        List<Long> instanceIds = paramIdList(params.instances)

        log.debug "updateSynonymyByInstance: Instance IDs: ${instanceIds.toString()}"
        ResultObject results = require('instances': instanceIds)

        handleResults(results) {
            TreeVersion treeVersion = got({
                TreeVersion.get(params.versionId as Long)
            }, "No draft version with id ${params.versionId} found.") as TreeVersion
            String userName = treeService.authorizeTreeOperation(treeVersion.tree)

            List<TreeVersionElement> payload = []
            for (Long instanceId in instanceIds) {
                TreeVersionElement tve = got({
                    treeService.findElementForInstanceId(instanceId as Long, treeVersion)
                }, "Tree version element for instance (${instanceId}) not found.") as TreeVersionElement
                payload.add(treeService.updateElementFromInstanceData(tve, userName))
            }
            results.payload = payload
        }
    }

    def updateSynonymyByEvent() {
        List<Long> eventIds = paramIdList(params.events)
        log.debug eventIds.toString()
        ResultObject results = requireTarget(eventIds, "No events supplied, updating nothing.")

        handleResults(results) {
            List<TreeVersionElement> payload = []
            for (Long eventId in eventIds) {
                EventRecord event = EventRecord.get(eventId)
                if (event && !event.dealtWith && event.type == EventRecordTypes.SYNONYMY_UPDATED) {
                    Tree tree = got({ Tree.get(event.data.treeId as Long) }, "Tree ${event.data.treeId}.") as Tree
                    String userName = treeService.authorizeTreeOperation(tree)
                    TreeVersion treeVersion = got({
                        tree.defaultDraftTreeVersion
                    }, "No default draft version for tree ${tree.name} found. Create a default draft first.") as TreeVersion
                    TreeVersionElement tve = got({
                        treeService.findElementForInstanceId(event.data.instanceId as Long, treeVersion)
                    }, "Tree version element for instance (${event.data.instanceId}) not found.") as TreeVersionElement
                    payload.add(treeService.updateElementFromInstanceData(tve, userName))
                    event.dealtWith = true
                }
            }
        }
    }

    private static List<Long> paramIdList(val) {
        if (!val) {
            return null
        }
        if (val instanceof Object[]) {
            val.collect { it as Long } as List
        } else {
            [val as Long]
        }
    }

}
