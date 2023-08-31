package services3

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.core.env.Environment
import org.springframework.context.EnvironmentAware
import java.nio.file.FileSystems

class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}
