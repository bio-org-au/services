package services

import au.org.biodiversity.nsl.Name

class RecheckSynonymyJob {

    def treeService

    static triggers = {
        //update at 5:53 PM every day. 1753 is start year of all taxonomy
        cron name: 'updateSynonymCache', startDelay: 10000, cronExpression: '0 53 17 * * ?'
    }

    def execute() {
        Name.withTransaction {
            // Update synonymy cache on instance object
            treeService.refreshSynonymHtmlCache()
        }
    }
}
