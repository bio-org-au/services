/*
    Copyright 2015 Australian National Botanic Gardens

    This file is part of NSL services project.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy
    of the License at http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package au.org.biodiversity.nsl

import org.apache.shiro.authz.annotation.RequiresRoles

import java.sql.Timestamp

class AuthorService implements AsyncHelper {

    LinkService linkService

    void autoDeduplicate(String user) {
        doAsync('Auto deduplicate authors') {
            List<Author> authorsMarkedAsDuplicates = Author.findAllByDuplicateOfIsNotNull()
            log.debug "duplicate authors: $authorsMarkedAsDuplicates"
            authorsMarkedAsDuplicates.each { Author author ->
                dedup(author, author.duplicateOf, user)
            }

            List<String> namesWithDuplicates = Author.executeQuery('select distinct(a.name) from Author a where exists (select 1 from Author a2 where a2.id <> a.id and a2.name = a.name)') as List<String>
            namesWithDuplicates.each { String name ->
                List<Author> authors = Author.findAllByName(name)
                if (authors) {
                    Map abbrevs = authors.groupBy { it.abbrev }
                    if (abbrevs.size() > 2) {
                        log.debug "more than two abbrevs for $name: ${abbrevs.keySet()}"
                    } else {
                        abbrevs.remove(null)
                        Author targetAuthor
                        if (abbrevs.size() == 0) {
                            targetAuthor = authors.min { it.id }
                            deduplicateAuthors(authors, targetAuthor, user)
                        } else if (abbrevs.size() == 1) {
                            targetAuthor = abbrevs.values().first().min { it.id }
                            deduplicateAuthors(authors, targetAuthor, user)
                        } else {
                            log.debug "more than one remaining abbrev for $name: $abbrevs"
                        }
                    }
                }
            }
        }
    }

    @RequiresRoles('admin')
    Map deduplicate(Author duplicate, Author target, String user) {
        if (!user) {
            return [success: false, errors: ['You must supply a user.']]
        }
        Map results = dedup(duplicate, target, user)
        return results
    }

    private Map deduplicateAuthors(List<Author> duplicateAuthors, Author targetAuthor, String user) {
        Map results = [success: true, target: targetAuthor, deduplicationResults: []]
        duplicateAuthors.each { Author dupeAuthor ->
            Map r = dedup(dupeAuthor, targetAuthor, user)
            results.deduplicationResults << r
            if (!r.success) {
                results.success = false
            }
        }
        return results
    }

    private Map dedup(Author dupeAuthor, Author targetAuthor, String user) {
        Map result = [:]
        Boolean success = true
        if (dupeAuthor != targetAuthor) {
            Author.withTransaction { tx ->
                try {
                    rewireDuplicateTo(targetAuthor, dupeAuthor, user)
                    result.rewired = true

                    log.debug "move links to TARGET AUTHOR: $targetAuthor FROM DUPE_AUTHOR: $dupeAuthor"

                    Map linkResult = linkService.moveTargetLinks(dupeAuthor, targetAuthor)
                    if (!linkResult.success) {
                        throw new Exception("relinking [$dupeAuthor] failed. Linker error: ($linkResult.errors)")
                    }

                    result.relinked = true
                    log.info "About to delete $dupeAuthor"
                    dupeAuthor.delete()
                    targetAuthor.duplicateOf = null
                    targetAuthor.save()
                } catch (e) {
                    result.error = "Author deduplication failed: ($e.message)"
                    log.error(result.error)
                    e.printStackTrace()
                    tx.setRollbackOnly()
                    success = false
                }
            }
        } else {
            result.error = "Duplicate ($dupeAuthor) = Target ($targetAuthor)"
            success = false
        }
        result.success = success
        return result
    }

    /**
     * rewire the duplicate author to the target author
     * @param target
     * @param duplicate
     * @return
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    private rewireDuplicateTo(Author target, Author duplicate, String user) {
        log.debug "rewiring links for $duplicate to $target"
        Timestamp now = new Timestamp(System.currentTimeMillis())

        duplicate.namesForAuthor.each { Name name ->
            log.debug "setting author on name $name to $target from $duplicate"
            name.author = target
            name.updatedAt = now
            name.updatedBy = user
            name.save()
        }
        duplicate.namesForBaseAuthor.each { Name name ->
            log.debug "setting base author on name $name to $target from $duplicate"
            name.baseAuthor = target
            name.updatedAt = now
            name.updatedBy = user
            name.save()
        }
        duplicate.namesForExAuthor.each { Name name ->
            log.debug "setting ex author on name $name to $target from $duplicate"
            name.exAuthor = target
            name.updatedAt = now
            name.updatedBy = user
            name.save()
        }
        duplicate.namesForExBaseAuthor.each { Name name ->
            log.debug "setting ex base author on name $name to $target from $duplicate"
            name.exBaseAuthor = target
            name.updatedAt = now
            name.updatedBy = user
            name.save()
        }
        duplicate.namesForSanctioningAuthor.each { Name name ->
            log.debug "setting sanctioning author on name $name to $target from $duplicate"
            name.sanctioningAuthor = target
            name.updatedAt = now
            name.updatedBy = user
            name.save()
        }
        duplicate.references.each { Reference reference ->
            log.debug "setting author on reference $reference to $target from $duplicate"
            reference.author = target
            reference.updatedAt = now
            reference.updatedBy = user
            reference.save()
        }
        duplicate.comments.each { Comment comment ->
            log.debug "setting author on comment $comment to $target from $duplicate"
            comment.author = target
            comment.updatedAt = now
            comment.updatedBy = user
            comment.save()
        }
        log.debug "setting duplicates for $duplicate to $target"
        Author.findAllByDuplicateOf(duplicate)*.duplicateOf = target
        duplicate.duplicateOf = target
        target.updatedAt = now
        target.updatedBy = user
        target.save()
        duplicate.updatedAt = now
        duplicate.updatedBy = user
        duplicate.save()
    }
}
