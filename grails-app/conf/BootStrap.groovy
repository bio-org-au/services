import grails.util.Environment
import io.jsonwebtoken.impl.crypto.MacProvider
import org.quartz.Scheduler
import groovy.sql.Sql

/*
    Copyright 2015 Australian National Botanic Gardens

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

class BootStrap {
    def grailsApplication
    def jsonRendererService
    def searchService
    def nameService
    def nslDomainService
    def shiroSecurityManager
    def shiroSubjectDAO
    def configService
    def photoService
    def flatViewService

    def init = { servletContext ->
        if (!nslDomainService.checkUpToDate()) {
            Map scriptParams = configService.getUpdateScriptParams()
            Sql sql = configService.getSqlForNSLDB()
            if (!nslDomainService.updateToCurrentVersion(sql, scriptParams)) {
                log.error "Database is not up to date. Run update script on the DB before restarting."
                throw new Exception('Database not at expected version.')
            }
            sql.close()
        }

        Sql sql = configService.getSqlForNSLDB()
        def result = sql.firstRow('select count(*) from information_schema.triggers where trigger_name = \'audit_trigger_row\';')
        if (result?.getAt(0) != 18) {
            log.error "\n\n*** Audit triggers not set up, expected 18 audit triggers got ${result?.getAt(0)} *** \n"
        }
        sql.close()

        searchService.registerSuggestions()
        jsonRendererService.registerObjectMashallers()
        // recreate the name and taxon views in the background on startup.
        if (shiroSecurityManager) {
            shiroSecurityManager.setSubjectDAO(shiroSubjectDAO)
            println "Set subject DAO on security manager."
            //this is just a random key generator - it does nothing :-)
            String keyString = MacProvider.generateKey().encodeAsBase64()
            println "key: $keyString"
        }
        environments {
            test {

            }
            development {
                photoService.refresh()
                nameService.startUpdatePolling()
            }
            production {
                photoService.refresh()
                nameService.startUpdatePolling()
            }
        }
    }
    def destroy = {
    }
}
