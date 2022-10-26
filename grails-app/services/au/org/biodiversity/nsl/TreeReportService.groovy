package au.org.biodiversity.nsl

import au.org.biodiversity.nsl.api.ValidationUtils
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.grails.web.json.JSONObject

import javax.sql.DataSource

@Transactional
class TreeReportService implements ValidationUtils {

    DataSource dataSource

    def treeService
    def eventService

    /**
     * create a difference between the first and second version. Normally the first version would be the currently
     * published version and the second version would be the "newer" draft version.
     *
     * This report is with respect to the second version. i.e. it will report things added too or removed from the
     * second version. 
     *
     * @param first
     * @param second
     * @return
     */
    TreeChangeSet diffReport(TreeVersion first, TreeVersion second) {
        new TreeChangeSet(first, second, getSql(), 1000)
    }

    MergeReport mergeReport(TreeVersion draft) {
        if (isBehindHead(draft)) {
            Sql sql = getSql()
            TreeChangeSet headChangeset = new TreeChangeSet(draft.previousVersion, draft.tree.currentTreeVersion, sql, 0)
            TreeChangeSet draftChangeSet = new TreeChangeSet(draft.previousVersion, draft, sql, 0)
            MergeReport report = new MergeReport(
                    from: draft.tree.currentTreeVersion,
                    to: draft,
                    upToDate: false,
                    conflicts: markConflicts(findConflictingChanges(headChangeset, draftChangeSet)),
                    nonConflicts: findNonConflictingChanges(headChangeset, draftChangeSet, draft))
            report.idDiffs()
            return report
        }
        return new MergeReport(
                from: draft.tree.currentTreeVersion,
                to: draft,
                upToDate: true,
                conflicts: [],
                nonConflicts: []
        )
    }

    MergeReport rehydrateReport(String jsonChangeset) {
        JSONObject changeset = JSON.parse(jsonChangeset) as JSONObject
        MergeReport report = new MergeReport(
                from: TreeVersion.get(changeset.mergeReport.fromVersionId as Long),
                to: TreeVersion.get(changeset.mergeReport.toVersionId as Long),
                upToDate: changeset.mergeReport.upToDate,
        )
        report.conflicts = changeset.mergeReport.conflicts.collect { diffFromData(it.tveDiff) }
        report.nonConflicts = changeset.mergeReport.nonConflicts.collect { diffFromData(it.tveDiff) }
        return report
    }

    private static TveDiff diffFromData(Map diffData) {
        TreeVersionElement from = diffData.from ? TreeVersionElement.get(diffData.from) : null
        TreeVersionElement to = diffData.to ? TreeVersionElement.get(diffData.to) : null
        TveDiff tveDiff = new TveDiff(from, to, diffData.fromType, diffData.toType)
        tveDiff.id = diffData.id
        return tveDiff
    }

    /**
     * Find changes to names that are only in head, not in draft
     *
     * find all the TVEs in the head changeset that are not in the draft changeset by name id then get to matching draft
     * TVE in the draft tree if it exists and create a TveDiff.
     *
     * @param head
     * @param draft
     * @return a set of diffs from draft to head
     */
    List<TveDiff> findNonConflictingChanges(TreeChangeSet head, TreeChangeSet draft, TreeVersion draftVersion) {
        List<Long> nonConflictingNameIds = head.all.collect { it.treeElement.nameId } - draft.all.collect {
            it.treeElement.nameId
        }
        List<TveDiff> diffs = []
        head.added.findAll { nonConflictingNameIds.contains(it.treeElement.nameId) }.each { tve ->
            TreeVersionElement draftTve = treeService.findElementForNameId(tve.treeElement.nameId, draftVersion)
            diffs.add(new TveDiff(draftTve, tve, TveDiff.UNCHANGED, TveDiff.ADDED))
        }
        head.removed.findAll { nonConflictingNameIds.contains(it.treeElement.nameId) }.each { tve ->
            TreeVersionElement draftTve = treeService.findElementForNameId(tve.treeElement.nameId, draftVersion)
            diffs.add(new TveDiff(draftTve, tve, TveDiff.UNCHANGED, TveDiff.REMOVED))
        }
        head.modifiedResult.findAll { nonConflictingNameIds.contains(it.treeElement.nameId) }.each { tve ->
            TreeVersionElement draftTve = treeService.findElementForNameId(tve.treeElement.nameId, draftVersion)
            diffs.add(new TveDiff(draftTve, tve, TveDiff.UNCHANGED, TveDiff.MODIFIED))
        }
        return diffs
    }

