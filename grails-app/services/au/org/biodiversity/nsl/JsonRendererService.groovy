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

import grails.converters.JSON
import grails.converters.XML
import org.hibernate.Hibernate
import org.hibernate.proxy.HibernateProxy

class JsonRendererService {

    def grailsApplication
    def linkService
    def instanceService
    def treeService

    def registerObjectMashallers() {
        JSON.registerObjectMarshaller(Namespace) { Namespace namespace -> getBriefNamespace(namespace) }
        JSON.registerObjectMarshaller(Name) { Name name -> marshallName(name) }
        JSON.registerObjectMarshaller(Instance) { Instance instance -> marshallInstance(instance) }
        JSON.registerObjectMarshaller(Reference) { Reference reference -> marshallReference(reference) }
        JSON.registerObjectMarshaller(Author) { Author author -> marshallAuthor(author) }
        JSON.registerObjectMarshaller(InstanceNote) { InstanceNote instanceNote -> marshallInstanceNote(instanceNote) }
        JSON.registerObjectMarshaller(Tree) { Tree tree -> marshallTree(tree) }
        JSON.registerObjectMarshaller(TreeVersion) { TreeVersion treeVersion -> marshallTreeVersion(treeVersion) }
        JSON.registerObjectMarshaller(TreeElement) { TreeElement treeElement -> marshallTreeElement(treeElement) }
        JSON.registerObjectMarshaller(TveDiff) { TveDiff tveDiff -> marshallTveDiff(tveDiff) }
        JSON.registerObjectMarshaller(MergeReport) { MergeReport mergeReport -> marshallMergReport(mergeReport) }
        //todo remove
        JSON.registerObjectMarshaller(TreeVersionElement) { TreeVersionElement treeVersionElement -> marshallTreeVersionElement(treeVersionElement) }

        XML.registerObjectMarshaller(new XmlMapMarshaller())
        XML.registerObjectMarshaller(Namespace) { Namespace namespace, XML xml -> xml.convertAnother(getBriefNamespace(namespace)) }
        XML.registerObjectMarshaller(Name) { Name name, XML xml -> xml.convertAnother(marshallName(name)) }
        XML.registerObjectMarshaller(Instance) { Instance instance, XML xml -> xml.convertAnother(marshallInstance(instance)) }
        XML.registerObjectMarshaller(Reference) { Reference reference, XML xml -> xml.convertAnother(marshallReference(reference)) }
        XML.registerObjectMarshaller(Author) { Author author, XML xml -> xml.convertAnother(marshallAuthor(author)) }
        XML.registerObjectMarshaller(InstanceNote) { InstanceNote instanceNote, XML xml -> xml.convertAnother(marshallInstanceNote(instanceNote)) }
        XML.registerObjectMarshaller(ResourceLink) { ResourceLink resourceLink, XML xml ->
            xml.startNode('link')
               .attribute('resources', resourceLink.resources.toString())
               .chars(resourceLink.link)
               .end()
        }
    }

    // we need this anywhere that citation and citationHtml appear as fields
    static String citationAuthYear(Reference reference) {
        if (reference) {
            return "${reference.author?.abbrev ?: reference.author?.name ?: reference.author?.fullName}, ${reference.getIsoYear()}"
        } else {
            return null
        }
    }

    private List treeElementChildren(TreeVersionElement treeVersionElement) {
        List<DisplayElement> childDisplayElements = treeService.childDisplayElements(treeVersionElement)
        List kids = []
        childDisplayElements.each { DisplayElement item ->
            kids.add(item.asMap())
        }
        return kids
    }


    Map getBriefNamespace(Namespace namespace) {
        [
                class          : namespace?.class?.name,
                name           : namespace?.name,
                descriptionHtml: namespace?.descriptionHtml
        ]
    }

    Map getBriefName(Name name) {
        brief(name, [
                nameElement : name?.nameElement,
                fullNameHtml: name?.fullNameHtml
        ])
    }

