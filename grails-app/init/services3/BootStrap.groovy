package services3

import grails.core.GrailsApplication
import groovy.sql.Sql
import io.jsonwebtoken.impl.crypto.MacProvider
import org.springframework.core.env.Environment

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BootStrap {
    GrailsApplication grailsApplication
    def jsonRendererService
    def searchService
    def nameService
    def nslDomainService
    def shiroSecurityManager
    def shiroSubjectDAO
    def configService
    def photoService

    def init = { servletContext ->
        grailsApplication.config.info.app.build.date = grailsApplication.metadata.getProperty("info.app.build.date", String)

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
        println "database url: ${grailsApplication.config.getProperty('dataSource.url')} ${LocalDateTime.now().format(dtf)}"
        if (!nslDomainService.checkUpToDate()) {
            Map scriptParams = configService.getUpdateScriptParams()
            Sql sql = configService.getSqlForNSLSchema()
            if (!nslDomainService.updateToCurrentVersion(sql, scriptParams)) {
                log.error "Database is not up to date. Run update script on the DB before restarting."
                throw new Exception('Database not at expected version.')
            }
            sql.close()
        }

        Sql sql = configService.getSqlForNSLDB()
        def result = sql.firstRow('select count(*) from information_schema.triggers where trigger_name = \'audit_trigger_row\';')
        int expectedTriggers = 21
        if (result?.getAt(0) != expectedTriggers) {
            log.error "\n\n*** Audit triggers not set up, expected ${expectedTriggers} audit triggers got ${result?.getAt(0)} *** \n"
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
                println grailsApplication.config.getProperty('updates.dir')
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
