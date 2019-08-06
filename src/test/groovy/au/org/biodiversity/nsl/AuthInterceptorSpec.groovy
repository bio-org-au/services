package au.org.biodiversity.nsl

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class AuthInterceptorSpec extends Specification implements InterceptorUnitTest<ApiInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test api interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"auth")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
