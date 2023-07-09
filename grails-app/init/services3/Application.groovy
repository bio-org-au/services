package services3

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.core.env.Environment
import org.springframework.context.EnvironmentAware
import java.nio.file.FileSystems

class Application extends GrailsAutoConfiguration implements EnvironmentAware {

    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    void setEnvironment(Environment environment) {
        def fs = FileSystems.getDefault()
        AppConfig appConfig = new AppConfig(environment.getProperty('server.name'))
        appConfig.configDirs = [fs.getPath(System.getProperty('user.dir')), fs.getPath(System.getProperty('user.home'), '.nsl')]
        appConfig.loadConfigs(environment)
    }
}
