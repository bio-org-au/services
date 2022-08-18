/*
    Copyright 2016 Australian National Botanic Gardens

    This file is part of NSL services project.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy
    of the License at http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package au.org.biodiversity.nsl

import au.org.biodiversity.nsl.config.ApplicationUser
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.recovery.ResilientFileOutputStream
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.slf4j.LoggerFactory

import javax.sql.DataSource

/**
 * This is a helper service for abstracting, accessing and managing configuration of the services.
 *
 * Service configuration is held in these places
 * * database under the shard_config table
 * * ~/home/.nsl/services-config.groovy
 *
 * The services-config file is a standard grails config file that is slurped at startup and can be accessed via the
 * grailsApplication.config object.
 */

@Transactional
class ConfigService {

    DataSource dataSource

    GrailsApplication grailsApplication

    private String nameSpaceName
    private Map shardConfig = null
    private Map appConfigMap = null

    private Map appConfig() {
        if (!appConfigMap) {
            appConfigMap = grailsApplication.config.flatten()
        }
        return appConfigMap
    }

    private String getShardConfigOrfail(String key) {
        if (shardConfig == null) {
            fetchShardConfig()
        }
        if (shardConfig.containsKey(key)) {
            shardConfig[key]
        } else {
            throw new Exception("Config error. Add '$key' to shard_config.")
        }
    }

    private fetchShardConfig() {
        Sql sql = getSqlForNSLDB()
        shardConfig = [:]
        sql.eachRow('SELECT * FROM shard_config') { row ->
            shardConfig.put(row.name, row.value)
        }
        sql.close()
    }

    String getNameSpaceName() {
        if (!nameSpaceName) {
            nameSpaceName = getShardConfigOrfail('name space')
        }
        return nameSpaceName
    }

    Namespace getNameSpace() {
        nameSpaceName = getNameSpaceName()
        Namespace nameSpace = Namespace.findByName(nameSpaceName)
        if (!nameSpace) {
            log.error "Namespace not correctly set in config. Add 'name space' to shard_config, and make sure Namespace exists."
        }
        return nameSpace
    }

    String getNameTreeName() {
        return getShardConfigOrfail('name tree label')
    }

    String getClassificationTreeName() {
        try {
            return getShardConfigOrfail('classification tree key')
        } catch (e) {
            log.error e.message
        }
        return getShardConfigOrfail('classification tree label')
    }

    String getShardDescriptionHtml() {
        return getShardConfigOrfail('description html')
    }

    String getPageTitle() {
        return getShardConfigOrfail('page title')
    }

    String getBannerText() {
        return getShardConfigOrfail('banner text')
    }

    String getBannerImage() {
        return getShardConfigOrfail('banner image')
    }

    String getCardImage() {
        return getShardConfigOrfail('card image')
    }

    String getProductDescription(String productName) {
        return getShardConfigOrfail("$productName description")
    }

    String getMenuLabel() {
        return getShardConfigOrfail('menu label')
    }

    String getHomeURL() {
        return getShardConfigOrfail('home URL')
    }

    /**
     * Disable the checkPolynomialsBelowNameParent function for virus shard
     */
    Boolean getDisableCheckPolynomialsBelowNameParent() {
        return getShardConfigOrfail("disable checkPolynomialsBelowNameParent") == 'true'
    }

    /**
     * used next to banner text when a product is defined. See _service_navigation.gsp
     * @param productName
     * @return label text
     */
    String getProductLabel(String productName) {
        return getShardConfigOrfail("$productName label")
    }

    String getPhotoServiceUri() {
        configOrThrow('services.photoService.url')
    }

    String getPhotoSearch(String name) {
        def search = configOrThrow('services.photoService.search')
        if (search && search instanceof Closure) {
            return search(name)
        }
        return null
    }

    Map getLdapConfig() {
        if (grailsApplication.config.containsKey('ldap')) {
            return grailsApplication.config.ldap as Map
        }
        throw new Exception("Config error. Add ldap config.")
    }

    Map<String, ApplicationUser> applicationUsers

    Map<String, ApplicationUser> getApiAuth() {
        if (!applicationUsers) {
            if (!grailsApplication.config.api) {
                throw new Exception("Config error. Add api config.")
            }
            if (grailsApplication.config.api.auth instanceof Map) {
                applicationUsers = [:]
                (Map) (grailsApplication.config.api.auth).each { k, v ->
                    applicationUsers.put(k, new ApplicationUser(k, v as Map))
                }
            } else {
                throw new Exception("Config error, api config is malformed, should be a Map.")
            }
        }
        return applicationUsers
    }

    String getJWTSecret() {
        configOrThrow('nslServices.jwt.secret')
    }

    String getServerUrl() {
        configOrThrow('grails.serverURL')
    }

    String getTempFileDir() {
        configOrThrow('shard.temp.file.directory')
    }

    String getInternalMapperURL() {
        configOrThrow('services.link.internalMapperURL')
    }

    String getPublicMapperURL() {
        configOrThrow('services.link.mapperURL')
    }

    Map getMapperCredentials() {
        configOrThrow('services.mapper') as Map
    }

    String getSystemMessageFilename() {
        configOrThrow('shard.system.message.file')
    }

    String getColourScheme() {
        configOrThrow('shard.colourScheme')
    }

    String getEditorlink() {
        configOrThrow('services.link.editor')
    }

    /**
     * pass the path from the grailsApplication.config point. If the config exists you get the value if not
     * it throws and exception saying the config is missing.
     * @param path
     */
    private def configOrThrow(String path) {
        if (appConfig() && appConfig().containsKey(path)) {
            return appConfig().get(path)
        } else {
            throw new Exception("Config error. Config option $path not found, please set it in '.nsl/services-config.groovy'.")
        }
    }

    String printAppConfig() {
        appConfig().toString()
    }

    List<FileAppender> getLogFiles() {
        LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory()
        List<FileAppender> logFiles = []
        for (Logger logger : context.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext();) {
                Appender<ILoggingEvent> appender = index.next()

                if (appender instanceof FileAppender) {
                    FileAppender<ILoggingEvent> fa = (FileAppender<ILoggingEvent>)appender
                    logFiles.add(fa)
                }
            }
        }
        return logFiles
    }

    File getLogFile(String name) {
        LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory()

        for (Logger logger : context.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext();) {
                Appender<ILoggingEvent> appender = index.next()

                if (appender instanceof FileAppender && appender.name == name) {
                    FileAppender<ILoggingEvent> fa = (FileAppender<ILoggingEvent>)appender
                    ResilientFileOutputStream rfos = (ResilientFileOutputStream)fa.getOutputStream()
                    return rfos.getFile()
                }
            }
        }
    }

    Sql getSqlForNSLDB() {
        String dbUrl = grailsApplication.config.dataSource.url
        String username = grailsApplication.config.dataSource.username
        String password = grailsApplication.config.dataSource.password
        String driverClassName = grailsApplication.config.dataSource.driverClassName
        Sql.newInstance(dbUrl, username, password, driverClassName)
    }

    String getWebUserName() {
        grailsApplication.config.shard.webUser
    }

    Map getUpdateScriptParams() {
        [
                webUserName           : getWebUserName(),
                classificationTreeName: classificationTreeName,
                nameTreeName          : nameTreeName,
                nameSpace             : getNameSpace().name.toLowerCase()
        ]
    }
}
