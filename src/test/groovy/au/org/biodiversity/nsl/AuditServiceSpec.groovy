package au.org.biodiversity.nsl

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

import java.sql.Timestamp

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(AuditService)
@Mock([Reference, Author, Name, Instance, Comment, InstanceNote])
class AuditServiceSpec extends Specification {

    def setup() {
//        service.configService = new ConfigService()
//        service.configService.grailsApplication = [config: makeAConfig()]
    }

    def cleanup() {
    }
//todo make this work in the CI or change the way we test it
//    void "test list user transactions"() {
//        when: "I call list"
//        GregorianCalendar fromCal = new GregorianCalendar(2017, 0, 1)
//        Timestamp from = new Timestamp(fromCal.time.time)
//        fromCal.add(Calendar.MONTH, 1)
//        Timestamp to = new Timestamp(fromCal.time.time)
//        List rows = service.list('%', from, to)
//
//        rows.each{ Audit r ->
//            println r
//        }
//
//        then: "we get results"
//        rows.size() > 0
//    }
//
//    private static ConfigObject makeAConfig() {
//        ConfigSlurper slurper = new ConfigSlurper('test')
//        String configString = '''
//dataSource_nsl {
//    username = "nsl"
//    password = "nsl"
//    url = "jdbc:postgresql://localhost:5432/nsl"
//    driverClassName = "org.postgresql.Driver"
//}
//'''
//        return slurper.parse(configString)
//    }
}
