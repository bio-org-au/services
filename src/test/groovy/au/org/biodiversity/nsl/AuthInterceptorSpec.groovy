package au.org.biodiversity.nsl

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class AuthInterceptorSpec extends Specification implements InterceptorUnitTest<AuthInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test auth interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"auth")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
