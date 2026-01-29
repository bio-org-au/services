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

import grails.gorm.transactions.Transactional
import org.springframework.transaction.TransactionStatus
import groovy.sql.Sql

import java.sql.Timestamp

class ReferenceService implements AsyncHelper {

    def instanceService
    def linkService
    def nameService
    def configService
    def sessionFactory

    private List<Long> seen = []

    /**
     * Create a reference citation from reference
     *
     * For a Section or Paper:
     * author [in publication author] (publication date), [reference title.] [<i>parent title</i>.] [Edn. edition[, volume]] [Herbarium annotation][Personal Communication]
     *
     * For everything else e.g. Book:
     * author [in parent author] (publication date), [<i>reference title</i>.] [<i>parent title</i>.] [Edn. edition[, volume]] [Herbarium annotation][Personal Communication]
     *
     * @param reference
     * @param unknownAuthor
     * @param editor
     * @return
     */
    static String generateReferenceCitation(Reference reference, Author unknownAuthor, RefAuthorRole editor) {
        use(ReferenceStringCategory) {

            List<Reference> parents = parents(reference)

            String authorName = ''

            if (reference.author.id != unknownAuthor.id) {
                if (reference.refAuthorRole.id == editor.id) {
                    authorName = "${reference.author.name?.trim()} (ed.)"
                } else {
                    authorName = "${reference.author.name?.trim()}"
                }
            }

            String parentAuthorName = ''

            if (reference.parent && reference.parent.author != unknownAuthor) {
                if (reference.author.id != reference.parent.author.id) {
                    parentAuthorName = (reference.parent.author.name?.trim() == "-") ? "" : "in ${reference.parent.author.name?.trim()}"
                }
                if (reference.parent?.refAuthorRole.id == editor.id) {
                    parentAuthorName = (reference.parent.author.name == "-") ? "" : "in ${reference.parent.author.name?.trim()} (ed.)"
                }
            }

            String pubDate = pubDate(reference)

            String referenceTitle = getReferenceTitle(reference)

            String superReferenceTitle = ((parents.size() > 1) ? getReferenceTitle(reference.parent) : '')

            String parentTitle = ultimateParentTitle(reference.parent)

            String volume = volume(reference)

            String edition = edition(reference, volume)

            String versionLabel = versionLabel(reference)

            String publisher = publisher(reference)

            String url = url(reference)

            List<String> bits = []
            //prefix
            bits << authorName.wrap('<author>', '</author>')
            bits << parentAuthorName.wrap('<author>', '</author>')
            bits << pubDate.wrap('<year>', '</year>').comma()

            if (superReferenceTitle) {  //only in book
                bits << '<ref-title>'
                bits << referenceTitle.comma()
                bits << "in"
                bits << superReferenceTitle.fullStop()
                bits << '</ref-title>'
            } else {
                if (parentTitle) {
                    bits << referenceTitle.fullStop().wrap('<ref-title>', '</ref-title>')
                } else {
                    bits << referenceTitle.wrap('<i>', '</i>').wrap('<ref-title>', '</ref-title>')
                }
            }

            //middle
            bits << parentTitle.wrap('<i>', '</i>').wrap('<par-title>', '</par-title>')
            bits << edition.wrap('<edition>', '</edition>')
            bits << volume.wrap('<volume>', '</volume>')
            if (reference.refType.name == 'Dataset series' || reference.refType.name == 'Dataset version') {
                bits << ('[Version] ' + versionLabel).wrap('<ref-version>', '</ref-version>')
                bits << '[Dataset]'.wrap('<ref-type>', '</ref-type>')
                bits << publisher.wrap('<ref-publisher>', '</ref-publisher>')
                bits << url.wrap('<ref-url>', '</ref-url>')
            }

            //postfix
            switch (reference.refType.name) {
                case 'Herbarium annotation':
                    bits << 'Herbarium annotation'
                    break
                case 'Personal Communication':
                    bits << 'Personal Communication'
                    break
            }

            String result = bits.findAll { it }
                                .join(' ')
                                .removeFullStop()
                                .wrap("<ref-${reference.refType.rdfId}>", "</ref-${reference.refType.rdfId}>")
                                .wrap("<ref data-id='${reference.id}'>", '</ref>')
            assert result != 'true'
            return result
        }
    }

    private static List<Reference> parents(Reference reference) {
        List<Reference> parents = []
        Reference parent = reference.parent
        while (parent) {
            parents << parent
            parent = parent.parent
        }
        return parents
    }

    private static String getReferenceTitle(Reference reference) {
        use(ReferenceStringCategory) {
            if (reference.title && reference.title != 'Not set') {
                return reference.title.removeFullStop()
            }
            return ''
        }
    }

