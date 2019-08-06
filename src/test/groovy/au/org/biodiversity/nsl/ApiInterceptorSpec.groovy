package au.org.biodiversity.nsl

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class ApiInterceptorSpec extends Specification implements InterceptorUnitTest<ApiInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test api interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"api")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
