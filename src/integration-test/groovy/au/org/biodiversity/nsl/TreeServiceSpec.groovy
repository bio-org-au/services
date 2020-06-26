package au.org.biodiversity.nsl

import grails.core.GrailsApplication
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.validation.ValidationException
import org.hibernate.SessionFactory
import org.hibernate.engine.spi.Status
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Timestamp

@Rollback
@Integration
class TreeServiceSpec extends Specification {

    GrailsApplication grailsApplication
    DataSource dataSource
    TreeService treeService
    SessionFactory sessionFactory

    def setup() {
        treeService.dataSource = dataSource
        treeService.configService = new ConfigService(grailsApplication: grailsApplication)
        treeService.linkService = Mock(LinkService)
        treeService.linkService.addTargetLink(_) >> 'Ignored mock'
        treeService.eventService = Mock(EventService)
        treeService.treeReportService = new TreeReportService()
        treeService.treeReportService.dataSource = dataSource
        treeService.linkService.getPreferredHost() >> 'http://localhost:7070/nsl-mapper'
        treeService.eventService.createDraftTreeEvent(_, _) >> { data, user ->
            return new EventRecord(data: data, dealtWith: false, updatedBy: user, createdBy: user)
        }
        println "\n---- $specificationContext.currentIteration.name ----\n"
    }

    def cleanup() {
    }

    void "test create new tree"() {
        given:
        int treeCount = Tree.count()

        when: 'I create a new unique tree'
        Tree tree = treeService.createNewTree('aTree', 'aGroup', null,
                '<p>A description</p>', 'http://trees.org/aTree', false)
        mockTxCommit()

        then: 'It should work'
        tree
        tree.name == 'aTree'
        tree.groupName == 'aGroup'
        tree.referenceId == null
        tree.currentTreeVersion == null
        tree.defaultDraftTreeVersion == null
        tree.id != null
        Tree.count() == treeCount + 1

        when: 'I try and create another tree with the same name'
        treeService.createNewTree('aTree', 'aGroup', null, '<p>A description</p>', 'http://trees.org/aTree', false)
        mockTxCommit()

        then: 'It will fail with an exception'
        thrown ObjectExistsException

        when: 'I try and create another tree with null name'
        treeService.createNewTree(null, 'aGroup', null, '<p>A description</p>', 'http://trees.org/aTree', false)
        mockTxCommit()

        then: 'It will fail with an exception'
        thrown ValidationException

        when: 'I try and create another tree with null group name'
        treeService.createNewTree('aNotherTree', null, null, '<p>A description</p>', 'http://trees.org/aTree', false)
        mockTxCommit()

        then: 'It will fail with an exception'
        thrown ValidationException

        when: 'I try and create another tree with reference ID'
        Tree tree2 = treeService.createNewTree('aNotherTree', 'aGroup', 12345l, '<p>A description</p>', 'http://trees.org/aTree', false)
        mockTxCommit()

        then: 'It will work'
        tree2
        tree2.name == 'aNotherTree'
        tree2.groupName == 'aGroup'
        tree2.referenceId == 12345l
        tree2.hostName == 'http://localhost:7070/nsl-mapper'
        Tree.count() == treeCount + 2
    }

    void "Test editing tree"() {
        given:
        Tree atree = makeATestTree() //new Tree(name: 'aTree', groupName: 'aGroup').save()
        Tree btree = makeBTestTree() //new Tree(name: 'b tree', groupName: 'aGroup').save()
        Long treeId = atree.id

        expect:
        atree
        treeId
        atree.name == 'aTree'
        btree
        btree.name == 'bTree'
        Tree.findByName('aTree') == atree
        Tree.findByName('bTree') == btree

        when: 'I change the name of a tree'
        Tree tree2 = treeService.editTree(atree, 'A new name', atree.groupName, 123456, '<p>A description</p>', 'http://trees.org/aTree', false)
        mockTxCommit()

        then: 'The name and referenceID are changed'
        atree == tree2
        atree.name == 'A new name'
        atree.groupName == 'aGroup'
        atree.referenceId == 123456

        when: 'I change nothing'

        Tree tree3 = treeService.editTree(atree, 'A new name', atree.groupName, 123456, '<p>A description</p>', 'http://trees.org/aTree', false)
        mockTxCommit()

        then: 'everything remains the same'
        atree == tree3
        atree.name == 'A new name'
        atree.groupName == 'aGroup'
        atree.referenceId == 123456

        when: 'I change the group and referenceId'

        Tree tree4 = treeService.editTree(atree, atree.name, 'A different group', null, '<p>A description</p>', 'http://trees.org/aTree', false)
        mockTxCommit()

        then: 'changes as expected'
        atree == tree4
        atree.name == 'A new name'
        atree.groupName == 'A different group'
        atree.referenceId == null

        when: 'I give a null name'

        treeService.editTree(atree, null, atree.groupName, null, '<p>A description</p>', 'http://trees.org/aTree', false)
        mockTxCommit()

        then: 'I get a bad argument exception'
        thrown BadArgumentsException

        when: 'I give a null group name'

        treeService.editTree(atree, atree.name, null, null, '<p>A description</p>', 'http://trees.org/aTree', false)
        mockTxCommit()

        then: 'I get a bad argument exception'
        thrown BadArgumentsException

        when: 'I give a name that is the same as another tree'

        treeService.editTree(atree, btree.name, atree.groupName, null, '<p>A description</p>', 'http://trees.org/aTree', false)
        mockTxCommit()

        then: 'I get a object exists exception'
        thrown ObjectExistsException
    }

    def "test creating a tree version"() {
        given:
        Tree tree = makeATestTree()
        treeService.linkService.bulkAddTargets(_) >> [success: true]
        expect:
        tree

        when: 'I create a new version on a new tree without a version'
        TreeVersion version = treeService.createTreeVersion(tree, null, 'my first draft', 'irma', 'This is a log entry')
        mockTxCommit()

        then: 'A new version is created on that tree'
        version
        version.tree == tree
        version.draftName == 'my first draft'
        tree.treeVersions.contains(version)

        when: 'I add some test elements to the version'
        List<TreeElement> testElements = TreeTstHelper.makeTestElements(version, TreeTstHelper.testElementData(), TreeTstHelper.testTreeVersionElementData())
        println version.treeVersionElements

        then: 'It should have 60 tree elements'
        testElements.size() == 60
        version.treeVersionElements.size() == 60
        version.treeVersionElements.contains(TreeVersionElement.findByTreeElementAndTreeVersion(testElements[3], version))
        version.treeVersionElements.contains(TreeVersionElement.findByTreeElementAndTreeVersion(testElements[13], version))
        version.treeVersionElements.contains(TreeVersionElement.findByTreeElementAndTreeVersion(testElements[23], version))

        when: 'I make a new version from this version'
        TreeVersion version2 = treeService.createTreeVersion(tree, version, 'my second draft', 'irma', 'This is a log entry')
        mockTxCommit()
        println version2.treeVersionElements

        then: 'It should copy the elements and set the previous version'
        version2
        version2.draftName == 'my second draft'
        version != version2
        version.id != version2.id
        version2.previousVersion == version
        version2.treeVersionElements.size() == 60
        versionsAreEqual(version, version2)
        version2.treeVersionElements.contains(TreeVersionElement.findByTreeElementAndTreeVersion(testElements[3], version2))
        version2.treeVersionElements.contains(TreeVersionElement.findByTreeElementAndTreeVersion(testElements[13], version2))
        version2.treeVersionElements.contains(TreeVersionElement.findByTreeElementAndTreeVersion(testElements[23], version2))

        when: 'I publish a draft version'
        TreeVersion version2published = treeService.publishTreeVersion(version2, 'testy mctestface', 'Publishing draft as a test')
        mockTxCommit()

        then: 'It should be published and set as the current version on the tree'
        version2published
        version2published.published
        version2published == version2
        version2published.logEntry == 'Publishing draft as a test'
        version2published.publishedBy == 'testy mctestface'
        tree.currentTreeVersion == version2published

        when: 'I create a default draft'
        TreeVersion draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my default draft', 'irma', 'This is a log entry')
        mockTxCommit()

        then: 'It copies the current version and sets it as the defaultDraft'
        draftVersion
        draftVersion != tree.currentTreeVersion
        tree.defaultDraftTreeVersion == draftVersion
        draftVersion.previousVersion == version2published
        draftVersion.treeVersionElements.size() == 60
        versionsAreEqual(version2, draftVersion)
        draftVersion.treeVersionElements.contains(TreeVersionElement.findByTreeElementAndTreeVersion(testElements[3], draftVersion))
        draftVersion.treeVersionElements.contains(TreeVersionElement.findByTreeElementAndTreeVersion(testElements[13], draftVersion))
        draftVersion.treeVersionElements.contains(TreeVersionElement.findByTreeElementAndTreeVersion(testElements[23], draftVersion))

        when: 'I set the first draft version as the default'
        TreeVersion draftVersion2 = treeService.setDefaultDraftVersion(version)
        mockTxCommit()

        then: 'It replaces draftVersion as the defaultDraft'
        draftVersion2
        draftVersion2 == version
        tree.defaultDraftTreeVersion == draftVersion2

        when: 'I try and set a published version as the default draft'
        treeService.setDefaultDraftVersion(version2published)
        mockTxCommit()

        then: 'It fails with bad argument'
        thrown BadArgumentsException
    }

