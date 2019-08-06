package au.org.biodiversity.nsl

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

import java.sql.Timestamp

@Rollback
@Integration
class AuditServiceIntSpec extends Specification {

    AuditService auditService

    void setupSpec() {
    }

    def setup() {
    }

    def cleanup() {
    }

    void "test list user transactions"() {
        when: "I call list"
        GregorianCalendar fromCal = new GregorianCalendar(2017, 0, 1)
        Timestamp from = new Timestamp(fromCal.time.time)
        fromCal.add(Calendar.MONTH, 1)
        Timestamp to = new Timestamp(fromCal.time.time)
        List rows = auditService.list('%', from, to)

        rows.each{ Audit r ->
            println r
        }

        then: "we get results"
        rows.size() > 0
    }

}
