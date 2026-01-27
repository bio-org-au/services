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

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.plugins.codecs.HTMLCodec
import spock.lang.Specification

import java.sql.Timestamp

//TODO convert to use TestUte

class ReferenceServiceSpec extends Specification  implements ServiceUnitTest<ReferenceService>, DataTest {

    private Namespace namespace = new Namespace(name: 'cosmos',
            rdfId: 'cosmos',
            descriptionHtml: '<b>cosmos</b>')

    private Author unknownAuthor
    private RefAuthorRole editorRole
    private RefAuthorRole authorRole
    private Language language
    private RefType paper

    void setupSpec() {
        mockDomains Author, Namespace, Reference, RefAuthorRole, Language, RefType
    }

    def setup() {
        service.transactionManager = getTransactionManager()
        unknownAuthor = saveAuthor(abbrev: '-', name: '-')
        authorRole = saveRefAuthorRole('Author')
        editorRole = saveRefAuthorRole('Editor')
        language = saveLanguage('au')
        paper = saveRefType('Paper')
        String.metaClass.encodeAsHTML = {
            HTMLCodec.xml_encoder.encode(delegate)
        }
        String.metaClass.decodeHTML = {
            HTMLCodec.decoder.decode(delegate)
        }

    }

    def cleanup() {
    }

    void "test reference string category remove full stop"() {
        when:
        String output = ReferenceStringCategory.removeFullStop(test)

        then:
        output == result

        where:
        test          | result
        "A string."   | "A string"
        "A string..." | "A string"
        "A string"    | "A string"

    }

    void "test reference string category full stop"() {
        when:
        String output = ReferenceStringCategory.fullStop(test)

        then:
        output == result

        where:
        test          | result
        "A string"    | "A string."
        "A string "   | "A string ."
        "A string..." | "A string..."
        "A string."   | "A string."
    }

    void "test reference string category comma"() {
        when:
        String output = ReferenceStringCategory.comma(test)

        then:
        output == result

        where:
        test        | result
        "A string"  | "A string,"
        "A string " | "A string ,"
        "A string," | "A string,"
    }

    void "test null string returns blank string"() {
        when:
        String result = ReferenceStringCategory.withString(null) {
            return 'result should not be this'
        }

        then:
        result == ''
    }

    void "test reference string category clean"() {
        when:
        String output = ReferenceStringCategory.clean(test)

        then:
        output == result

        where:
        test               | result
        "A (str)ing"       | "A string"
        "A st(ri)ng "      | "A string"
        "A (1984) string"  | "A 1984 string"
        "A string"         | "A string"
        "A string (1984 )" | "A string 1984"
    }

    void "test authorUpdated updates the authors references"() {
        when:
        Author author = saveAuthor(abbrev: 'a1', name: 'Author One')
        Reference r1 = saveReference(title: "reference one", author: author)
        Reference r2 = saveReference(title: "reference two", author: author)

        then:
        1 == 1
        //r1.citation == 'Author One n.d., reference one'
        //r2.citation == 'Author One n.d., reference two'

        when: "we change and update the author the citation changes"
        author.name = 'Changed Author'
        author.save()
        service.authorUpdated(author, new Notification(message: 'updated', objectId: author.id))

        then:
        1 == 1
//        r1.citation == 'Changed Author reference one'
//        r2.citation == 'Changed Author reference two'
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

    private Reference saveReference(Map params) {
        Map base = [
                refType      : paper,
                published    : true,
                refAuthorRole: authorRole,
                language     : language,
                displayTitle : 'Not set',
                updatedAt    : new Timestamp(System.currentTimeMillis()),
                updatedBy    : 'test',
                createdAt    : new Timestamp(System.currentTimeMillis()),
                createdBy    : 'test',
                namespace    : namespace
        ] << params
        Reference reference = new Reference(base)
        reference.save()
        reference.citationHtml = service.generateReferenceCitation(reference, unknownAuthor, editorRole)
        reference.citation = NameConstructionService.stripMarkUp(reference.citationHtml)
        reference.save()
        return reference
    }

    private saveRefAuthorRole(String name) {
        RefAuthorRole role = new RefAuthorRole(name: name, rdfId: name,
                descriptionHtml: name)
        role.save()
        return role
    }

    private saveLanguage(String language) {
        Language l = new Language(iso6391Code: language, iso6393Code: language, name: language)
        l.save()
        return l
    }

    private saveRefType(String name) {
        RefType t = new RefType(
                name: name,
                parentOptional: true,
                parent: null,
                rdfId: 'blah',
                descriptionHtml: 'blah',
                useParentDetails: false
        )
        t.save()
        return t
    }
}