    private static Boolean versionsAreEqual(TreeVersion v1, TreeVersion v2) {
        v1.treeVersionElements.size() == v2.treeVersionElements.size() &&
                v1.treeVersionElements.collect { it.treeElement.id }.containsAll(v2.treeVersionElements.collect {
                    it.treeElement.id
                }) &&
                v1.treeVersionElements.collect { it.taxonId }.containsAll(v2.treeVersionElements.collect { it.taxonId })
    }

    def "test making and deleting a tree"() {
        given:
        treeService.linkService.bulkAddTargets(_) >> [success: true]
        treeService.linkService.bulkRemoveTargets(_) >> [success: true]
        Tree tree = makeATestTree()
        TreeVersion draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my default draft', 'irma', 'This is a log entry')
        mockTxCommit()
        TreeTstHelper.makeTestElements(draftVersion, TreeTstHelper.testElementData(), TreeTstHelper.testTreeVersionElementData())
        TreeVersion publishedVersion = treeService.publishTreeVersion(draftVersion, 'tester', 'publishing to delete')
        mockTxCommit()
        draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my next draft', 'irma', 'This is a log entry')
        mockTxCommit()

        expect:
        tree
        draftVersion
        draftVersion.treeVersionElements.size() == 60
        publishedVersion
        publishedVersion.treeVersionElements.size() == 60
        tree.defaultDraftTreeVersion == draftVersion
        tree.currentTreeVersion == publishedVersion

        when: 'I delete the tree'
        treeService.deleteTree(tree)
        mockTxCommit()

        then: 'I get a published version exception'
        thrown(PublishedVersionException)

        when: 'I unpublish the published version and then delete the tree'
        publishedVersion.published = false
        publishedVersion.save()
        treeService.deleteTree(tree)
        mockTxCommit()

        then: "the tree, it's versions and their elements are gone"
        Tree.get(tree.id) == null
        Tree.findByName('aTree') == null
        TreeVersion.get(draftVersion.id) == null
        TreeVersion.get(publishedVersion.id) == null
        TreeVersionElement.findByTreeVersion(draftVersion) == null
    }

    def "test making and deleting a draft tree version"() {
        given:
        treeService.linkService.bulkAddTargets(_) >> [success: true]
        treeService.linkService.bulkRemoveTargets(_) >> [success: true]
        Tree tree = makeATestTree()
        TreeVersion draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my default draft', 'irma', 'This is a log entry')
        mockTxCommit()
        TreeTstHelper.makeTestElements(draftVersion, TreeTstHelper.testElementData(), TreeTstHelper.testTreeVersionElementData())
        TreeVersion publishedVersion = treeService.publishTreeVersion(draftVersion, 'tester', 'publishing to delete')
        mockTxCommit()
        draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my next draft', 'irma', 'This is a log entry')
        mockTxCommit()

        expect:
        tree
        draftVersion
        draftVersion.treeVersionElements.size() == 60
        publishedVersion
        publishedVersion.treeVersionElements.size() == 60
        tree.defaultDraftTreeVersion == draftVersion
        tree.currentTreeVersion == publishedVersion
        versionsAreEqual(publishedVersion, draftVersion)

        when: 'I delete the tree version'
        tree = treeService.deleteTreeVersion(draftVersion)
        mockTxCommit()
        publishedVersion.refresh() //the refresh() is required by deleteTreeVersion

        then: "the draft version and it's elements are gone"
        tree
        tree.currentTreeVersion == publishedVersion
        //the below should no longer exist.
        tree.defaultDraftTreeVersion == null
        TreeVersion.get(draftVersion.id) == null
        TreeVersionElement.executeQuery('select element from TreeVersionElement element where treeVersion.id = :draftVersionId',
                [draftVersionId: draftVersion.id]).empty
    }

    def "test getting synonyms from instance"() {
        given:
        Tree tree = Tree.findByName('APC')
        TreeVersion treeVersion = tree.currentTreeVersion
        Instance ficusVirens = Instance.get(781547)
        treeService.linkService.getPreferredLinkForObject(_) >> {
            String url = "http://localhost:7070/nsl-mapper/${it[0].class.simpleName.toLowerCase()}/apni/${it[0].id}"
            println url
            return url
        }

        expect:
        tree
        treeVersion
        ficusVirens

        when: 'I get element data for ficus virens'
        TaxonData taxonData = treeService.elementDataFromInstance(ficusVirens)
        mockTxCommit()
        println taxonData.synonymsHtml
        println taxonData.synonyms.asMap()

        then: 'I get 20 synonyms'
        taxonData.synonyms.size() == 20
        taxonData.synonymsHtml.startsWith('<synonyms><tax><scientific><name data-id=\'90571\'><scientific><name data-id=\'73030\'>')
    }

    def "test check synonyms"() {
        given:
        Tree tree = Tree.findByName('APC')
        TreeVersion treeVersion = tree.currentTreeVersion
        Instance ficusVirensSublanceolata = Instance.get(692695)
        treeService.linkService.getPreferredLinkForObject(_) >> {
            String url = "http://localhost:7070/nsl-mapper/${it[0].class.simpleName.toLowerCase()}/apni/${it[0].id}"
            println url
            return url
        }

        expect:
        tree
        treeVersion
        ficusVirensSublanceolata

        when: 'I try to place Ficus virens var. sublanceolata sensu Jacobs & Packard (1981)'
        TaxonData taxonData = treeService.elementDataFromInstance(ficusVirensSublanceolata)
        List<Long> nameIdList = taxonData.synonyms.filtered().collect { it.nameId } + [taxonData.nameId]
        List<Map> existingSynonyms = treeService.checkNameIdsAgainstAllSynonyms(nameIdList, treeVersion, [])
        println existingSynonyms

        then: 'I get two found synonyms'
        existingSynonyms != null
        !existingSynonyms.empty
        existingSynonyms.size() == 2
        existingSynonyms.first().simpleName == 'Ficus virens'
    }

    def "test check synonyms of this taxon not on tree"() {
        given:
        Tree tree = Tree.findByName('APC')
        TreeVersion treeVersion = tree.currentTreeVersion
        Instance xanthosiaPusillaBunge = Instance.get(712692)
        treeService.linkService.getPreferredLinkForObject(_) >> {
            String url = "http://localhost:7070/nsl-mapper/${it[0].class.simpleName.toLowerCase()}/apni/${it[0].id}"
            println url
            return url
        }

        expect:
        tree
        treeVersion
        xanthosiaPusillaBunge

        when: 'I try to place Xanthosia pusilla Bunge'
        TaxonData taxonData = treeService.elementDataFromInstance(xanthosiaPusillaBunge)
        treeService.checkSynonymsOfNameNotOnTheTree(taxonData, treeVersion, null)

        then: 'I get en error'
        def e = thrown(BadArgumentsException)
        println e.message
        e.message.startsWith("Can’t place this concept - synonym")
        e.message.contains("Xanthosia")
        e.message.contains("tasmanica")
    }

