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

import grails.transaction.Transactional
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Transactional
class VocabularyTermsService {

    Closure sortOrder = { PrintWriter w, row -> w.write "        <boa:sortOrder rdf:dataType='&xsd;int'>${row.sortOrder}</boa:sortOrder>\n" }
    Closure appliesToGroup = { PrintWriter w, row -> w.write "        <boa_name:appliesToGroup rdf:resource='http://biodiversity.org.au/voc/boa/NameGroupTerm#${row.nameGroup.rdfId}'/>\n" }
    Closure deprecated = { PrintWriter w, row -> if (row.deprecated) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/nsl/NSL#Deprecated'/>\n" }

    File getVocabularyZipFile() {
        File zf = File.createTempFile("vocabulary-terms", ".zip")
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zf))

        describe(NameCategory, "boa", zout, [
                sortOrder
        ])

        describe(NameGroup, "boa", zout)

        describe(NameType, "boa", zout, [
                sortOrder,
                appliesToGroup,
                { PrintWriter w, NameType row -> w.write "        <boa_name:typeCategory rdf:resource='http://biodiversity.org.au/voc/boa/NameCategoryTerm#${row.nameCategory.rdfId}'/>\n" },
                { PrintWriter w, NameType row -> if (row.autonym) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Name#Type-autonym'/>\n" },
                { PrintWriter w, NameType row -> if (row.formula) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Name#Type-formula'/>\n" },
                { PrintWriter w, NameType row -> if (row.hybrid) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Name#Type-hybrid'/>\n" },
                { PrintWriter w, NameType row -> if (row.scientific) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Name#Type-scientific'/>\n" },
                { PrintWriter w, NameType row -> if (row.connector) w.write "        <boa_name:connector rdf:dataType='&xsd;string'>${row.connector}</boa_name:connector>\n" },
        ])

        describe(Namespace, "nsl", zout)

        describe(NameRank, "boa", zout, [
                sortOrder,
                appliesToGroup,
                { PrintWriter w, NameRank row -> if (row.abbrev) w.write "        <boa_name:rankAbbrev rdf:dataType='&xsd;string'>${row.abbrev}</boa_name:rankAbbrev>\n" },
                deprecated,
                { PrintWriter w, NameRank row -> if (row.italicize) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Name#Rank-italicize'/>\n" },
                { PrintWriter w, NameRank row -> if (row.major) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Name#Rank-major'/>\n" },
                { PrintWriter w, NameRank row -> if (row.visibleInName) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Name#Rank-visible'/>\n" },
                { PrintWriter w, NameRank row -> if (!row.visibleInName) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Name#Rank-hidden'/>\n" },
        ])

        describe(NameStatus, "boa", zout, [
                appliesToGroup,
                { PrintWriter w, NameStatus row -> if (row.display) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Name#Status-display'/>\n" },
                { PrintWriter w, NameStatus row -> if (!row.display) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Name#Status-hidden'/>\n" },
                { PrintWriter w, NameStatus row -> if (row.nomIlleg) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Name#Status-nom-illeg'/>\n" },
                { PrintWriter w, NameStatus row -> if (row.nomInval) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Name#Status-nom-inval'/>\n" },
        ])

        describe(RefAuthorRole, "boa", zout)

        describe(RefType, "boa", zout, [
                { PrintWriter w, RefType row -> if (row.parent) w.write "        <boa_ref:typeParent rdf:resource='http://biodiversity.org.au/voc/boa/ReferenceTypeTerm#${row.parent.rdfId}'/>\n" },
                { PrintWriter w, RefType row -> if (row.parentOptional) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Reference#Type-parent-optional'/>\n" },
                { PrintWriter w, RefType row -> if (!row.parentOptional) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Reference#Type-parent-required'/>\n" },
        ])

        describe(InstanceNoteKey, "nsl", zout, [
                sortOrder,
                deprecated,
                { PrintWriter w, InstanceNoteKey row -> w.write "        <rdf:seeAlso rdf:resource='http://biodiversity.org.au/voc/nsl/hasInstanceNote#${row.rdfId}'/>\n" },
        ])

        describe(InstanceType, "boa", zout, [
                sortOrder,
                deprecated,
                { PrintWriter w, InstanceType row -> if (row.citing) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Instance#Type-citing'/>\n" },
                { PrintWriter w, InstanceType row -> if (row.doubtful) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Instance#Type-doubtful'/>\n" },
                { PrintWriter w, InstanceType row -> if (row.misapplied) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Instance#Type-misapplied'/>\n" },
                { PrintWriter w, InstanceType row -> if (row.nomenclatural) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Instance#Type-nomenclatural'/>\n" },
                { PrintWriter w, InstanceType row -> if (row.primaryInstance) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Instance#Type-primary'/>\n" },
                { PrintWriter w, InstanceType row -> if (row.proParte) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Instance#Type-pro-parte'/>\n" },
                { PrintWriter w, InstanceType row -> if (row.protologue) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Instance#Type-protologue'/>\n" },
                { PrintWriter w, InstanceType row -> if (row.relationship) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Instance#Type-relationship'/>\n" },
                { PrintWriter w, InstanceType row -> if (row.secondaryInstance) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Instance#Type-secondary'/>\n" },
                { PrintWriter w, InstanceType row -> if (row.standalone) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Instance#Type-standalone'/>\n" },
                { PrintWriter w, InstanceType row -> if (row.synonym) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Instance#Type-synonym'/>\n" },
                { PrintWriter w, InstanceType row -> if (row.taxonomic) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Instance#Type-taxonomic'/>\n" },
                { PrintWriter w, InstanceType row -> if (row.unsourced) w.write "        <rdf:type rdf:resource='http://biodiversity.org.au/voc/boa/Instance#Type-unsourced'/>\n" },
                { PrintWriter w, InstanceType row -> w.write "        <rdf:seeAlso rdf:resource='http://biodiversity.org.au/voc/boa/relationship#${row.rdfId}'/>\n" },
        ])

        // This is not used anywhere.
        // describe(NomenclaturalEventType, "nsl", zout)

        concretizeInstanceType zout

        concretizeInstanceNoteKey zout

        zout.finish()
        zout.close()
        return zf
    }

    def describe(clazz, String dir, ZipOutputStream zout, cols = []) {
        String clazzName = clazz.simpleName

        if (clazz == RefAuthorRole) clazzName = 'ReferenceAuthorRole'
        if (clazz == RefType) clazzName = 'ReferenceType'


        zout.putNextEntry(new ZipEntry("voc/${dir}/${clazzName}Term.rdf"))

        PrintWriter w = new PrintWriter(new OutputStreamWriter(zout, "UTF-8"))

        String prefix = (dir == "boa" || dir == "nsl") ? dir : 'UNKNOWN_VOC'

        rdfHead(w)

        ont(w, dir, clazzName)

        clazz.all.each {
            w.write """
    <${prefix}:${clazzName} rdf:about="http://biodiversity.org.au/voc/${dir}/${clazzName}Term#${it.rdfId}">
        <rdfs:label>${it.name}</rdfs:label>
        <dcterms:title>${clazzName} ${it.name}</dcterms:title>
        <dcterms:description rdf:parseType="Literal" xmlns="http://www.w3.org/1999/xhtml">${it.descriptionHtml}</dcterms:description>
        <nsl:id>${it.rdfId}</nsl:id>
"""
            cols.each { col -> col.call(w, it) }

            w.write("""
    </${prefix}:${clazzName}>
"""
            )

        }

        rdfTail(w)

        w.flush()
    }

    def concretizeInstanceNoteKey(ZipOutputStream zout) {
        zout.putNextEntry(new ZipEntry("voc/nsl/hasInstanceNote.rdf"))

        PrintWriter w = new PrintWriter(new OutputStreamWriter(zout, "UTF-8"))
        rdfHead(w)
        ont w, 'nsl', 'hasInstanceNote', false

        InstanceNoteKey.all.each { InstanceNoteKey it ->

            w.write """
    <owl:DatatypeProperty rdf:about="http://biodiversity.org.au/voc/nsl/hasInstanceNote#${it.rdfId}">
        <rdfs:label>${it.name}</rdfs:label>
        <dcterms:title>has ${it.name} Instance Note</dcterms:title>
        <dcterms:description rdf:parseType="Literal" xmlns="http://www.w3.org/1999/xhtml">${it.descriptionHtml}</dcterms:description>
        <nsl:id>${it.rdfId}</nsl:id>
        <rdfs:subPropertyOf rdf:resource='http://biodiversity.org.au/voc/nsl/InstanceNote#hasInstanceNote'/>
        <rdf:seeAlso rdf:resource='http://biodiversity.org.au/voc/nsl/InstanceNoteKeyTerm#${it.rdfId}'/>
    </owl:DatatypeProperty>
"""

        }

        rdfTail(w)
        w.flush()
    }

    def concretizeInstanceType(ZipOutputStream zout) {
        zout.putNextEntry(new ZipEntry("voc/boa/relationship.rdf"))

        PrintWriter w = new PrintWriter(new OutputStreamWriter(zout, "UTF-8"))
        rdfHead(w)
        ont w, 'boa', 'relationship', false

        InstanceType.all.each { InstanceType it ->
            if (!it.standalone) { // cant use hibernate findAll for this, which is interesting
                w.write """
    <owl:ObjectProperty rdf:about="http://biodiversity.org.au/voc/boa/relationship#${it.rdfId}">
        <rdfs:label>${it.name}</rdfs:label>
        <dcterms:title>has ${it.name}</dcterms:title>
        <dcterms:description rdf:parseType="Literal" xmlns="http://www.w3.org/1999/xhtml">${it.descriptionHtml}</dcterms:description>
        <nsl:id>${it.rdfId}</nsl:id>
        <rdfs:subPropertyOf rdf:resource='http://biodiversity.org.au/voc/boa/Instance#relationship'/>
        <rdf:seeAlso rdf:resource='http://biodiversity.org.au/voc/boa/InstanceTypeTerm#${it.rdfId}'/>
"""

                if (it.citing) w.write "        <rdfs:subPropertyOf rdf:resource='http://biodiversity.org.au/voc/boa/Instance#relationship-citing'/>\n"
                if (it.doubtful) w.write "        <rdfs:subPropertyOf rdf:resource='http://biodiversity.org.au/voc/boa/Instance#relationship-doubtful'/>\n"
                if (it.misapplied) w.write "        <rdfs:subPropertyOf rdf:resource='http://biodiversity.org.au/voc/boa/Instance#relationship-misapplied'/>\n"
                if (it.nomenclatural) w.write "        <rdfs:subPropertyOf rdf:resource='http://biodiversity.org.au/voc/boa/Instance#relationship-nomenclatural'/>\n"
                if (it.primaryInstance) w.write "        <rdfs:subPropertyOf rdf:resource='http://biodiversity.org.au/voc/boa/Instance#relationship-primary'/>\n"
                if (it.proParte) w.write "        <rdfs:subPropertyOf rdf:resource='http://biodiversity.org.au/voc/boa/Instance#relationship-pro-parte'/>\n"
                if (it.protologue) w.write "        <rdfs:subPropertyOf rdf:resource='http://biodiversity.org.au/voc/boa/Instance#relationship-protologue'/>\n"
                if (it.relationship) w.write "        <rdfs:subPropertyOf rdf:resource='http://biodiversity.org.au/voc/boa/Instance#relationship-relationship'/>\n"
                if (it.secondaryInstance) w.write "        <rdfs:subPropertyOf rdf:resource='http://biodiversity.org.au/voc/boa/Instance#relationship-secondary'/>\n"
                if (it.standalone) w.write "        <rdfs:subPropertyOf rdf:resource='http://biodiversity.org.au/voc/boa/Instance#relationship-standalone'/>\n"
                if (it.synonym) w.write "        <rdfs:subPropertyOf rdf:resource='http://biodiversity.org.au/voc/boa/Instance#relationship-synonym'/>\n"
                if (it.taxonomic) w.write "        <rdfs:subPropertyOf rdf:resource='http://biodiversity.org.au/voc/boa/Instance#relationship-taxonomic'/>\n"
                if (it.unsourced) w.write "        <rdfs:subPropertyOf rdf:resource='http://biodiversity.org.au/voc/boa/Instance#relationship-unsourced'/>\n"

                w.write "    </owl:ObjectProperty>\n"

            }
        }
        rdfTail(w)
        w.flush()
    }


    static void ont(PrintWriter w, String dir, String clazzName, boolean addTerm = true) {
        w.write """
    <owl:Ontology rdf:about="http://biodiversity.org.au/voc/${dir}/${clazzName}${addTerm ? 'Term' : ''}#ONTOLOGY">
        <rdfs:isDefinedBy rdf:resource="http://biodiversity.org.au/voc/${dir}/${clazzName}${addTerm ? 'Term' : ''}#ONTOLOGY"/>
        <rdfs:label>${clazzName} terms ontology</rdfs:label>
        <rdf:seeAlso rdf:resource="http://biodiversity.org.au/voc/${dir}/${clazzName}"/>
        <dcterms:title>${clazzName} terms - List of terms used in BOA</dcterms:title>
        <dcterms:description rdf:parseType='Literal' rdf:dataType='&rdf;XMLLiteral'>
            <div xmlns='http://www.w3.org/1999/xhtml' class='description ${clazzName}'>
                <div class="header">${clazzName} terms ontology</div>
                <div class="content">List of terms appearing in BOA of type ${clazzName}</div>
            </div>
        </dcterms:description>
    </owl:Ontology>
"""
    }

    static void rdfHead(PrintWriter w) {
        w.write """<?xml version="1.0" encoding="utf-8"?>

<!DOCTYPE rdf:RDF [
        <!ENTITY owl "http://www.w3.org/2002/07/owl#" >
        <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#" >
        <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#" >
        <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#" >
        ]>

<rdf:RDF
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
        xmlns:owl="http://www.w3.org/2002/07/owl#"
        xmlns:dcterms="http://purl.org/dc/terms/"
        xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
        xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"

        xmlns:boa="http://biodiversity.org.au/voc/boa/BOA#"
        xmlns:boa_name="http://biodiversity.org.au/voc/boa/Name#"
        xmlns:boa_name_type="http://biodiversity.org.au/voc/boa/NameType#"
        xmlns:boa_auth="http://biodiversity.org.au/voc/boa/Author#"
        xmlns:boa_ref="http://biodiversity.org.au/voc/boa/Reference#"
        xmlns:boa_rel="http://biodiversity.org.au/voc/boa/relationship#"
        xmlns:boa_inst="http://biodiversity.org.au/voc/boa/Instance#"
        xmlns:boa_instNote="http://biodiversity.org.au/voc/nsl/InstanceNote#"
        xmlns:boa_er="http://biodiversity.org.au/voc/nsl/ExternalReference#"
        xmlns:nsl="http://biodiversity.org.au/voc/nsl/NSL#"
        >
"""
    }

    static void rdfTail(PrintWriter w) {
        w.write("""
</rdf:RDF>
""")

    }
}