    private static String edition(Reference reference, String volume) {
        if (reference.edition) {
            return "Edn. ${reference.edition.trim()}${volume ? ',' : ''}"
        }
        if (reference.parent) {
            return edition(reference.parent, volume)
        }
        return ''
    }

    private static String ultimateParentTitle(Reference reference) {
        if (!reference) {
            return ''
        }

        use(ReferenceStringCategory) {
            if (reference.parent) {
                return ultimateParentTitle(reference.parent)
            }
            if (reference.title && reference.title != 'Not set') {
                return reference.title.removeFullStop()
            }
            return ''
        }
    }

    private static String volume(Reference reference) {
        if (reference.volume) {
            return reference.volume.trim()
        }
        if (reference.refType.useParentDetails && reference.parent) {
            return volume(reference.parent)
        }
        return ''
    }

    private static String pubDate(Reference reference) {
        use(ReferenceStringCategory) {
            if (reference.isoPublicationDate) {
                return "(${reference.isoPublicationDate.isoDateFormat()})"
            }
            if (reference.publicationDate) {
                return "(${reference.publicationDate.clean()})"
            }
            if (reference.refType.useParentDetails && reference.parent) {
                return pubDate(reference.parent)
            }
            return 'n.d.'
        }
    }

    static String findReferenceIsoPublicationYear(Reference reference) {
        if (!reference) {
            return null
        }
        if (reference.isoPublicationDate) {
            return reference.getIsoYear()
        }
        if (reference.refType.useParentDetails) {
            return reference.parent.getIsoYear()
        }
        return null
    }

    static String findReferenceIsoPublicationDate(Reference reference) {
        if (!reference) {
            return null
        }
        if (reference.isoPublicationDate) {
            return reference.isoPublicationDate
        }
        if (reference.refType.useParentDetails) {
            return reference.parent.isoPublicationDate
        }
        return null
    }

    private static String versionLabel(Reference reference) {
        if (reference.versionLabel) {
            return reference.versionLabel.trim()
        }
        return ''
    }

    private static String publisher(Reference reference) {
        if (reference.publisher) {
            return reference.publisher.trim()
        }
        return ''
    }

    private static String url(Reference reference) {
        if (reference.url) {
            return reference.url.trim()
        }
        return ''
    }

    void checkReferenceChanges(Reference reference) {
        reconstructChildCitations(reference)
    }

    @Transactional
    void reconstructChildCitations(Reference parent) {
        Author unknownAuthor = Author.findByName('-')
        RefAuthorRole editor = RefAuthorRole.findByName('Editor')

        Reference.findAllByParent(parent).each { Reference child ->
            String citationHtml = generateReferenceCitation(child, unknownAuthor, editor)
            if (child.citationHtml != citationHtml) {
                child.citationHtml = citationHtml
                child.citation = NameConstructionService.stripMarkUp(citationHtml)
                child.save()
                //don't need to go down a level and check the child of the child since a change in the reference
                //will cause a new notification, which will check.
                log.debug "saved $child.citationHtml"
            } else {
                log.debug "skipping $child.citationHtml, no change."
                child.discard()
            }
        }
    }

    @Transactional
    reconstructAllCitations() {
        doAsync('Reconstruct all citations') {
            String updaterWas = nameService.pollingStatus()
            nameService.pauseUpdates()
            Closure query = { Map params ->
                Reference.listOrderById(params)
            }

            Author unknownAuthor = Author.findByName('-')
            RefAuthorRole editor = RefAuthorRole.findByName('Editor')

            NameService.chunkThis(1000, query) { List<Reference> references, bottom, top ->
                long start = System.currentTimeMillis()
                Name.withSession { session ->
                    references.each { Reference reference ->
                        try {
                            String citationHtml = generateReferenceCitation(reference, unknownAuthor, editor)

                            if (reference.citationHtml != citationHtml) {
                                reference.citationHtml = citationHtml
                                reference.citation = NameConstructionService.stripMarkUp(citationHtml)
                                reference.save()
                            }
                        } catch (e) {
                            log.error "Error updating citations $e.message"
                            e.printStackTrace()
                        }
                    }
                    session.flush()
                    session.clear()
                }
                log.info "$top done. ${top - bottom} took ${System.currentTimeMillis() - start} ms"
            }
            log.info "Completed update."
            if (updaterWas == 'running') {
                nameService.resumeUpdates()
            }
        }
    }

    @Transactional
    def setCitation(Reference reference, String citation, String citationHtml) {
        reference.citation = citation
        reference.citationHtml = citationHtml
        reference.save()
    }