    def "test check validation, relationship instance"() {
        when: "I try to get taxonData for a relationship instance"
        Instance relationshipInstance = Instance.get(889353)
        TaxonData taxonData = treeService.elementDataFromInstance(relationshipInstance)

        then: 'I get null'
        taxonData == null
    }

    def "test check validation, existing instance"() {
        given:
        Tree tree = makeATestTree()
        TreeVersion draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my default draft', 'irma', 'This is a log entry')
        mockTxCommit()
        TreeTstHelper.makeTestElements(draftVersion,
                [TreeTstHelper.blechnaceaeElementData,
                 TreeTstHelper.doodiaElementData,
                 TreeTstHelper.asperaElementData],
                [TreeTstHelper.blechnaceaeTVEData,
                 TreeTstHelper.doodiaTVEData,
                 TreeTstHelper.asperaTVEData])
        Instance asperaInstance = Instance.get(781104)
        TreeVersionElement doodiaElement = treeService.findElementBySimpleName('Doodia', draftVersion)
        TreeVersionElement asperaElement = treeService.findElementBySimpleName('Doodia aspera', draftVersion)
        treeService.linkService.getPreferredLinkForObject(_) >> {
            String url = "http://localhost:7070/nsl-mapper/${it[0].class.simpleName.toLowerCase()}/apni/${it[0].id}"
            println url
            return url
        }
        mockTxCommit()

        expect:
        tree
        draftVersion
        doodiaElement
        asperaInstance
        asperaElement

        when: 'I try to place Doodia aspera'
        TaxonData taxonData = treeService.elementDataFromInstance(asperaInstance)
        treeService.validateNewElementPlacement(doodiaElement, taxonData)

        then: 'I get bad argument, instance already on the tree'
        def e = thrown(BadArgumentsException)
        e.message.startsWith("Can’t place this concept")
        e.message.contains("Doodia")
        e.message.contains("aspera")

    }

    def "test check validation, ranked above parent"() {
        given:
        Tree tree = makeATestTree()
        TreeVersion draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my default draft', 'irma', 'This is a log entry')
        mockTxCommit()
        TreeTstHelper.makeTestElements(draftVersion, [TreeTstHelper.blechnaceaeElementData, TreeTstHelper.asperaElementData],
                [TreeTstHelper.blechnaceaeTVEData, TreeTstHelper.asperaTVEData])
        Instance doodiaInstance = Instance.get(578615)
        TreeVersionElement blechnaceaeElement = treeService.findElementBySimpleName('Blechnaceae', draftVersion)
        TreeVersionElement asperaElement = treeService.findElementBySimpleName('Doodia aspera', draftVersion)

        //these shouldn't matter so long as they're not on the draft tree
        treeService.linkService.getPreferredLinkForObject(doodiaInstance.name) >> 'http://blah/name/apni/70914'
        treeService.linkService.getPreferredLinkForObject(doodiaInstance) >> 'http://blah/instance/apni/578615'

        expect:
        tree
        draftVersion
        draftVersion.treeVersionElements.size() == 2
        tree.defaultDraftTreeVersion == draftVersion
        blechnaceaeElement
        asperaElement
        doodiaInstance

        when: 'I try to place Doodia under Doodia aspera '
        TaxonData taxonData = treeService.elementDataFromInstance(doodiaInstance)
        treeService.validateNewElementPlacement(asperaElement, taxonData)

        then: 'I get bad argument, doodia aspera ranked below doodia'
        def e = thrown(BadArgumentsException)
        e.message == 'Name Doodia of rank Genus is not below rank Species of Doodia aspera.'

        when: 'I try to place Doodia under Blechnaceae'
        taxonData = treeService.elementDataFromInstance(doodiaInstance)
        treeService.validateNewElementPlacement(blechnaceaeElement, taxonData)

        then: 'it should work'
        notThrown(BadArgumentsException)
    }

    def "test check validation, nomIlleg nomInval"() {
        given:
        Tree tree = makeATestTree()
        TreeVersion draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my default draft', 'irma', 'This is a log entry')
        mockTxCommit()
        TreeTstHelper.makeTestElements(draftVersion, [TreeTstHelper.blechnaceaeElementData, TreeTstHelper.asperaElementData],
                [TreeTstHelper.blechnaceaeTVEData, TreeTstHelper.asperaTVEData])
        Instance doodiaInstance = Instance.get(578615)
        TreeVersionElement blechnaceaeElement = treeService.findElementBySimpleName('Blechnaceae', draftVersion)

        //these shouldn't matter so long as they're not on the draft tree
        treeService.linkService.getPreferredLinkForObject(doodiaInstance.name) >> 'http://blah/name/apni/70914'
        treeService.linkService.getPreferredLinkForObject(doodiaInstance) >> 'http://blah/instance/apni/578615'

        expect:
        tree
        draftVersion
        draftVersion.treeVersionElements.size() == 2
        tree.defaultDraftTreeVersion == draftVersion
        blechnaceaeElement
        doodiaInstance

        when: 'I try to place a nomIlleg name'
        def taxonData = treeService.elementDataFromInstance(doodiaInstance)
        taxonData.nomIlleg = true
        List<String> warnings = treeService.validateNewElementPlacement(blechnaceaeElement, taxonData)

        then: 'I get a warning, nomIlleg'
        warnings
        warnings.first() == 'Doodia is nomIlleg'

        when: 'I try to place a nomInval name'
        taxonData.nomIlleg = false
        taxonData.nomInval = true
        warnings = treeService.validateNewElementPlacement(blechnaceaeElement, taxonData)

        then: 'I get a warning, nomInval'
        warnings
        warnings.first() == 'Doodia is nomInval'

    }

    def "test check validation, existing name"() {
        given:
        Tree tree = makeATestTree()
        TreeVersion draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my default draft', 'irma', 'This is a log entry')
        mockTxCommit()
        TreeTstHelper.makeTestElements(draftVersion, [TreeTstHelper.blechnaceaeElementData, TreeTstHelper.doodiaElementData, TreeTstHelper.asperaElementData],
                [TreeTstHelper.blechnaceaeTVEData, TreeTstHelper.doodiaTVEData, TreeTstHelper.asperaTVEData])
        Instance asperaInstance = Instance.get(781104)
        TreeVersionElement doodiaElement = treeService.findElementBySimpleName('Doodia', draftVersion)
        TreeVersionElement asperaElement = treeService.findElementBySimpleName('Doodia aspera', draftVersion)
        treeService.linkService.getPreferredLinkForObject(_) >> {
            String url = "http://localhost:7070/nsl-mapper/${it[0].class.simpleName.toLowerCase()}/apni/${it[0].id}"
            println url
            return url
        }

        expect:
        tree
        draftVersion
        doodiaElement
        asperaInstance
        asperaElement

        when: 'I try to place Doodia aspera under Doodia'
        TaxonData taxonData = treeService.elementDataFromInstance(asperaInstance)
        treeService.validateNewElementPlacement(doodiaElement, taxonData)

        then: 'I get bad argument, name is already on the tree'
        def e = thrown(BadArgumentsException)
        e.message.startsWith("Can’t place this concept - <data><scientific><name data-id='70944'>")
    }

