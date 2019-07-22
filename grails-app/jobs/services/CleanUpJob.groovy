package services

import au.org.biodiversity.nsl.Name

class CleanUpJob {

    def referenceService
    def authorService
    def nameService
    def instanceService
    def treeService

    def concurrent = false
    def sessionRequired = true

    static triggers = {
        //6 AM every day
        cron name: 'cleanUp', startDelay: 20000, cronExpression: '0 0 6 * * ?'
    }

    def execute() {
        Name.withTransaction {
            println "Running cleanup."
            authorService.autoDeduplicate('cleanUpJob')
            referenceService.deduplicateMarked('cleanUpJob')
            instanceService.updateMissingUris()
            nameService.updateMissingUris()
            treeService.refreshDisplayHtml()
        }
    }
}