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

import grails.core.GrailsApplication
import grails.transaction.Transactional
import groovy.sql.Sql
import org.apache.commons.logging.LogFactory

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

    DataSource dataSource_nsl

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
            log.debug "read config: $row.name: $row.value"
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
            LogFactory.getLog(this).error e.message
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
        if(grailsApplication.config.containsKey('ldap')) {
            return grailsApplication.config.ldap as Map
        }
        throw new Exception("Config error. Add ldap config.")
    }

    Map getApiAuth() {
        if (grailsApplication.config.api?.auth instanceof Map) {
            return grailsApplication.config.api?.auth as Map
        }
        throw new Exception("Config error. Add api config.")
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

    String getMapperApiKey() {
        configOrThrow('services.mapper.apikey')
    }

    String getSystemMessageFilename(){
        configOrThrow('shard.system.message.file')
    }

    String getColourScheme() {
        configOrThrow('shard.colourScheme')
    }

    String getEditorlink(){
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

    Sql getSqlForNSLDB() {
        String dbUrl = grailsApplication.config.dataSource_nsl.url
        String username = grailsApplication.config.dataSource_nsl.username
        String password = grailsApplication.config.dataSource_nsl.password
        String driverClassName = grailsApplication.config.dataSource_nsl.driverClassName
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