    Map getBriefReference(Reference reference) {
        brief(reference, [
                citation        : reference?.citation,
                citationHtml    : reference?.citationHtml,
                citationAuthYear: citationAuthYear(reference)
        ])
    }

    Map getBriefInstance(Instance instance) {
        brief(instance, [
                instanceType    : instance?.instanceType?.name,
                page            : instance?.page,
                name            : instance?.name?.fullNameHtml,
                protologue      : instance?.instanceType?.protologue,
                citation        : instance?.reference?.citation,
                citationHtml    : instance?.reference?.citationHtml,
                citationAuthYear: citationAuthYear(instance?.reference)
        ])
    }

    Map getBriefNameWithHtml(Name name) {
        brief(name, [
                nameType       : name?.nameType?.name,
                nameStatus     : name?.nameStatus?.name,
                nameRank       : name?.nameRank?.displayName,
                primaryInstance: instanceService.findPrimaryInstance(name)?.collect { Instance instance -> getBriefInstance(instance) },
                fullName       : name?.fullName,
                fullNameHtml   : name?.fullNameHtml,
                simpleName     : name?.simpleName,
                simpleNameHtml : name?.simpleNameHtml
        ])
    }

    /** Get instance with html not containing the name. This is used for the picklist of instances belonging to a name */

    Map getBriefInstanceForNameWithHtml(Instance instance) {
        brief(instance, [
                instanceType    : instance?.instanceType?.name,
                page            : instance?.page,
                citation        : instance?.reference?.citation,
                citationHtml    : instance?.reference?.citationHtml,
                citationAuthYear: citationAuthYear(instance?.reference),
                parent          : instance?.parent?.id,
                cites           : instance?.cites?.id,
                citedBy         : instance?.citedBy?.id
        ])
    }

    Map getBriefAuthor(Author author) {
        brief(author, [name: author?.abbrev])
    }

    Map brief(target, Map extra = [:]) {
        if (!target) {
            return [:]
        }
        target = initializeAndUnproxy(target)
        return [
                class : target.class.name,
                _links: getPreferredLink(target),
        ] << extra
    }

    Map getBaseInfo(target) {
        if (!target) {
            return [:]
        }
        target = initializeAndUnproxy(target)
        def links = getLinks(target)
        Map inner = [
                class : target.class.name,
                _links: links,
                audit : getAudit(target),
        ]
        String targetKey = target.class.simpleName.toLowerCase()
        Map outer = [:]
        outer[targetKey] = inner
        return outer
    }

    Map getAudit(target) {

        if (target.hasProperty('createdBy') && target.hasProperty('updatedBy')) {
            return [
                    created: [by: target.createdBy, at: target.createdAt],
                    updated: [by: target.updatedBy, at: target.updatedAt]
            ]
        }
        if (target.hasProperty('updatedBy')) {
            return [
                    updated: [by: target.updatedBy, at: target.updatedAt]
            ]
        }
        return null
    }

    Map getLinks(target) {
        try {
            ArrayList links = linkService.getLinksForObject(target)
            if (links && links.size() > 0) {
                List<ResourceLink> resourceLinks = links.collect { link -> new ResourceLink(link.resourceCount as Integer, link.link as String, link.preferred as Boolean) }
                return [permalinks: resourceLinks]
            }
        } catch (e) {
            log.debug e.message
        }
        return [permalinks: []]
    }

    Map getPreferredLink(target) {
        String link = linkService.getPreferredLinkForObject(target)
        if (link) {
            ResourceLink resourceLink = new ResourceLink(1 as Integer, link, true)
            return [permalink: resourceLink]
        }
        return [permalink: []]
    }

    Map instanceType(InstanceType instanceType) {
        [
                name           : instanceType.name,
                flags          : [
                        primaryInstance  : instanceType.primaryInstance,
                        secondaryInstance: instanceType.secondaryInstance,
                        relationship     : instanceType.relationship,
                        protologue       : instanceType.protologue,
                        taxonomic        : instanceType.taxonomic,
                        nomenclatural    : instanceType.nomenclatural,
                        synonym          : instanceType.synonym,
                        proParte         : instanceType.proParte,
                        doubtful         : instanceType.doubtful,
                        misapplied       : instanceType.misapplied,
                        standalone       : instanceType.standalone,
                        unsourced        : instanceType.unsourced,
                        citing           : instanceType.citing,
                        deprecated       : instanceType.deprecated
                ],
                sortOrder      : instanceType.sortOrder,
                rdfId          : instanceType.rdfId,
                descriptionHtml: instanceType.descriptionHtml
        ]
    }

