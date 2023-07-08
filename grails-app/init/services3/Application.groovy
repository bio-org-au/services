package services3

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.grails.config.yaml.YamlPropertySourceLoader
import org.springframework.core.env.Environment
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource

import java.nio.file.FileSystems
import java.nio.file.Path

class Application extends GrailsAutoConfiguration implements EnvironmentAware {

    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    void setEnvironment(Environment environment) {
        def fs = FileSystems.getDefault()
        AppConfig appConfig = new AppConfig('services-g5')
        appConfig.configDirs = [fs.getPath(System.getProperty('user.dir')), fs.getPath(System.getProperty('user.home'), '.nsl')]
        appConfig.loadConfigs(environment)
    }

    /**
     * loadConfigs will load the config file name.. either .groovy, .yml or .properties from each of
     * the directories given, unless an environment variable override is set for the location
     * The environment variable override is the same as 'name'
     * @param environment - value received from setEnvironment
     * @param name - name of config file
     * @param configDirs - directories to look for config file
     */
    void loadConfigs(Environment environment, String name, List<Path> configDirs) {
        def fs = FileSystems.getDefault()
//        environment.propertySources.addFirst(grails.util.Metadata.current)
//        grails.util.Metadata.current
//        String info = this.getClass().getResource("/META-INF/grails.build.info").toURI().toURL().getFile()
//        loadConfig(environment, fs.getPath(info))
        def configFiles = ['groovy', 'yml', 'properties'].collect { name.concat(".$it") }
        String env = System.getenv(name.replaceAll('-', '_'))
        if (env) {
            loadConfig(environment, fs.getPath(env))
        } else {
            configDirs.each { dir ->
                def configPaths = configFiles.collect { dir.resolve(it) }
                System.out.println("Looking for external config in ${configPaths}")
                for (configPath in configPaths) {
                    loadConfig(environment, configPath)
                }
            }
        }
    }

    /**
     * loadConfig - load a single config file
     * @param environment - value received from setEnvironment
     * @param configPath - path of the config file
     */
    void loadConfig(Environment environment, Path configPath) {
        Resource resource = new FileSystemResource(configPath)
        if (resource.exists()) {
            try {
                PropertySource propertySource
                String nm = configPath.getFileName().toString()
                if (nm.endsWith('.groovy')) {
                    // without flatten() it seems to work in the IDE, but sometimes not as a runnable jar
                    // In any case, it seems correct https://blog.mrhaki.com/2016/07/spring-sweets-using-groovy.html
                    ConfigObject config = new ConfigSlurper().parse(configPath.toUri().toURL()).flatten()
                    propertySource = new MapPropertySource(resource.filename, config)
                } else if (nm.endsWith('.yml')) {
                    List<PropertySource<?>> sources = new YamlPropertySourceLoader().load(resource.filename, resource)
                    if (sources.size() > 0) {
                        propertySource = sources.first() as PropertySource
                    }
                } else if (nm.endsWith('.properties') || nm.endsWith('.info')) {
                    def props = new Properties()
                    props.load(resource.inputStream)
                    propertySource = new MapPropertySource(resource.filename, props as Map)
                }
                if (propertySource?.getSource() && !propertySource.getSource().isEmpty()) {
                    println "Loading external configuration: ${configPath}"
                    environment.propertySources.addFirst(propertySource)
                } else {
                    println "Unknown external configuration type: ${nm}"
                }
            } catch (Throwable x) {
                println "Error loading ${configPath}: $x.localizedMessage"
                x.printStackTrace()
            }
        }
    }
}


//@Override
//    void setEnvironment(Environment environment) {
//        String homeConfig = System.getProperty("user.home") + '/.nsl/services-g5-config.groovy'
//        String configFileName = System.getenv('nsl_services_config') ?: homeConfig
//        File configBase = new File(configFileName)
//
//        if (configBase.exists()) {
//            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
//            println "Loading external configuration from Groovy: ${configBase.absolutePath} ${LocalDateTime.now().format(dtf)}"
//            def config = new ConfigSlurper().parse(configBase.toURI().toURL()).flatten()
//            println "load database url: ${config.getProperty('dataSource.url')} ${LocalDateTime.now().format(dtf)}"
//            environment.propertySources.addFirst(new MapPropertySource("externalGroovyConfig", config))
//        } else {
//            println "External config could not be found, checked ${configBase.absolutePath}"
//        }
//    }
//}