    @Transactional
    Map deduplicateMarked(String user) {
        List<Map> refs = []
        //remove nested duplicates first
        Reference.findAllByDuplicateOfIsNotNull().each { Reference reference ->
            int depth = 0
            while (reference.duplicateOf.duplicateOf && depth++ < 6) {
                reference.duplicateOf = reference.duplicateOf.duplicateOf
                reference.save(flush: true)
            }
        }

        Reference.findAllByDuplicateOfIsNotNull().each { Reference reference ->
            Map result = [source: reference.id, target: reference.duplicateOf.id]
            //noinspection GroovyAssignabilityCheck
            result << moveReference(reference, reference.duplicateOf, user)
            refs << result
        }
        return [action: "deduplicate marked references", count: Reference.countByDuplicateOfIsNotNull(), references: refs]
    }

/**
 * Move all the
 * - instances
 * - referencesForParent
 * - comments
 * - move reference note to the instances of the source.
 *
 * from the source reference to the target reference.
 * Also moves the URI of the source to the target.
 *
 * No references can have the source as their duplicate of id or this will fail.
 *
 * @param source
 * @param target
 * @return
 */
    @Transactional
    Map moveReference(Reference source, Reference target, String user) {
        if (target.duplicateOf) {
            throw new Exception("Target $target is a duplicate")
        }
        if (!user) {
            return [ok: false, errors: ['You must supply a user.']]
        }
        if (!source) {
            return [ok: false, errors: ['You must supply source.']]
        }
        if (source.referencesForDuplicateOf.size() > 0) {
            return [ok: false, errors: ['References say they are a duplicate of the source.']]
        }
        InstanceNoteKey refNote = instanceService.getInstanceNoteKey('Reference Note', true)
        try {
            Reference.withTransaction { t ->
                Reference.withSession { session ->
                    Timestamp now = new Timestamp(System.currentTimeMillis())
                    if (source.notes && source.notes != target.notes) {
                        //copy to the source instances as an instance note
                        if (source.instances.size() > 0) {
                            InstanceNote note = new InstanceNote(
                                    value: source.notes,
                                    instanceNoteKey: refNote,
                                    namespace: configService.nameSpace,
                                    updatedBy: user,
                                    updatedAt: now,
                                    createdBy: user,
                                    createdAt: now
                            )
                            source.instances.each { instance ->
                                instance.addToInstanceNotes(note)
                                instance.save()
                                log.info "Added reference note $note to $instance"
                            }
                        } else {
                            //append to the reference notes.
                            if (target.notes) {
                                target.notes = "$target.notes (duplicate refererence Note: $source.notes)"
                                log.info "Appended notes to $target.notes"
                            } else {
                                target.notes = source.notes
                                log.info "Set target notes to $target.notes"
                            }
                        }
                    }
                    List<Instance> instances = Instance.executeQuery('select i from Instance i where reference = :ref', [ref: source])
                    log.debug "${instances.size()} instance found..."
                    instances.each { instance ->
                        log.info "Moving instance $instance to $target"
                        instance.reference = target
                        instance.updatedAt = now
                        instance.updatedBy = user
                        instance.save(flush: true)
                    }
                    source.referencesForParent.each { ref ->
                        log.info "Moving parent of $ref to $target"
                        ref.parent = target
                        ref.parent.updatedAt = now
                        ref.parent.updatedBy = user
                        ref.save(flush: true)
                    }
                    source.comments.each { comment ->
                        log.info "Moving comment $comment to $target"
                        comment.reference = target
                        comment.updatedAt = now
                        comment.updatedBy = user
                        comment.save(flush: true)
                    }

                    Map response = linkService.moveTargetLinks(source, target)

                    if (!response.success) {
                        List<String> errors = ["Error moving the link in the mapper."]
                        log.error "Setting rollback only: $response.errors"
                        t.setRollbackOnly()
                        return [ok: false, errors: errors]
                    }
                    target.refresh()
                    source.refresh()
                    target.updatedAt = now
                    target.updatedBy = user
                    target.save(flush: true)
                    source.save(flush: true)
                    log.debug "instances on reference ${source.instances.size()}"
                    source.delete(flush: true)
                    session.flush()
                    return [ok: true]
                }
            }
        } catch (e) {
            log.error e.getLocalizedMessage()
            List<String> errors = [e.message]
            while (e.cause) {
                e = e.cause
                errors << e.message
            }
            return [ok: false, errors: errors]
        }
    }

    @Transactional
    Map deleteReference(Reference reference, String reason) {
        Map canWeDelete = canDelete(reference, reason)
        if (canWeDelete.ok) {
            try {
                Reference.withTransaction { TransactionStatus t ->
                    Map response = linkService.deleteReferenceLinks(reference, reason)

                    reference.delete()
                    if (!response.success) {
                        List<String> errors = ["Error deleting link from the mapper"]
                        errors.addAll(response.errors as List<String>)
                        t.setRollbackOnly()
                        return [ok: false, errors: errors]
                    }

                    t.flush()
                }
            } catch (e) {
                List<String> errors = [e.message]
                while (e.cause) {
                    e = e.cause
                    errors << e.message
                }
                return [ok: false, errors: errors]
            }
        }
        return canWeDelete
    }

