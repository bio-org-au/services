import au.org.biodiversity.nsl.Name
import grails.test.spock.IntegrationSpec
import spock.lang.Shared

/**
 * User: pmcneil
 * Date: 11/02/16
 *
 */
class LinkServiceFunctionalSpec extends IntegrationSpec {

    @Shared
    def grailsApplication

    @Shared
    def linkService

    def setup() {

    }
    def cleanup() {

    }

    void "get links for"() {
        when: 'I ask for links for doodia'
        Name doodia = Name.findBySimpleName('Doodia')
        List<Map> links = linkService.getLinksForObject(doodia)
        println links

        then: 'I get a list of links'
        links
        links.size() > 0
        links[0].containsKey('link')
        links[0].containsKey('resourceCount')
        links[0].containsKey('preferred')

        when: 'I try to get links for an unsaved name'
        Name noodia = new Name(doodia.properties)
        noodia.id = null
        noodia.comments = []
        noodia.instances = []
        noodia.tags = []
        noodia.nameElement = 'noodia'
        noodia.simpleName = 'noodia'

        links = linkService.getLinksForObject(noodia)
        println links

        then: 'I get a no links (an error happened)'
        links.size() == 0
    }

    void "get mapper identity"() {

        when: 'I call the mapper with a URI that exists'
        def identity = linkService.getMapperIdentityForLink('http://localhost:7070/nsl-mapper/name/apni/92690')
        println "identity is $identity"

        then: 'identity should be found and match'
        identity instanceof Map
        identity.nameSpace == 'apni'
        identity.objectType == 'name'
        identity.idNumber == 92690

        when: 'We call the mapper for a URI that doesnt exists'
        identity = linkService.getMapperIdentityForLink('http://localhost:7070/nsl-mapper/name/apni/false')
        println "identity is $identity"

        then: 'identity should be null'
        identity == null
    }

    void "get an object for a uri"() {
        when: 'you ask the linkservice for the object identified by a uri'
        def object = linkService.getObjectForLink('http://localhost:7070/nsl-mapper/name/apni/92690')
        println object

        then: 'you get back a Name object'
        object instanceof Name
        object.fullName == 'Hakea ceratophylla var. tricuspis Meisn.'

    }

    void "add/remove target link for object"() {
        when: 'I try to add a default link for Doodia, which exists'
        Name doodia = Name.findBySimpleName('Doodia')
        String response = linkService.addTargetLink(doodia)
        println response

        then: 'I get the existing url'
        response == 'http://localhost:7070/nsl-mapper/name/apni/70914'

        when: 'I make a new name'
        Name noodia = new Name(doodia.properties)
        noodia.id = null
        noodia.comments = []
        noodia.instances = []
        noodia.tags = []
        noodia.nameElement = 'noodia'
        noodia.simpleName = 'noodia'
        noodia.uri = null
        noodia.save()

        then:
        noodia.id != doodia.id

        when: 'I add noodia to the mapper'
        response = linkService.addTargetLink(noodia)
        println response

        then: 'I get a non null response'
        response

        when: 'I try to get the noonia url'
        List<Map> links = linkService.getLinksForObject(noodia)
        println links

        then: 'I should get one'
        links.size() == 1

        when: 'I move noodia links to Doodia it should work'
        Map moveResponse = linkService.moveTargetLinks(noodia, doodia)
        println moveResponse

        then: 'I get a success response'
        moveResponse
        moveResponse.success

        when: 'I delete noodia'
        Map deleteResponse = linkService.deleteNameLinks(noodia, 'This is just a test')
        println deleteResponse

        then: 'It should remove the identity'
        deleteResponse
        deleteResponse.success

        when: 'I remove a the noonia url from doodia'
        Map removeResponse = linkService.removeNameLink(doodia, links[0].link)
        println removeResponse

        then: 'It should succed'
        removeResponse
        removeResponse.success
    }
}
