package au.org.biodiversity.nsl

import au.org.biodiversity.nsl.api.ValidationUtils
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import groovy.transform.Synchronized
import org.apache.shiro.SecurityUtils
import org.hibernate.SessionFactory

import javax.sql.DataSource
import java.sql.Timestamp
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * The 2.0 Tree service. This service is the central location for all interaction with the tree.
 * TODO break this up - it crosses a couple of concerns and is too big. Delete unused methods where suppressed warnings.
 * Consider breaking out as
 *  - validation stuff
 *  - search all read only stuff in one service
 *  - edit
 */
@Transactional
class TreeService implements ValidationUtils, AsyncHelper {

    DataSource dataSource
    ConfigService configService
    LinkService linkService
    RestCallService restCallService
    TreeReportService treeReportService
    EventService eventService
    DistributionService distributionService
    SessionFactory sessionFactory
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)

    /**
     * get the named tree. This is case insensitive
     * @param name
     * @return tree or null if not found
     */
    @Transactional(readOnly = true)
    Tree getTree(String name) {
        mustHave('Tree name': name)
        Tree.findByNameIlike(name)
    }

    Tree getAcceptedTree() {
        getTree(configService.classificationTreeName)
    }

    @Transactional(readOnly = true)
    TreeVersionElement getTreeVersionElement(Long versionId, Long elementId) {
        TreeVersionElement.find('from TreeVersionElement where treeVersion.id = :versionId and treeElement.id = :elementId',
                [versionId: versionId, elementId: elementId])
    }

    @Transactional(readOnly = true)
    TreeVersionElement findElementBySimpleName(String simpleName, TreeVersion treeVersion) {
        TreeVersionElement.find("from TreeVersionElement tve where tve.treeElement.simpleName = :simpleName and treeVersion= :version", [simpleName: simpleName, version: treeVersion])
    }

    /**
     * get the current TreeElement for a name on the given tree
     * @param name
     * @param tree
     * @return TreeVersionElement or null if not on the tree
     */
    @Transactional(readOnly = true)
    TreeVersionElement findCurrentElementForName(Name name, Tree tree) {
        if (name && tree) {
            return findElementForName(name, tree.currentTreeVersion)
        }
        return null
    }

    /**
     * get the TreeElement for a name in the given version of a tree
     * @param name
     * @param treeVersion
     * @return TreeVersionElement or null if not on the tree
     */
    @Transactional(readOnly = true)
    TreeVersionElement findElementForName(Name name, TreeVersion treeVersion) {
        findElementForNameId(name?.id, treeVersion)
    }

    /**
     * get the TreeElement for a name in the given version of a tree
     * @param name
     * @param treeVersion
     * @return TreeVersionElement or null if not on the tree
     */
    @Transactional(readOnly = true)
    TreeVersionElement findElementForNameId(Long nameId, TreeVersion treeVersion) {
        if (nameId && treeVersion) {
            return TreeVersionElement.find('from TreeVersionElement tve where tve.treeVersion = :treeVersion and tve.treeElement.nameId = :nameId',
                    [treeVersion: treeVersion, nameId: nameId])
        }
        return null
    }

    @Transactional(readOnly = true)
    TreeVersionElement findElementForNameLink(String nameLink, TreeVersion treeVersion) {
        if (nameLink && treeVersion) {
            return TreeVersionElement.find('from TreeVersionElement tve where tve.treeVersion = :treeVersion and tve.treeElement.nameLink = :nameLink',
                    [treeVersion: treeVersion, nameLink: nameLink])
        }
        return null
    }

    /**
     * get the TreeElement for an instance in the current version of a tree
     * @param instance
     * @param tree
     * @return TreeVersionElement or null if not on the tree
     */
    @Transactional(readOnly = true)
    TreeVersionElement findCurrentElementForInstance(Instance instance, Tree tree) {
        if (instance && tree) {
            return findElementForInstance(instance, tree.currentTreeVersion)
        }
        return null
    }

    /**
     * get the TreeElement for an instance in the given version of a tree
     * @param instance
     * @param treeVersion
     * @return TreeVersionElement or null if not on the tree
     */
    @Transactional(readOnly = true)
    TreeVersionElement findElementForInstance(Instance instance, TreeVersion treeVersion) {
        findElementForInstanceId(instance.id, treeVersion)
    }

    @Transactional(readOnly = true)
    TreeVersionElement findElementForInstanceId(Long instanceId, TreeVersion treeVersion) {
        if (instanceId && treeVersion) {
            return TreeVersionElement.find('from TreeVersionElement tve where tve.treeVersion = :treeVersion and tve.treeElement.instanceId = :instanceId',
                    [treeVersion: treeVersion, instanceId: instanceId])
        }
        return null
    }

    @Transactional(readOnly = true)
    TreeVersionElement findElementForInstanceLink(String instanceLink, TreeVersion treeVersion) {
        if (instanceLink && treeVersion) {
            return TreeVersionElement.find('from TreeVersionElement tve where tve.treeVersion = :treeVersion and tve.treeElement.instanceLink = :instanceLink',
                    [treeVersion: treeVersion, instanceLink: instanceLink])
        }
        return null
    }

    /**
     * Look for the latest treeVersionElement version for this tree which uses this instance
     * @param instance
     * @param tree
     * @return treeVersionElement
     */
    @Transactional(readOnly = true)
    TreeVersionElement findLatestElementForInstance(Instance instance, Tree tree) {
        if (instance && tree) {
            return TreeVersionElement.find(
                    'from TreeVersionElement where treeVersion.tree = :tree and treeElement.instanceId = :instanceId and treeVersion.published = true order by treeVersion.id desc',
                    [tree: tree, instanceId: instance.id])
        }
        return null
    }

    @Transactional(readOnly = true)
    List<TreeVersionElement> findFirstAndLastElementForInstance(Instance instance, Tree tree, Boolean publishedOny = true) {
        if (instance && tree) {
            TreeVersionElement first = publishedOny ? firstPublished(instance, tree) : firstAnyVersion(instance, tree)
            TreeVersionElement last = publishedOny ? lastPublished(instance, tree) : lastAnyVersion(instance, tree)
            return new Tuple(first, last)
        }
        return null
    }

    @Transactional(readOnly = true)
    TreeVersionElement firstPublished(Instance instance, Tree tree) {
        // note bug in GORM find, it doesn't seem to like new lines which is why I haven't used multiline strings here
        TreeVersionElement.find(
                'from TreeVersionElement where treeVersion.tree = :tree and treeElement.instanceId = :instanceId and treeVersion.published = true order by treeVersion.id asc',
                [tree: tree, instanceId: instance.id])
    }

    @Transactional(readOnly = true)
    TreeVersionElement firstAnyVersion(Instance instance, Tree tree) {
        // note bug in GORM find, it doesn't seem to like new lines which is why I haven't used multiline strings here
        TreeVersionElement.find(
                'from TreeVersionElement where treeVersion.tree = :tree and treeElement.instanceId = :instanceId order by treeVersion.id asc',
                [tree: tree, instanceId: instance.id])
    }

    @Transactional(readOnly = true)
    TreeVersionElement lastPublished(Instance instance, Tree tree) {
        // note bug in GORM find, it doesn't seem to like new lines which is why I haven't used multiline strings here
        TreeVersionElement.find(
                'from TreeVersionElement where treeVersion.tree = :tree and treeElement.instanceId = :instanceId and treeVersion.published = true order by treeVersion.id desc',
                [tree: tree, instanceId: instance.id])
    }

    @Transactional(readOnly = true)
    TreeVersionElement lastAnyVersion(Instance instance, Tree tree) {
        // note bug in GORM find, it doesn't seem to like new lines which is why I haven't used multiline strings here
        TreeVersionElement.find(
                'from TreeVersionElement where treeVersion.tree = :tree and treeElement.instanceId = :instanceId order by treeVersion.id desc',
                [tree: tree, instanceId: instance.id])
    }

    @Transactional(readOnly = true)
    List<TreeVersionElement> findElementsForSynonym(Long nameId, TreeVersion treeVersion, Sql sql = getSql()) {
        if (nameId && treeVersion) {
            List<TreeVersionElement> tves = []
            sql.eachRow('''
        SELECT tve.element_link as element_link
FROM tree_element el join tree_version_element tve on el.id = tve.tree_element_id,
     jsonb_array_elements(synonyms -> 'list') AS tax_syn join instance i on (tax_syn ->> 'instance_id'):: NUMERIC :: BIGINT = i.id
WHERE tve.tree_version_id = :versionId
  AND synonyms is not null
  AND synonyms ->> 'list' is not null
  and i.name_id = :nameId''', [versionId: treeVersion.id, nameId: nameId]) { row ->
                tves.add(TreeVersionElement.get(row.element_link as String))
            }
            return tves
        }
        return null
    }

    @Transactional(readOnly = true)
    TreeVersionElement lastChangeVersion(TreeVersionElement tve) {
        TreeElement previousElement = tve.treeElement.previousElement
        if (previousElement) {
            return TreeVersionElement.find(
                    'from TreeVersionElement where treeVersion.tree = :tree and treeElement = :element order by treeVersion.id desc',
                    [tree: tve.treeVersion.tree, element: previousElement])
        } else {
            List<TreeVersionElement> history = historyForName(tve.treeElement.nameId, tve.treeVersion.tree)
            Integer idx = 0
            while (idx < history.size() && (history[idx++].elementLink != tve.elementLink))

                if (idx < history.size() && history[idx].elementLink != tve.elementLink) {
                    return history[idx]
                }
        }
        return null
    }

    /**
     * get a list of TreeVersionElements in reverse chronological order at the last version a tree element was used.
     * @param nameId
     * @param tree
     * @return
     */
    @Transactional(readOnly = true)
    List<TreeVersionElement> historyForName(Long nameId, Tree tree) {
        List<TreeVersionElement> tves = TreeVersionElement.findAll(
                'from TreeVersionElement  where treeVersion.tree = :tree and treeElement.nameId = :nameId and treeVersion.published = true order by treeVersion.id desc',
                [tree: tree, nameId: nameId]
        )
        Long elementId = 0
        List<TreeVersionElement> history = tves.findAll {
            if (it.treeElementId != elementId) {
                elementId = it.treeElementId
                return true
            }
            return false
        }
        if (tves && !tves.empty && !history.contains(tves.last())) {
            history.add(tves.last())
        }
        return history
    }

    /************* End Finds *************/

    /**
     * get the tree path as a list of TreeVersionElements
     * @param treeElement
     * @return List of TreeVersionElements
     */
    @Transactional(readOnly = true)
    List<TreeVersionElement> getElementPath(TreeVersionElement treeVersionElement) {
        mustHave(treeVersionElement: treeVersionElement)
        List<TreeVersionElement> path = []
        TreeVersionElement current = treeVersionElement
        while (current) {
            path.add(current)
            current = current.parent
        }
        return path.reverse()
    }

    @SuppressWarnings("unused")
    @Transactional(readOnly = true)
    List<TreeVersionElement> getChildElementsToDepth(TreeVersionElement parent, int depth) {
        mustHave(parent: parent, 'parent.treeElement': parent.treeElement, 'parent.treeVersion': parent.treeVersion)
        String pattern = "^${parent.treePath}(/[^/]*){1,$depth}\$"
        getElementsByPath(parent.treeVersion, pattern)
    }

    /**
     * Get child elements (not including this element)
     * @param parent
     * @return
     */
    @Transactional(readOnly = true)
    List<TreeVersionElement> getAllChildElements(TreeVersionElement parent) {
        mustHave(parent: parent, 'parent.treeElement': parent.treeElement, 'parent.treeVersion': parent.treeVersion)
        log.debug "getting children for $parent.treeElement.simpleName"
        String pattern = "^${parent.treePath}/.*"
        getElementsByPath(parent.treeVersion, pattern)
    }

    /**
     * Get just the display string and links for all the child tree elements.
     * @param treeElement
     * @return List of DisplayElements
     */
    @Transactional(readOnly = true)
    List<DisplayElement> childDisplayElements(TreeVersionElement treeVersionElement) {
        mustHave(TreeVersionElement: treeVersionElement)
        String pattern = "^${treeVersionElement.treePath}.*"
        fetchDisplayElements(pattern, treeVersionElement.treeVersion)
    }

    @Transactional(readOnly = true)
    int countAllChildElements(TreeVersionElement parent) {
        mustHave(parent: parent, 'parent.treeElement': parent.treeElement, 'parent.treeVersion': parent.treeVersion)
        String pattern = "^${parent.treePath}/.*"
        countElementsByPath(parent.treeVersion, pattern)
    }

    @Transactional(readOnly = true)
    int countElementsAtDepth(TreeVersion treeVersion, String prefix, int depth) {
        mustHave(treeVersion: treeVersion, prefix: prefix)
        String pattern = "$prefix(/[^/]*){0,$depth}\$"
        countElementsByPath(treeVersion, pattern)
    }

    /**
     * Get just the display string and link to the child tree elements to depth
     * @param treeVersionElement
     * @return List of DisplayElements
     */
    @SuppressWarnings("unused")
    @Transactional(readOnly = true)
    List<DisplayElement> childDisplayElementsToDepth(TreeVersionElement treeVersionElement, int depth) {
        mustHave(treeVersionElement: treeVersionElement)
        String pattern = "^${treeVersionElement.treePath}(/[^/]*){0,$depth}\$"
        fetchDisplayElements(pattern, treeVersionElement.treeVersion)
    }

    /**
     * Get just the display string and link to the child tree elements to depth
     * @param treeElement
     * @return List of DisplayElement
     */
    @Transactional(readOnly = true)
    List<DisplayElement> displayElementsToDepth(TreeVersion treeVersion, int depth) {
        mustHave(treeElement: treeVersion)
        String pattern = "^[^/]*(/[^/]*){0,$depth}\$"
        fetchDisplayElements(pattern, treeVersion)
    }

    @SuppressWarnings("unused")
    @Transactional(readOnly = true)
    List<DisplayElement> displayElementsToLimit(TreeVersionElement treeVersionElement, Integer limit) {
        displayElementsToLimit(treeVersionElement.treeVersion, "^${treeVersionElement.treePath}", limit)
    }

    @Transactional(readOnly = true)
    List<DisplayElement> displayElementsToLimit(TreeVersion treeVersion, Integer limit) {
        displayElementsToLimit(treeVersion, "^[^/]*", limit)
    }

    @Transactional(readOnly = true)
    List<DisplayElement> displayElementsToLimit(TreeVersion treeVersion, String prefix, Integer limit) {
        mustHave(treeVersion: treeVersion, limit: limit)
        int depth = 15 //pick a maximum depth - current APC has 10
        int count = countElementsAtDepth(treeVersion, prefix, depth)
        while (depth > 0 && count > limit) {
            depth--
            count = countElementsAtDepth(treeVersion, prefix, depth)
        }
        String pattern = "$prefix(/[^/]*){0,$depth}\$"
        fetchDisplayElements(pattern, treeVersion)
    }

    /**
     * get a list of DisplayElements
     * @param pattern
     * @param treeVersion
     * @return
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    @Transactional(readOnly = true)
    private List<DisplayElement> fetchDisplayElements(String pattern, TreeVersion treeVersion) {
        mustHave(treeVersion: treeVersion, pattern: pattern)
        log.debug("getting $pattern")
        String hostPart = treeVersion.hostPart()
        TreeElement.executeQuery('''
select tve.treeElement.displayHtml, tve.elementLink, tve.treeElement.nameLink, tve.treeElement.instanceLink, 
 tve.treeElement.excluded, tve.depth, tve.treeElement.synonymsHtml 
    from TreeVersionElement tve 
    where tve.treeVersion = :version
    and regex(tve.treePath, :pattern) = true 
    order by tve.namePath
''', [version: treeVersion, pattern: pattern]).collect { data ->
            new DisplayElement(data as List, hostPart)
        } as List<DisplayElement>
    }

    /**
     * Get tree version elements by treePath pattern ordered by treePath.
     * @param version
     * @param pattern
     * @return
     */
    @Transactional(readOnly = true)
    List<TreeVersionElement> getElementsByPath(TreeVersion version, String pattern) {
        mustHave(version: version, pattern: pattern)
        log.debug("getting $pattern")

        TreeVersionElement.executeQuery('''
select tve 
    from TreeVersionElement tve 
    where tve.treeVersion = :version
    and regex(tve.treePath, :pattern) = true 
    order by tve.treePath
''', [version: version, pattern: pattern])
    }

    @Transactional(readOnly = true)
    int countElementsByPath(TreeVersion parent, String pattern) {
        mustHave(parent: parent, pattern: pattern)
        log.debug("counting $pattern")

        int count = TreeElement.executeQuery('''
select count(tve) 
    from TreeVersionElement tve 
    where tve.treeVersion = :version
    and regex(tve.treePath, :pattern) = true  
''', [version: parent, pattern: pattern]).first() as int
        return count
    }

    @Transactional(readOnly = true)
    Map profileComment(TreeVersionElement tve) {
        mustHave('Tree version element': tve)
        profileItem(tve, commentKey(tve.treeVersion.tree))
    }

    @Transactional(readOnly = true)
    Map profileDistribution(TreeVersionElement tve) {
        mustHave('Tree version element': tve)
        profileItem(tve, distributionKey(tve))
    }

    @Transactional(readOnly = true)
    Map profileItem(TreeVersionElement tve, String key) {
        profileItem(tve.treeElement.profile as Map, key)
    }

    Map profileItem(Map profile, String key) {
        if (profile?.containsKey(key)) {
            Map value = profile.get(key) as Map
            if (value) {
                return [name: key] + value
            }
        }
        return null
    }

    @Transactional(readOnly = true)
    private String commentKey(TreeVersionElement tve) {
        commentKey(tve.treeVersion.tree)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    @Transactional(readOnly = true)
    private String commentKey(Tree tree) {
        tree.config.comment_key
    }

    @Transactional(readOnly = true)
    private String distributionKey(TreeVersionElement tve) {
        distributionKey(tve.treeVersion.tree)
    }

    @Transactional(readOnly = true)
    private String distributionKey(TreeVersion version) {
        distributionKey(version.tree)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    @Transactional(readOnly = true)
    private String distributionKey(Tree tree) {
        tree.config.distribution_key
    }

    /** Editing *****************************/

    Tree createNewTree(String treeName, String groupName, Long referenceId, String descriptionHtml,
                       String linkToHomePage, Boolean acceptedTree) {
        Tree tree = Tree.findByName(treeName)
        if (tree) {
            throw new ObjectExistsException("A Tree named $treeName already exists.")
        }
        tree = new Tree(
                name: treeName,
                groupName: groupName,
                referenceId: referenceId,
                descriptionHtml: descriptionHtml,
                linkToHomePage: linkToHomePage,
                acceptedTree: acceptedTree,
                config: [comment_key: "Comment", distribution_key: "Dist."],
                hostName: linkService.getPreferredHost()
        )
        tree.save()
        linkService.addTargetLink(tree)
        return tree
    }

    Tree editTree(Tree tree, String treeName, String groupName, Long referenceId, String descriptionHtml,
                  String linkToHomePage, Boolean acceptedTree) {
        if (!(treeName && groupName)) {
            throw new BadArgumentsException("Tree name ('$treeName') and Group name ('$groupName') must not be null.")
        }
        if (treeName != tree.name && Tree.findByName(treeName)) {
            throw new ObjectExistsException("A Tree named $treeName already exists.")
        }

        if (acceptedTree) {
            //there can only be one. Don't set the current tree to false as this will all be done within a session
            // and transaction.
            Tree.executeUpdate('update Tree set acceptedTree = false where id <> :treeId', [treeId: tree.id])
        }

        tree.name = treeName
        tree.groupName = groupName
        tree.referenceId = referenceId
        tree.descriptionHtml = descriptionHtml
        tree.linkToHomePage = linkToHomePage

        tree.acceptedTree = acceptedTree
        tree.save()

        return tree
    }

    /**
     * Delete a tree and all it's versions/elements
     *
     * WARNING: Because of the nature of a delete the session is flushed and cleared, so you need to refresh any objects
     * held prior to calling this method that you wish to re use. You should also discard anything you don't want persisted.
     *
     * @param tree
     */
    void deleteTree(Tree tree) {
        log.debug "Delete tree $tree"

        Sql sql = getSql()
        for (TreeVersion v in tree.treeVersions) {
            tree = deleteTreeVersion(v, sql)
        }
        tree.delete()
    }

    /**
     * This deletes a tree version and all it's elements.
     *
     * WARNING: Because of the nature of a delete the session is flushed and cleared, so you need to refresh any objects
     * held prior to calling this method that you wish to re use. You should also discard anything you don't want persisted.
     *
     * We return the re-loaded tree from this method as a nice way of reloading the object and helping the old object be
     * GC'd. So if you have a reference to tree call this like:
     *
     * tree = treeService.deleteTreeVersion(treeVersion)
     *
     * @param treeVersion
     * @param sql
     * @return reloaded Tree object of this version.
     */
    Tree deleteTreeVersion(TreeVersion treeVersion, Sql sql = getSql()) {
        notPublished(treeVersion)
        log.debug "deleting version $treeVersion"
        Long treeVersionId = treeVersion.id
        Long treeId = treeVersion.tree.id

        Map result = linkService.bulkRemoveTargets(treeVersion.treeVersionElements)
        log.info result.toString()
        if (!result.success) {
            throw new ServiceException("Error deleting tree links from the mapper: ${result.errors}")
        }

        TreeVersion.withSession { s ->
            s.flush()
            s.clear()
        }

        sql.execute('''
UPDATE tree SET default_draft_tree_version_id = NULL WHERE default_draft_tree_version_id = :treeVersionId;
UPDATE tree SET current_tree_version_id = NULL WHERE current_tree_version_id = :treeVersionId;
UPDATE tree_version SET previous_version_id = NULL WHERE previous_version_id = :treeVersionId;
DELETE FROM tree_version_element WHERE tree_version_id = :treeVersionId;
DELETE FROM tree_version WHERE id = :treeVersionId;
''', [treeVersionId: treeVersionId])
        deleteOrphanedTreeElements()
        return Tree.get(treeId)
    }

    private void deleteOrphanedTreeElements() {
        Closure work = {
            Sql sql = getSql()
            sql.withTransaction { t ->
                Integer count = sql.firstRow('SELECT count(*) FROM tree_element WHERE id NOT IN (SELECT DISTINCT(tree_element_id) FROM tree_version_element)')[0] as Integer
                if (count) {
                    log.debug "deleting $count orphaned elements."

                    sql.executeUpdate('''
SELECT id INTO TEMP orphans FROM tree_element WHERE id NOT IN (SELECT DISTINCT(tree_element_id) FROM tree_version_element);
UPDATE tree_element SET previous_element_id = NULL FROM orphans o WHERE previous_element_id = o.id;
DELETE from tree_element_distribution_entries using orphans o where tree_element_id = o.id;
DELETE FROM tree_element e USING orphans o WHERE e.id = o.id;
DROP TABLE IF EXISTS orphans;''')
                }
                log.debug "orphan delete complete"
                t.commit()
            }
        }
        //We could make this a worker thread that does a GC every so often
        log.debug "Scheduling delete orphan elements"
        scheduler.schedule(work, 30, TimeUnit.SECONDS)
    }

    TreeVersion publishTreeVersion(TreeVersion treeVersion, String publishedBy, String logEntry) {
        log.debug "Publish tree version $treeVersion by $publishedBy, with log entry $logEntry"
        treeVersion.published = true
        treeVersion.logEntry = logEntry
        treeVersion.publishedAt = new Timestamp(System.currentTimeMillis())
        treeVersion.publishedBy = publishedBy
        treeVersion.save()
        if (treeVersion.tree.defaultDraftTreeVersion == treeVersion) {
            treeVersion.tree.defaultDraftTreeVersion = null
        }
        treeVersion.tree.currentTreeVersion = treeVersion
        treeVersion.tree.save()
        publishDraftInstances(treeVersion, publishedBy)
        //clean up any draft tree elements left behind
        deleteOrphanedTreeElements()

        return treeVersion
    }

    def publishDraftInstances(TreeVersion treeVersion, String publishedBy) {
        List<Instance> draftInstances = Instance.executeQuery('''select i from TreeVersionElement tve, Instance i 
             where tve.treeVersion = :treeVersion  
               and tve.treeElement.instanceId = i.id 
               and i.draft = true''', [treeVersion: treeVersion])
        Date now = new Date()
        String today = now.format('dd MMM YYYY')
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis())
        draftInstances.each { Instance instance ->
            instance.draft = false
            if (!instance.reference.published) {
                instance.reference.published = true
                instance.reference.publicationDate = today
                instance.reference.isoPublicationDate = now[Calendar.YEAR]
                instance.reference.save()
            }
            instance.updatedAt = timeStamp
            instance.updatedBy = publishedBy
            instance.save()
        }
    }

    void bgCreateDefaultDraftVersion(Tree tree, TreeVersion treeVersion, String draftName, String userName, String logEntry) {
        doAsync('Create default draft version in background') {
            Thread.sleep(1000)
            TreeVersion.withNewSession { s ->
                TreeVersion.withNewTransaction {
                    log.debug "Async create"
                    tree.refresh()
                    treeVersion.refresh()
                    createDefaultDraftVersion(tree, treeVersion, draftName, userName, logEntry)
                }
            }
        }
    }

    TreeVersion createDefaultDraftVersion(Tree tree, TreeVersion treeVersion, String draftName, String userName, String logEntry) {
        log.debug "create default draft version $draftName on $tree using $treeVersion"
        tree.defaultDraftTreeVersion = createTreeVersion(tree, treeVersion, draftName, userName, logEntry)
        tree.save()
        return tree.defaultDraftTreeVersion
    }

    TreeVersion setDefaultDraftVersion(TreeVersion treeVersion) {
        log.debug "set default draft version $treeVersion"
        if (treeVersion.published) {
            throw new BadArgumentsException("TreeVersion must be draft to set as the default draft version. $treeVersion")
        }
        treeVersion.tree.defaultDraftTreeVersion = treeVersion
        treeVersion.tree.save()
        return treeVersion
    }

    TreeVersion createTreeVersion(Tree tree, TreeVersion treeVersion, String draftName, String userName, String logEntry) {
        log.debug "create tree version $draftName on $tree using $treeVersion"
        if (!draftName) {
            throw new BadArgumentsException("Draft name is required and can't be blank.")
        }
        TreeVersion fromVersion = (treeVersion ?: tree.currentTreeVersion)
        TreeVersion newVersion = new TreeVersion(
                tree: tree,
                previousVersion: fromVersion,
                draftName: draftName,
                logEntry: logEntry,
                createdBy: userName,
                createdAt: new Timestamp(System.currentTimeMillis())
        )
        newVersion.save() // set the ID before it's placed in the tree.treeVersions collection
        tree.addToTreeVersions(newVersion)
        tree.save(flush: true)
        EventRecord event = eventService.createDraftTreeEvent([tree: tree.id, version: newVersion.id], userName)

        String link = linkService.addTargetLink(newVersion)
        log.debug "added TreeVersion link $link"

        if (fromVersion) {
            copyVersion(fromVersion, newVersion)
            newVersion.previousVersion = fromVersion
        }
        eventService.dealWith(event)
        return newVersion
    }

    void copyVersion(TreeVersion fromVersion, TreeVersion toVersion) {
        if (!(fromVersion && toVersion)) {
            throw new BadArgumentsException("A from and to version are required to copy a version.")
        }
        log.debug "copying from $fromVersion to $toVersion"

        Sql sql = getSql()

        sql.execute('''
INSERT INTO tree_version_element (tree_version_id, 
                                  tree_element_id, 
                                  parent_id, 
                                  taxon_id, 
                                  element_link, 
                                  taxon_link, 
                                  tree_path,
                                  name_path,
                                  depth,
                                  updated_at,
                                  updated_by) 
  (SELECT :toVersionId, 
          tve.tree_element_id, 
          regexp_replace(tve.parent_id,  :fromVersionIdMatch, :toVersionIdMatch) ,
          tve.taxon_id, 
          regexp_replace(tve.element_link,  :fromVersionIdMatch, :toVersionIdMatch),
          tve.taxon_link,
          tve.tree_path,
          tve.name_path,
          tve.depth,
          tve.updated_at,
          tve.updated_by
   FROM tree_version_element tve WHERE tree_version_id = :fromVersionId)''',
                [fromVersionId     : fromVersion.id,
                 toVersionId       : toVersion.id,
                 fromVersionIdMatch: "/${fromVersion.id}/".toString(),
                 toVersionIdMatch  : "/${toVersion.id}/".toString()])

        //we need this for the postgresql edge case of not regenerating the stats for tree_version_element and picking the wrong strategy
        sql.execute('analyse tree_version_element (tree_version_id);')
        toVersion.refresh()

        if (fromVersion.treeVersionElements.size() != toVersion.treeVersionElements.size()) {
            throw new ServiceException("Error copying tree version $fromVersion to $toVersion. They are not the same size. ${fromVersion.treeVersionElements.size()} != ${toVersion.treeVersionElements.size()}")
        }
        Map result = linkService.bulkAddTargets(toVersion.treeVersionElements)
        log.info result.toString()
        if (!result.success) {
            throw new ServiceException("Error adding new tree links to the mapper: ${result.errors}")
        }
    }

    String authorizeTreeOperation(Tree tree) {
        String groupName = tree.groupName
        log.debug("checking ${SecurityUtils.subject.principal} has role ${groupName}")
        SecurityUtils.subject.checkRole(groupName)
        return SecurityUtils.subject.principal as String
    }

    String authorizeTreeBuilder() {
        SecurityUtils.subject.checkRole('treeBuilder')
        return SecurityUtils.subject.principal as String
    }

    TreeVersion editTreeVersion(TreeVersion treeVersion, String draftName) {
        if (!draftName) {
            throw new BadArgumentsException('Draft name must be set when editing tree version.')
        }
        treeVersion.draftName = draftName
        treeVersion.save()
        return treeVersion
    }

    Map placeTaxonUri(TreeVersionElement parentElement, String instanceUri, Boolean excluded, Map profile, String userName) {

        TaxonData taxonData = findInstanceByUri(instanceUri)
        if (!taxonData) {
            throw new ObjectNotFoundException("Taxon $instanceUri not found, trying to place it in $parentElement")
        }
        notPublished(parentElement)
        taxonData.excluded = excluded
        taxonData.profile = profile
        //will throw exceptions for invalid placements, not warnings
        List<String> warnings = validateNewElementPlacement(parentElement, taxonData)

        TreeElement treeElement = findTreeElement(taxonData) ?: makeTreeElementFromTaxonData(taxonData, null, userName)

        String distKey = distributionKey(parentElement)
        String distString = (profile && profile[distKey] && profile[distKey].value) ? profile[distKey].value : ''
        distributionService.reconstructDistribution(treeElement, distString)

        TreeVersionElement childElement = saveTreeVersionElement(treeElement, parentElement, nextSequenceId(), null, userName)
        updateParentTaxonId(parentElement)

        String message = "#### Placed ${childElement.treeElement.name.fullName} ####"
        if (warnings && !warnings.empty) {
            message += '\n\n *with these warnings:* \n'
            warnings.each {
                message += "\n * $it"
            }
        }

        return [childElement: childElement, warnings: warnings, message: message]
    }

    /**
     * Place a taxon at the "top" of the tree, with no parent
     * @param treeVersion
     * @param instanceUri
     * @param excluded
     * @param userName
     * @return
     */
    Map placeTaxonUri(TreeVersion treeVersion, String instanceUri, Boolean excluded, Map profile, String userName) {

        TaxonData taxonData = findInstanceByUri(instanceUri)
        if (!taxonData) {
            throw new ObjectNotFoundException("Taxon $instanceUri not found, trying to place it in ${treeVersion.draftName}")
        }
        notPublished(treeVersion)
        taxonData.excluded = excluded
        taxonData.profile = profile
        //will throw exceptions for invalid placements, not warnings
        List<String> warnings = validateNewElementTopPlacement(treeVersion, taxonData)

        TreeElement treeElement = findTreeElement(taxonData) ?: makeTreeElementFromTaxonData(taxonData, null, userName)

        String distKey = distributionKey(treeVersion)
        String distString = (profile && profile[distKey] && profile[distKey].value) ? profile[distKey].value : ''
        distributionService.reconstructDistribution(treeElement, distString)

        TreeVersionElement childElement = saveTreeVersionElement(treeElement, null, treeVersion, nextSequenceId(), null, userName)

        return [childElement: childElement, warnings: warnings, message: "#### Placed ${childElement.treeElement.name.fullName} ####"]
    }

    /**
     * Replace an existing Taxon Concept with another one and possibly move it's placement on the tree.
     * This moves the child taxa from the replaced taxon to this new one.
     *
     * A new taxon (tree element) is created with a new instance and placed on the tree under the desired
     * parent element. The child tree version elements tree paths are updated.
     *
     * This will copy the status from the replaced taxon. The profile will be copied from the replacedTaxon
     * only if the taxonData from the instance doesn't contain profile data.
     *
     * The old tree element will be removed from the current version of the tree.
     *
     * @param currentTve
     * @param parentTve
     * @param instanceUri
     * @param userName
     * @return
     */
    Map replaceTaxon(TreeVersionElement currentTve, TreeVersionElement parentTve, String instanceUri, Boolean excluded, Map profile, String userName) {
        mustHave('Current Element': currentTve, 'Parent Element': parentTve, 'Instance Uri': instanceUri, userName: userName)
        notPublished(parentTve)

        if (currentTve.treeElement.instanceLink == instanceUri) {
            return [replacementElement: currentTve, problems: "#### Same instance #### \\n\\n *Didn't do anything*"]
        }

        TaxonData taxonData = findInstanceByUri(instanceUri)
        if (!taxonData) {
            throw new ObjectNotFoundException("Taxon $instanceUri not found, trying to place it in $parentTve")
        }

        taxonData.excluded = excluded
        taxonData.profile = profile

        List<String> warnings = validateReplacementElement(parentTve, currentTve, taxonData)

        TreeElement treeElement = findTreeElement(taxonData) ?: makeTreeElementFromTaxonData(taxonData, currentTve.treeElement, userName)

        String distKey = distributionKey(currentTve)
        String distString = (profile && profile[distKey] && profile[distKey].value) ? profile[distKey].value : ''
        distributionService.reconstructDistribution(treeElement, distString)

        TreeVersionElement replacementTve = saveTreeVersionElement(treeElement, parentTve, nextSequenceId(), null, userName)

        updateParentId(currentTve, replacementTve)
        //using flush mode commit, means we have to flush here
        sessionFactory.currentSession.flush()
        updateChildTreePath(replacementTve, currentTve)
        updateChildNamePath(replacementTve, currentTve)
        updateChildNameDepth(replacementTve)
        Set<TreeVersionElement> parents = getParentTreeVersionElements(parentTve) + getParentTreeVersionElements(currentTve.parent)
        updateParentTaxonId(parents, parentTve.treeVersion.hostPart())

        deleteTreeVersionElement(currentTve)

        String message = null
        if (warnings && !warnings.empty) {
            message = "#### Replaced with ${replacementTve.treeElement.name.fullName} #### \n\n *Note these warnings:* \n"
            warnings.each {
                message += "\n * $it"
            }
        }

        return [replacementElement: replacementTve, problems: message]
    }

    /**
     * Change the parent taxon only of a current unpublished taxon.
     * @param currentTve
     * @param newParentTve
     * @param userName
     * @return
     */
    Map changeParentTaxon(TreeVersionElement currentTve, TreeVersionElement newParentTve, String userName) {
        mustHave('Current Element': currentTve, 'Parent Element': newParentTve, userName: userName)
        notPublished(newParentTve)

        //get data from link because it could be external to this shard
        TaxonData taxonData = findInstanceByUri(currentTve.treeElement.instanceLink)
        if (!taxonData) {
            throw new ObjectNotFoundException("Taxon $currentTve.treeElement.instanceLink not found, trying to place it in $newParentTve")
        }

        taxonData.excluded = currentTve.treeElement.excluded
        taxonData.profile = currentTve.treeElement.profile

        List<String> warnings = validateChangeParentElement(newParentTve, taxonData)

        updateExistingTve(currentTve, newParentTve, userName)

        String message = null
        if (warnings && !warnings.empty) {
            message = "#### Replaced parent with ${newParentTve.treeElement.name.fullName} #### \n\n *Note these warnings:* \n"
            warnings.each {
                message += "\n * $it"
            }
        }

        return [replacementElement: currentTve, problems: message]
    }

    /**
     * for each treeVersionElement in the parent branch set the taxonId to a new, unique value. This can only happen in
     * a draft tree version, so first check the taxonId is not already unique (i.e. already been updated in this version)
     * before updating, to prevent wasting ID space and links.
     *
     * This does *not* check the draft status of the parent, so it needs to be checked before calling.
     *
     * @param parent
     */
    private void updateParentTaxonId(TreeVersionElement parent) {
        updateParentTaxonId(getParentTreeVersionElements(parent), parent.treeVersion.hostPart())
    }

    private void updateParentTaxonId(Collection<TreeVersionElement> parents, String hostPart) {
        if (parents) {
            Sql sql = getSql()
            parents.each { TreeVersionElement element ->
                if (!isUniqueTaxon(element)) {
                    element.taxonId = nextSequenceId(sql)
                    element.taxonLink = linkService.addTaxonIdentifier(element) - hostPart
                    element.save()
                }
            }
            sql.close()
        }
    }

    private static boolean isUniqueTaxon(TreeVersionElement element) {
        TreeVersionElement.countByTaxonId(element.taxonId) <= 1 //if 0 then it hasn't been saved/flushed yet
    }

    /**
     * Fetch the tree version elements for each tree element in the tree path.
     *
     * @param treeVersionElement
     * @return a set of TreeVersionElements
     */
    static List<TreeVersionElement> getParentTreeVersionElements(TreeVersionElement treeVersionElement) {
        List<TreeVersionElement> parents = [treeVersionElement]
        TreeVersionElement parent = treeVersionElement.parent
        while (parent) {
            parents.add(parent)
            parent = parent.parent
        }
        return parents
    }

    /**
     * Remove this tree version element and all it's children.
     * @param treeVersionElement
     * @return count of treeVersionElements removed.
     */
    Map removeTreeVersionElement(TreeVersionElement treeVersionElement) {
        notPublished(treeVersionElement)

        TreeVersionElement parent = treeVersionElement.parent

        List<TreeVersionElement> elements = getAllChildElements(treeVersionElement).reverse()
        elements.add(treeVersionElement)
        int count = elements.size()

        String message = "Deleted $count elements:\n"
        elements.each {
            message += "\n * ${it.treeElement.displayHtml}"
        }

        log.debug "Deleting ${count} tree version elements."
        Map result = linkService.bulkRemoveTargets(elements)
        log.info result.toString
        if (!result.success) {
            throw new ServiceException("Error deleting tree links from the mapper: ${result.errors}")
        }

        for (TreeVersionElement kid in elements) {
            log.debug "Deleting $kid"
            kid.treeElement.removeFromTreeVersionElements(kid)
            kid.treeVersion.removeFromTreeVersionElements(kid)
            kid.delete(flush: true)
        }

        elements.clear()

        updateParentTaxonId(parent)

        //if this is removing new elements in a draft we may orphan some tree elements so it pays to clean up
        //this may be moved to a background garbage collection task if it is too slow.
        deleteOrphanedTreeElements()
        return [count: count, message: message]
    }

    /**
     * Edit the profile of a tee element for a draft tree. This will create a new tree element if the profile is different.
     *
     * A profile looks something like this:
     *{*   "APC Dist.": {*     "value": "Qld, NSW, LHI, NI, Vic, Tas",
     *     "created_at": "2014-03-25T00:00:00+11:00",
     *     "created_by": "KIRSTENC",
     *     "updated_at": "2014-03-25T14:04:06+11:00",
     *     "updated_by": "KIRSTENC",
     *     "source_link": "http://localhost:7070/nsl-mapper/instanceNote/apni/1132306"
     *},
     *   "APC Comment": {*     "value": "Treated as <i>Blechnum neohollandicum</i> Christenh. in NSW.",
     *     "created_at": "2016-06-10T15:21:38.135+10:00",
     *     "created_by": "blepschi",
     *     "updated_at": "2016-06-10T15:21:38.135+10:00",
     *     "updated_by": "blepschi",
     *     "source_link": "http://localhost:7070/nsl-mapper/instanceNote/apni/6842405"*   }*}*
     * @param treeVersionElement
     * @param profile
     * @param userName
     * @return
     */

    TreeVersionElement editProfile(TreeVersionElement treeVersionElement, Map profile, String userName) {
        mustHave(treeVersionElement: treeVersionElement, userName: userName)
        notPublished(treeVersionElement)
        treeVersionElement.treeElement.refresh() //fetch the element data including treeVersionElements

        log.debug treeVersionElement.treeElement.profile.toString()
        log.debug profile.toString()
        if (treeVersionElement.treeElement.profile == profile) {
            return treeVersionElement // data is equal, do nothing
        }

        //if there is an element that matches the new data use that element
        Map elementComparators = comparators(treeVersionElement.treeElement)

        String distKey = distributionKey(treeVersionElement)

        excludedValidation(elementComparators.excluded, profile, distKey)

        //we don't want to check the whole profile, just the *values* of the comment and distribution
        //so remove the profile and then compare the values for all the matches

        elementComparators.remove('profile')
        //order by latest first so the first match will be the latest. (yes there are multiple exact matches in some data sets)
        List<TreeElement> matchingElements = TreeElement.findAllWhere(elementComparators).sort { a, b -> b.id <=> a.id }
        log.debug "Found ${matchingElements.size()} matching elements"

        TreeElement foundElement = null

        if (matchingElements?.size()) {
            foundElement = matchingElements.find { TreeElement te ->
                log.debug "Checking profile of ${te}"
                profile && te.profile &&
                        compareProfileMapValues(profile, te.profile)
            }
        }

        if (foundElement) {
            log.debug "Reusing $foundElement"
            return changeElement(treeVersionElement, foundElement, userName)
        }

        //if this is not a draft only element clone it
        if (treeVersionElement.treeElement.treeVersionElements.size() > 1) {
            log.debug "No matching element, creating a new one."
            TreeElement copiedElement = copyTreeElement(treeVersionElement.treeElement, userName)
            treeVersionElement = changeElement(treeVersionElement, copiedElement, userName)
            treeVersionElement.save(flush: true)
            log.debug "Created. ${treeVersionElement.treeElement}."
            //don't update taxonId above as the taxon hasn't changed
        } else {
            log.debug "Editing draft element ${treeVersionElement.treeElement}."
        }

        String distString = (profile && profile[distKey] && profile[distKey].value) ? profile[distKey].value : ''
        distributionService.reconstructDistribution(treeVersionElement.treeElement, distString)

        Timestamp now = new Timestamp(System.currentTimeMillis())

        treeVersionElement.treeElement.profile = profile
        treeVersionElement.treeElement.updatedBy = userName
        treeVersionElement.treeElement.updatedAt = now
        treeVersionElement.updatedBy = userName
        treeVersionElement.updatedAt = now
        treeVersionElement.save()
        deleteOrphanedTreeElements()
        return treeVersionElement
    }

    static boolean compareProfileMapValues(Map m, Map n) {
        if (m.keySet() == n.keySet()) {
            //find first example of values *not* matching
            String result = m.keySet().find { key ->
                m[key]['value'] != n[key]['value']
            }
            return result == null
        }
        return false
    }

    TreeVersionElement minorEditDistribution(TreeVersionElement treeVersionElement, String distribution, String reason, String userName) {
        excludedValidation(treeVersionElement.treeElement.excluded, distribution)
        String distKey = distributionKey(treeVersionElement)
        //this will throw an exception if the distribution string is bad.
        distributionService.reconstructDistribution(treeVersionElement.treeElement, distribution)
        //re-order the distribution string correctly
        distribution = distributionService.constructDistributionString(treeVersionElement.treeElement)
        treeVersionElement.treeElement.save(flush: true)
        return minorEditProfile(treeVersionElement, distribution, reason, userName, distKey)
    }

    TreeVersionElement minorEditComment(TreeVersionElement treeVersionElement, String comment, String reason, String userName) {
        String commentKey = commentKey(treeVersionElement)
        return minorEditProfile(treeVersionElement, comment, reason, userName, commentKey)
    }

    TreeVersionElement minorEditProfile(TreeVersionElement treeVersionElement, String value, String reason, String userName, String key) {
        mustHave(treeVersionElement: treeVersionElement, value: value, reason: reason, userName: userName)
        published(treeVersionElement)
        treeVersionElement.treeElement.refresh()

        Map profile = treeVersionElement.treeElement.profile
        log.debug profile.toString()
        if (profile[key]?.value == value) {
            //value hasn't changed do nothing
            log.debug "no change in $key, doing nothing."
            return treeVersionElement
        }

        ProfileValue profileValue = new ProfileValue(value, userName, profile[key] as Map, reason)

        treeVersionElement.treeElement.profile[key] = profileValue.toMap()
        treeVersionElement.treeElement.save()
        log.debug treeVersionElement.treeElement.profile.toString()
        return treeVersionElement
    }

    /**
     * Edit the excluded status on the TreeVersionElement. This will replace the TreeVersionElement
     *
     * @param treeVersionElement
     * @param excluded
     * @param userName
     * @return
     */
    TreeVersionElement editExcluded(TreeVersionElement treeVersionElement, Boolean excluded, String userName) {
        mustHave(treeVersionElement: treeVersionElement, userName: userName)
        notPublished(treeVersionElement)
        treeVersionElement.treeElement.refresh() //fetch the element data including treeVersionElements

        if (treeVersionElement.treeElement.excluded == excluded) {
            return treeVersionElement // data equal, do nothing
        }

        //if there is an element that matches the new data use that element
        Map elementComparators = comparators(treeVersionElement.treeElement)
        elementComparators.excluded = excluded

        excludedValidation(excluded, elementComparators.profile, distributionKey(treeVersionElement))

        TreeElement foundElement = findTreeElement(elementComparators)
        if (foundElement) {
            return changeElement(treeVersionElement, foundElement, userName)
        }

        //if this is not a draft only element clone it
        if (treeVersionElement.treeElement.treeVersionElements.size() > 1) {
            TreeElement copiedElement = copyTreeElement(treeVersionElement.treeElement, userName)
            treeVersionElement = changeElement(treeVersionElement, copiedElement, userName)
            //don't update taxonId above as the taxon hasn't changed
        } else {
            treeVersionElement.treeElement.updatedBy = userName
            treeVersionElement.treeElement.updatedAt = new Timestamp(System.currentTimeMillis())
        }

        treeVersionElement.treeElement.excluded = excluded
        treeVersionElement.save()
        return treeVersionElement
    }

    /**
     * Change the TreeElement based on the Instance Data provided. This will either create a new TreeElement
     * or find a matching existing TreeElement to use. It will replace the TreeVersionElement which must not be published.
     *
     * @param treeVersionElement
     * @param userName
     * @return
     */
    @Synchronized
    TreeVersionElement updateElementFromInstanceData(TreeVersionElement treeVersionElement, String userName) {
        mustHave(treeVersionElement: treeVersionElement, userName: userName)
        notPublished(treeVersionElement)
        treeVersionElement.refresh()
        treeVersionElement.treeElement.refresh() //fetch the element data including treeVersionElements

        //if there is an element that matches the new data use that element
        TaxonData taxonData = findInstanceByUri(treeVersionElement.treeElement.instanceLink)
        taxonData.excluded = treeVersionElement.treeElement.excluded
        taxonData.profile = treeVersionElement.treeElement.profile
        if (taxonData.equalsElement(treeVersionElement.treeElement)) {
            return treeVersionElement // data equal, do nothing
        }

        TreeElement foundElement = findTreeElement(taxonData)
        if (foundElement) {
            log.debug "Reusing $foundElement with $treeVersionElement.elementLink"
            if (foundElement.id != treeVersionElement.treeElementId) {
                return changeElement(treeVersionElement, foundElement, userName)
            } else {
                //nothing has actually changed
                log.debug "No change to treeversion element detected, returning $treeVersionElement.elementLink."
                return treeVersionElement
            }
        }

        //if this is not a draft only element clone it. Note check relies on treeVersionElement not being published
        //as checked above.
        if (treeVersionElement.treeElement.treeVersionElements.size() > 1) {
            TreeElement updatedElement = makeTreeElementFromTaxonData(taxonData, treeVersionElement.treeElement, userName)
            treeVersionElement = changeElement(treeVersionElement, updatedElement, userName)
            // don't update taxonId above as the instance hasn't changed, and other taxon changes will have already
            // updated the taxon id
        } else {
            treeVersionElement.treeElement.synonyms = taxonData.synonyms.asMap()
            treeVersionElement.treeElement.synonymsHtml = taxonData.synonymsHtml
            treeVersionElement.treeElement.save()
        }

        Timestamp now = new Timestamp(System.currentTimeMillis())
        treeVersionElement.updatedBy = userName
        treeVersionElement.updatedAt = now
        treeVersionElement.save()
        return treeVersionElement
    }

    /**
     * just update the cached synonymy
     * @param instance
     * @param userName
     */
    def checkSynonymyUpdated(Instance instance) {
        String synonyms = getSynonymsHtmlViaDBFunction(instance.id)
        instance.cachedSynonymyHtml = synonyms
        instance.save()
    }

    def refreshSynonymHtmlCache() {
        log.debug "Refreshing synonymy cache"
        Sql sql = getSql()
        sql.executeUpdate("update instance set cached_synonymy_html = coalesce(synonyms_as_html(id), '<synonyms></synonyms>') where id in (select distinct instance_id from tree_element);")
    }

    /**
     * If a name is changed the way we display the name on the tree may change so we should update the display_html.
     * This is not a change to the tree, just a change to the way the name is displayed (e.g. corrections to author/reference)
     *
     * We just assume something may have changed and set the displayHtml again, since a comparison would be the same
     * amount of work
     * @param name
     */
    def checkNameOnTreeChanged(Name name) {
        TreeElement.findAllByNameId(name.id).each { te ->
            te.displayHtml = "<data>${name.fullNameHtml} <citation>${te.instance.reference.citationHtml}</citation></data>"
            te.simpleName = name.simpleName
            te.save()
        }
    }

    @SuppressWarnings("unused")
    Integer countChangedDisplayHtml() {
        Sql sql = getSql()
        sql.firstRow('''select count(te)
  from tree_element te
    join name n on te.name_id = n.id
    join instance i on te.instance_id = i.id
    join reference r on i.reference_id = r.id
where te.display_html <> ('<data>' || n.full_name_html || ' <citation>' || r.citation_html || '</citation></data>')
''')[0] as Integer
    }

    /**
     * rewrite all the displayHtml fields on all tree elements. use as daily refresh to catch any changes missed by lost
     * triggers.
     */
    def refreshDisplayHtml() {
        Sql sql = getSql()
        sql.executeUpdate('''
update tree_element te 
    set display_html = '<data>' || n.full_name_html || 
    '<name-status class="' || ns.name|| '">, ' || ns.name || '</name-status> <citation>' || r.citation_html || '</citation></data>'
  from name n join name_status ns on n.name_status_id = ns.id, 
  instance i, reference r
  where te.name_id = n.id
    and te.instance_id = i.id
    and i.reference_id = r.id;
''')
    }

    /**
     * Checks to see if any current trees use a deleted instance and create an appropriate EventRecord.
     *
     * An accepted instance *may* be deleted from a tree if the tree isn't on the same services database as the tree.
     *
     * @param instanceId
     * @param userName
     */
    def checkUsageOfDeletedInstance(Long instanceId, Long citedById, String userName) {
        List<Tree> trees = Tree.list()
        for (tree in trees) {
            TreeVersionElement tve
            if (tree.defaultDraftTreeVersion) {
                tve = findElementForInstanceId(instanceId, tree.defaultDraftTreeVersion)
            } else if (tree.currentTreeVersion) {
                tve = findElementForInstanceId(instanceId, tree.currentTreeVersion)
            }
            if (tve) {
                // found a usage of the instance on a tree as an accepted name
                makeAcceptedInstanceDeletedEventRecord(tree, tve, instanceId, userName)
            } else if (citedById) {
                Instance instance = Instance.get(citedById)
                if (instance) {
                    checkSynonymyUpdated(instance)
                }
            }
        }
    }

    private makeAcceptedInstanceDeletedEventRecord(Tree tree, TreeVersionElement tve, Long instanceId, String userName) {
        List<TreeVersion> affectedVersions = TreeVersion.findAllByTreeAndPublished(tree, false)
        affectedVersions.add(tree.currentTreeVersion)
        Map data = [
                treeId            : tree.id,
                affectedVersions  : affectedVersions,
                treeVersionElement: tve.elementLink,
                instanceId        : instanceId
        ]
        eventService.createAcceptedInstanceDeletedEvent(data, userName)
    }

    /**
     * Replace an existing tree version element with a new one using a different tree element.
     *
     * This updates the tree version element tree path and deletes the existing treeVersionElement.
     *
     * You must make sure the existing treeVersionElement not published before calling this (i.e. it is on a draft tree.
     * Since you should only edit a draft tree that should be true.)
     *
     * @param treeVersionElement
     * @param newElement
     * @param userName
     * @return the replacement tree version element
     */
    private TreeVersionElement changeElement(TreeVersionElement treeVersionElement, TreeElement newElement, String userName) {
        TreeVersionElement replacementTve = saveTreeVersionElement(newElement, treeVersionElement.parent,
                treeVersionElement.treeVersion, treeVersionElement.taxonId, treeVersionElement.taxonLink, userName)
        updateParentId(treeVersionElement, replacementTve)
        updateChildTreePath(replacementTve, treeVersionElement)
        deleteTreeVersionElement(treeVersionElement)
        return replacementTve
    }

    /**
     * for tree version elements in this version update tree paths that contain the old element ids to contain the new
     * element id.
     *
     * Note that because this does an update, if you have any of the children loaded in the session you will need to
     * refresh them to see the change in the treePath.
     *
     * @param newTve
     * @param oldElementId
     * @return
     */
    TreeVersionElement updateChildTreePath(TreeVersionElement newTve, TreeVersionElement oldTve) {
        updateChildTreePath(newTve.treePath, oldTve.treePath, newTve.treeVersion)
        return newTve
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    void updateChildTreePath(String newPath, String oldPath, TreeVersion treeVersion) {
        log.debug "Replacing tree path $oldPath with $newPath"
        TreeVersionElement.executeUpdate('''
update TreeVersionElement set treePath = regexp_replace(treePath, :oldId, :newId)
where treeVersion = :version
and regex(treePath, :oldId) = true
''',
                [oldId  : "^$oldPath/",
                 newId  : "$newPath/",
                 version: treeVersion])
    }

    TreeVersionElement updateChildNamePath(TreeVersionElement newTve, TreeVersionElement oldTve) {
        updateChildNamePath(newTve.namePath, oldTve.namePath, newTve.treeVersion)
        return newTve
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    void updateChildNamePath(String newPath, String oldPath, TreeVersion treeVersion) {
        log.debug "Replacing name path $oldPath with $newPath"
        TreeVersionElement.executeUpdate('''
update TreeVersionElement set namePath = regexp_replace(namePath, :oldPath, :newPath)
where treeVersion = :version
and regex(namePath, :oldPath) = true
''',
                [oldPath: "^$oldPath/",
                 newPath: "$newPath/",
                 version: treeVersion])
    }

    TreeVersionElement updateChildNameDepth(TreeVersionElement newTve) {
        updateChildNameDepth(newTve.namePath, newTve.treeVersion)
        return newTve
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    void updateChildNameDepth(String newPath, TreeVersion treeVersion) {
        log.debug "Updating depth for path $newPath"
        TreeVersionElement.executeUpdate('''
update TreeVersionElement set depth = array_length(regexp_split_to_array(treePath, '/'),1) - 1
where treeVersion = :version
and regex(namePath, :newPath) = true
''',
                [newPath: "^$newPath/",
                 version: treeVersion])
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private TreeVersionElement updateParentId(TreeVersionElement oldParent, TreeVersionElement newParent) {
        TreeVersionElement.findAllByParent(oldParent).each { child ->
            child.parent = newParent
            log.debug "Updating parent of tve $child.elementLink from $oldParent.elementLink to $newParent.elementLink"
            child.save()
        }
        return newParent
    }


    private void removeLink(TreeVersionElement treeVersionElement) {
        Map result = linkService.bulkRemoveTargets([treeVersionElement])
        if (!result.success) {
            throw new ServiceException("Error deleting tree links from the mapper: ${result.errors}")
        }
    }

    /**
     * deletes a tree version element.
     *
     * WARNING: you must check this is a draft before calling this.
     *
     * @param target
     * @return
     */
    private deleteTreeVersionElement(TreeVersionElement target) {
        removeLink(target)
        target.treeElement.removeFromTreeVersionElements(target)
        target.treeVersion.removeFromTreeVersionElements(target)
        target.delete()
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private TreeElement copyTreeElement(TreeElement source, String userName) {
        TreeElement treeElement = new TreeElement(instanceId: source.instanceId,
                nameId: source.nameId,
                excluded: source.excluded,
                displayHtml: source.displayHtml,
                synonymsHtml: source.synonymsHtml,
                simpleName: source.simpleName,
                nameElement: source.nameElement,
                rank: source.rank,
                sourceShard: source.sourceShard,
                synonyms: source.synonyms,
                profile: source.profile,
                sourceElementLink: source.sourceElementLink,
                nameLink: source.nameLink,
                instanceLink: source.instanceLink,
                updatedBy: userName,
                updatedAt: new Timestamp(System.currentTimeMillis()))
        treeElement.save()
        // setting these references here because of a bug? setting in the map above where the parentElement
        // changes to this new element.
        treeElement.previousElement = source
        treeElement.save(flush: true)
        return treeElement
    }

    static published(TreeVersionElement element) {
        published(element.treeVersion)
    }

    static published(TreeVersion version) {
        if (!version.published) {
            throw new PublishedVersionException("You can't do this with an unpublished tree. $version.tree.name version $version.id is not published.")
        }
    }

    static notPublished(TreeVersionElement element) {
        notPublished(element.treeVersion)
    }

    static notPublished(TreeVersion version) {
        if (version.published) {
            throw new PublishedVersionException("You can't do this with a Published tree. $version.tree.name version $version.id is already published.")
        }
    }


    static TreeElement makeTreeElementFromTaxonData(TaxonData taxonData, TreeElement previousElement, String userName) {
        TreeElement element = new TreeElement(taxonData.asMap())
        element.previousElement = previousElement
        element.updatedBy = userName
        element.updatedAt = new Timestamp(System.currentTimeMillis())
        element.sourceElementLink = null
        element.save()
    }

    /**
     * Use a map of fields to find a tree element
     * Comparing json objects with json arrays doesn't work directly so we remove the synonyms object if present and
     * rely on the synonymsHtml field
     * @param treeElementData
     * @return
     */
    static TreeElement findTreeElement(Map treeElementData) {
        return TreeElement.findWhere(treeElementData)
    }

    static TreeElement findTreeElement(TaxonData taxonData) {
        findTreeElement(comparators(taxonData))
    }

    /**
     * returns a map of comparators from taxonData
     * @param taxonData
     * @return
     */
    static Map comparators(TaxonData taxonData) {
        [
                instanceId  : taxonData.instanceId,
                nameId      : taxonData.nameId,
                excluded    : taxonData.excluded,
                simpleName  : taxonData.simpleName,
                nameElement : taxonData.nameElement,
                sourceShard : taxonData.sourceShard,
                synonymsHtml: taxonData.synonymsHtml,
                profile     : taxonData.profile
        ]
    }

    static Map comparators(TreeElement treeElement) {
        [
                instanceId  : treeElement.instanceId,
                nameId      : treeElement.nameId,
                excluded    : treeElement.excluded,
                simpleName  : treeElement.simpleName,
                nameElement : treeElement.nameElement,
                sourceShard : treeElement.sourceShard,
                synonymsHtml: treeElement.synonymsHtml,
                profile     : treeElement.profile
        ]
    }

    private TreeVersionElement saveTreeVersionElement(TreeElement element, TreeVersionElement parentTve, Long taxonId, String taxonLink, String userName) {
        saveTreeVersionElement(element, parentTve, parentTve.treeVersion, taxonId, taxonLink, userName)
    }

    private TreeVersionElement saveTreeVersionElement(TreeElement element, TreeVersionElement parentTve, TreeVersion version, Long taxonId, String taxonLink, String userName) {
        TreeVersionElement treeVersionElement = new TreeVersionElement(
                treeElement: element,
                treeVersion: version,
                parent: parentTve,
                taxonId: taxonId,
                treePath: makeTreePath(parentTve, element),
                namePath: makeNamePath(parentTve, element),
                depth: (parentTve?.depth ?: 0) + 1,
                updatedBy: userName,
                updatedAt: new Timestamp(System.currentTimeMillis())
        )

        treeVersionElement.elementLink = (addTargetLinkOrBust(treeVersionElement) - version.hostPart())
        treeVersionElement.taxonLink = taxonLink ?: (addTaxonIdentifierOrBust(treeVersionElement) - version.hostPart())
        treeVersionElement.save()
        return treeVersionElement
    }

    private String addTargetLinkOrBust(TreeVersionElement tve) {
        String link = linkService.addTargetLink(tve)
        if (!link) {
            throw new ServiceException('Link not added. Is mapper up?')
        }
        return link
    }

    private String addTaxonIdentifierOrBust(TreeVersionElement tve) {
        String taxonLink = linkService.addTaxonIdentifier(tve)
        if (!taxonLink) {
            throw new ServiceException('Taxon Link not added. Is mapper up?')
        }
        return taxonLink
    }

    private static String makeTreePath(TreeVersionElement parentTve, TreeElement element) {
        if (parentTve) {
            parentTve.treePath + "/${element.id}"
        } else {
            "/${element.id}"
        }
    }

    private static String makeNamePath(TreeVersionElement parentTve, TreeElement element) {
        if (parentTve) {
            parentTve.namePath + "/${element.nameElement}"
        } else {
            element.nameElement
        }
    }

    private Long nextSequenceId(Sql sql = getSql()) {
        sql.firstRow("SELECT nextval('nsl_global_seq')")[0] as Long
    }

    static excludedValidation(Boolean excluded, String distribution) {
        if (excluded && distribution) {
            throw new BadArgumentsException("An excluded taxon can't have a distribution.")
        }
    }

    static excludedValidation(Boolean excluded, Map profile, String distKey) {
        if (excluded && profile && profile[distKey] && profile[distKey].value) {
            throw new BadArgumentsException("An excluded taxon can't have a distribution.")
        }
    }

    /**
     * Checks name validity and parent validations i.e. rank and matching Name parent
     * @param parentElement
     * @param taxonData
     * @return
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    List<String> basicPlacementValidation(TreeVersionElement parentElement, TaxonData taxonData) {

        List<String> warnings = checkNameValidity(taxonData)

        excludedValidation(taxonData.excluded, taxonData.profile, distributionKey(parentElement))

        NameRank taxonRank = NameRank.findByName(taxonData.rank)
        NameRank parentRank = NameRank.findByName(parentElement.treeElement.rank)

        //is rank below parent
        if (!RankUtils.rankHigherThan(parentRank, taxonRank)) {
            throw new BadArgumentsException("Name $taxonData.simpleName of rank $taxonRank.displayName is not below rank $parentRank.displayName of $parentElement.treeElement.simpleName.")
        }

        //polynomials must be placed under parent
        if (!configService.disableCheckPolynomialsBelowNameParent) {
            checkPolynomialsBelowNameParent(taxonData.simpleName, taxonData.excluded, taxonRank, parentElement.namePath.split('/'))
        }
        return warnings
    }

    List<String> validateNewElementPlacement(TreeVersionElement parentElement, TaxonData taxonData) {
        TreeVersion treeVersion = parentElement.treeVersion

        List<String> warnings = basicPlacementValidation(parentElement, taxonData)

        checkInstanceIsNotOnTheTree(taxonData, treeVersion)
        checkNameIsNotOnTheTree(taxonData, treeVersion, null)
        checkNameNotAnExistingSynonym(taxonData, treeVersion, [])
        checkSynonymsOfNameNotOnTheTree(taxonData, treeVersion, null)
        checkSynonymsAreNotSynonymsOnTheTree(taxonData, treeVersion, [])

        return warnings
    }

    /**
     * Replacing a taxon needs to exclude the taxon being replaced from the checks, but must be a different instance.
     * @param parentElement
     * @param currentTve
     * @param taxonData
     * @return
     */
    List<String> validateReplacementElement(TreeVersionElement parentElement, TreeVersionElement currentTve, TaxonData taxonData) {

        TreeVersion treeVersion = parentElement.treeVersion

        List<String> warnings = basicPlacementValidation(parentElement, taxonData)

        checkInstanceIsNotOnTheTree(taxonData, treeVersion)
        checkSynonymsOfNameNotOnTheTree(taxonData, treeVersion, currentTve)
        checkNameNotAnExistingSynonym(taxonData, treeVersion, [currentTve])
        checkSynonymsAreNotSynonymsOnTheTree(taxonData, treeVersion, [currentTve])

        return warnings
    }

    /**
     * When we change parent the instance is the same, just the parent changes so don't check if the instance is on the
     * tree and exclude the current taxon from validation.
     * @param parentElement
     * @param currentTve
     * @param taxonData
     * @return
     */
    List<String> validateChangeParentElement(TreeVersionElement parentElement, TaxonData taxonData) {

        List<String> warnings = basicPlacementValidation(parentElement, taxonData)

        return warnings
    }

    /**
     * A top element, being at the top of the tree needs no parent checks
     * @param treeVersion
     * @param taxonData
     * @return
     */
    List<String> validateNewElementTopPlacement(TreeVersion treeVersion, TaxonData taxonData) {
        List<String> warnings = checkNameValidity(taxonData)
        checkInstanceIsNotOnTheTree(taxonData, treeVersion)
        checkNameIsNotOnTheTree(taxonData, treeVersion, null)
        checkNameNotAnExistingSynonym(taxonData, treeVersion, [])
        checkSynonymsOfNameNotOnTheTree(taxonData, treeVersion, null)
        checkSynonymsAreNotSynonymsOnTheTree(taxonData, treeVersion, [])
        return warnings
    }

    private static List<String> checkNameValidity(TaxonData taxonData) {
        List<String> warnings = []
        //name should not be invalid or illegal
        if (taxonData.nomIlleg) {
            warnings.add("$taxonData.simpleName is nomIlleg")
        }
        if (taxonData.nomInval) {
            warnings.add("$taxonData.simpleName is nomInval")
        }
        return warnings
    }

    private void checkInstanceIsNotOnTheTree(TaxonData taxonData, TreeVersion treeVersion) {
        //is instance already in the tree. We use instance link because that works across shards, there is a remote possibility instance id will clash.
        TreeVersionElement existingElement = findElementForInstanceLink(taxonData.instanceLink, treeVersion)
        if (existingElement) {
            String message = "Can’t place this concept - ${taxonData.displayHtml} is accepted concept **${existingElement.treeElement.displayHtml}**"
            throw new BadArgumentsException(message)
        }
    }

    private void checkNameIsNotOnTheTree(TaxonData taxonData, TreeVersion treeVersion, TreeVersionElement excluding) {
        //a name can't be in the tree already
        TreeVersionElement existingNameElement = findElementForNameLink(taxonData.nameLink, treeVersion)
        if (existingNameElement && existingNameElement.elementLink != excluding?.elementLink) {
            String message = "Can’t place this concept - ${taxonData.simpleName} is accepted concept **${existingNameElement.treeElement.displayHtml}**"
            throw new BadArgumentsException(message)
        }
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    void checkSynonymsOfNameNotOnTheTree(TaxonData taxonData, TreeVersion treeVersion, TreeVersionElement excluding) {
        List<Synonym> synonyms = taxonData.synonyms.filtered()
        synonyms.each { Synonym synonym ->
            TreeVersionElement tve = TreeVersionElement
                    .find('from TreeVersionElement tve where tve.treeVersion = :treeVersion and tve.treeElement.nameId = :nameId and tve.elementLink <> :excluding',
                            [treeVersion: treeVersion, nameId: synonym.nameId, excluding: excluding?.elementLink ?: ''])
            if (tve) {
                String message = "Can’t place this concept - synonym is accepted concept **${tve.treeElement.displayHtml}**"
                throw new BadArgumentsException("$message")
            }
        }
    }

    private void checkSynonymsAreNotSynonymsOnTheTree(TaxonData taxonData, TreeVersion treeVersion, List<TreeVersionElement> excluding) {
        //a name can't be already in the tree as a synonym
        List<Long> nameIdList = taxonData.synonyms.filtered().collect { it.nameId }
        List<Map> existingSynonyms = checkNameIdsAgainstAllSynonyms(nameIdList, treeVersion, excluding)
        if (!existingSynonyms.empty) {
            String message = "Can’t place this concept "
            existingSynonyms.each { Map s ->
                message += "- synonym ${s.synonym} is also a synonym of **${s.displayHtml}**\n"
            }
            throw new BadArgumentsException("$message")
        }
    }

    private void checkNameNotAnExistingSynonym(TaxonData taxonData, TreeVersion treeVersion, List<TreeVersionElement> excluding) {
        //a name can't be already in the tree as a synonym
        List<Map> existingSynonyms = checkNameIdsAgainstAllSynonyms(([taxonData.nameId] as List<Long>), treeVersion, excluding)
        if (!existingSynonyms.empty) {
            String message = "Can’t place this concept "
            existingSynonyms.each { Map s ->
                message += "- ${taxonData.simpleName} is in synonymy under accepted concept **${s.displayHtml}**\n"
            }
            throw new BadArgumentsException("$message")
        }
    }

    List<Map> checkNameIdsAgainstAllSynonyms(List<Long> nameIdList, TreeVersion treeVersion, List<TreeVersionElement> excluding, Sql sql = getSql()) {

        List<Map> synonymsFound = []

        String nameIds = nameIdList.join(',')

        if (!nameIds) {
            return []
        }

        String excludedLinks = "'" + excluding.collect { it.elementLink }.join("','") + "'"

        sql.eachRow("""
SELECT
  el.name_id as name_id,
  el.simple_name as simple_name,
  el.display_html as display_html,
  el.instance_id as instance_id,
  tax_syn ->> 'simple_name' as synonym,
  tax_syn ->> 'type' as syn_type,
  tax_syn ->> 'name_id' as syn_id,
  tve.element_link as element_link
FROM tree_element el join tree_version_element tve on el.id = tve.tree_element_id 
  JOIN name n ON el.name_id = n.id,
      jsonb_array_elements(synonyms -> 'list') AS tax_syn
WHERE tve.tree_version_id = :versionId
and synonyms is not null 
and synonyms ->> 'list' is not null
and tve.element_link not in ($excludedLinks)
      AND tax_syn ->> 'type' !~ '.*(misapp|pro parte|common|vernacular).*'
      AND (tax_syn ->> 'name_id'):: NUMERIC :: BIGINT in ($nameIds)""", [versionId: treeVersion.id]) { row ->
            synonymsFound << [nameId: row.name_id, simpleName: row.simple_name, displayHtml: row.display_html, synonym: row.synonym, type: row.syn_type, synonymId: row.syn_id, existing: row.element_link]
        }
        return synonymsFound
    }

    static checkPolynomialsBelowNameParent(String simpleName, Boolean excluded, NameRank taxonRank,
                                           String[] parentNameElements) {

        if (!excluded && RankUtils.rankLowerThan(taxonRank, 'Genus')) {
            //if this is a hybrid it takes the first part, if not it's just the name
            String firstNamePart = simpleName.split(' x ').first()
            String elementFound = parentNameElements.find { String nameElement ->
                firstNamePart.contains(nameElement)
            }
            if (!elementFound) {
                throw new BadArgumentsException("Name *$simpleName* is not under an appropriate parent name." +
                        "It should probably be under\n *${firstNamePart.split(' ').first()}* \n which isn't in this parents name path:\n\n" +
                        "* ${parentNameElements.join('\n* ')}")
            }
        }
    }

    static NameRank rankOfElement(Map rankPath, String elementName) {
        String rankName = rankPath.keySet().find { key ->
            (rankPath[key] as Map).name == elementName
        }
        NameRank rank = NameRank.findByName(rankName)
        return rank
    }

    TaxonData elementDataFromInstance(Instance instance) {

        //can't put relationship instances on a tree
        if (instance.instanceType.relationship) {
            return null
        }

        Synonyms synonyms = getSynonyms(instance)
        String synonymsHtml = getSynonymsHtmlViaDBFunction(instance.id)

        new TaxonData(
                nameId: instance.name.id,
                instanceId: instance.id,
                simpleName: instance.name.simpleName,
                nameElement: instance.name.nameElement,
                displayHtml: "<data>$instance.name.fullNameHtml <citation>$instance.reference.citationHtml</citation></data>",
                synonymsHtml: synonymsHtml,
                sourceShard: configService.nameSpaceName,
                synonyms: synonyms,
                rank: instance.name.nameRank.name,
                nameLink: linkService.getPreferredLinkForObject(instance.name),
                instanceLink: linkService.getPreferredLinkForObject(instance),
                nomInval: instance.name.nameStatus.nomInval,
                nomIlleg: instance.name.nameStatus.nomIlleg
        )
    }

    Synonyms getSynonyms(Instance instance) {
        Synonyms synonyms = new Synonyms()
        instance.instancesForCitedBy.each { Instance synonymInstance ->
            if (!synonymInstance.instanceType.unsourced) {
                synonyms.add(new Synonym(synonymInstance, linkService))
            }
        }
        return synonyms
    }

    /**
     * this is not meant to be used to set synonyms html but for testing.
     * @param instance
     * @param sql
     * @return
     */
    String getSynonymsHtmlViaDBFunction(Long instanceId, Sql sql = getSql()) {
        def row = sql.firstRow('''select coalesce(synonyms_as_html(:instanceId), '<synonyms></synonyms>');''', [instanceId: instanceId])
        return row[0]
    }

    TaxonData findInstanceByUri(String instanceUri) {
        Instance taxon = linkService.getObjectForLink(instanceUri) as Instance
        TaxonData instanceData
        if (taxon) {
            instanceData = elementDataFromInstance(taxon)
        } else {
            Map instanceDataMap = fetchInstanceData(instanceUri)
            if (instanceDataMap.success) {
                List<Map> synonymsDataList = instanceDataMap.data.synonyms.list as List
                instanceDataMap.data.synonyms = new Synonyms(synonymsDataList)
                instanceDataMap.data.profile = (instanceDataMap.data.profile as Map)
                instanceData = new TaxonData(instanceDataMap.data as Map)
            } else {
                instanceData = null
            }
        }
        return instanceData
    }

    TaxonData getInstanceDataByUri(String instanceUri) {
        Instance taxon = linkService.getObjectForLink(instanceUri) as Instance
        if (taxon) {
            return elementDataFromInstance(taxon)
        } else {
            return null
        }
    }

/**
 * Fetch instance data from another service
 * @param instanceUri
 * @return
 */
    private Map fetchInstanceData(String instanceUri) {
        Map result = [success: true]
        String uri = "$instanceUri/api/element-data-from-instance"
        try {
            String failMessage = "Couldn't fetch $uri"
            restCallService.json('get', uri,
                    { Map data ->
                        log.debug "Fetched $uri. Response: $data"
                        result.data = data.payload
                    },
                    { Map data, List errors ->
                        log.error "$failMessage. Errors: $errors"
                        result = [success: false, errors: errors]
                    },
                    { data ->
                        log.error "$failMessage. Not found response: $data"
                        result = [success: false, errors: ["$failMessage. Not found response: $data"]]
                    },
                    { data ->
                        log.error "$failMessage. Response: $data"
                        result = [success: false, errors: ["$failMessage. Response: $data"]]
                    }
            )
        } catch (RestCallException e) {
            log.error e.message
            result = [success: false, errors: "Communication error with mapper."]
        }
        return result
    }

    private Sql getSql() {
        return Sql.newInstance(dataSource)
    }

    List<TreeVersionElement> nameInAnyCurrentTree(Name name) {
        TreeVersionElement.executeQuery("""from TreeVersionElement tve 
            where (tve.treeVersion.published = false or tve.treeVersion = tve.treeVersion.tree.currentTreeVersion)
                and tve.treeElement.nameId = :id""", [id: name.id])
    }

    List<TreeVersionElement> instanceInAnyCurrentTree(Instance instance) {
        TreeVersionElement.executeQuery("""from TreeVersionElement tve 
            where (tve.treeVersion.published = false or tve.treeVersion = tve.treeVersion.tree.currentTreeVersion)
                and tve.treeElement.instanceId = :id""", [id: instance.id])
    }

    Map merge(TreeVersion draftVersion, MergeReport report, String userName) {
        if (draftVersion.published) {
            throw new ServiceException("Error: $userName tried to merge into published version of ${draftVersion.tree.name}")
        }
        if (draftVersion.tree.currentTreeVersion.id == draftVersion.previousVersion.id) {
            return [message: 'Nothing to do, merge report may be out of date?', merged: 0, complete: true, report: []]
        }

        if (report.from == draftVersion.tree.currentTreeVersion && report.to == draftVersion) {
            return doMerge(draftVersion, report, userName)
        } else {
            return [message: "Merge report is out of date. From and To versions don't match", merged: 0, complete: true, report: []]
        }
    }

    /**
     * merge the selected changes
     * collect all *useFrom* diffs and clear the from tve mergeConflict
     * collect all useTo removed - sort bottom up by treePath and call removeTreeVersionElement on fromTve if exists
     * collect all useTo added - sort top down by treePath and call placePublished on toTve
     * collect all useTo modified where tree_element differs between from and to tve and call changeElement
     * collect all useTo modified where tree_element is the same for from and to tve and update the placement/tve data(?)
     *
     * @param draftVersion
     * @param report
     * @param userName
     * @return
     */
    private Map doMerge(TreeVersion draftVersion, MergeReport report, String userName) {
        List<String> mergeLog = []

        // collect all *useFrom* diffs and clear the from tve mergeConflict
        report.getUseFrom().each { diff ->
            diff.from.mergeConflict = false
            mergeLog.add "Kept ${diff.from.treeElement.simpleName}: ${diff.from.elementLink}"
        }

        // collect all useTo removed - sort bottom up by treePath and call removeTreeVersionElement on fromTve if exists
        report.getUseToType(TveDiff.REMOVED)
              .sort { a, b -> b.to.namePath <=> a.to.namePath }
              .each { diff ->
                  if (diff.from) {
                      removeTreeVersionElement(diff.from)
                      mergeLog.add "Removed ${diff.to.treeElement.simpleName}"
                  }
              }

        // collect all useTo added - sort top down by treePath and call placePublished on toTve
        report.getUseToType(TveDiff.ADDED)
              .sort { a, b -> a.to.namePath <=> b.to.namePath }
              .each { diff ->
                  TreeVersionElement newTve = placePublishedTve(diff.to, draftVersion, userName)
                  mergeLog.add "Added ${newTve.treeElement.simpleName}: ${newTve.elementLink} "
              }

        // collect all useTo modified where tree_element differs between from and to tve and change the element
        // collect all useTo modified where tree_element is the same for from and to tve and update the placement/tve data(?)
        report.getUseToType(TveDiff.MODIFIED)
              .sort { a, b -> b.to.namePath <=> a.to.namePath }
              .each { diff ->
                  TreeVersionElement newTve = updateFromPublised(diff.from, diff.to, userName)
                  mergeLog.add "Updated ${newTve.treeElement.simpleName}: ${newTve.elementLink} "
              }

        List<TreeVersionElement> conflicted = TreeVersionElement.findAllByMergeConflictAndTreeVersion(true, draftVersion)
        if (conflicted.size()) {
            return [message: "Merge incomplete: merged ${mergeLog.size()} elements, ${conflicted.size()} conflicts remaining.", merged: mergeLog.size(), complete: false, report: mergeLog]
        }
        draftVersion.previousVersion = draftVersion.tree.currentTreeVersion
        draftVersion.save()
        return [message: "Merge complete: merged ${mergeLog.size()} elements, 0 conflicts remaining.", merged: mergeLog.size(), complete: true, report: mergeLog]

    }

    private TreeVersionElement placePublishedTve(TreeVersionElement publishedTve, TreeVersion draftVersion, String userName) {
        TreeVersionElement.withSession { s -> s.flush() } //flush before query
        TreeVersionElement draftParentTve = findElementForNameId(publishedTve.parent.treeElement.nameId, draftVersion)
        TreeVersionElement replacementTve = saveTreeVersionElement(publishedTve.treeElement, draftParentTve,
                draftVersion, publishedTve.taxonId, publishedTve.taxonLink, userName)
        return replacementTve
    }

    private TreeVersionElement updateFromPublised(TreeVersionElement currentTve, TreeVersionElement publishedTve, String userName) {
        notPublished(currentTve)
        TreeVersionElement.withSession { s -> s.flush() } //flush before query
        TreeVersionElement newParentTve = findElementForNameId(publishedTve.parent.treeElement.nameId, currentTve.treeVersion)

        Boolean elementChanged = currentTve.treeElement != publishedTve.treeElement
        if (elementChanged) {
            return copyPublishedTve(publishedTve, newParentTve, currentTve, userName)
        } else {
            return updateExistingTve(currentTve, newParentTve, userName)
        }
    }

    private TreeVersionElement updateExistingTve(TreeVersionElement currentTve, TreeVersionElement newParentTve, String userName) {
        String oldTreePath = currentTve.treePath
        String oldNamePath = currentTve.namePath
        TreeVersionElement oldParent = currentTve.parent
        currentTve.parent = newParentTve
        currentTve.treePath = makeTreePath(newParentTve, currentTve.treeElement)
        currentTve.namePath = makeNamePath(newParentTve, currentTve.treeElement)
        currentTve.depth = (newParentTve?.depth ?: 0) + 1
        currentTve.updatedBy = userName
        currentTve.updatedAt = new Timestamp(System.currentTimeMillis())

        sessionFactory.currentSession.flush()
        updateChildTreePath(currentTve.treePath, oldTreePath, currentTve.treeVersion)
        updateChildNamePath(currentTve.namePath, oldNamePath, currentTve.treeVersion)
        updateChildNameDepth(currentTve.namePath, currentTve.treeVersion)

        Set<TreeVersionElement> parents = getParentTreeVersionElements(newParentTve) + getParentTreeVersionElements(oldParent)
        updateParentTaxonId(parents, currentTve.treeVersion.hostPart())
        return currentTve
    }

    private TreeVersionElement copyPublishedTve(TreeVersionElement publishedTve, TreeVersionElement newParentTve, TreeVersionElement currentTve, String userName) {
        TreeVersionElement replacementTve = saveTreeVersionElement(
                publishedTve.treeElement,
                newParentTve,
                currentTve.treeVersion,
                publishedTve.taxonId,
                publishedTve.taxonLink,
                userName)
        updateParentId(currentTve, replacementTve)
        updateChildTreePath(replacementTve, currentTve)
        updateChildNamePath(replacementTve, currentTve)
        updateChildNameDepth(replacementTve)

        updateParentTaxonId(newParentTve)
        if (currentTve.parent && newParentTve != currentTve.parent) {
            updateParentTaxonId(currentTve.parent)
        }

        deleteTreeVersionElement(currentTve)
        return replacementTve
    }
}