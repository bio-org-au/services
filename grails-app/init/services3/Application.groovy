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

        String configFileName = System.getenv('nsl_services_config') ?: "/etc/nsl/services-config-g3.groovy"
        File configBase = new File(configFileName)

        if(configBase.exists()) {
            println "Loading external configuration from Groovy: ${configBase.absolutePath}"
            def config = new ConfigSlurper().parse(configBase.toURI().toURL())
            environment.propertySources.addFirst(new MapPropertySource("externalGroovyConfig", config))
        } else {
            println "External config could not be found, checked ${configBase.absolutePath}"
        }
    }
}