    def "test place taxon"() {
        given:
        treeService.linkService.bulkAddTargets(_) >> [success: true]
        Tree tree = makeATestTree()
        TreeVersion draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my default draft', 'irma', 'This is a log entry')
        mockTxCommit()
        TreeTstHelper.makeTestElements(draftVersion, [TreeTstHelper.blechnaceaeElementData, TreeTstHelper.doodiaElementData], [TreeTstHelper.blechnaceaeTVEData, TreeTstHelper.doodiaTVEData])
        treeService.publishTreeVersion(draftVersion, 'testy mctestface', 'Publishing draft as a test')
        mockTxCommit()
        draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my new default draft', 'irma', 'This is a log entry')
        mockTxCommit()

        Instance asperaInstance = Instance.get(781104)
        TreeVersionElement blechnaceaeElement = treeService.findElementBySimpleName('Blechnaceae', draftVersion)
        TreeVersionElement doodiaElement = treeService.findElementBySimpleName('Doodia', draftVersion)
        TreeVersionElement nullAsperaElement = treeService.findElementBySimpleName('Doodia aspera', draftVersion)
        String instanceUri = 'http://localhost:7070/nsl-mapper/instance/apni/781104'
        Long blechnaceaeTaxonId = blechnaceaeElement.taxonId
        Long doodiaTaxonId = doodiaElement.taxonId
        treeService.linkService.getPreferredLinkForObject(_) >> {
            String url = "http://localhost:7070/nsl-mapper/${it[0].class.simpleName.toLowerCase()}/apni/${it[0].id}"
            println url
            return url
        }

        println TreeVersionElement.findAllByTaxonId(blechnaceaeElement.taxonId)
        printTveAndCountChildren(blechnaceaeElement)

        expect:
        tree
        draftVersion
        blechnaceaeElement
        blechnaceaeTaxonId
        TreeVersionElement.findAllByTaxonId(blechnaceaeElement.taxonId).size() > 1
        TreeVersionElement.countByTaxonId(blechnaceaeElement.taxonId) > 1
        doodiaElement
        doodiaTaxonId
        asperaInstance
        !nullAsperaElement

        when: 'I try to place Doodia aspera under Doodia'
        Map result = treeService.placeTaxonUri(doodiaElement, instanceUri, false, null, 'A. User')
        mockTxCommit()
        println result

        then: 'It should work'
        1 * treeService.linkService.getObjectForLink(instanceUri) >> asperaInstance
        1 * treeService.linkService.addTargetLink(_) >> { TreeVersionElement tve -> "http://localhost:7070/nsl-mapper/tree/$tve.treeVersion.id/$tve.treeElement.id" }
        3 * treeService.linkService.addTaxonIdentifier(_) >> { TreeVersionElement tve ->
            println "Adding taxonIdentifier for $tve"
            "http://localhost:7070/nsl-mapper/taxon/apni/$tve.taxonId"
        }
        result.childElement == treeService.findElementBySimpleName('Doodia aspera', draftVersion)
        result.warnings.empty
        //taxon id should be set to a unique/new positive value
        result.childElement.taxonId != 0
        TreeVersionElement.countByTaxonId(result.childElement.taxonId) == 1
        result.childElement.taxonLink == "/taxon/apni/${result.childElement.taxonId}"
        //taxon id for the taxon above has changed to new IDs
        blechnaceaeElement.taxonId != 0
        blechnaceaeElement.taxonId != blechnaceaeTaxonId
        TreeVersionElement.countByTaxonId(blechnaceaeElement.taxonId) == 1
        blechnaceaeElement.taxonLink == "/taxon/apni/$blechnaceaeElement.taxonId"
        doodiaElement.taxonId != 0
        doodiaElement.taxonId != doodiaTaxonId
        TreeVersionElement.countByTaxonId(doodiaElement.taxonId) == 1
        doodiaElement.taxonLink == "/taxon/apni/$doodiaElement.taxonId"

        println result.childElement.elementLink
    }

    private TreeVersion setupTree() {
        Tree tree = makeATestTree()
        treeService.linkService.bulkAddTargets(_) >> [success: true]
        TreeVersion draftVersion = treeService.createTreeVersion(tree, null, 'my first draft', 'irma', 'This is a log entry')
        mockTxCommit()
        List<TreeElement> testElements = TreeTstHelper.makeTestElements(draftVersion, TreeTstHelper.testElementData(), TreeTstHelper.testTreeVersionElementData())
        treeService.publishTreeVersion(draftVersion, 'testy mctestface', 'Publishing draft as a test')
        mockTxCommit()
        draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my new default draft', 'irma', 'This is a log entry')
        mockTxCommit()
        return draftVersion
    }

    def "test replace a taxon"() {
        given: "we make a test tree"
        TreeVersion draftVersion = setupTree()

        expect: "sane results"
        draftVersion.treeVersionElements.size() == 60
        !draftVersion.published

        when: "we get some exisitng elements"
        TreeVersionElement AlseuosmiaceaeTve = treeService.findElementBySimpleName('Alseuosmiaceae', draftVersion)
        TreeVersionElement CrispilobaTve = treeService.findElementBySimpleName('Crispiloba', draftVersion)
        TreeVersionElement ArgophyllaceaeTve = treeService.findElementBySimpleName('Argophyllaceae', draftVersion)

        then: "they do exist"
        AlseuosmiaceaeTve
        AlseuosmiaceaeTve.treeElement
        AlseuosmiaceaeTve.treeElement.name
        CrispilobaTve
        CrispilobaTve.treeElement
        CrispilobaTve.treeElement.name
        CrispilobaTve.parent == AlseuosmiaceaeTve
        ArgophyllaceaeTve

        when: "we use the elements fot get some data"
        List<Long> originalArgophyllaceaeParentTaxonIDs = treeService.getParentTreeVersionElements(ArgophyllaceaeTve)
                                                                     .collect { it.taxonId }
        List<Long> originalAlseuosmiaceaeParentTaxonIDs = treeService.getParentTreeVersionElements(AlseuosmiaceaeTve)
                                                                     .collect { it.taxonId }
        Instance replacementCrispilobaInstance = Instance.get(50729719)
        TreeElement CrispilobaTe = CrispilobaTve.treeElement
        Long ArgophyllaceaeInitialTaxonId = ArgophyllaceaeTve.taxonId

        printTveAndCountChildren(ArgophyllaceaeTve)
        printTveAndCountChildren(AlseuosmiaceaeTve)

        then: "we get the expected results"
        originalArgophyllaceaeParentTaxonIDs.size() == 2 //fragment of tree, only two parents
        originalAlseuosmiaceaeParentTaxonIDs.size() == 2
        replacementCrispilobaInstance
        CrispilobaTe
        ArgophyllaceaeInitialTaxonId
        treeService.treeReportService

        when: 'I try to move a taxon, Crispiloba under Argophyllaceae'
        Map result = treeService.replaceTaxon(CrispilobaTve, ArgophyllaceaeTve,
                'http://localhost:7070/nsl-mapper/instance/apni/50729719',
                CrispilobaTve.treeElement.excluded,
                CrispilobaTve.treeElement.profile,
                'test move taxon')
        mockTxCommit()
        draftVersion.refresh()

        List<TreeVersionElement> CrispilobaChildren = treeService.getAllChildElements(result.replacementElement)
        List<TreeVersionElement> ArgophyllaceaeChildren = treeService.getAllChildElements(ArgophyllaceaeTve)

        printTveAndCountChildren(ArgophyllaceaeTve)
        printTveAndCountChildren(AlseuosmiaceaeTve)

        then: 'It works'
        1 * treeService.linkService.bulkRemoveTargets(_) >> { List<TreeVersionElement> elements ->
            [success: true]
        }
        1 * treeService.linkService.getObjectForLink(_) >> replacementCrispilobaInstance
        1 * treeService.linkService.getPreferredLinkForObject(replacementCrispilobaInstance.name) >> 'http://localhost:7070/nsl-mapper/name/apni/121601'
        1 * treeService.linkService.getPreferredLinkForObject(replacementCrispilobaInstance) >> 'http://localhost:7070/nsl-mapper/instance/apni/50729719'
        1 * treeService.linkService.addTargetLink(_) >> { TreeVersionElement tve -> "http://localhost:7070/nsl-mapper/tree/$tve.treeVersion.id/$tve.treeElement.id" }
        // Crispiloba, Argophyllaceae, Asterales, Alseuosmiaceae get new taxon identifiers
        4 * treeService.linkService.addTaxonIdentifier(_) >> { TreeVersionElement tve ->
            println "Adding taxonIdentifier for $tve"
            "http://localhost:7070/nsl-mapper/taxon/apni/$tve.taxonId"
        }
        deleted(CrispilobaTve) //deleted
        !deleted(CrispilobaTe) // not deleted because it's referenced elsewhere
        result.replacementElement
        result.replacementElement == treeService.findElementBySimpleName('Crispiloba', draftVersion)
        result.replacementElement.treeVersion == draftVersion
        result.replacementElement.treeElement != CrispilobaTe
        ArgophyllaceaeTve.taxonId != ArgophyllaceaeInitialTaxonId
        draftVersion.treeVersionElements.size() == 60
        CrispilobaChildren.size() == 1 //Crispiloba/disperma
        result.replacementElement.parent == ArgophyllaceaeTve
        CrispilobaChildren[0].treeElement.nameElement == 'disperma'
        CrispilobaChildren[0].parent == result.replacementElement
        ArgophyllaceaeChildren.containsAll(CrispilobaChildren)
        // all the parent taxonIds should have been updated
        !treeService.getParentTreeVersionElements(ArgophyllaceaeTve).collect { it.taxonId }.find {
            originalArgophyllaceaeParentTaxonIDs.contains(it)
        }
        !treeService.getParentTreeVersionElements(AlseuosmiaceaeTve).collect { it.taxonId }.find {
            originalAlseuosmiaceaeParentTaxonIDs.contains(it)
        }

        when: 'I publish the version then try a move'
        treeService.publishTreeVersion(draftVersion, 'tester', 'publishing to delete')
        mockTxCommit()
        treeService.replaceTaxon(CrispilobaTve, AlseuosmiaceaeTve,
                'http://localhost:7070/nsl-mapper/instance/apni/50729719',
                true,
                [:],
                'test move taxon')
        mockTxCommit()

        then: 'I get a PublishedVersionException'
        thrown(PublishedVersionException)
    }

