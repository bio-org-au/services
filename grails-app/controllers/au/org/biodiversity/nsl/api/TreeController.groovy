package au.org.biodiversity.nsl.api


import au.org.biodiversity.nsl.Instance
import au.org.biodiversity.nsl.Tree
import au.org.biodiversity.nsl.TreeVersion

import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED

class TreeController extends BaseApiController {

    def treeService
    def treeReportService
    def jsonRendererService
    def linkService

    static responseFormats = [
            createTree   : ['json', 'html'],
            editTree     : ['json', 'html'],
            copyTree     : ['json', 'html'],
            deleteTree   : ['json', 'html'],
            createVersion: ['json', 'html']
    ]

    static allowedMethods = [
            createTree   : ['PUT'],
            editTree     : ['POST'],
            copyTree     : ['PUT'],
            deleteTree   : ['DELETE', 'GET'],
            createVersion: ['PUT']
    ]

    def index() {
        [trees: Tree.list()]
    }

    def createTree() {
        withJsonData(request.JSON, false, ['treeName', 'descriptionHtml']) { ResultObject results, Map data ->
            String treeName = data.treeName
            String groupName = data.groupName
            Long referenceId = data.referenceId
            String descriptionHtml = data.descriptionHtml
            String linkToHomePage = data.linkToHomePage
            Boolean acceptedTree = data.acceptedTree

            String userName = treeService.authorizeTreeBuilder()
            results.payload = treeService.createNewTree(treeName, groupName ?: userName, referenceId, descriptionHtml, linkToHomePage, acceptedTree)
        }
    }

    def editTree() {
        withTree { ResultObject results, Tree tree, Map data ->
            treeService.authorizeTreeOperation(tree)
            results.payload = treeService.editTree(tree,
                    (String) data.treeName,
                    (String) data.groupName,
                    (Long) data.referenceId,
                    (String) data.descriptionHtml,
                    (String) data.linkToHomePage,
                    (Boolean) data.acceptedTree
            )
        }
    }

    def deleteTree(Long treeId) {
        Tree tree = Tree.get(treeId)
        ResultObject results = requireTarget(tree, "No Tree with id: $treeId found")
        handleResults(results) {
            treeService.authorizeTreeOperation(tree)
            treeService.deleteTree(tree)
        }
    }

    def copyTree() { respond(['Not implemented'], status: NOT_IMPLEMENTED) }

    /**
     * Creates a new draft tree version. If there is a currently published version it copies that versions elements to
     * this new version. If defaultVersion is set to true then the new version becomes the default draft version.
     * @return
     */
    def createVersion() {
        withJsonData(request.JSON, false, ['treeId', 'draftName']) { ResultObject results, Map data ->
            log.debug "createVersion: starting"
            Long treeId = data.treeId
            Long fromVersionId = data.fromVersionId
            String draftName = data.draftName
            String logEntry = data.log
            Boolean defaultVersion = data.defaultDraft

            Tree tree = Tree.get(treeId)
            log.debug "createVersion: Getting tree: $tree"
            if (tree) {
                String userName = treeService.authorizeTreeOperation(tree)
                TreeVersion fromVersion = TreeVersion.get(fromVersionId)
                log.debug "createVersion: Getting version: $fromVersion"
                if (defaultVersion) {
                    log.debug "createVersion: creating default draft version"
                    results.payload = treeService.createDefaultDraftVersion(tree, fromVersion, draftName, userName, logEntry)
                } else {
                    log.debug "createVersion: creating a non-default draft version"
                    results.payload = treeService.createTreeVersion(tree, fromVersion, draftName, userName, logEntry)
                }
            } else {
                results.ok = false
                results.fail("Tree with id $treeId not found", NOT_FOUND)
            }
        }
    }

    def checkCurrentSynonymy(Long treeVersionId, Boolean embed) {
        TreeVersion treeVersion = TreeVersion.get(treeVersionId)
        log.debug "checkCurrentSynonymy: for treeVersion $treeVersion"
        ResultObject results = requireTarget(treeVersion, "No Tree version with id: $treeVersionId found")
        handleResults(results, { checkSynRespond(results, treeVersion, embed) }) {
            results.payload = treeReportService.checkCurrentSynonymy(treeVersion, 100)
        }
    }

    def synonymyOrderingInfo(Instance instance) {
        ResultObject results = requireTarget(instance, "No instance supplied.")
        log.debug "synonymyOrderingInfo: Getting Synonomy ordering info"
        handleResults(results, { synOrderRespond(results) }) {
            results.payload = treeReportService.getSynonymOrderingInfo(instance)
        }
    }

    private synOrderRespond(ResultObject resultObject) {
        log.debug "synOrderRespond: Rendering synOrderInfo view with $resultObject"
        render(view: 'synOrderInfo', model: resultObject.payload)
    }

    private checkSynRespond(ResultObject resultObject, TreeVersion treeVersion, Boolean embed) {
        log.debug "checkSynRespond: result status is ${resultObject?.status} - action: ${resultObject?.action} count: ${resultObject?.payload?.count}"
        if (embed) {
            //noinspection GroovyAssignabilityCheck
            log.debug "checkSynRespond: Rendering checkSynContent for treeVersion: $treeVersion"
            render(template: 'checkSynContent', model: [treeVersion: treeVersion, data: resultObject], status: resultObject.remove('status'))
        } else {
            //noinspection GroovyAssignabilityCheck
            log.debug "checkSynRespond: Rendering checkSynReport for treeVersion: $treeVersion"
            render(view: 'checkSynReport', model: [treeVersion: treeVersion, data: resultObject], status: resultObject.remove('status'))
        }
    }

    private withTree(Closure work) {
        Map data = request.JSON as Map
        Tree tree = Tree.get(data.id as Long)
        ResultObject results = requireTarget(tree, "No Tree with id: $data.id found")
        handleResults(results) {
            work(results, tree, data)
        }
    }

}