    Map canDelete(Reference reference, String reason) {
        List<String> errors = []

        if (!reason) {
            errors << "You need to supply a reason for deleting."
        }

        if (reference.instances.size() > 0) {
            //noinspection GroovyAssignabilityCheck
            errors << "There are ${reference.instances.size()} instances for this reference."
        }

        if (reference.referencesForParent.size() > 0) {
            //noinspection GroovyAssignabilityCheck
            errors << "There are ${reference.referencesForParent.size()} children of this reference."
        }

        if (reference.comments.size() > 0) {
            //noinspection GroovyAssignabilityCheck
            errors << "There are ${reference.comments.size()} comments for this reference."
        }

        if (reference.referencesForDuplicateOf.size() > 0) {
            //noinspection GroovyAssignabilityCheck
            errors << "There are ${reference.referencesForDuplicateOf.size()} references that are a duplicate of this reference."
        }

        if (errors.size() > 0) {
            return [ok: false, errors: errors]
        }
        return [ok: true]
    }

    def replaceXICSinReferenceTitles() {

        doAsync('replace XICS in reference titles.') {

            def count = Reference.executeQuery("select count(ref) from Reference ref where regex(title, '.*(\\~[a-zA-Z]|\\<[A-Z]|\\^).*') = true").first()
            log.debug "about to change $count Reference titles"

            long changed = 0

            List<Reference> references = Reference.executeQuery("select ref from Reference ref where regex(title, '.*(\\~[a-zA-Z]|\\<[A-Z]|\\^).*') = true order by id")
            Reference.withSession { session ->
                references.each { Reference reference ->
                    String newValue = ApniFormatService.transformXicsToUTF8(reference.title)
                    if (newValue != reference.title) {
                        reference.title = newValue
                        reference.save()
                        changed++
                    }
                }
                session.flush()
                session.clear()
            }
            log.debug "changed $changed notes"
        }
    }

    @Transactional
    authorUpdated(Author author, Notification note) {
        if (seen.contains(note.id)) {
            log.info "seen note, skipping $note"
            return
        }
        
        seen.add(note.id)

        Author unknownAuthor = Author.findByName('-')
        RefAuthorRole editor = RefAuthorRole.findByName('Editor')

        author.references.each { Reference reference ->
            String citationHtml = generateReferenceCitation(reference, unknownAuthor, editor)
            if (reference.citationHtml != citationHtml) {
                reference.citationHtml = citationHtml
                reference.citation = NameConstructionService.stripMarkUp(citationHtml)
                reference.updatedBy = author.updatedBy
                reference.updatedAt = new Timestamp(System.currentTimeMillis())
                reference.save()
                log.debug "saved $reference.citationHtml"
            } else {
                reference.discard()
            }
        }
    }

    @Transactional
    def updateSynonymyOnTreeForAuthor(Long id) {
        Sql sql = getSql();
        def query = "select fn_errata_author_change($id);"
        sql.execute(query)
        log.debug "Updated synonymy on the tree for author_id (${id})"
    }

    @Transactional
    def updateSynonymyOnTreeForReference(Long id) {
        Sql sql = getSql();
        def query = "select fn_errata_ref_change($id);"
        sql.execute(query)
        log.debug "Updated synonymy on the tree for reference_id (${id})"
    }

    private Sql getSql() {
        return new Sql(sessionFactory.currentSession.connection())
    }

}

class ReferenceStringCategory {

    static String withString(String string, Closure work) {
        if (string) {
            return work()
        }
        return ''
    }

    static String clean(String string) {
        withString(string) {
            string.replaceAll(/[()]/, '').trim()
        }
    }

    static String removeFullStop(String string) {
        withString(string) {
            string.endsWith('.') ? string.replaceAll(/\.*$/, '') : string
        }
    }

    static String fullStop(String string) {
        withString(string) {
            string.endsWith('.') ? string : string + '.'
        }
    }

    static String comma(String string) {
        withString(string) {
            string.endsWith(',') ? string : string + ','
        }
    }

    static String wrap(String string, String prefix, String postfix) {
        withString(string) {
            prefix + string + postfix
        }
    }

    static final months = ['', 'January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']

    static String isoDateFormat(String isoDate) {
        withString(isoDate) {
            String[] parts = isoDate.split('-')
            switch (parts.size()) {
                case 2:
                    String year = parts[0]
                    String month = months[parts[1].toInteger()]
                    return "$month $year"
                    break;
                case 3:
                    String year = parts[0]
                    String month = months[parts[1].toInteger()]
                    String day = parts[2].toInteger()
                    return "$day $month $year"
                    break;
                default:
                    return isoDate
                    break;
            }
        }
    }
}