    List<TveDiff> markConflicts(List<TveDiff> conflicts) {
        conflicts.each { TveDiff diff ->
            diff.to.mergeConflict = true
        }
        return conflicts
    }

    /**
     * Find changes to names in both head and draft versions
     * @param head
     * @param draft
     * @return
     */
    List<TveDiff> findConflictingChanges(TreeChangeSet head, TreeChangeSet draft) {
        List<TveDiff> diffs = []
        conflictSet(diffs, head.added, draft, TveDiff.ADDED)
        conflictSet(diffs, head.removed, draft, TveDiff.REMOVED)
        conflictSet(diffs, head.modifiedResult, draft, TveDiff.MODIFIED)
        return diffs
    }

    List<TveDiff> conflictSet(List<TveDiff> diffs, List<TreeVersionElement> head, TreeChangeSet draft, int fromType) {
        conflictSet(diffs, head, draft.added, fromType, TveDiff.ADDED)
        if (fromType != TveDiff.REMOVED) { //both removed this name, not a conflict
            conflictSet(diffs, head, draft.removed, fromType, TveDiff.REMOVED)
        }
        conflictSet(diffs, head, draft.modifiedResult, fromType, TveDiff.MODIFIED)
        return diffs
    }

    List<TveDiff> conflictSet(List<TveDiff> diffs, List<TreeVersionElement> head, List<TreeVersionElement> draft, int fromType, int toType) {
        head.each { TreeVersionElement headTve ->
            draft.each { TreeVersionElement draftTve ->
                if (headTve.treeElement.nameId == draftTve.treeElement.nameId) {
                    diffs << new TveDiff(draftTve, headTve, toType, fromType)
                }
            }
        }
        return diffs
    }

    /**
     * check if this version is behind the current published (head) version.
     *
     * If the version you are checking is published it checks the published dates
     * If it's not published it checks if the previous version id equals the current published
     * version, if not then it is behind.
     * @param version
     * @return
     */
    Boolean isBehindHead(TreeVersion version) {
        if (version.published) {
            return version.tree.currentTreeVersion.publishedAt > version.publishedAt
        }
        version.tree.currentTreeVersion.id != version.previousVersion.id
    }

    Map validateTreeVersion(TreeVersion treeVersion) {
        if (!treeVersion) {
            throw new BadArgumentsException("Tree version needs to be set.")
        }
        Sql sql = getSql()

        Map problems = [:]

        problems.synonymsOfAcceptedNames = checkVersionSynonyms(sql, treeVersion)
        problems.commonSynonyms = sortCommonSynonyms(checkVersionCommonSynonyms(sql, treeVersion))
        return problems
    }

    List<EventRecord> currentSynonymyUpdatedEventRecords(Tree tree) {
        Sql sql = getSql()
        List<EventRecord> records = []
        sql.eachRow('''select id
from event_record
where type = :type
  and dealt_with = false
  and (data ->> 'treeId') :: NUMERIC :: BIGINT = :treeId
''', [treeId: tree.id, type: EventRecordTypes.SYNONYMY_UPDATED]) { row ->
            records.add(EventRecord.get(row.id as Long))
        }
        return records
    }


