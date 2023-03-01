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

import groovy.sql.GroovyResultSet
import groovy.sql.Sql

class FlatViewService implements WithSql {

    def grailsApplication
    ConfigService configService

    private static String TAXON_VIEW = 'dwc_taxon_v'
    private static String NAME_VIEW = 'dwc_name_v'
    private static String COMMON_VIEW = 'common_name_export'

    boolean taxonViewExists() {
        return viewExists(TAXON_VIEW)
    }

    boolean nameViewExists() {
        return viewExists(NAME_VIEW)
    }

    boolean commonViewExists() {
        return viewExists(COMMON_VIEW)
    }

    File exportTaxonToCSV() {
        exportToCSV(TAXON_VIEW, "${configService.classificationTreeName}-taxon")
    }

    File exportNamesToCSV() {
        exportToCSV(NAME_VIEW, "${configService.nameTreeName}-names")
    }

    File exportCommonToCSV() {
        exportToCSV(COMMON_VIEW, "${configService.nameTreeName}-common-names")
    }

    private File exportToCSV(String viewName, String namePrefix) {
        Date date = new Date()
        String tempFileDir = configService.tempFileDir
        String fileName = "$namePrefix-${date.format('yyyy-MM-dd-mmss')}.csv"
        File outputFile = new File(tempFileDir, fileName)
        withSql { Sql sql ->
            if (!viewExists(sql, viewName)) {
                throw new Exception("$viewName doesn't exist.")
            }
            DataExportService.sqlCopyToCsvFile("SELECT * FROM $viewName", outputFile, sql)
        }
        return outputFile
    }

    private boolean viewExists(String viewName) {
        boolean rtn = false
        withSql { Sql sql ->
            rtn = viewExists(sql, viewName)
        }
        return rtn
    }

    Map findNameRow(Name name, String namespace = configService.nameSpace.name.toLowerCase()) {
        try {
            String query = "select * from $NAME_VIEW where \"scientificNameID\" like '%/name/$namespace/$name.id'"
            List<Map> results = executeQuery(query, [])
            if (results.size()) {
                return results.first()
            }
        } catch (Exception e) {
            log.error("Couldn't get name row. $e.message")
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
        String query = "select * from $TAXON_VIEW where lower(\"canonicalName\") like ? or lower(\"scientificName\") like ? limit 100"
        List<Map> allResults = executeQuery(query, [nameQuery, nameQuery])
        List<Map> acceptedResults = allResults.findAll { Map result ->
            result.acceptedNameUsage == null
        }
        allResults.removeAll(acceptedResults)
        results.acceptedNames = [:]
        allResults.each { Map result ->
            results.acceptedNames[result.scientificNameID as String] = result
            List<Map> synonyms = executeQuery("select * from $TAXON_VIEW where \"acceptedNameUsage\" = ? and \"taxonomicStatus\" <> \'accepted\' limit 100", [result.scientificName])
            results.acceptedNames[result.scientificNameID as String].synonyms = synonyms
        }
        return results

    }

    private static Boolean viewExists(Sql sql, String tableName) {
        String query = """
SELECT EXISTS
( SELECT 1 FROM   pg_catalog.pg_class c
  JOIN   pg_catalog.pg_namespace n ON n.oid = c.relnamespace
  WHERE  n.nspname = 'public'
       AND    c.relname = '$tableName'
       AND    c.relkind in ('m', 'v'))
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
