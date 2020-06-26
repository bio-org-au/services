package au.org.biodiversity.nsl

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

import javax.sql.DataSource

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@Rollback
@Integration
class TreeReportServiceSpec extends Specification  {

    DataSource dataSource
    TreeReportService treeReportService


    def setup() {
    }

    def cleanup() {
    }

    void "Test getting a diff v1=51357046&v2=51357890"() {
        when:
        TreeChangeSet changeSet = treeReportService.diffReport(TreeVersion.get(51357046), TreeVersion.get(51357890))
        println changeSet.modified
        
        then:
        changeSet.added.empty
        changeSet.removed.empty
        changeSet.changed
        !changeSet.modified.empty

    }

}