    def "test replace a taxon with multiple child levels"() {
        given: "we make a test tree"
        TreeVersion draftVersion = setupTree()

        expect: "sane results"
        draftVersion.treeVersionElements.size() == 60
        !draftVersion.published

        when: "we get some exisitng elements"
        TreeVersionElement AdenostemmaTve = treeService.findElementBySimpleName('Adenostemma', draftVersion)
        TreeVersionElement ArgophyllaceaeTve = treeService.findElementBySimpleName('Argophyllaceae', draftVersion)
        TreeVersionElement AsteraceaeTve = treeService.findElementBySimpleName('Asteraceae', draftVersion)

        then: "they exist"
        AdenostemmaTve
        ArgophyllaceaeTve
        AsteraceaeTve

        when: "we use the elements to get some data"
        List<Long> originalArgophyllaceaeTaxonIDs = treeService.getParentTreeVersionElements(ArgophyllaceaeTve).collect {
            it.taxonId
        }
        List<Long> originalAsteraceaeTaxonIDs = treeService.getParentTreeVersionElements(AsteraceaeTve).collect {
            it.taxonId
        }
        Instance replacementAdenostemmaInstance = Instance.get(489499)

        then: "we get the expected results"
        replacementAdenostemmaInstance
        AdenostemmaTve.parent == AsteraceaeTve
        originalArgophyllaceaeTaxonIDs.size() == 2
        originalAsteraceaeTaxonIDs.size() == 2
        printTveAndCountChildren(AdenostemmaTve) == 4
        printTveAndCountChildren(AsteraceaeTve) == 42
        printTveAndCountChildren(ArgophyllaceaeTve) == 10

        when: 'I move Adenostemma under Argophyllaceae'
        Map result = treeService.replaceTaxon(AdenostemmaTve, ArgophyllaceaeTve,
                'http://localhost:7070/nsl-mapper/instance/apni/753978',
                AdenostemmaTve.treeElement.excluded,
                AdenostemmaTve.treeElement.profile,
                'test move taxon')
        mockTxCommit()

        TreeVersionElement replacementAdenostemma = result.replacementElement
        draftVersion.refresh()

        then: 'It works'
        1 * treeService.linkService.bulkRemoveTargets(_) >> { List<TreeVersionElement> elements ->
            [success: true]
        }
        1 * treeService.linkService.getObjectForLink(_) >> replacementAdenostemmaInstance
        1 * treeService.linkService.getPreferredLinkForObject(replacementAdenostemmaInstance.name) >> 'http://localhost:7070/nsl-mapper/name/apni/142301'
        1 * treeService.linkService.getPreferredLinkForObject(replacementAdenostemmaInstance) >> 'http://localhost:7070/nsl-mapper/instance/apni/753978'
        1 * treeService.linkService.addTargetLink(_) >> { TreeVersionElement tve -> "http://localhost:7070/nsl-mapper/tree/$tve.treeVersion.id/$tve.treeElement.id" }
        //  Argophyllaceae, Argophyllaceae, Asterales, Asteraceae
        4 * treeService.linkService.addTaxonIdentifier(_) >> { TreeVersionElement tve ->
            println "Adding taxonIdentifier for $tve"
            "http://localhost:7070/nsl-mapper/taxon/apni/$tve.taxonId"
        }

        deleted(AdenostemmaTve) //deleted
        result.replacementElement
        result.replacementElement == treeService.findElementBySimpleName('Adenostemma', draftVersion)
        result.replacementElement.treeVersion == draftVersion

        draftVersion.treeVersionElements.size() == 60
        printTveAndCountChildren(replacementAdenostemma) == 4
        printTveAndCountChildren(ArgophyllaceaeTve) == 15
        printTveAndCountChildren(AsteraceaeTve) == 37
        // all the parent taxonIds should have been updated
        !treeService.getParentTreeVersionElements(ArgophyllaceaeTve).collect { it.taxonId }.find {
            originalArgophyllaceaeTaxonIDs.contains(it)
        }
        !treeService.getParentTreeVersionElements(AsteraceaeTve).collect { it.taxonId }.find {
            originalAsteraceaeTaxonIDs.contains(it)
        }

    }

    def "test place taxon without a parent"() {
        given:
        treeService.linkService.bulkAddTargets(_) >> [success: true]
        Tree tree = makeATestTree()
        TreeVersion draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my default draft', 'irma', 'This is a log entry')
        mockTxCommit()

        Instance asperaInstance = Instance.get(781104)
        String instanceUri = 'http://localhost:7070/nsl-mapper/instance/apni/781104'
        treeService.linkService.getPreferredLinkForObject(_) >> {
            String url = "http://localhost:7070/nsl-mapper/${it[0].class.simpleName.toLowerCase()}/apni/${it[0].id}"
            println url
            return url
        }
        draftVersion.refresh()

        expect:
        tree
        draftVersion
        draftVersion.treeVersionElements.size() == 0
        asperaInstance

        when: 'I try to place Doodia aspera in the version without a parent'
        Map result = treeService.placeTaxonUri(draftVersion, instanceUri, false, null, 'A. User')
        mockTxCommit()

        then: 'It should work'
        1 * treeService.linkService.getObjectForLink(instanceUri) >> asperaInstance
        1 * treeService.linkService.addTargetLink(_) >> { TreeVersionElement tve -> "http://localhost:7070/nsl-mapper/tree/$tve.treeVersion.id/$tve.treeElement.id" }
        1 * treeService.linkService.addTaxonIdentifier(_) >> { TreeVersionElement tve ->
            println "Adding taxonIdentifier for $tve"
            "http://localhost:7070/nsl-mapper/taxon/apni/$tve.taxonId"
        }
        result.childElement == treeService.findElementBySimpleName('Doodia aspera', draftVersion)
        result.warnings.empty
        //taxon id should be set to a unique/new positive value
        result.childElement.taxonId != 0
        TreeVersionElement.countByTaxonId(result.childElement.taxonId) == 1
        result.childElement.taxonLink == "/taxon/apni/${result.childElement.taxonId}"
        println result.childElement.elementLink
    }

