package services3

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import grails.util.Holders
import org.springframework.boot.CommandLineRunner
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.core.env.MapPropertySource

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Application extends GrailsAutoConfiguration implements EnvironmentAware {

    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    void setEnvironment(Environment environment) {

        String configFileName = System.getenv('nsl_services_config') ?: "/etc/nsl/services-config-g5.groovy"
        File configBase = new File(configFileName)

        if (configBase.exists()) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
            println "Loading external configuration from Groovy: ${configBase.absolutePath} ${LocalDateTime.now().format(dtf)}"
            def config = new ConfigSlurper().parse(configBase.toURI().toURL()).flatten()
            println "load database url: ${config.getProperty('dataSource.url')} ${LocalDateTime.now().format(dtf)}"
            environment.propertySources.addFirst(new MapPropertySource("externalGroovyConfig", config))
        } else {
            println "External config could not be found, checked ${configBase.absolutePath}"
        }
    }
}
