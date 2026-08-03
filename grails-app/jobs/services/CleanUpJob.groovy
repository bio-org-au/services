package services

import au.org.biodiversity.nsl.Name

class CleanUpJob {

    def referenceService
    def authorService
    def nameService
    def instanceService
    def treeService

    static concurrent = false
    static sessionRequired = true

    static triggers = {
        //6 AM every day
        cron name: 'cleanUp', startDelay: 20000, cronExpression: '0 0 6 * * ?'
    }

    def execute() {
        println "Running cleanup."
        referenceService.deduplicateMarked('reference.dedup')
        instanceService.updateMissingUris()
        nameService.updateMissingUris()
        authorService.autoDeduplicate('author.dedup')
        treeService.refreshDisplayHtml()
    }
}