    def "test remove a taxon"() {
        given: "we make a test tree"
        treeService.linkService.bulkRemoveTargets(_) >> [success: true]
        TreeVersion draftVersion = setupTree()

        expect: "sane results"
        draftVersion.treeVersionElements.size() == 60
        !draftVersion.published

        when: "we use the elements to get some data"
        TreeVersionElement Asteraceae = treeService.findElementBySimpleName('Asteraceae', draftVersion)
        TreeVersionElement Adenostemma = treeService.findElementBySimpleName('Adenostemma', draftVersion)
        List<Long> originalAsteraceaeTaxonIDs = treeService.getParentTreeVersionElements(Asteraceae).collect {
            it.taxonId
        }

        then: "We get expected results"
        Asteraceae
        Adenostemma
        Adenostemma.parent == Asteraceae
        originalAsteraceaeTaxonIDs.size() == 2

        when: 'I try to remove a taxon'
        Map result = treeService.removeTreeVersionElement(Adenostemma)
        mockTxCommit()

        then: 'It works'
        //Asteraceae, Asterales
        2 * treeService.linkService.addTaxonIdentifier(_) >> { TreeVersionElement tve ->
            println "Adding taxonIdentifier for $tve"
            "http://localhost:7070/nsl-mapper/taxon/apni/$tve.taxonId"
        }
        result.count == 5
        draftVersion.treeVersionElements.size() == 55
        treeService.findElementBySimpleName('Adenostemma', draftVersion) == null
        //The taxonIds for Adenostemma' parents should have changed
        !treeService.getParentTreeVersionElements(Asteraceae).collect { it.taxonId }.find {
            originalAsteraceaeTaxonIDs.contains(it)
        }
    }

    def "test edit taxon profile"() {
        given:
        treeService.linkService.bulkAddTargets(_) >> [success: true]
        treeService.linkService.bulkRemoveTargets(_) >> [success: true]
        Tree tree = makeATestTree()
        TreeVersion draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my default draft', 'irma', 'This is a log entry')
        mockTxCommit()
        TreeTstHelper.makeTestElements(draftVersion, TreeTstHelper.testElementData(), TreeTstHelper.testTreeVersionElementData())
        TreeVersion publishedVersion = treeService.publishTreeVersion(draftVersion, 'tester', 'publishing to delete')
        mockTxCommit()
        draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my next draft', 'irma', 'This is a log entry')
        mockTxCommit()
        TreeVersionElement AdenostemmaTve = treeService.findElementBySimpleName('Adenostemma', draftVersion)
        TreeVersionElement pubAdenostemmaTve = treeService.findElementBySimpleName('Adenostemma', publishedVersion)

        expect:
        tree
        tree.config.distribution_key == "Dist." // note not the same as the APC Dist below
        draftVersion
        draftVersion.treeVersionElements.size() == 60
        publishedVersion
        publishedVersion.treeVersionElements.size() == 60
        tree.defaultDraftTreeVersion == draftVersion
        tree.currentTreeVersion == publishedVersion
        pubAdenostemmaTve
        AdenostemmaTve.treeElement.profile == [
                "APC Dist.":
                        ["value"      : "WA, NT, Qld, NSW",
                         "created_at" : "2011-06-29T00:00:00+10:00",
                         "created_by" : "BRONWYNC",
                         "updated_at" : "2011-06-29T00:00:00+10:00",
                         "updated_by" : "BRONWYNC",
                         "source_link": "https://id.biodiversity.org.au/instanceNote/apni/1118806"]
        ]

        when: 'I update the profile on the published version'
        treeService.editProfile(pubAdenostemmaTve, ['APC Dist.': [value: "WA, NT, SA, Qld, NSW"]], 'test edit profile')
        mockTxCommit()

        then: 'I get a PublishedVersionException'
        thrown(PublishedVersionException)

        when: 'I update a profile on the draft version'
        List<TreeVersionElement> childTves = TreeVersionElement.findAllByParent(AdenostemmaTve)
        TreeElement oldElement = AdenostemmaTve.treeElement
        Timestamp oldUpdatedAt = AdenostemmaTve.treeElement.updatedAt
        Long oldTaxonId = AdenostemmaTve.taxonId
        TreeVersionElement replacedAdenostemmaTve = treeService.editProfile(AdenostemmaTve, ['APC Dist.': [value: "WA, NT, SA, Qld, NSW"]], 'test edit profile')
        mockTxCommit()
        childTves.each {
            it.refresh()
        }

        then: 'It updates the treeElement profile'
        1 * treeService.linkService.addTargetLink(_) >> { TreeVersionElement tve -> "http://localhost:7070/nsl-mapper/tree/$tve.treeVersion.id/$tve.treeElement.id" }
        oldElement
        deleted(AdenostemmaTve)
        replacedAdenostemmaTve
        childTves.findAll {
            it.parent == replacedAdenostemmaTve
        }.size() == childTves.size()
        replacedAdenostemmaTve.taxonId == oldTaxonId
        replacedAdenostemmaTve.treeElement != oldElement
        replacedAdenostemmaTve.treeElement.profile == ['APC Dist.': [value: "WA, NT, SA, Qld, NSW"]]
        replacedAdenostemmaTve.treeElement.updatedBy == 'test edit profile'
        replacedAdenostemmaTve.treeElement.updatedAt.after(oldUpdatedAt)

        when: 'I change a profile to the same thing'
        TreeVersionElement AdenostemmaLavenia = treeService.findElementBySimpleName('Adenostemma lavenia', draftVersion)
        TreeElement oldACElement = AdenostemmaLavenia.treeElement
        oldTaxonId = AdenostemmaLavenia.taxonId
        Map oldProfile = new HashMap(AdenostemmaLavenia.treeElement.profile)
        TreeVersionElement treeVersionElement1 = treeService.editProfile(AdenostemmaLavenia, oldProfile, 'test edit profile')
        mockTxCommit()

        then: 'nothing changes'

        treeVersionElement1 == AdenostemmaLavenia
        treeVersionElement1.taxonId == oldTaxonId
        treeVersionElement1.treeElement == oldACElement
        treeVersionElement1.treeElement.profile == oldProfile
    }

    def "test edit draft only taxon profile"() {
        given:
        Tree tree = makeATestTree()
        TreeVersion draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my default draft', 'irma', 'This is a log entry')
        mockTxCommit()
        TreeTstHelper.makeTestElements(draftVersion, TreeTstHelper.testElementData(), TreeTstHelper.testTreeVersionElementData())
        TreeVersionElement Adenostemma = treeService.findElementBySimpleName('Adenostemma', draftVersion)

        expect:
        tree
        draftVersion
        draftVersion.treeVersionElements.size() == 60
        tree.defaultDraftTreeVersion == draftVersion
        tree.currentTreeVersion == null
        Adenostemma.treeElement.profile == [
                "APC Dist.":
                        ["value"      : "WA, NT, Qld, NSW",
                         "created_at" : "2011-06-29T00:00:00+10:00",
                         "created_by" : "BRONWYNC",
                         "updated_at" : "2011-06-29T00:00:00+10:00",
                         "updated_by" : "BRONWYNC",
                         "source_link": "https://id.biodiversity.org.au/instanceNote/apni/1118806"]
        ]

        when: 'I update a profile on the draft version'
        TreeElement oldElement = Adenostemma.treeElement
        Timestamp oldTimestamp = Adenostemma.treeElement.updatedAt
        Long oldTaxonId = Adenostemma.taxonId
        TreeVersionElement treeVersionElement = treeService.editProfile(Adenostemma, ['APC Dist.': [value: "WA, NT, SA, Qld, NSW"]], 'test edit profile')
        mockTxCommit()

        then: 'It updates the treeElement and updates the profile and not the taxonId'
        treeVersionElement
        oldElement
        treeVersionElement == Adenostemma
        treeVersionElement.taxonId == oldTaxonId
        treeVersionElement.treeElement == oldElement
        treeVersionElement.treeElement.profile == ['APC Dist.': [value: "WA, NT, SA, Qld, NSW"]]
        treeVersionElement.treeElement.updatedBy == 'test edit profile'
        treeVersionElement.treeElement.updatedAt > oldTimestamp
    }

