package au.org.biodiversity.nsl

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import spock.lang.Specification

import java.sql.Timestamp

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(AuthorService)
@TestMixin(DomainClassUnitTestMixin)
@Mock([Author, Namespace])
class AuthorServiceSpec extends Specification {

    private Namespace namespace = new Namespace(name: 'cosmos',
            rdfId: 'cosmos',
            descriptionHtml: '<b>cosmos</b>')

    def setup() {
        service.transactionManager = getTransactionManager()
    }

    def cleanup() {
    }

    void "test deduplicate"() {
        when: "link service fails"
        def failingLinkServiceMock = mockFor(LinkService)
        failingLinkServiceMock.demand.moveTargetLinks(1..1) { Object thing1, Object thing2 ->
            return [success: false, errors: 'It was meant to fail']
        }
        service.linkService = failingLinkServiceMock.createMock()
        Author target = saveAuthor(abbrev: 'a1', name: 'Author One')
        Author duplicate = saveAuthor(abbrev: 'a2', name: 'Duplicate Author')
        Long dupId = duplicate.id
        Long targetId = target.id

        Map result = service.deduplicate(duplicate, target, 'admin')

        then: "We get a failed result with a message"
        println result
        result.success == false
        Author.get(dupId)
        Author.get(targetId)
        result.error == 'Author deduplication failed: (relinking [Author 2: Duplicate Author] failed. Linker error: (It was meant to fail))'

        when: "link service works"
        def linkServiceMock = mockFor(LinkService)
        linkServiceMock.demand.moveTargetLinks(1..1) { Object thing1, Object thing2 ->
            return [success: true]
        }
        service.linkService = linkServiceMock.createMock()

        Map result2 = service.deduplicate(duplicate, target, 'admin')

        then: "We get a success and the duplicate author is deleted"
        println result2
        result2.success == true
        !Author.get(dupId)
        Author.get(targetId)
        result2.rewired
        result2.relinked
    }

    private Author saveAuthor(Map params) {
        Map base = [
                updatedAt: new Timestamp(System.currentTimeMillis()),
                updatedBy: 'test',
                createdAt: new Timestamp(System.currentTimeMillis()),
                createdBy: 'test',
                namespace: namespace
        ] << params
        Author a = new Author(base)
        a.save()
        return a
    }
}
