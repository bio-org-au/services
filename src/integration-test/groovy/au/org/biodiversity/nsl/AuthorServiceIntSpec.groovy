package au.org.biodiversity.nsl


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.apache.shiro.SecurityUtils
import org.apache.shiro.mgt.DefaultSecurityManager
import org.apache.shiro.subject.Subject
import org.apache.shiro.util.ThreadContext
import spock.lang.Specification

import java.sql.Timestamp

/**
 * User: pmcneil
 * Date: 10/07/18
 *
 */

@Rollback
@Integration
class AuthorServiceIntSpec extends Specification {

    AuthorService authorService

    def setup() {
        DefaultSecurityManager securityManager = Mock()
        ThreadContext.bind(securityManager)
        Subject mockSubject = Mock()
        ThreadContext.bind(mockSubject)
    }

    def cleanup() {

    }

    void "test deduplicate"() {
        expect:
        SecurityUtils.getSecurityManager()

        when: "link service fails"
        LinkService failingLinkService = Mock()
        failingLinkService.moveTargetLinks(_, _) >> [success: false, errors: 'It was meant to fail']
        authorService.linkService = failingLinkService
        Author target = saveAuthor(abbrev: 'a1', name: 'Author One')
        Author duplicate = saveAuthor(abbrev: 'a2', name: 'Duplicate Author')
        Long dupId = duplicate.id
        Long targetId = target.id

        Map result = authorService.deduplicate(duplicate, target, 'admin')

        then: "We get a failed result with a message"
        println result
        !result.success
        Author.get(dupId)
        Author.get(targetId)
        result.error == "Author deduplication failed: (relinking [Author $dupId: Duplicate Author] failed. Linker error: (It was meant to fail))"

        when: "link service works"
        LinkService linkServiceMock = Mock()
        linkServiceMock.moveTargetLinks(_, _) >> [success: true]
        authorService.linkService = linkServiceMock

        Map result2 = authorService.deduplicate(duplicate, target, 'admin')

        then: "We get a success and the duplicate author is deleted"
        println result2
        result2.success
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
                namespace: Namespace.list().first()
        ] << params
        Author a = new Author(base)
        a.save(failOnError: true)
        return a
    }

}