    def "test edit taxon excluded status"() {
        given:
        treeService.linkService.bulkAddTargets(_) >> [success: true]
        treeService.linkService.bulkRemoveTargets(_) >> [success: true]
        TreeVersion draftVersion = setupTree()
        TreeVersion publishedVersion = draftVersion.tree.currentTreeVersion

        expect: "sane results"
        draftVersion.treeVersionElements.size() == 60
        !draftVersion.published

        when: "I get some elements"
        TreeVersionElement Adenostemma = treeService.findElementBySimpleName('Adenostemma', draftVersion)
        TreeVersionElement pubAdenostemma = treeService.findElementBySimpleName('Adenostemma', publishedVersion)

        then:
        pubAdenostemma
        !Adenostemma.treeElement.excluded

        when: 'I update the profile on the published version'
        treeService.editExcluded(pubAdenostemma, true, 'test edit profile')
        mockTxCommit()

        then: 'I get a PublishedVersionException'
        thrown(PublishedVersionException)

        when: 'I update a profile on the draft version'
        Long oldTaxonId = Adenostemma.taxonId
        TreeElement oldElement = Adenostemma.treeElement
        TreeVersionElement treeVersionElement = treeService.editExcluded(Adenostemma, true, 'test edit status')
        mockTxCommit()

        then: 'It creates a new treeVersionElement and treeElement updates the children TVEs and updates the status'
        1 * treeService.linkService.bulkRemoveTargets(_) >> { List<TreeVersionElement> tves -> [success: true] }
        1 * treeService.linkService.addTargetLink(_) >> { TreeVersionElement tve -> "http://localhost:7070/nsl-mapper/tree/$tve.treeVersion.id/$tve.treeElement.id" }
        treeVersionElement
        oldElement
        deleted(Adenostemma)
        treeVersionElement.taxonId == oldTaxonId
        treeVersionElement.treeElement != oldElement
        treeVersionElement.treeElement.excluded
        treeVersionElement.treeElement.updatedBy == 'test edit status'

        when: 'I change status to the same thing'
        TreeVersionElement AdenostemmaLavenia = treeService.findElementBySimpleName('Adenostemma lavenia', draftVersion)
        TreeElement oldACElement = AdenostemmaLavenia.treeElement
        oldTaxonId = AdenostemmaLavenia.taxonId
        TreeVersionElement treeVersionElement1 = treeService.editExcluded(AdenostemmaLavenia, false, 'test edit status')
        mockTxCommit()

        then: 'nothing changes'
        treeVersionElement1 == AdenostemmaLavenia
        treeVersionElement1.taxonId == oldTaxonId
        treeVersionElement1.treeElement == oldACElement
        !treeVersionElement1.treeElement.excluded
    }

    def "test update child tree path"() {
        given:
        Tree tree = makeATestTree()
        treeService.linkService.bulkAddTargets(_) >> [success: true]
        TreeVersion draftVersion = treeService.createTreeVersion(tree, null, 'my first draft', 'irma', 'This is a log entry')
        mockTxCommit()
        TreeTstHelper.makeTestElements(draftVersion, TreeTstHelper.testElementData(), TreeTstHelper.testTreeVersionElementData())
        TreeTstHelper.makeTestElements(draftVersion, [TreeTstHelper.doodiaElementData], [TreeTstHelper.doodiaTVEData]).first()
        treeService.publishTreeVersion(draftVersion, 'testy mctestface', 'Publishing draft as a test')
        mockTxCommit()
        draftVersion = treeService.createDefaultDraftVersion(tree, null, 'my new default draft', 'irma', 'This is a log entry')
        mockTxCommit()

        TreeVersionElement AdenostemmaTve = treeService.findElementBySimpleName('Adenostemma', draftVersion)
        TreeVersionElement doodiaTve = treeService.findElementBySimpleName('Doodia', draftVersion)
        List<TreeVersionElement> AdenostemmaChildren = treeService.getAllChildElements(AdenostemmaTve)
        printTveAndCountChildren(AdenostemmaTve)

        expect:
        tree
        draftVersion.treeVersionElements.size() == 61
        !draftVersion.published
        AdenostemmaTve
        doodiaTve
        AdenostemmaChildren.size() == 4
        AdenostemmaChildren.findAll { it.treePath.contains(AdenostemmaTve.treeElement.id.toString()) }.size() == 4

        when: "I update the tree path changing an element"
        treeService.updateChildTreePath(doodiaTve.treePath, AdenostemmaTve.treePath, AdenostemmaTve.treeVersion)
        mockTxCommit()

        List<TreeVersionElement> doodiaChildren = treeService.getAllChildElements(doodiaTve)

        then: "The tree paths of Adenostemma kids have changed"
        doodiaChildren.size() == 4
        AdenostemmaChildren.containsAll(doodiaChildren)
    }

    def "test change a taxons parent"() {
        given:
        TreeVersion draftVersion = setupTree()
        expect: "sane results"
        draftVersion.treeVersionElements.size() == 60
        !draftVersion.published

        when: "we get some data"
        TreeVersionElement AsteraceaeTve = treeService.findElementBySimpleName('Asteraceae', draftVersion)
        TreeVersionElement AdenostemmaTve = treeService.findElementBySimpleName('Adenostemma', draftVersion)
        TreeVersionElement ArgophyllaceaeTve = treeService.findElementBySimpleName('Argophyllaceae', draftVersion)
        List<Long> originalArgophyllaceaeParentTaxonIDs = treeService.getParentTreeVersionElements(ArgophyllaceaeTve).collect {
            it.taxonId
        }
        List<Long> originalAsteraceaeParentTaxonIDs = treeService.getParentTreeVersionElements(AsteraceaeTve).collect {
            it.taxonId
        }
        Instance replacementAdenostemmaInstance = Instance.get(489499)
        TreeElement AdenostemmaTe = AdenostemmaTve.treeElement
        Long ArgophyllaceaeInitialTaxonId = ArgophyllaceaeTve.taxonId

        printTveAndCountChildren(ArgophyllaceaeTve)
        printTveAndCountChildren(AsteraceaeTve)

        then: "it looks right"
        AsteraceaeTve
        AdenostemmaTve
        AdenostemmaTve.parent == AsteraceaeTve
        ArgophyllaceaeTve
        originalArgophyllaceaeParentTaxonIDs.size() == 2
        originalAsteraceaeParentTaxonIDs.size() == 2
        treeService.treeReportService

        when: 'I try to move a taxon, Adenostemma under Argophyllaceae'
        Map result = treeService.changeParentTaxon(AdenostemmaTve, ArgophyllaceaeTve, 'test move taxon')
        mockTxCommit()
        draftVersion.refresh()

        List<TreeVersionElement> AdenostemmaChildren = treeService.getAllChildElements(result.replacementElement)
        List<TreeVersionElement> ArgophyllaceaeChildren = treeService.getAllChildElements(ArgophyllaceaeTve)

        printTveAndCountChildren(ArgophyllaceaeTve)
        printTveAndCountChildren(AsteraceaeTve)

        then: 'It works'
        1 * treeService.linkService.getObjectForLink(_) >> replacementAdenostemmaInstance
        1 * treeService.linkService.getPreferredLinkForObject(replacementAdenostemmaInstance.name) >> 'http://localhost:7070/nsl-mapper/name/apni/121601'
        1 * treeService.linkService.getPreferredLinkForObject(replacementAdenostemmaInstance) >> 'http://localhost:7070/nsl-mapper/instance/apni/753948'
        // Argophyllaceae, Asterales, Asteraceae
        3 * treeService.linkService.addTaxonIdentifier(_) >> { TreeVersionElement tve ->
            println "Adding taxonIdentifier for $tve"
            "http://localhost:7070/nsl-mapper/taxon/apni/$tve.taxonId"
        }
        !deleted(AdenostemmaTve)// changing the parent simply changes the parent
        !deleted(AdenostemmaTe) // We are using the same tree element
        result.replacementElement
        result.replacementElement == treeService.findElementBySimpleName('Adenostemma', draftVersion)
        result.replacementElement.elementLink == AdenostemmaTve.elementLink //haven't changed tree version elements
        result.replacementElement.treeVersion == draftVersion
        result.replacementElement.treeElement == AdenostemmaTe
        ArgophyllaceaeTve.taxonId != ArgophyllaceaeInitialTaxonId
        draftVersion.treeVersionElements.size() == 60
        AdenostemmaChildren.size() == 4
        result.replacementElement.parent == ArgophyllaceaeTve
        AdenostemmaChildren[0].treeElement.nameElement == 'lavenia'
        AdenostemmaChildren[0].parent == result.replacementElement
        AdenostemmaChildren[1].treeElement.nameElement == 'lanceolatum'
        AdenostemmaChildren[2].treeElement.nameElement == 'lavenia'
        AdenostemmaChildren[3].treeElement.nameElement == 'macrophyllum'
        ArgophyllaceaeChildren.containsAll(AdenostemmaChildren)
        // all the parent taxonIds should have been updated
        !treeService.getParentTreeVersionElements(ArgophyllaceaeTve).collect { it.taxonId }.find {
            originalArgophyllaceaeParentTaxonIDs.contains(it)
        }
        !treeService.getParentTreeVersionElements(AsteraceaeTve).collect { it.taxonId }.find {
            originalAsteraceaeParentTaxonIDs.contains(it)
        }
    }