    private static List<Map> checkVersionSynonyms(Sql sql, TreeVersion treeVersion) {
        List<Map> problems = []
        sql.eachRow('''
SELECT
  e1.name_id                                                            AS accepted_name_id,
  e1.simple_name                                                        AS accepted_name,
  '<div class="tr">' || e1.display_html || e1.synonyms_html || '</div>' as accepted_html,
  tve1.element_link                                                     AS accepted_name_tve,
  tve1.name_path                                                        AS accepted_name_path,
  e2.simple_name                                                        AS synonym_accepted_name,
  '<div class="tr">' || e2.display_html || e2.synonyms_html || '</div>' as synonym_accepted_html,
  tve2.element_link                                                     AS synonym_tve,
  tax_syn                                                               AS synonym_record,
  tax_syn ->> 'type'                                                    AS synonym_type,
  tax_syn ->> 'name_id'                                                 as synonym_name_id,
  tax_syn ->> 'simple_name'                                             as synonym_name
FROM tree_version_element tve1
  JOIN tree_element e1 ON tve1.tree_element_id = e1.id
  ,
  tree_version_element tve2
  JOIN tree_element e2 ON tve2.tree_element_id = e2.id
  ,
      jsonb_array_elements(e2.synonyms -> 'list') AS tax_syn
WHERE tve1.tree_version_id = :treeVersionId
      AND tve2.tree_version_id = :treeVersionId
      AND tve2.tree_element_id <> tve1.tree_element_id
      AND e1.excluded = FALSE
      AND e2.excluded = FALSE
      AND e2.synonyms IS NOT NULL
      AND (tax_syn ->> 'name_id') :: NUMERIC :: BIGINT = e1.name_id
      AND tax_syn ->> 'type' !~ '.*(misapp|pro parte|common|vernacular).*'
order by accepted_name_path;
      ''', [treeVersionId: treeVersion.id]) { row ->
            Map record = [
                    accepted_name_id     : row.accepted_name_id,
                    accepted_name        : row.accepted_name,
                    accepted_html        : row.accepted_html,
                    accepted_name_tve    : row.accepted_name_tve,
                    accepted_name_path   : row.accepted_name_path,
                    synonym_accepted_name: row.synonym_accepted_name,
                    synonym_accepted_html: row.synonym_accepted_html,
                    synonym_tve          : row.synonym_tve,
                    synonym_record       : row.synonym_record,
                    synonym_type         : row.synonym_type,
                    synonym_name_id      : row.synonym_name_id,
                    synonym_name         : row.synonym_name
            ]
            record.description = "Accepted name ${record.accepted_name} is a ${record.synonym_type} of accepted name ${record.synonym_accepted_name}."
            problems.add(record)
        }
        return problems
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private sortCommonSynonyms(List<Map> commonSynonyms) {
        for (Map result in commonSynonyms) {
            String nameId = result.keySet().first()
            result.elements = result.remove(nameId) as List<Map>
            Name synonym = Name.get(nameId as Long)
            result.commonSynonym = synonym
            if (!synonym) {
                log.error "No name found for ${result.keySet().first()}"
                log.debug result.toString()
            }
        }
        return commonSynonyms.sort { it.commonSynonym?.namePath }
    }

    private static List<Map> checkVersionCommonSynonyms(Sql sql, TreeVersion treeVersion) {
        List<Map> problems = []
        sql.eachRow('''
SELECT
  (tax_syn2 ->> 'name_id')       AS common_synonym,
  jsonb_build_object((tax_syn2 ->> 'name_id') ::NUMERIC :: BIGINT,
                     jsonb_agg(jsonb_build_object('html',
                                                  '<div class="tr">' || e1.display_html || e1.synonyms_html || '</div>',
                                                  'name_link', e1.name_link,
                                                  'tree_link', tree.host_name || tve1.element_link,
                                                  'type', tax_syn1 ->> 'type',
                                                  'syn_name_id', tax_syn2 ->> 'name_id\'
                               ))) as names
FROM tree_version_element tve1
  JOIN tree_element e1 ON tve1.tree_element_id = e1.id
  ,
  tree_version_element tve2
  JOIN tree_element e2 ON tve2.tree_element_id = e2.id
  ,
      jsonb_array_elements(e1.synonyms -> 'list') AS tax_syn1,
      jsonb_array_elements(e2.synonyms -> 'list') AS tax_syn2,
  tree_version tv
  join tree on tv.tree_id = tree.id
WHERE tv.id = :treeVersionId
      AND tve1.tree_version_id = tv.id
      AND tve2.tree_version_id = tv.id
      AND tve2.tree_element_id <> tve1.tree_element_id
      AND e1.excluded = FALSE
      AND e2.excluded = FALSE
      AND e1.synonyms ->> 'list' is not null
      AND tax_syn1 ->> 'type' !~ '.*(misapp|pro parte|common|vernacular).*\'
      AND e2.synonyms ->> 'list' is not null
      AND tax_syn2 ->> 'type' !~ '.*(misapp|pro parte|common|vernacular).*\'
      AND (tax_syn1 ->> 'name_id') = (tax_syn2 ->> 'name_id')
group by common_synonym
order by common_synonym;
      ''', [treeVersionId: treeVersion.id]) { row ->
            String jsonString = row['names']

            problems.add(JSON.parse(jsonString) as Map)
        }
        return problems
    }

    private Sql getSql() {
        //noinspection GroovyAssignabilityCheck
        return Sql.newInstance(dataSource)
    }

    Map checkCurrentSynonymy(TreeVersion treeVersion, Integer limit = 100) {
        Sql sql = getSql()
        List<Map> results = []
        Integer count = sql.firstRow('''select count(tve.element_link)
from tree_element te
       join tree_version_element tve on te.id = tve.tree_element_id
       join instance i on te.instance_id = i.id
where tve.tree_version_id = :versionId 
      and te.synonyms_html <> i.cached_synonymy_html''', [versionId: treeVersion.id])[0] as Integer

        sql.eachRow('''select tve.element_link, te.instance_id, te.instance_link, i.cached_synonymy_html, coalesce(synonyms_as_html(i.id), '<synonyms></synonyms>') calc_syn_html
from tree_element te
       join tree_version_element tve on te.id = tve.tree_element_id
       join instance i on te.instance_id = i.id
where tve.tree_version_id = :versionId 
      and te.synonyms_html <> i.cached_synonymy_html''',
                [versionId: treeVersion.id], 0, limit) { row ->
            TreeVersionElement tve = TreeVersionElement.get(row.element_link as String)
            //double check that the cached value is up to date
            if (row.cached_synonymy_html != row.calc_syn_html) {
                Instance i = Instance.get(row.instance_id)
                i.cachedSynonymyHtml = row.calc_syn_html
                i.save()
            }
            //only add to results if tree is different to calc
            if (tve.treeElement.synonymsHtml != row.calc_syn_html) {
                Map d = [
                        synonymsHtml      : row.calc_syn_html,
                        treeVersionElement: tve,
                        instanceLink      : row.instance_link,
                        instanceId        : row.instance_id
                ]
                results.add(d)
            } else {
                log.debug "dropped changed synonymy for $tve.treeElement.simpleName"
                count--
            }
        }
        return [count: count, results: results]
    }

    Map getSynonymOrderingInfo(Instance instance) {
        Sql sql = getSql()
        List<Map> nom = []

        sql.eachRow('select * from  apni_ordered_nom_synonymy(:instanceId);',
                [instanceId: instance.id]) { row ->
            nom.add([
                    fullName          : row.full_name,
                    instanceType      : row.instance_type,
                    refPublicationDate: row.iso_publication_date,
                    sortName          : row.sort_name,
                    page              : row.page
            ])
        }

        List<Map> tax = []
        sql.eachRow('select * from  apni_ordered_other_synonymy(:instanceId);',
                [instanceId: instance.id]) { row ->
            tax.add([
                    fullName          : row.full_name,
                    instanceType      : row.instance_type,
                    groupPubDate      : row.group_iso_pub_date,
                    groupName         : row.group_name,
                    groupHead         : row.group_head,
                    origPubDate       : row.og_year,
                    origName          : row.og_name,
                    origHead          : row.og_head,
                    refPublicationDate: row.iso_publication_date,
                    sortName          : row.sort_name,
                    page              : row.page
            ])
        }
        return [nomenclatural: nom, taxanomic: tax]
    }

}
