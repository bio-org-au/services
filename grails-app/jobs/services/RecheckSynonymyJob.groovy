package services

import au.org.biodiversity.nsl.Name

class RecheckSynonymyJob {

    def treeService

    static triggers = {
        //update at 6:30AM every day
        cron name: 'updateSynonymCache', startDelay: 10000, cronExpression: '0 53 17 * * ?'
    }

    def execute() {
        Name.withTransaction {
            println "execute: refresh synonymy cache - started"
            treeService.refreshSynonymHtmlCache()
            println "execute: refresh synonymy cache - complete"
        }
    }
}
