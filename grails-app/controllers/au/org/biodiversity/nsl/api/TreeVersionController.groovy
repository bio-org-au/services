package au.org.biodiversity.nsl.api

import au.org.biodiversity.nsl.MergeReport
import au.org.biodiversity.nsl.ObjectNotFoundException
import au.org.biodiversity.nsl.TreeVersion
import au.org.biodiversity.nsl.TreeVersionElement

class TreeVersionController extends BaseApiController {

    def treeService
    def treeReportService
    def jsonRendererService
    def linkService

    static responseFormats = [
            delete                    : ['json', 'html'],
            setDefaultDraftTreeVersion: ['json', 'html'],
            edit                      : ['json', 'html'],
            validate                  : ['html', 'json'],
            publish                   : ['json', 'html'],
            diff                      : ['html', 'json'],
    ]

    static allowedMethods = [
            createTreeVersion         : ['PUT', 'POST'],
            delete                    : ['DELETE', 'POST'],
            setDefaultDraftTreeVersion: ['PUT', 'POST'],
            editTreeVersion           : ['POST'],
            validate                  : ['GET'],
            publish                   : ['PUT', 'POST'],
            diff                      : ['GET']
    ]

    def delete(Long id) {
        TreeVersion treeVersion = TreeVersion.get(id)
        ResultObject results = requireTarget(treeVersion, "No Tree version with id: $id found")
        handleResults(results) {
            treeService.authorizeTreeOperation(treeVersion.tree)
            results.payload = treeService.deleteTreeVersion(treeVersion)
        }
    }

    def setDefaultDraftTreeVersion(Long version) {
        TreeVersion treeVersion = TreeVersion.get(version)
        ResultObject results = requireTarget(treeVersion, "No Tree version with id $version found")
        handleResults(results) {
            treeService.authorizeTreeOperation(treeVersion.tree)
            results.payload = treeService.setDefaultDraftVersion(treeVersion)
        }
    }

    def publish() {
        Map data = request.JSON as Map
        Long versionId = (data.versionId ?: null) as Long
        String logEntry = data.logEntry

        TreeVersion treeVersion = TreeVersion.get(versionId)
        ResultObject results = requireTarget(treeVersion, "No Tree version with id: $versionId found")
        handleResults(results) {
            String userName = treeService.authorizeTreeOperation(treeVersion.tree)
            Boolean createNewDraft = treeVersion == treeVersion.tree.defaultDraftTreeVersion
            results.payload = treeService.publishTreeVersion(treeVersion, userName, logEntry)
            if (createNewDraft) {
                results.autocreate = true
                String draftName = data.nextDraftName ?: treeVersion.draftName
                treeService.bgCreateDefaultDraftVersion(treeVersion.tree, treeVersion, draftName, userName, logEntry ?: "default draft")
            }
        }
    }

    def edit() {
        Map data = request.JSON as Map
        Long versionId = (data.versionId ?: null) as Long
        String draftName = data.draftName
        Boolean defaultVersion = data.defaultDraft

        TreeVersion treeVersion = TreeVersion.get(versionId)
        ResultObject results = requireTarget(treeVersion, "No Tree version with id: $versionId found")
        handleResults(results) {
            treeService.authorizeTreeOperation(treeVersion.tree)
            results.payload = treeService.editTreeVersion(treeVersion, draftName)
            if (defaultVersion) {
                treeService.setDefaultDraftVersion(treeVersion)
            }
        }
    }

    def validate(Long version, Boolean embed) {
        log.debug "validate tree version $version"
        TreeVersion treeVersion = TreeVersion.get(version)
        ResultObject results = requireTarget(treeVersion, "No Tree version with id: $version found")
        handleResults(results, { viewRespond('validate', results, embed) }) {
            results.treeVersion = treeVersion
            results.payload = treeReportService.validateTreeVersion(treeVersion)
        }
    }

    def diff(Long v1, Long v2, Boolean embed, Boolean list) {
        ResultObject results = require('Version 1 ID': v1, 'Version 2 ID': v2)
        String view = list ? 'diffList' : 'diff'
        handleResults(results, { viewRespond(view, results, embed) }) {
            TreeVersion first = TreeVersion.get(v1)
            if (!first) {
                throw new ObjectNotFoundException("Version $v1, not found.")
            }
            TreeVersion second = TreeVersion.get(v2)
            if (!second) {
                throw new ObjectNotFoundException("Version $v2, not found.")
            }
            results.payload = treeReportService.diffReport(first, second).toMap()
        }
    }

    def diffElement(String e1, String e2, Boolean embed) {
        TreeVersionElement tve1 = TreeVersionElement.get(e1)
        TreeVersionElement tve2 = TreeVersionElement.get(e2)
        TreeVersion v1 = tve2.treeVersion
        TreeVersion v2 = tve1.treeVersion

        [
                v1   : v1,
                v2   : v2,
                mod  : [tve1, tve2],
                embed: embed
        ]
    }

    def mergeReport(Long draftId, Boolean embed) {
        ResultObject results = require('Draft Version ID': draftId)

        handleResults(results, { viewRespond('mergeReport', results, embed) }) {
            TreeVersion draft = TreeVersion.get(draftId)
            if (!draft) {
                throw new ObjectNotFoundException("Version $draftId, not found.")
            }
            def p = treeReportService.mergeReport(draft)
            results.payload = p
        }
    }

    def merge() {
        println params
        ResultObject results = require('Draft Version ID': params.draftVersion, 'Changeset': params.changeset)
        handleResults(results, { viewRespond('merge', results, false) }) {
            MergeReport report = treeReportService.rehydrateReport(params.changeset)
            params.keySet()
                  .findAll { key -> (key as String).startsWith('diff-') }
                  .each { key ->
                      Integer diffId = (key - 'diff-') as Integer
                      report.setUse(diffId, params[key] as String)
                  }
            TreeVersion draft = TreeVersion.get(params.draftVersion as Long)
            if (!draft) {
                throw new ObjectNotFoundException("Version ${params.draftVersion}, not found.")
            }
            String userName = treeService.authorizeTreeOperation(draft.tree)
            results.payload = treeService.merge(draft, report, userName)
        }
    }


    private viewRespond(String view, ResultObject resultObject, Boolean embed) {
        log.debug "viewRespond: result status is ${resultObject.status}"
        resultObject.put('embed', embed)
        respond(resultObject, view: view, model: resultObject)
    }

}