    def "test find tree element"() {
        given:
        Tree tree = Tree.findByName('APC')

        TreeElement blechnumDissectum = treeService.findElementBySimpleName('Blechnum dissectum', tree.currentTreeVersion)?.treeElement
        treeService.linkService.getPreferredLinkForObject(_) >> {
            String url = "http://localhost:7070/nsl-mapper/${it[0].class.simpleName.toLowerCase()}/apni/${it[0].id}"
            println url
            return url
        }

        expect:
        blechnumDissectum

        when: 'I try finding the element using its data'
        Map edfe = treeService.comparators(blechnumDissectum)
        TreeElement found1 = treeService.findTreeElement(edfe)

        then:
        found1
        found1.id == blechnumDissectum.id

        when: 'I compare element data to taxon data it matches'
        TaxonData taxonData = treeService.elementDataFromInstance(blechnumDissectum.instance)
        taxonData.excluded = blechnumDissectum.excluded
        taxonData.profile = blechnumDissectum.profile
        Map edfi = treeService.comparators(taxonData)

        then:
        2 * treeService.linkService.getPreferredHost() >> 'http://localhost:7070/nsl-mapper'

        edfi.instanceId == edfe.instanceId
        edfi.nameId == edfe.nameId
        edfi.excluded == edfe.excluded
        edfi.simpleName == edfe.simpleName
        edfi.nameElement == edfe.nameElement
        edfi.sourceShard == edfe.sourceShard
        edfi.synonymsHtml == edfe.synonymsHtml
        edfi.profile == edfe.profile

        when: 'I get the TaxonData from the instance, it still works'
        TreeElement found2 = treeService.findTreeElement(taxonData)

        then:
        found2
        found2.id == blechnumDissectum.id
    }

    //TODO re-instate: temporarily commented out while working on synonym ordering
//    @Unroll("test repeat #i")
//    def "test db synonymy against app synonymy"() {
//        given:
//        TreeElement physalisHederifolia = TreeElement.findBySimpleName('Physalis hederifolia')
//        TreeElement abrotanellaScapigera = TreeElement.findBySimpleName('Abrotanella scapigera')
//        TreeElement hibbertiaHirticalyx = TreeElement.findBySimpleName('Hibbertia hirticalyx')
//        TreeElement cardamineLilacina = TreeElement.findBySimpleName('Cardamine lilacina')
//
//        expect:
//        physalisHederifolia
//        abrotanellaScapigera
//        hibbertiaHirticalyx
//        cardamineLilacina
//
//        when: "we generate the synonyms html for physalis"
//        String physalisSynonymsDb = service.getSynonymsHtmlViaDBFunction(physalisHederifolia.instanceId)
//        String physalisSynonymsHtml = service.getSynonyms(physalisHederifolia.instance).html()
//
//        then: "we get them and they are equal"
//        physalisSynonymsDb
//        physalisSynonymsHtml
//        physalisSynonymsDb == physalisSynonymsHtml
//
//        when: "we generate the synonyms html for abrotanella"
//        String abrotanellaSynonymsDb = service.getSynonymsHtmlViaDBFunction(abrotanellaScapigera.instanceId)
//        String abrotanellaSynonymsHtml = service.getSynonyms(abrotanellaScapigera.instance).html()
//
//        then: "we get them and they are equal"
//        abrotanellaSynonymsDb
//        abrotanellaSynonymsHtml
//        abrotanellaSynonymsDb == abrotanellaSynonymsHtml
//
//        when: "we generate the synonyms html for hibbertia"
//        String hibbertiaSynonymsDb = service.getSynonymsHtmlViaDBFunction(hibbertiaHirticalyx.instanceId)
//        String hibbertiaSynonymsHtml = service.getSynonyms(hibbertiaHirticalyx.instance).html()
//
//        then: "we get them and they are equal"
//        hibbertiaSynonymsDb
//        hibbertiaSynonymsHtml
//        hibbertiaSynonymsDb == hibbertiaSynonymsHtml
//
//        when: "we generate the synonyms html for cardamine"
//        String cardamineSynonymsDb = service.getSynonymsHtmlViaDBFunction(cardamineLilacina.instanceId)
//        String cardamineSynonymsHtml = service.getSynonyms(cardamineLilacina.instance).html()
//
//        then: "we get them and they are equal"
//        cardamineSynonymsDb
//        cardamineSynonymsHtml
//        cardamineSynonymsDb == cardamineSynonymsHtml
//
//        where:
//
//        i << (1..10)
//    }

    static deleted(domainObject) {
        Name.withSession { session ->
            session.persistenceContext.getEntry(domainObject)?.status in [null, Status.DELETED]
        }
    }

    private Tree makeATestTree() {
        Tree t = treeService.createNewTree('aTree', 'aGroup', null, '<p>A description</p>',
                'http://trees.org/aTree', false)
        t.save(flush: true) //need this in tests because it doesn't persist, which means GORM can find it
        return t
    }

    private Tree makeBTestTree() {
        Tree t = treeService.createNewTree('bTree', 'aGroup', null, '<p>B description</p>',
                'http://trees.org/bTree', false)
        t.save(flush: true) //need this in tests because it doesn't persist, which means GORM can find it
        return t
    }

    private long printTveAndCountChildren(TreeVersionElement target) {
        println "\n*** Taxon ${target?.taxonId}: ${target?.treeElement?.name?.simpleName} Children ***"
        List<TreeVersionElement> children = treeService.getAllChildElements(target)
        for (TreeVersionElement tve in children) {
            tve.refresh()
            println "Taxon: $tve.taxonId, Names: $tve.namePath, Path: $tve.treePath"
        }
        println "***\n"
        return children.size()
    }

    /**
     * because this integration test uses @Rollback to revert all changes to the database made during a test, it
     * circumvents flushing of the session on commit. This is because @Rollback creates a transaction around the test
     * and therefore the service transaction. The hibernate flushmode = COMMIT, default setting means that the hibernate
     * session is flushed to the database on the transaction commit. In these tests the commit doesn't happen at the end
     * of the service method, so queries of the database will *not* see objects saved in the service method - they are
     * in the session only.
     *
     * To fix this, and replicate what should happen in the running application as closely as possible, we need to flush
     * the session after calls service methods.
     * @return
     */
    private void mockTxCommit() {
        sessionFactory.getCurrentSession().flush()
    }

}
