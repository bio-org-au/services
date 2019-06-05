package services

import au.org.biodiversity.nsl.Name

class RecheckSynonymyJob {

    def treeService

    static triggers = {
        //update at 6:30AM every day
        cron name: 'updateSynonymCache', startDelay: 10000, cronExpression: '0 52 13 * * ?'
    }

    def execute() {
        Name.withTransaction {
            println "refresh synonymy cache"
            treeService.refreshSynonymHtmlCache()
        }
    }
}
