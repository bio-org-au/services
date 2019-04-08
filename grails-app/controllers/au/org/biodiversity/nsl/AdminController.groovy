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

import au.org.biodiversity.nsl.api.ResultObject
import grails.transaction.Transactional
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authz.annotation.RequiresRoles

import static org.springframework.http.HttpStatus.OK

@Transactional
class AdminController {

    def nameService
    def apcTreeService
    def referenceService
    def instanceService
    def authorService
    def configService
    def adminService
    def flatViewService
    def postgresInfoService
    def jsonRendererService

    @RequiresRoles('admin')
    index() {
        Map stats = [:]
        stats.namesNeedingConstruction = nameService.countIncompleteNameStrings()
        stats.deletedNames = Name.executeQuery("select n from Name n where n.nameStatus.name = '[deleted]'")
        Boolean servicing = adminService.serviceMode()
        String dbInfo = postgresInfoService.connectionInfo.toString()
        String appConfig = configService.printAppConfig()
        [pollingNames: nameService.pollingStatus(), stats: stats, servicing: servicing, dbInfo: dbInfo, appConfig: appConfig]
    }

    @RequiresRoles('admin')
    checkNames() {
        log.info "check all names"
        File tempFile = nameService.checkAllNames()
        render(file: tempFile, fileName: 'name-changes.csv', contentType: 'text/plain')
    }

    @RequiresRoles('admin')
    reconstructNames() {
        nameService.reconstructAllNames()
        flash.message = "reconstructing all names where changed."
        redirect(action: 'index')
    }

    @RequiresRoles('admin')
    reconstructSortNames() {
        nameService.reconstructSortNames()
        flash.message = "reconstructing all sort names where changed."
        redirect(action: 'index')
    }

    @RequiresRoles('admin')
    reconstructCitations() {
        referenceService.reconstructAllCitations()
        flash.message = "reconstructing all reference citations."
        redirect(action: 'index')
    }


    @RequiresRoles('admin')
    constructMissingNames() {
        nameService.constructMissingNames()
        flash.message = "constructing missing names."
        redirect(action: 'index')
    }

    @RequiresRoles('admin')
    replaceInstanceNoteXics() {
        instanceService.replaceXICSinInstanceNotes()
        flash.message = "replacing XICs in instance notes."
        redirect(action: 'index')
    }

    @RequiresRoles('admin')
    replaceReferenceTitleXics() {
        referenceService.replaceXICSinReferenceTitles()
        flash.message = "replacing XICs in reference titles."
        redirect(action: 'index')
    }

    @RequiresRoles('admin')
    autoDedupeAuthors() {
        authorService.autoDeduplicate(SecurityUtils.subject.principal.toString())
        redirect(action: 'index')
    }

    def logs() {
        try {
            SecurityUtils.subject.checkRole('admin')
            List<String> processLog = logSummary(300)
            render(template: 'log', model: [processLog: processLog])
        } catch (ignored) {
            render(template: 'log', model: [processLog: ["You are not logged in."]])
        }
    }

    @RequiresRoles('admin')
    startUpdater() {
        log.debug "starting updater"
        nameService.startUpdatePolling()
        redirect(action: 'index')
    }

    @RequiresRoles('admin')
    pauseUpdates() {
        log.debug "pausing updater"
        nameService.pauseUpdates()
        redirect(action: 'index')
    }

    @RequiresRoles('admin')
    resumeUpdates() {
        log.debug "un-pausing updater"
        nameService.resumeUpdates()
        redirect(action: 'index')
    }

    @RequiresRoles('admin')
    notifyMissingApniNames() {
        log.debug "adding notifications for names not in APNI"
        nameService.addNamesNotInNameTree(configService.nameTreeName)
        redirect(action: 'index')
    }

    @RequiresRoles('admin')
    transferApcProfileData() {
        log.debug "applying instance APC comments and distribution text to the APC tree"
        flash.message = apcTreeService.transferApcProfileData()
        redirect(action: 'index')
    }

    @RequiresRoles('admin')
    deduplicateMarkedReferences() {
        String user = SecurityUtils.subject.principal.toString()
        ResultObject results = new ResultObject(referenceService.deduplicateMarked(user))
        //noinspection GroovyAssignabilityCheck
        respond(results, [status: OK, view: '/common/serviceResult', model: [data: results,]])
    }

    @RequiresRoles('admin')
    deduplicateMarkedNames() {
        String user = SecurityUtils.subject.principal.toString()
        ResultObject results = new ResultObject(nameService.deduplicateMarked(user))
        //noinspection GroovyAssignabilityCheck
        respond(results, [status: OK, view: '/common/serviceResult', model: [data: results,]])
    }

    @RequiresRoles('admin')
    setAdminModeOn() {
        adminService.enableServiceMode(true)
        redirect(action: 'index')
    }

    @RequiresRoles('admin')
    setAdminModeOff() {
        adminService.enableServiceMode(false)
        redirect(action: 'index')
    }

    @RequiresRoles('admin')
    refreshViews() {
        log.debug "Refreshing views."
        String namespaceName = configService.nameSpace.name.toLowerCase()
        flatViewService.refreshTaxonView()
        flatViewService.refreshNameView()
        log.debug "Refreshed views."
        redirect(action: 'index')
    }

    private static List<String> logSummary(Integer lineLength) {
        String logFileName = (System.getProperty('catalina.base') ?: 'target') + "/logs/nsl-services.log"
        List<String> logLines = new File(logFileName).readLines().reverse().take(50)
        List<String> processedLog = []
        logLines.each { String line ->
            line = line.replaceAll(/grails.app.services.au.org.biodiversity.nsl./, '')
            if (line.size() > lineLength) {
                line = line[0..lineLength] + '...'
            }
            processedLog.add(line)
        }
        return processedLog
    }


}