    Map language(Language language) {
        [
                iso6391Code: language.iso6391Code,
                iso6393Code: language.iso6393Code,
                name       : language.name
        ]
    }

/** ********************/

    Map marshallName(Name name) {
        List<String> tags = (name.tags.collect { NameTagName tag ->
            tag.tag.name
        } ?: []) as List<String>
        Map data = getBaseInfo(name)
        data.name << [
                fullName         : name.fullName,
                fullNameHtml     : name.fullNameHtml,
                nameElement      : name.nameElement,
                simpleName       : name.simpleName,
                sortName         : name.sortName,
                rank             : [name: name.nameRank.displayName, sortOrder: name.nameRank.sortOrder],
                verbatimRank     : name.verbatimRank,
                type             : name.nameType.name,
                status           : name.nameStatus.name,
                tags             : tags,
                family           : getBriefName(name.family),
                parent           : getBriefName(name.parent),
                basionym         : getBriefName(nameService.getBasionym(name)),
                secondParent     : getBriefName(name.secondParent),
                instances        : name.instances.collect { getBriefInstance(it) },
                author           : getBriefAuthor(name.author),
                baseAuthor       : getBriefAuthor(name.baseAuthor),
                exAuthor         : getBriefAuthor(name.exAuthor),
                exBaseAuthor     : getBriefAuthor(name.exBaseAuthor),
                sanctioningAuthor: getBriefAuthor(name.sanctioningAuthor),
                primaryInstance  : instanceService.findPrimaryInstance(name)?.collect { Instance instance -> getBriefInstance(instance) }
        ]

        return data.name as Map
    }

    Map marshallInstance(Instance instance) {
        Map data = getBaseInfo(instance)
        data.instance << [
                verbatimNameString : instance.verbatimNameString,
                page               : instance.page,
                pageQualifier      : instance.pageQualifier,
                nomenclaturalStatus: instance.nomenclaturalStatus,
                bhlUrl             : instance.bhlUrl,
                instanceType       : instanceType(instance.instanceType),
                name               : getBriefName(instance.name),
                reference          : getBriefReference(instance.reference),
                parent             : getBriefInstance(instance.parent),
                cites              : getBriefInstance(instance.cites),
                citedBy            : getBriefInstance(instance.citedBy),
                instancesForCitedBy: instance.instancesForCitedBy.sort {
                    Instance a, Instance b ->
                        a.instanceType.sortOrder != b.instanceType.sortOrder ?
                                a.instanceType.sortOrder <=> b.instanceType.sortOrder :
                                a.name.simpleName <=> b.name.simpleName
                }.collect { getBriefInstance(it) },
                instancesForCites  : instance.instancesForCites.sort {
                    Instance a, Instance b ->
                        a.instanceType.sortOrder != b.instanceType.sortOrder ?
                                a.instanceType.sortOrder <=> b.instanceType.sortOrder :
                                a.name.simpleName <=> b.name.simpleName
                }.collect { getBriefInstance(it) },
                instancesForParent : instance.instancesForParent.sort {
                    Instance a, Instance b ->
                        a.instanceType.sortOrder != b.instanceType.sortOrder ?
                                a.instanceType.sortOrder <=> b.instanceType.sortOrder :
                                a.name.simpleName <=> b.name.simpleName
                }.collect { getBriefInstance(it) },
                instanceNotes      : instance.instanceNotes

        ]
        return data.instance as Map
    }

