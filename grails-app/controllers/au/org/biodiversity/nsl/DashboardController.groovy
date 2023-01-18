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

package au.org.biodiversity.nsl

import grails.core.GrailsApplication
import org.apache.shiro.authz.annotation.RequiresRoles

import java.sql.Timestamp

class DashboardController {

    GrailsApplication grailsApplication
    VocabularyTermsService vocabularyTermsService
    AuditService auditService
    ConfigService configService
    private static final WEEK_MS = 604800000l

    def index() {
        String url = configService.serverUrl

        Map stats = [:]
        stats.names = Name.count()
        stats.namesWithInstances = Name.executeQuery("select count( n ) from Name n where exists (select 1 from Instance where name = n)").first()
        stats.references = Reference.count()
        stats.authors = Author.count()
        stats.instancesOfNameUsage = Instance.count()

        stats.NameTypeStats = Name.executeQuery(
                'select nt.name, count(*) as total from Name n, NameType nt where nt = n.nameType group by nt.id order by total desc')
        stats.NameStatusStats = Name.executeQuery(
                'select ns.name, count(*) as total from Name n, NameStatus ns where ns = n.nameStatus group by ns.id order by total desc')
        stats.NameRankStats = Name.executeQuery(
                'select nr.displayName, count(*) as total from Name n, NameRank nr where nr = n.nameRank group by nr.id order by total desc')

        stats.instanceTypeStats = Instance.executeQuery(
                'select t.name, count(*) as total from Instance i, InstanceType t where t = i.instanceType group by t.id order by total desc')
        stats.recentNameUpdates = Name.executeQuery('select n from Name n order by n.updatedAt desc', [max: 10]).collect {
            [it, it.updatedAt, it.updatedBy]
        }
        stats.recentReferenceUpdates = Reference.executeQuery('select n from Reference n order by n.updatedAt desc', [max: 10]).collect {
            [it, it.updatedAt, it.updatedBy]
        }
        stats.recentAuthorUpdates = Author.executeQuery('select n from Author n order by n.updatedAt desc', [max: 10]).collect {
            [it, it.updatedAt, it.updatedBy]
        }
        stats.recentInstanceUpdates = Instance.executeQuery('select n from Instance n order by n.updatedAt desc', [max: 10]).collect {
            [it, it.updatedAt, it.updatedBy]
        }

        [stats: stats]
    }

    def error() {
        log.debug 'In error action: throwing an error.'
        throw new Exception('This is a test error. Have a nice day :-)')
    }

    def downloadVocabularyTerms() {
        File zip = vocabularyTermsService.getVocabularyZipFile()
        // create a temp zip file
        // ask the service to populate it
        // write it to the output, with a disposition etc

        response.setHeader("Content-disposition", "attachment; filename=NslVocabulary.zip")
        response.setContentType("application/zip")

        byte[] buf = new byte[1024]

        OutputStream os = response.getOutputStream()
        InputStream is = new FileInputStream(zip)

        int n
        while ((n = is.read(buf)) > 0) {
            os.write(buf, 0, n)
        }

        is.close()
        os.close()

        zip.delete()

        return null
    }

    static Map values(String fromStr, String toStr) {
        List<Integer> fromInts = fromStr ? fromStr.split('/').collect { it.toInteger() } : null
        List<Integer> toInts = toStr ? toStr.split('/').collect { it.toInteger() } : null
        Timestamp from
        Timestamp to
        String msg
        if (!(toInts?.size() == 3 || fromInts?.size() == 3)) {
            msg = "No period set, using last 7 days."
        }
        if (fromInts?.size() == 3) {
            GregorianCalendar fromCal = new GregorianCalendar(fromInts[2], fromInts[1] - 1, fromInts[0])
            from = new Timestamp(fromCal.time.time)
        } else {
            from = new Timestamp(System.currentTimeMillis() - WEEK_MS)
        }
        if (toInts?.size() == 3) {
            GregorianCalendar toCal = new GregorianCalendar(toInts[2], toInts[1] - 1, toInts[0] + 1)
            to = new Timestamp(toCal.time.time)
        } else {
            to = new Timestamp(System.currentTimeMillis())
        }
        return [from: from, to: to, msg: msg]
    }

    @RequiresRoles('QA')
    audit(String userName, String fromStr, String toStr) {
        if (params._action_audit) {
            def vals = values(fromStr, toStr)
            if (vals.msg) {
                flash.message = vals.msg
            }
            List rows = userName ? auditService.list(userName, vals.from, vals.to, params?.filterBy) : auditService.list('', vals.from, vals.to, params?.filterBy)
            // log.debug "rows: $rows"
            [auditRows: rows, query: params]
        } else {
            flash.message = "Nothing to show."
            [auditRows: null, query: [:]]            
        }
    }

    @RequiresRoles('QA')
    stats(String userName, String fromStr, String toStr) {
        if (params._export_stats) {

            def vals = values(fromStr, toStr)
            if (vals.msg) {
                flash.message = vals.msg
            }
            Map stats = auditService.report(vals.from, vals.to)
            response.contentType = grailsApplication.config.grails.mime.types['text/csv']
            response.setHeader("Content-disposition", "attachment; filename=books.${params.extension}")

            List fields = ["Last Modified By"]
            for (String thing in stats[stats.keySet()[0]]?.keySet()) {
                fields.add(thing)
                fields.add(thing)
                fields.add(thing)
            }


            exportService.export(params.format, response.outputStream, Book.list(params), fields, labels, formatters, parameters)
        } else if (params._action_stats) {
            def vals = values(fromStr, toStr)
            if (vals.msg) {
                flash.message = vals.msg
            }
            Map stats = auditService.report(vals.from, vals.to)
            // log.debug stats.toString()
            // log.debug "rows: $rows"
            [query: params, stats: stats]
        } else {
            flash.message = "Nothing to show."
            [auditRows: null, query: [:]]
        }
    }
}
