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

/**
 * This service provides for a flat view of the NSL data for export of various outputs. This replaces the NslSimpleName
 * table.
 *
 * See JIRA NSL-1369
 *
 */
package au.org.biodiversity.nsl

import grails.transaction.Transactional
import groovy.sql.GroovyResultSet
import groovy.sql.Sql

import java.util.concurrent.atomic.AtomicBoolean

@Transactional
class FlatViewService implements WithSql {

    def grailsApplication
    def configService

    private static String TAXON_VIEW = 'taxon_view'
    private static String NAME_VIEW = 'name_view'

    def refreshNameView(Sql sql) {
        log.debug "Refreshing name view..."
        if (viewExists(sql, NAME_VIEW)) {
            String refresh = "REFRESH MATERIALIZED VIEW $NAME_VIEW"
            sql.execute(refresh)
        } else {
            throw new Exception("Name View doesn't exist")
        }
        log.debug "Refreshing name view complete."
    }

    def refreshNameView() {
        withSql { Sql sql ->
            refreshNameView(sql)
        }
    }

    def refreshTaxonView(Sql sql) {
        log.debug "Refreshing taxon view..."
        if (viewExists(sql, TAXON_VIEW)) {
            String refresh = "REFRESH MATERIALIZED VIEW ${TAXON_VIEW}"
            sql.execute(refresh)
        } else {
            throw new Exception("Taxon View doesn't exist.")
        }
        log.debug "Refreshing taxon view complete."
    }

    def refreshTaxonView() {
        withSql { Sql sql ->
            refreshTaxonView(sql)
        }
    }

    def createView(String namespace, String viewName, Sql sql, Closure viewDefn) {
        String drop = "DROP MATERIALIZED VIEW IF EXISTS ${viewName}"
        sql.execute(drop)
        String query = viewDefn(namespace)
        sql.execute(query)
    }

    File exportTaxonToCSV() {
        exportToCSV(TAXON_VIEW, "${configService.classificationTreeName}-taxon", taxonView)
    }

    File exportNamesToCSV() {
        exportToCSV(NAME_VIEW, "${configService.nameTreeName}-names", nameView)
    }

    private File exportToCSV(String viewName, String namePrefix, Closure viewDefn) {
        Date date = new Date()
        String tempFileDir = configService.tempFileDir
        String fileName = "$namePrefix-${date.format('yyyy-MM-dd-mmss')}.csv"
        File outputFile = new File(tempFileDir, fileName)
        withSql { Sql sql ->
            if (!viewExists(sql, viewName)) {
                log.debug "creating $viewName view for export."
                createView(configService.nameSpace.name.toLowerCase(), viewName, sql, viewDefn)
            }
            DataExportService.sqlCopyToCsvFile("SELECT * FROM $viewName", outputFile, sql)
        }
        return outputFile
    }

    Map findNameRow(Name name, String namespace = configService.nameSpace.name.toLowerCase()) {
        String query = "select * from $NAME_VIEW where \"scientificNameID\" like '%/name/$namespace/$name.id'"
        List<Map> results = executeQuery(query, [])
        if (results.size()) {
            return results.first()
        }
        return null
    }

    /**
     * Search the Taxon view for an accepted name tree (currently just APC) giving an APC format data output
     * as a list of taxon records.
     * See NSL-1805
     * @param nameQuery - the query name
     * @return a Map of synonyms and accepted names that match the query
     */
    Map taxonSearch(String name) {
        String nameQuery = name.toLowerCase()
        Map results = [:]
        ensureView(TAXON_VIEW, taxonView)
        String query = "select * from $TAXON_VIEW where lower(\"canonicalName\") like ? or lower(\"scientificName\") like ? limit 100"
        List<Map> allResults = executeQuery(query, [nameQuery, nameQuery])
        List<Map> acceptedResults = allResults.findAll { Map result ->
            result.acceptedNameUsage == null
        }
        allResults.removeAll(acceptedResults)
        if (!allResults.empty) {
            results.synonyms = allResults
        }
        results.acceptedNames = [:]
        acceptedResults.each { Map result ->
            results.acceptedNames[result.scientificNameID as String] = result
            List<Map> synonyms = executeQuery("select * from $TAXON_VIEW where \"acceptedNameUsage\" = ? limit 100", [result.scientificName])
            results.acceptedNames[result.scientificNameID as String].synonyms = synonyms
        }
        return results

    }

    private ensureView(viewName, Closure viewDefn) {
        withSql { Sql sql ->
            if (!viewExists(sql, viewName)) {
                log.debug "creating $viewName view."
                createView(configService.nameSpaceName.toLowerCase(), viewName, sql, viewDefn)
            }
        }
    }

    private static Boolean viewExists(Sql sql, String tableName) {
        String query = """
SELECT EXISTS
( SELECT 1 FROM   pg_catalog.pg_class c
  JOIN   pg_catalog.pg_namespace n ON n.oid = c.relnamespace
  WHERE  n.nspname = 'public'
       AND    c.relname = '$tableName'
       AND    c.relkind = 'm')
AS exists"""
        def rowResult = sql.firstRow(query)
        return rowResult.exists
    }

    private List<Map> executeQuery(String query, List params) {
        log.debug "executing query: $query, $params"
        List results = []
        withSql { Sql sql ->
            sql.eachRow(query, params) { GroovyResultSet row ->
                def res = row.toRowResult()
                Map d = new LinkedHashMap()
                res.keySet().each { key ->
                    d[key] = res[key] as String
                }
                results.add(d)
            }
        }
        return results
    }

}