    Map marshallReference(Reference reference) {
        Map data = getBaseInfo(reference)
        data.reference << [
                doi              : reference.doi,
                title            : reference.title,
                displayTitle     : reference.displayTitle,
                abbrevTitle      : reference.abbrevTitle,
                year             : reference.getIsoYear()?.toInteger(),
                volume           : reference.volume,
                edition          : reference.edition,
                pages            : reference.pages,
                verbatimReference: reference.verbatimReference,
                verbatimCitation : reference.verbatimCitation,
                verbatimAuthor   : reference.verbatimAuthor,
                citation         : reference.citation,
                citationHtml     : reference.citationHtml,
                citationAuthYear : citationAuthYear(reference),
                notes            : reference.notes,
                published        : reference.published,
                publisher        : reference.publisher,
                publishedLocation: reference.publishedLocation,
                publicationDate  : reference.publicationDate,
                isbn             : reference.isbn,
                issn             : reference.issn,
                bhlUrl           : reference.bhlUrl,
                tl2              : reference.tl2,
                refType          : reference.refType.name,

                parent           : getBriefReference(reference.parent),
                author           : getBriefAuthor(reference.author),

                refAuthorRole    : reference.refAuthorRole.name,
                duplicateOf      : getBriefReference(reference.duplicateOf),
                language         : language(reference.language),
                instances        : reference.instances.collect { getBriefInstance(it) },
                parentOf         : reference.referencesForParent.collect { getBriefReference(it) }
        ]
        return data.reference as Map
    }

    Map marshallAuthor(Author author) {
        Map data = getBaseInfo(author)
        data.author << [
                abbrev          : author.abbrev,
                name            : author.name,
                fullName        : author.fullName,
                dateRange       : author.dateRange,
                notes           : author.notes,
                ipniId          : author.ipniId,
                duplicateOf     : author.duplicateOf,
                references      : author.references.collect { it.citation },
                names           : author.namesForAuthor.collect { it.fullName },
                baseNames       : author.namesForBaseAuthor.collect { it.fullName },
                exNames         : author.namesForExAuthor.collect { it.fullName },
                exBaseNames     : author.namesForExBaseAuthor.collect { it.fullName },
                sanctioningNames: author.namesForSanctioningAuthor.collect { it.fullName }
        ]
        return data.author as Map
    }

    Map marshallInstanceNote(InstanceNote instanceNote) {
        Map data = getBaseInfo(instanceNote)
        data.instancenote << [
                instanceNoteKey: instanceNote.instanceNoteKey.name,
                value          : instanceNote.value,
                instance       : getBriefInstance(instanceNote.instance)
        ]
        return data.instanceNote as Map
    }

    Map briefTree(Tree tree) {
        tree = initializeAndUnproxy(tree)
        Map data = getBaseInfo(tree)
        data.tree << [name: tree.name]
    }

    Map marshallTree(Tree tree) {
        tree = initializeAndUnproxy(tree)
        Map data = getBaseInfo(tree)
        data.tree << [
                name               : tree.name,
                groupName          : tree.groupName,
                referenceId        : tree.referenceId,
                currentVersion     : briefTreeVersion(tree.currentTreeVersion),
                defaultDraftVersion: briefTreeVersion(tree.defaultDraftTreeVersion),
                descriptionHtml    : tree.descriptionHtml,
                linkToHomePage     : tree.linkToHomePage,
                acceptedTree       : tree.acceptedTree,
                versions           : tree.treeVersions.collect { TreeVersion v -> briefTreeVersion(v) }
        ]
        return data.tree as Map
    }

    Map briefTreeVersion(TreeVersion treeVersion) {
        if (treeVersion) {
            treeVersion = initializeAndUnproxy(treeVersion)
            return [
                    versionNumber: treeVersion.id,
                    draftName    : treeVersion.draftName
            ]
        }
        return null
    }

    Map marshallTreeVersion(TreeVersion treeVersion) {
        treeVersion = initializeAndUnproxy(treeVersion)
        Map data = getBaseInfo(treeVersion)
        data.treeversion <<
                [
                        versionNumber     : treeVersion.id,
                        draftName         : treeVersion.draftName,
                        tree              : briefTree(treeVersion.tree),
                        firstOrderChildren: treeService.displayElementsToDepth(treeVersion, 1)
                ]
        return data.treeversion as Map
    }

