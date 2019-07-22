package au.org.biodiversity.nsl.api

import au.org.biodiversity.nsl.Author
import au.org.biodiversity.nsl.AuthorService
import au.org.biodiversity.nsl.JsonRendererService
import au.org.biodiversity.nsl.LinkService
import au.org.biodiversity.nsl.Namespace
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import spock.lang.Specification

import java.sql.Timestamp

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(AuthorController)
@TestMixin(DomainClassUnitTestMixin)
@Mock([Author, Namespace])
class AuthorControllerSpec extends Specification {

    Namespace namespace = new Namespace(name: 'cosmos',
            rdfId : 'cosmos',
            descriptionHtml : '<b>cosmos</b>')

    def setup() {
        controller.transactionManager = getTransactionManager()
        controller.jsonRendererService = new JsonRendererService()
        def linkServiceMock = mockFor(LinkService)
        linkServiceMock.demand.getLinksForObject(0..10) { Object thing -> ["first $thing link", "second $thing link"] }
        linkServiceMock.demand.getPreferredLinkForObject(0..10) { Object thing -> "Link for $thing" }
        controller.jsonRendererService.linkService = linkServiceMock.createMock()
        def authorServiceMock = mockFor(AuthorService)
        authorServiceMock.demand.deduplicate(0..10) {Author duplicate, Author target, String user -> [success : true]}
        controller.authorService = (AuthorService)authorServiceMock.createMock()
    }

    def cleanup() {
    }

    void "Test deduplicate accepts DELETE only"() {
        when: "no parameters are passed"
        request.method = method
        response.format = 'json'
        controller.deduplicate(1l, 2l, 'tester')

        then: "? is returned"
        response.status == status
        println controller.response.text

        where:
        method   | status
        'GET'    | 405
        'PUT'    | 405
        'POST'   | 405
        'DELETE' | 404
        'PATCH'  | 405
    }

    void "test author deduplication"() {
        when:
        resetCallToDedupe()
        controller.deduplicate(1l, 2l, 'tester')

        then:
        response.status == 404
        controller.response.text == '{"action":null,"error":"Target author not found.\\n Duplicate author not found.","ok":false}'

        when:
        resetCallToDedupe()
        Author target = saveAuthor(abbrev: 'a1', name: 'Author One')
        Author duplicate = saveAuthor(abbrev: 'a2', name: 'Duplicate Author')
        controller.deduplicate(duplicate.id, target.id, 'tester')

        then:
        println controller.response.text
        response.status == 200
    }

    private resetCallToDedupe() {
        response.reset()
        response.format = 'json'
        request.method = 'DELETE'
    }

    private Author saveAuthor(Map params) {
        Map base = [
                updatedAt : new Timestamp(System.currentTimeMillis()),
                updatedBy : 'test',
                createdAt :new Timestamp(System.currentTimeMillis()),
                createdBy : 'test',
                namespace : namespace
        ] << params
        Author a = new Author(base)
        a.save()
        return a
    }

}
