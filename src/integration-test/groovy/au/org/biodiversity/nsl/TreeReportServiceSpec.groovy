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
    def treeReportService


    def setup() {
    }

    def cleanup() {
    }


}