    Map marshallTreeVersionElement(TreeVersionElement treeVersionElement) {
        treeVersionElement = initializeAndUnproxy(treeVersionElement)
        TreeElement treeElement = treeVersionElement.treeElement
        return [treeElement:
                        [
                                class           : treeElement.class.name,
                                _links          : [
                                        elementLink      : treeVersionElement.fullElementLink(),
                                        taxonLink        : treeVersionElement.fullTaxonLink(),
                                        parentElementLink: treeVersionElement.parent?.fullElementLink(),
                                        nameLink         : treeElement.nameLink,
                                        instanceLink     : treeElement.instanceLink,
                                        sourceElementLink: treeElement.sourceElementLink,
                                ],
                                tree            : briefTree(treeVersionElement.treeVersion.tree),
                                simpleName      : treeElement.simpleName,
                                namePath        : treeVersionElement.namePath,
                                treePath        : treeVersionElement.treePath,
                                displayHtml     : treeElement.displayHtml,
                                sourceShard     : treeElement.sourceShard,
                                synonymsHtml    : treeElement.synonymsHtml,
                                synonyms        : treeElement.synonyms,
                                profile         : treeElement.profile,
                                children        : treeElementChildren(treeVersionElement),
                                versionUpdatedBy: treeVersionElement.updatedBy,
                                versionUpdatedAt: treeVersionElement.updatedAt,
                                elementUpdatedBy: treeElement.updatedBy,
                                elementUpdatedAt: treeElement.updatedAt
                        ]
        ]
    }

    Map marshallTreeElement(TreeElement treeElement) {
        treeElement = initializeAndUnproxy(treeElement)
        return [treeElement:
                        [
                                class       : treeElement.class.name,
                                _links      : [
                                        elementLink      : linkService.getPreferredLinkForObject(treeElement),
                                        nameLink         : treeElement.nameLink,
                                        instanceLink     : treeElement.instanceLink,
                                        sourceElementLink: treeElement.sourceElementLink,
                                ],
                                simpleName  : treeElement.simpleName,
                                displayHtml : treeElement.displayHtml,
                                sourceShard : treeElement.sourceShard,
                                synonymsHtml: treeElement.synonymsHtml,
                                synonyms    : treeElement.synonyms,
                                profile     : treeElement.profile,
                                updatedBy   : treeElement.updatedBy,
                                updatedAt   : treeElement.updatedAt
                        ],
                NOTE       : 'You probably want a TreeVersionElement'
        ]
    }

    Map marshallMergReport(MergeReport mergeReport) {
        return [mergeReport: [
                class        : mergeReport.class.name,
                fromVersionId: mergeReport.from.id,
                toVersionId  : mergeReport.to.id,
                upToDate     : mergeReport.upToDate,
                conflicts    : mergeReport.conflicts,
                nonConflicts : mergeReport.nonConflicts
        ]]
    }

    Map marshallTveDiff(TveDiff tveDiff) {
        return [tveDiff: [
                class         : tveDiff.class.name,
                id            : tveDiff.id,
                parentId      : tveDiff.parentId,
                from          : tveDiff.from?.elementLink,
                to            : tveDiff.to?.elementLink,
                fromType      : tveDiff.fromType,
                toType        : tveDiff.toType,
                fromTypeString: tveDiff.fromTypeString,
                toTypeString  : tveDiff.toTypeString
        ]]
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    static <T> T initializeAndUnproxy(T entity) {
        if (entity == null) {
            throw new NullPointerException("Entity passed for initialization is null")
        }

        Hibernate.initialize(entity)
        if (entity instanceof HibernateProxy) {
            entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer()
                                                  .getImplementation()
        }
        return entity
    }
}

class ResourceLink {
    final Integer resources
    final String link
    final Boolean preferred

    ResourceLink(Integer count, String link, Boolean preferred) {
        this.resources = count
        this.link = link
        this.preferred = preferred
    }
}
