package au.org.biodiversity.nsl

import grails.test.spock.IntegrationSpec
import grails.transaction.Rollback

import javax.sql.DataSource

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@Rollback
class TreeReportServiceSpec extends IntegrationSpec {

    DataSource dataSource_nsl
    def treeReportService


    def setup() {

    }

    def cleanup() {
    }


}
