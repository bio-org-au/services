package au.org.biodiversity.nsl.api

import au.org.biodiversity.nsl.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class AuthorControllerSpec extends Specification implements ControllerUnitTest<AuthorController>, DataTest {

    @Shared
    AuthorService authorServiceMock

    void setupSpec() {
        mockDomains Author, Namespace
    }

    def setup() {
        controller.jsonRendererService = new JsonRendererService()

        LinkService linkServiceMock = Mock()
        linkServiceMock.getLinksForObject(_) >> { Object thing -> ["first $thing link", "second $thing link"] }
        linkServiceMock.getPreferredLinkForObject(_) >> { Object thing -> "Link for $thing" }
        controller.jsonRendererService.linkService = linkServiceMock

        authorServiceMock = Mock()
        controller.authorService = authorServiceMock
    }

    def cleanup() {
    }

    @Unroll
    void "Test deduplicate accepts DELETE only; #method"() {
        when: "no parameters are passed"
        request.method = method
        response.format = 'json'
        controller.deduplicate(1l, 2l, 'tester')

        then: "? is returned"
        status == stat
        println response.text

        where:
        method   | stat
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
        status == 404
        response.text == '{"action":null,"status":{"enumType":"org.springframework.http.HttpStatus","name":"NOT_FOUND"},"error":"Target author not found.\\n Duplicate author not found.","ok":false}'

        when:
        resetCallToDedupe()
        Author target = TestUte.saveAuthor([abbrev: 'a1', name: 'Author One'])
        Author duplicate = TestUte.saveAuthor([abbrev: 'a2', name: 'Duplicate Author'])
        controller.deduplicate(duplicate.id, target.id, 'tester')

        then:
        1 * authorServiceMock.deduplicate(_, _, 'tester') >> {
            Author d, Author t, String user -> [success: true]
        }
        println response.text
        response.text.contains('"ok":true')
        status == 200

        when: "service returns no result it's handled"
        resetCallToDedupe()
        controller.deduplicate(duplicate.id, target.id, 'break mock')

        then:
        1 * authorServiceMock.deduplicate(_, _, _) >> {
            return null
        }
        println response.text
        response.text.contains('Author deduplication failed: No results from service.')
        status == 500

    }

    private resetCallToDedupe() {
        response.reset()
        response.format = 'json'
        request.method = 'DELETE'
    }

}
