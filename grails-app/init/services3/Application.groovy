package services3

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.core.env.MapPropertySource

class Application extends GrailsAutoConfiguration implements EnvironmentAware {

    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    void setEnvironment(Environment environment) {
        def configBase = new File("${System.getProperty('user.home')}/.nsl/services-config-g3.groovy")

        if(configBase.exists()) {
            println "Loading external configuration from Groovy: ${configBase.absolutePath}"
            def config = new ConfigSlurper().parse(configBase.toURI().toURL())
            environment.propertySources.addFirst(new MapPropertySource("externalGroovyConfig", config))
        } else {
            println "External config could not be found, checked ${configBase.absolutePath}"
        }
    }
}