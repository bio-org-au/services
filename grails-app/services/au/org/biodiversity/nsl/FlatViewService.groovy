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
    private AtomicBoolean creatingTaxonView = new AtomicBoolean(false)
    private AtomicBoolean creatingNameView = new AtomicBoolean(false)


    Closure nameView = { namespace ->
        return """  
DROP FUNCTION IF EXISTS find_rank( BIGINT, INT );

CREATE FUNCTION find_rank(name_id BIGINT, rank_sort_order INT)
  RETURNS TABLE(name_element TEXT, rank TEXT, sort_order INT) LANGUAGE SQL
AS \$\$
WITH RECURSIVE walk (parent_id, name_element, rank, sort_order) AS (
  SELECT
    parent_id,
    n.name_element,
    r.name,
    r.sort_order
  FROM name n
    JOIN name_rank r ON n.name_rank_id = r.id
  WHERE n.id = name_id AND r.sort_order >= rank_sort_order
  UNION ALL
  SELECT
    n.parent_id,
    n.name_element,
    r.name,
    r.sort_order
  FROM walk w, name n
    JOIN name_rank r ON n.name_rank_id = r.id
  WHERE n.id = w.parent_id AND r.sort_order >= rank_sort_order
)
SELECT
  w.name_element,
  w.rank,
  w.sort_order
FROM walk w
WHERE w.sort_order = rank_sort_order
\$\$;

CREATE MATERIALIZED VIEW name_view AS
SELECT n.full_name                                                                                                 AS "scientificName",
       n.full_name_html                                                                                            AS "scientificNameHTML",
       n.simple_name                                                                                               AS "canonicalName",
       n.simple_name_html                                                                                          AS "canonicalNameHTML",
       n.name_element                                                                                              AS "nameElement",
       mapper_host.value || n.uri                                                                           AS "scientificNameID",

       nt.name                                                                                                     AS "nameType",
       CASE
         WHEN (select te.excluded
               from tree_element te
                      JOIN tree_version_element tve ON te.id = tve.tree_element_id
                      JOIN tree ON tve.tree_version_id = tree.current_tree_version_id AND tree.accepted_tree = TRUE
               where te.name_id = n.id
         ) IS NULL
           THEN 'unplaced'
         ELSE CASE
                WHEN true
                  THEN 'excluded'
                ELSE 'accepted'
           END
         END                                                                                                       AS "taxonomicStatus",

       CASE
         WHEN ns.name NOT IN ('legitimate', '[default]')
           THEN ns.name
         ELSE NULL END                                                                                             AS "nomenclaturalStatus",

       CASE
         WHEN nt.autonym
           THEN NULL
         ELSE
           regexp_replace(substring(n.full_name_html FROM '<authors>(.*)</authors>'), '<[^>]*>', '', 'g')
         END                                                                                                       AS "scientificNameAuthorship",

       CASE
         WHEN nt.cultivar = TRUE
           THEN n.name_element
         ELSE NULL END                                                                                             AS "cultivarEpithet",

       nt.autonym                                                                                                  AS "autonym",
       nt.hybrid                                                                                                   AS "hybrid",
       nt.cultivar                                                                                                 AS "cultivar",
       nt.formula                                                                                                  AS "formula",
       nt.scientific                                                                                               AS "scientific",
       ns.nom_inval                                                                                                AS "nomInval",
       ns.nom_illeg                                                                                                AS "nomIlleg",
       coalesce(primary_ref.citation,
                (SELECT r.citation
                 FROM instance s
                        JOIN instance_type it ON s.instance_type_id = it.id AND it.secondary_instance
                        JOIN reference r ON s.reference_id = r.id
                 ORDER BY r.year ASC
                 LIMIT 1
                ))                                                                                                 AS "namePublishedIn",
       coalesce(primary_ref.year,
                (SELECT r.year
                 FROM instance s
                        JOIN instance_type it ON s.instance_type_id = it.id AND it.secondary_instance
                        JOIN reference r ON s.reference_id = r.id
                 ORDER BY r.year ASC
                 LIMIT 1
                ))                                                                                                 AS "namePublishedInYear",
       primary_it.name                                                                                             AS "nameInstanceType",
       basionym.full_name                                                                                          AS "originalNameUsage",
       CASE
         WHEN basionym_inst.id IS NOT NULL
           THEN mapper_host.value || (select uri from instance where id = basionym_inst.cites_id) :: TEXT
         ELSE
           CASE
             WHEN primary_inst.id IS NOT NULL
               THEN mapper_host.value || primary_inst.uri :: TEXT
             ELSE NULL END
         END                                                                                                       AS "originalNameUsageID",

       CASE
         WHEN nt.autonym = TRUE
           THEN parent_name.full_name
         ELSE
           (SELECT string_agg(regexp_replace(VALUE, E'[\\n\\r\\u2028]+', ' ', 'g'), ' ')
            FROM instance_note note
                   JOIN instance_note_key key1
                        ON key1.id = note.instance_note_key_id
                          AND key1.name = 'Type'
            WHERE note.instance_id = coalesce(basionym_inst.cites_id, primary_inst.id))
         END                                                                                                       AS "typeCitation",

       (SELECT name_element FROM find_rank(n.id, 10))                                                              AS "kingdom",
       family_name.name_element                                                                                    AS "family",
       (SELECT name_element FROM find_rank(n.id, 120))                                                             AS "genericName",
       (SELECT name_element FROM find_rank(n.id, 190))                                                             AS "specificEpithet",
       (SELECT name_element FROM find_rank(n.id, 191))                                                             AS "infraspecificEpithet",

       rank.name                                                                                                   AS "taxonRank",
       rank.sort_order                                                                                             AS "taxonRankSortOrder",
       rank.abbrev                                                                                                 AS "taxonRankAbbreviation",

       first_hybrid_parent.full_name                                                                               AS "firstHybridParentName",
       mapper_host.value || first_hybrid_parent.uri                                                                AS "firstHybridParentNameID",
       second_hybrid_parent.full_name                                                                              AS "secondHybridParentName",
       mapper_host.value || second_hybrid_parent.uri                                                        AS "secondHybridParentNameID",

       n.created_at                                                                                                AS "created",
       n.updated_at                                                                                                AS "modified",
       -- boiler plate
       (select coalesce((SELECT value FROM shard_config WHERE name = 'nomenclatural code'),
                        'ICN')) :: TEXT                                                                            AS "nomenclaturalCode",
       dataset.value                                                                                               AS "datasetName",
       'http://creativecommons.org/licenses/by/3.0/' :: TEXT                                                       AS "license",
       mapper_host.value || n.uri                                                                           AS "ccAttributionIRI"

FROM name n
       JOIN name_type nt ON n.name_type_id = nt.id
       JOIN name_status ns ON n.name_status_id = ns.id
       JOIN name_rank rank ON n.name_rank_id = rank.id

       LEFT OUTER JOIN name parent_name ON n.parent_id = parent_name.id
       LEFT OUTER JOIN name family_name ON n.family_id = family_name.id

       LEFT OUTER JOIN NAME first_hybrid_parent ON n.parent_id = first_hybrid_parent.id AND nt.hybrid
       LEFT OUTER JOIN NAME second_hybrid_parent ON n.second_parent_id = second_hybrid_parent.id AND nt.hybrid

       LEFT OUTER JOIN INSTANCE primary_inst
       JOIN instance_type primary_it
            ON primary_it.id = primary_inst.instance_type_id AND primary_it.primary_instance = TRUE
       JOIN REFERENCE primary_ref ON primary_inst.reference_id = primary_ref.id
            ON primary_inst.name_id = n.id

       LEFT OUTER JOIN INSTANCE basionym_inst
       JOIN instance_type bit ON bit.id = basionym_inst.instance_type_id AND bit.name = 'basionym'
       JOIN NAME basionym ON basionym.id = basionym_inst.name_id
            ON basionym_inst.cited_by_id = primary_inst.id,
     (SELECT value FROM public.shard_config WHERE name = 'mapper host') mapper_host,
     (SELECT value FROM public.shard_config WHERE name = 'name label') dataset
WHERE exists(SELECT 1
             FROM instance
             WHERE name_id = n.id)
ORDER BY n.sort_name;
"""
    }

    def createNameView(String namespace, Sql sql) {
        createView(namespace, NAME_VIEW, sql, nameView)
    }

    def createNameView(String namespace) {
        withSql { Sql sql ->
            createNameView(namespace, sql)
        }
    }

    def refreshNameView(String namespace, Sql sql) {
        log.debug "Refreshing name view..."
        if (viewExists(sql, NAME_VIEW)) {
            String refresh = "REFRESH MATERIALIZED VIEW $NAME_VIEW"
            sql.execute(refresh)
        } else {
            createNameView(namespace, sql)
        }
        log.debug "Refreshing name view complete."
    }

    def refreshNameView(String namespace) {
        withSql { Sql sql ->
            refreshNameView(namespace, sql)
        }
    }

    Closure taxonView = { namespace ->
        return """   
DROP FUNCTION IF EXISTS find_tree_rank( TEXT, INT );
-- this function is a little slow, but it works for now.
CREATE FUNCTION find_tree_rank(tve_id TEXT, rank_sort_order INT)
  RETURNS TABLE(name_element TEXT, rank TEXT, sort_order INT) LANGUAGE SQL
AS \$\$
WITH RECURSIVE walk (parent_id, name_element, rank, sort_order) AS (
  SELECT
    tve.parent_id,
    n.name_element,
    r.name,
    r.sort_order
  FROM tree_version_element tve
    JOIN tree_element te ON tve.tree_element_id = te.id
    JOIN name n ON te.name_id = n.id
    JOIN name_rank r ON n.name_rank_id = r.id
  WHERE tve.element_link = tve_id AND r.sort_order >= rank_sort_order
  UNION ALL
  SELECT
    tve.parent_id,
    n.name_element,
    r.name,
    r.sort_order
  FROM walk w,
    tree_version_element tve
    JOIN tree_element te ON tve.tree_element_id = te.id
    JOIN name n ON te.name_id = n.id
    JOIN name_rank r ON n.name_rank_id = r.id
  WHERE tve.element_link = w.parent_id AND r.sort_order >= rank_sort_order
)
SELECT
  w.name_element,
  w.rank,
  w.sort_order
FROM walk w
WHERE w.sort_order = rank_sort_order
\$\$;

CREATE MATERIALIZED VIEW taxon_view AS

  -- synonyms bit
  (SELECT tree.host_name || '/' || (syn ->> 'concept_link')                                               AS "taxonID",
          acc_nt.name                                                                                     AS "nameType",
          tree.host_name || tve.element_link                                                              AS "acceptedNameUsageID",
          acc_name.full_name                                                                              AS "acceptedNameUsage",
          CASE
            WHEN acc_ns.name NOT IN ('legitimate', '[default]')
              THEN acc_ns.name
            ELSE NULL END                                                                                 AS "nomenclaturalStatus",
          syn ->> 'type'                                                                                  AS "taxonomicStatus",
          (syn ->> 'type' ~ 'parte')                                                                      AS "proParte",
          syn_name.full_name                                                                              AS "scientificName",
          tree.host_name || '/' || (syn ->> 'name_link')                                                  AS "scientificNameID",
          syn_name.simple_name                                                                            AS "canonicalName",
          CASE
            WHEN syn_nt.autonym
              THEN NULL
            ELSE regexp_replace(substring(syn_name.full_name_html FROM '<authors>(.*)</authors>'), '<[^>]*>', '', 'g')
            END                                                                                           AS "scientificNameAuthorship",
          -- only in accepted names
          NULL                                                                                            AS "parentNameUsageID",
          syn_rank.name                                                                                   AS "taxonRank",
          syn_rank.sort_order                                                                             AS "taxonRankSortOrder",
          (SELECT name_element FROM find_tree_rank(tve.element_link, 10) ORDER BY sort_order ASC LIMIT 1) AS "kindom",
          -- the below works but is a little slow
          -- find another efficient way to do it.
          (SELECT name_element FROM find_tree_rank(tve.element_link, 30) ORDER BY sort_order ASC LIMIT 1) AS "class",
          (SELECT name_element FROM find_tree_rank(tve.element_link, 40) ORDER BY sort_order ASC LIMIT 1) AS "subclass",
          (SELECT name_element FROM find_tree_rank(tve.element_link, 80) ORDER BY sort_order ASC LIMIT 1) AS "family",
          syn_name.created_at                                                                             AS "created",
          syn_name.updated_at                                                                             AS "modified",
          tree.name                                                                                       AS "datasetName",
          tree.host_name || '/' || (syn ->> 'concept_link')                                               AS "taxonConceptID",
          (syn ->> 'cites')                                                                               AS "nameAccordingTo",
          tree.host_name || (syn ->> 'cites_link')                                                        AS "nameAccordingToID",
          profile -> 'APC Comment' ->> 'value'                                                            AS "taxonRemarks",
          profile -> 'APC Dist.' ->> 'value'                                                              AS "taxonDistribution",
          -- todo check this is ok for synonyms
          regexp_replace(tve.name_path, '/', '|', 'g')                                                    AS "higherClassification",
          CASE
            WHEN firstHybridParent.id IS NOT NULL
              THEN firstHybridParent.full_name
            ELSE NULL END                                                                                 AS "firstHybridParentName",
          CASE
            WHEN firstHybridParent.id IS NOT NULL
              THEN tree.host_name || '/' || firstHybridParent.uri
            ELSE NULL END                                                                                 AS "firstHybridParentNameID",
          CASE
            WHEN secondHybridParent.id IS NOT NULL
              THEN secondHybridParent.full_name
            ELSE NULL END                                                                                 AS "secondHybridParentName",
          CASE
            WHEN secondHybridParent.id IS NOT NULL
              THEN tree.host_name || '/' || secondHybridParent.uri
            ELSE NULL END                                                                                 AS "secondHybridParentNameID",
          -- boiler plate stuff at the end of the record
          (select coalesce((SELECT value FROM shard_config WHERE name = 'nomenclatural code'),
                           'ICN')) :: TEXT                                                                AS "nomenclaturalCode",
          'http://creativecommons.org/licenses/by/3.0/' :: TEXT                                           AS "license",
          syn ->> 'instance_link'                                                                         AS "ccAttributionIRI "
   FROM tree_version_element tve
          JOIN tree ON tve.tree_version_id = tree.current_tree_version_id AND tree.accepted_tree = TRUE
          JOIN tree_element te ON tve.tree_element_id = te.id
          JOIN instance acc_inst ON te.instance_id = acc_inst.id
          JOIN instance_type acc_it ON acc_inst.instance_type_id = acc_it.id
          JOIN reference acc_ref ON acc_inst.reference_id = acc_ref.id
          JOIN NAME acc_name ON te.name_id = acc_name.id
          JOIN name_type acc_nt ON acc_name.name_type_id = acc_nt.id
          JOIN name_status acc_ns ON acc_name.name_status_id = acc_ns.id,
        jsonb_array_elements(synonyms -> 'list') syn
          JOIN NAME syn_name ON syn_name.id = (syn ->> 'name_id') :: NUMERIC :: BIGINT
          JOIN name_rank syn_rank ON syn_name.name_rank_id = syn_rank.id
          JOIN name_type syn_nt ON syn_name.name_type_id = syn_nt.id
          LEFT OUTER JOIN NAME firstHybridParent ON syn_name.parent_id = firstHybridParent.id AND syn_nt.hybrid
          LEFT OUTER JOIN NAME secondHybridParent
                          ON syn_name.second_parent_id = secondHybridParent.id AND syn_nt.hybrid
   UNION
   -- The accepted names bit
   SELECT tree.host_name || tve.element_link                                                              AS "taxonID",
          acc_nt.name                                                                                     AS "nameType",
          tree.host_name || tve.element_link                                                              AS "acceptedNameUsageID",
          acc_name.full_name                                                                              AS "acceptedNameUsage",
          CASE
            WHEN acc_ns.name NOT IN ('legitimate', '[default]')
              THEN acc_ns.name
            ELSE NULL END                                                                                 AS "nomenclaturalStatus",
          CASE
            WHEN te.excluded
              THEN 'excluded'
            ELSE 'accepted'
            END                                                                                           AS "taxonomicStatus",
          FALSE                                                                                           AS "proParte",
          acc_name.full_name                                                                              AS "scientificName",
          te.name_link                                                                                    AS "scientificNameID",
          acc_name.simple_name                                                                            AS "canonicalName",
          CASE
            WHEN acc_nt.autonym
              THEN NULL
            ELSE regexp_replace(substring(acc_name.full_name_html FROM '<authors>(.*)</authors>'), '<[^>]*>', '', 'g')
            END                                                                                           AS "scientificNameAuthorship",
          tree.host_name || tve.parent_id                                                                 AS "parentNameUsageID",
          te.rank                                                                                         AS "taxonRank",
          acc_rank.sort_order                                                                             AS "taxonRankSortOrder",
          (SELECT name_element FROM find_tree_rank(tve.element_link, 10) ORDER BY sort_order ASC LIMIT 1) AS "kindom",
          -- the below works but is a little slow
          -- find another efficient way to do it.
          (SELECT name_element FROM find_tree_rank(tve.element_link, 30) ORDER BY sort_order ASC LIMIT 1) AS "class",
          (SELECT name_element FROM find_tree_rank(tve.element_link, 40) ORDER BY sort_order ASC LIMIT 1) AS "subclass",
          (SELECT name_element FROM find_tree_rank(tve.element_link, 80) ORDER BY sort_order ASC LIMIT 1) AS "family",
          acc_name.created_at                                                                             AS "created",
          acc_name.updated_at                                                                             AS "modified",
          tree.name                                                                                       AS "datasetName",
          te.instance_link                                                                                AS "taxonConceptID",
          acc_ref.citation                                                                                AS "nameAccordingTo",
          tree.host_name || '/reference/${namespace}/' || acc_ref.id                                      AS "nameAccordingToID",
          profile -> 'APC Comment' ->> 'value'                                                            AS "taxonRemarks",
          profile -> 'APC Dist.' ->> 'value'                                                              AS "taxonDistribution",
          -- todo check this is ok for synonyms
          regexp_replace(tve.name_path, '/', '|', 'g')                                                    AS "higherClassification",
          CASE
            WHEN firstHybridParent.id IS NOT NULL
              THEN firstHybridParent.full_name
            ELSE NULL END                                                                                 AS "firstHybridParentName",
          CASE
            WHEN firstHybridParent.id IS NOT NULL
              THEN tree.host_name || '/' || firstHybridParent.uri
            ELSE NULL END                                                                                 AS "firstHybridParentNameID",
          CASE
            WHEN secondHybridParent.id IS NOT NULL
              THEN secondHybridParent.full_name
            ELSE NULL END                                                                                 AS "secondHybridParentName",
          CASE
            WHEN secondHybridParent.id IS NOT NULL
              THEN tree.host_name || '/' || secondHybridParent.uri
            ELSE NULL END                                                                                 AS "secondHybridParentNameID",
          -- boiler plate stuff at the end of the record
          (select coalesce((SELECT value FROM shard_config WHERE name = 'nomenclatural code'),
                           'ICN')) :: TEXT                                                                AS "nomenclaturalCode",
          'http://creativecommons.org/licenses/by/3.0/' :: TEXT                                           AS "license",
          tve.element_link                                                                                AS "ccAttributionIRI "
   FROM tree_version_element tve
          JOIN tree ON tve.tree_version_id = tree.current_tree_version_id AND tree.accepted_tree = TRUE
          JOIN tree_element te ON tve.tree_element_id = te.id
          JOIN instance acc_inst ON te.instance_id = acc_inst.id
          JOIN instance_type acc_it ON acc_inst.instance_type_id = acc_it.id
          JOIN reference acc_ref ON acc_inst.reference_id = acc_ref.id
          JOIN NAME acc_name ON te.name_id = acc_name.id
          JOIN name_type acc_nt ON acc_name.name_type_id = acc_nt.id
          JOIN name_status acc_ns ON acc_name.name_status_id = acc_ns.id
          JOIN name_rank acc_rank ON acc_name.name_rank_id = acc_rank.id
          LEFT OUTER JOIN NAME firstHybridParent ON acc_name.parent_id = firstHybridParent.id AND acc_nt.hybrid
          LEFT OUTER JOIN NAME secondHybridParent
                          ON acc_name.second_parent_id = secondHybridParent.id AND acc_nt.hybrid
   ORDER BY "higherClassification");
"""
    }

    def createTaxonView(String namespace, Sql sql) {
        createView(namespace, TAXON_VIEW, sql, taxonView)
    }

    def createTaxonView(String namespace) {
        withSql { Sql sql ->
            createTaxonView(namespace, sql)
        }
    }

    def refreshTaxonView(String namespace, Sql sql) {
        log.debug "Refreshing taxon view..."
        if (viewExists(sql, TAXON_VIEW)) {
            String refresh = "REFRESH MATERIALIZED VIEW ${TAXON_VIEW}"
            sql.execute(refresh)
        } else {
            createTaxonView(namespace, sql)
        }
        log.debug "Refreshing taxon view complete."
    }

    def refreshTaxonView(String namespace) {
        withSql { Sql sql ->
            refreshTaxonView(namespace, sql)
        }
    }

    def createView(String namespace, String viewName, Sql sql, Closure viewDefn) {
        String drop = "DROP MATERIALIZED VIEW IF EXISTS ${viewName}"
        sql.execute(drop)
        String query = viewDefn(namespace)
        sql.execute(query)
    }

    def bgRecreateViews() {
        runAsync {
            log.debug "Doing background recreate of taxon and name views."
            String namespaceName = configService.nameSpace.name.toLowerCase()
            createTaxonView(namespaceName)
            createNameView(namespaceName)
            log.debug "Recreate views complete."
        }
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
