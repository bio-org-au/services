package au.org.biodiversity.nsl

import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.grails.plugins.codecs.HTMLCodec
import spock.lang.Specification

import java.sql.Timestamp

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestMixin(DomainClassUnitTestMixin)
@Mock([Name, NameGroup, NameCategory, NameStatus, NameRank, NameType, Namespace, Instance, Reference, Author,
        RefAuthorRole, RefType, Language, InstanceType])
class SynonymUnitSpec extends Specification {

    def linkService = Mock(LinkService)
    Name doodia
    Instance instance1
    Instance instance2
    Reference r1

    def setup() {
        linkService.getPreferredHost() >> {
            return "http://my.test.host"
        }

        linkService.getPreferredLinkForObject(_) >> {
            String url = "http://my.test.host/${it[0].class.simpleName.toLowerCase()}/apni/${it[0].id}"
            println url
            return url
        }

        String.metaClass.encodeAsHTML = {
            HTMLCodec.xml_encoder.encode(delegate)
        }
        String.metaClass.decodeHTML = {
            HTMLCodec.decoder.decode(delegate)
        }


        Namespace namespace = new Namespace(name: 'test', rfId: 'blah', descriptionHtml: '<p>blah</p>')
        TestUte.setUpNameInfrastructure()
        doodia = TestUte.makeName('Doodia', 'Genus', null, namespace)
        doodia.simpleName = 'Doodia'
        doodia.fullNameHtml = '<tag>Doodia R.Br.</tag>'
        doodia.fullName = 'Doodia R.Br.'
        doodia.save()
        r1 = TestUte.genericReference()
        Reference r2 = TestUte.genericReference()
        TestUte.setUpInstanceTypes()

        instance1 = new Instance(
                uri: 'http://something',
                verbatimNameString: '',
                page: '',
                pageQualifier: '',
                nomenclaturalStatus: '',
                bhlUrl: '',
                instanceType: InstanceType.findByName('primary reference'),
                name: doodia,
                reference: r1,
                namespace: namespace,
                updatedBy: 'pmcneil',
                updatedAt: new Timestamp(System.currentTimeMillis()),
                createdBy: 'pmcneil',
                createdAt: new Timestamp(System.currentTimeMillis())
        )

        instance1.save()

        instance2 = new Instance(
                uri: 'http://something',
                verbatimNameString: '',
                page: '',
                pageQualifier: '',
                nomenclaturalStatus: '',
                bhlUrl: '',
                instanceType: InstanceType.findByName('taxonomic synonym'),
                name: doodia,
                reference: r2,
                cites: instance1,
                namespace: namespace,
                updatedBy: 'pmcneil',
                updatedAt: new Timestamp(System.currentTimeMillis()),
                createdBy: 'pmcneil',
                createdAt: new Timestamp(System.currentTimeMillis())
        )
        instance2.save()
    }

    def cleanup() {
    }

    void "when I create synonym from instance the links hosts are good"() {

        when: 'I create a synonym from an instance'
        Synonym s = new Synonym(instance2, linkService)
        println s.asMap()

        then: 'synonym links dont have the host'
        s != null
        s.host == 'http://my.test.host'
        s.nameLink == "/name/apni/${doodia.id}"
        s.instanceLink == "/instance/apni/${instance2.id}"
        s.conceptLink == "/instance/apni/${instance1.id}"
        s.citesLink == "/reference/apni/${r1.id}"

        when: 'I get the map it looks right'
        Map m = s.asMap()

        then:
        m == [
                host          : 'http://my.test.host',
                instance_id   : instance2.id,
                instance_link : "/instance/apni/${instance2.id}",
                concept_link  : "/instance/apni/${instance1.id}",
                simple_name   : "Doodia",
                type          : 'taxonomic synonym',
                name_id       : doodia.id,
                name_link     : "/name/apni/${doodia.id}",
                full_name_html: '<tag>Doodia R.Br.</tag>',
                nom           : false,
                tax           : true,
                mis           : false,
                syn           : true,
                cites         : 'Author One (1999), reference one',
                citesHtml     : '<ref data-id=\'1\'><ref-paper><author>Author One</author> <year>(1999)</year>, <ref-title><i>reference one</i></ref-title></ref-paper></ref>',
                cites_link    : "/reference/apni/${r1.id}",
                year          : 1999
        ]
    }

}
