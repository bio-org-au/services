package services

import au.org.biodiversity.nsl.Name

class RefreshViewsJob {

    def configService
    def flatViewService
    def photoService

    static triggers = {
        //update at 7AM every day
        cron name: 'updateViews', startDelay: 10000, cronExpression: '0 0 7 * * ?'
    }

    def execute() {
        Name.withTransaction {
            flatViewService.refreshNameView()
            flatViewService.refreshTaxonView()
            photoService.refresh()
        }
    }
}
