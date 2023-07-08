package services3

import grails.util.Holders
import grails.util.Metadata
import org.grails.config.yaml.YamlPropertySourceLoader
import org.springframework.core.env.Environment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import java.nio.file.FileSystems
import java.nio.file.Path

class AppConfig {
    String appName
    List<Path> configFiles = []
    List<Path> configDirs = []

    AppConfig(String appName) {
        // It seems like info.app.name can't be accessed in Application.groovy
        this.appName = appName
        String envName = (appName + '-config').replaceAll('-', '_')
        println "looking for config in environment variable: $envName"
        String env = System.getenv(envName)
        if (env) {
            def fs = FileSystems.getDefault()
            configFiles.add(fs.getPath(env))
        } else {
            def fs = FileSystems.getDefault()
            configDirs = [fs.getPath(System.getProperty('user.dir')), fs.getPath(System.getProperty('user.home'), '.' + appName)]
        }
    }

    static void printInfo() {
        Metadata metadata = Holders.grailsApplication.metadata
        // info.app.name can be changed by setting rootProject.name in settings.gradle
        String appName = metadata.getProperty('info.app.name', String, 'Unknown')
        println("$appName application version ${metadata.getProperty('info.app.version', String.class, 'Unknown')}")
        println("$appName grails version ${metadata.getProperty('info.app.grailsVersion', String.class, 'Unknown')}")
        println("$appName build timestamp ${metadata.getProperty('info.app.build.date', String.class, 'Unknown')}")
    }

    /**
     * loadConfigs will load the config file name.. either .groovy, .yml or .properties from each of
     * the directories given, unless an environment variable override is set for the location
     * The environment variable override is the same as 'name'
     * @param environment - value received from setEnvironment
     * @param name - name of config file
     * @param configDirs - directories to look for config file
     */
    void loadConfigs(Environment environment) {
        List results = []
        if (this.configFiles.size() > 0) {
            this.configFiles.each { path ->
                println("Looking for external config in $path")
                FileSystemResource fsr = new FileSystemResource(path)
                if (fsr.exists()) {
                    results.add([propertySource: loadConfigBasedOnFileExtension(fsr), resource: fsr, path: path])
                }
            }
        } else {
            results = loadConfigs()
        }
        results.each { result ->
            addPropertySource(environment, result.propertySource, result.resource)
            configFiles.add(result.path)
        }
    }

    Path getConfigFilename(Path dir, String appName, String suffix) {
        return dir.resolve("${appName}-config.${suffix}")
    }

    List loadConfigs() {
        List rtn = []
        def fs = FileSystems.getDefault()
        this.configDirs.each { dir ->
            println("Looking for external config in " + getConfigFilename(dir, appName, '{groovy,yml,properties}'))
            Path path
            FileSystemResource fsr
            path = getConfigFilename(dir, appName, 'groovy')
            fsr = new FileSystemResource(path)
            if (fsr.exists()) {
                rtn.add([propertySource: loadGroovy(fsr), resource: fsr, path: path])
            }
            path = getConfigFilename(dir, appName, 'yml')
            fsr = new FileSystemResource(path)
            if (fsr.exists()) {
                rtn.add([propertySource: loadYaml(fsr), resource: fsr, path: path])
            }
            path = getConfigFilename(dir, appName, 'properties')
            fsr = new FileSystemResource(path)
            if (fsr.exists()) {
                rtn.add([propertySource: loadProperties(fsr), resource: fsr, path: path])
            }
        }
        return rtn
    }

    static PropertySource loadConfigBasedOnFileExtension(Resource resource) {
        if (resource.filename.endsWith('.groovy')) {
            loadGroovy(resource)
        } else if (resource.filename.endsWith('.yml')) {
            loadYaml(resource)
        } else if (resource.filename.endsWith('.properties')) {
            loadProperties(resource)
        }
    }

    static void addPropertySource(Environment environment, PropertySource propertySource, Resource resource) {
        if (propertySource && propertySource?.getSource() && !propertySource.getSource().isEmpty()) {
            println "Loading external configuration: ${resource.description}"
            environment.propertySources.addFirst(propertySource)
        }
    }

    static PropertySource loadGroovy(Resource resource) {
        InputStreamReader isReader = new InputStreamReader(resource.inputStream);
        try {
            BufferedReader reader = new BufferedReader(isReader);
            StringBuffer sb = new StringBuffer();
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str);
                sb.append('\n')
            }
            ConfigObject config = new ConfigSlurper().parse(sb.toString()).flatten()
            return new MapPropertySource(resource.filename, config)
        } finally {
            isReader?.close()
        }
    }

    static PropertySource loadYaml(Resource resource) {
        List<PropertySource<?>> sources = new YamlPropertySourceLoader().load(resource.filename, resource)
        if (sources.size() > 0) {
            return sources.first() as PropertySource
        }
    }

    static PropertySource loadProperties(Resource resource) {
        def props = new Properties()
        props.load(resource.inputStream)
        return new MapPropertySource(resource.filename, props as Map)
    }
}
