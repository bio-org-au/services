package au.org.biodiversity.nsl.api

import au.org.biodiversity.nsl.Author
import au.org.biodiversity.nsl.AuthorService
import grails.transaction.Transactional
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authz.annotation.RequiresRoles
import org.grails.plugins.metrics.groovy.Timed
import org.springframework.http.HttpStatus

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import static org.springframework.http.HttpStatus.OK

@Transactional
class AuthorController implements WithTarget {

    static responseFormats = [
            index      : ['html'],
            deduplicate: ['json', 'xml', 'html']
    ]

    static allowedMethods = [
            deduplicate: ["DELETE"]
    ]

    AuthorService authorService
    def jsonRendererService

    def index() {}

    @Timed
    @RequiresRoles('admin')
    deduplicate(long id, long target, String user) {
        Author targetAuthor = Author.get(target)
        Author duplicate = Author.get(id)
        withTargets(["Target author": targetAuthor, "Duplicate author": duplicate]) { ResultObject result ->
            if (!user) {
                user = SecurityUtils.subject.principal.toString()
            }
            if (duplicate.id == targetAuthor.id) {
                result.error("Duplicate and Target author are the same.")
                result.status = HttpStatus.BAD_REQUEST
                result.ok = false
                return
            }
            try {
                Map dedupeResults = authorService.deduplicate(duplicate, targetAuthor, user)
                result << dedupeResults
                //rename success to OK for consistency
                result.ok = dedupeResults.success
                result.remove('success')
                if (result.ok) {
                    result.status = OK
                } else {
                    result.status = INTERNAL_SERVER_ERROR
                }

            } catch (e) {
                e.printStackTrace()
                result.error "Could not deduplicate: $e.message"
                result.status = INTERNAL_SERVER_ERROR
                result.ok = false
            }
        }
    }
}
