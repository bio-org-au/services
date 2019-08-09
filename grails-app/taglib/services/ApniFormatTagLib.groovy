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

package services

import au.org.biodiversity.nsl.*
import net.htmlparser.jericho.Source
import net.htmlparser.jericho.SourceFormatter
import org.apache.shiro.SecurityUtils

class ApniFormatTagLib {

    def nameConstructionService
    def treeService
    def instanceService
    LinkService linkService
    def configService

    @SuppressWarnings("GroovyUnusedDeclaration")
    static defaultEncodeAs = 'raw'

    static namespace = "af"

    def getTypeNotes = { attrs, body ->
        List<String> types = ['Type', 'Lectotype', 'Neotype']
        filterNotes(attrs, types, body)
    }

    def getDisplayableNonTypeNotes = { attrs, body ->
        List<String> types = ['Type', 'Lectotype', 'Neotype', 'EPBC Advice', 'EPBC Impact', 'Synonym']
        if (attrs.incApc) {
            types += ['APC Comment', 'APC Dist.']
        }
        filterNotes(attrs, types, body, true)
    }

    def getAPCNotes = { attrs, body ->
        List<String> types = ['APC Comment', 'APC Dist.']
        filterNotes(attrs, types, body)
    }

    def ifOnTree = { attrs, body ->
        TreeVersionElement tve = attrs.tve
        Instance instance = attrs.instance
        if (tve && tve.treeElement.instanceId == instance?.id) {
            out << body()
        }
    }

    def rangeOnAcceptedTree = { attrs, body ->
        Instance instance = attrs.instance
        def (TreeVersionElement first, TreeVersionElement last) = treeService.findFirstAndLastElementForInstance(instance,
                Tree.findByAcceptedTree(true))
        if (first) {
            Boolean current = last.treeVersion.id == last.treeVersion.tree.currentTreeVersion.id
            out << body("first": first, "last": last, "current": current)
        }
    }

    def ifEverOnAcceptedTree = { attrs, body ->
        Instance instance = attrs.instance
        TreeVersionElement excludedTve = attrs.exclude
        TreeVersionElement tve = treeService.findLatestElementForInstance(instance,
                Tree.findByAcceptedTree(true))
        if (tve && tve != excludedTve) {
            out << body("tve": tve)
        }
    }

    def ifNeverOnAcceptedTreeSet = { attrs, body ->
        Instance instance = attrs.instance
        String varName = attrs.var
        Object settable = attrs.set ?: true
        TreeVersionElement tve = treeService.findLatestElementForInstance(instance,
                Tree.findByAcceptedTree(true))
        out << body((varName): tve ? settable : null)
    }

    def treeComment = { attrs, body ->
        TreeVersionElement tve = attrs.tve
        String var = attrs.var ?: "note"
        Boolean showEmpty = attrs.showEmpty
        Boolean create = attrs.createIfNull
        if (tve) {
            Map comment = treeService.profileComment(tve)
            if (comment && (showEmpty || comment.value)) {
                out << body((var): comment)
            } else if (create) {
                comment = new ProfileValue('Enter new comment', SecurityUtils.subject.principal.toString()).toMap()
                out << body((var): comment)
            }
        }
    }

    def treeDistribution = { attrs, body ->
        TreeVersionElement tve = attrs.tve
        String var = attrs.var ?: "note"
        Boolean showEmpty = attrs.showEmpty
        Boolean create = attrs.createIfNull
        if (tve) {
            Map dist = treeService.profileDistribution(tve)
            if (dist && (showEmpty || dist.value)) {
                out << body((var): dist)
            } else if (create) {
                dist = new ProfileValue('Enter new distribution', SecurityUtils.subject.principal.toString()).toMap()
                out << body((var): dist)
            }
        }
    }

    def previousComments = { attrs, body ->
        TreeVersionElement tve = attrs.tve
        String var = attrs.var ?: "note"
        if (tve) {
            Map comment = treeService.profileComment(tve)
            while (comment && comment.previous) {
                comment = comment.previous
                out << body((var): comment)
            }
        }
    }

    def previousDistribution = { attrs, body ->
        TreeVersionElement tve = attrs.tve
        String var = attrs.var ?: "note"
        if (tve) {
            Map dist = treeService.profileDistribution(tve)
            while (dist && dist.previous) {
                dist = dist.previous
                out << body((var): dist)
            }
        }
    }

    private void filterNotes(Map attrs, List<String> types, body, boolean invertMatch = false) {
        Instance instance = attrs.instance
        String var = attrs.var ?: "note"
        def notes = instance.instanceNotes.findAll { InstanceNote note ->
            invertMatch ^ (types.contains(note.instanceNoteKey.name))
        }
        notes.sort { it.instanceNoteKey.sortOrder }.each { InstanceNote note ->
            out << body((var): note)
        }
    }

    def tidy = { attr ->
        Source source = new Source(attr.text as String)
        SourceFormatter sf = source.getSourceFormatter()
        out << sf.toString()
    }

    def sortedReferences = { attrs, body ->
        List<Instance> instances = new ArrayList<>(attrs.instances as Set)
        String var = attrs.var ?: "instance"
        String sortOn = attrs.sortOn
        instances.sort { a1, b1 ->
            InstanceService.compareReferences(a1, b1, sortOn)
        }.each { Instance instance ->
            out << body((var): instance)
        }
    }

    def sortedInstances = { attrs, body ->
        List<Instance> instances = new ArrayList<>(attrs.instances as Set)
        String var = attrs.var ?: "instance"
        String page = 'Not a page'
        Instance citedBy = null
        try {
            instances = instanceService.sortInstances(instances)
            instances.eachWithIndex { Instance instance, Integer i ->
                Boolean showRef = page != instance.page || instance.citedBy != citedBy
                out << body((var): instance, i: i, newPage: showRef)
                page = instance.page
                citedBy = instance.citedBy
            }
        } catch (e) {
            println e.message
        }
    }

    def apcSortedInstances = { attrs, body ->
        List<Instance> instances = new ArrayList<>(attrs.instances as Set)
        String var = attrs.var ?: "instance"
        instances.sort { a, b ->
            if (a.name.simpleName == b.name.simpleName) {
                if (a.cites?.reference?.year == b.cites?.reference?.year) {
                    if (a.cites?.reference == b.cites?.reference) {
                        if (a.cites.page == b.cites.page) {
                            return b.cites.id <=> a.cites.id
                        }
                        return b.cites.page <=> a.cites.page
                    }
                    return a.cites?.reference?.citation <=> b.cites?.reference?.citation
                }
                return (a.cites?.reference?.year) <=> (b.cites?.reference?.year)
            }
            return a.name.simpleName <=> b.name.simpleName
        }.each { Instance instance ->
            out << body((var): instance)
        }
    }


    def author = { attrs ->
        Name name = attrs.name
        out << nameConstructionService.constructAuthor(name)
    }

    def harvard = { attrs ->
        Reference reference = attrs.reference
        out << "<span title=\"${reference.citation}\">${reference.author.name} ($reference.getIsoYear())</span>"
    }

    def branch = { attrs, body ->
        Name name = attrs.name
        String treeName = attrs.tree ?: configService.classificationTreeName
        TreeVersionElement treeVersionElement = treeService.findCurrentElementForName(name, treeService.getTree(treeName))
        if (treeVersionElement) {
            out << "<branch title=\"click to see branch in $treeName.\">"
            out << body()

            List<TreeVersionElement> elementPath = treeService.getElementPath(treeVersionElement)

            out << '<ul>'
            out << "<li>$treeName</li>"
            elementPath.each { TreeVersionElement tve ->
                String link = tve.fullElementLink()
                if (link) {
                    out << "<li><a href='${link}'>${tve.treeElement.name?.nameElement}</a> <span class=\"text-muted\">(${tve.treeElement.name?.nameRank?.abbrev})</span></li>"
                } else {
                    out << "<li>${tve.treeElement.name?.nameElement} <span class=\"text-muted\">(${tve.treeElement.name?.nameRank?.abbrev})</span></li>"
                }
            }
            out << '</ul></branch>'
        }
    }

    def page = { attrs ->
        Instance instance = attrs.instance
        if (instance.page) {
            out << ApniFormatService.transformXics(instance.page)
        } else {
            if (instance.instanceType.citing && instance.citedBy.page) {
                if (instance.instanceType.name.contains('common')) {
                    out << "~ ${ApniFormatService.transformXics(instance.citedBy.page)}"
                } else {
                    out << "${ApniFormatService.transformXics(instance.citedBy.page)}"
                }
            } else {
                out << '-'
            }
        }
    }

    def bhlLink = { attrs ->
        Name name = attrs.name
        out << "<bhl>"
        out << "<a href='http://www.biodiversitylibrary.org/name/${name.simpleName.replaceAll(' ', '_')}'>BHL <span class='fa fa-external-link'></span></a>"
        out << " <bhl>"
    }

    def refNameTreeSearchLink = { attrs ->
        String citation = attrs.citation
        String product = attrs.product
        Map params = [publication: citation, search: true, advanced: true, display: 'apni']
        if (product) {
            params << [product: product]
        } else {
            //if not logged in force to name tree name
            if (!SecurityUtils.subject?.principal) {
                params << [product: configService.nameTreeName]
            }
        }
        out << g.createLink(absolute: true, controller: 'search', action: 'search', params: params)
    }

    def refAPCSearchLink = { attrs ->
        String citation = attrs.citation
        String product = attrs.product
        Map params = [publication: citation, search: true, advanced: true, display: 'apc', 'tree.id': 1133571] //todo remove hard coded reference number
        if (product) {
            params << [product: product]
        } else {
            //if not logged in force to APC
            if (!SecurityUtils.subject?.principal) {
                params << [product: 'apc']
            }
        }
        out << g.createLink(controller: 'search', action: 'search', params: params)
    }

    def apniLink = { attrs ->
        String link = attrs.link
        if (!link) {
            Name name = attrs.name
            link = linkService.getPreferredLinkForObject(name) + '/api/apni-format'
        }
        if (link) {
            out << """<apnilink>
      <a class="vertbar" href="${link}">
        <i class="fa fa-list-alt see-through"></i>
        apni
      </a>
    </apnilink>"""
        }
    }

    def tick = { attrs ->
        if (attrs.val) {
            out << '<i class="fa fa-check-square-o"></i>'
        } else {
            out << '<i class="fa fa-square-o"></i>'
        }
    }

    /**
     * add an "apc" tag with link and title for a given tree element
     * you must provide an "element" attribute.
     * if an "instance" attribute is supplied it is compared with the element attribute to decide if to display
     * the apc tag.
     * This is generic and can be used on any tree.
     *
     * attrs.element is a TreeVersionElement
     *
     */
    def onTree = { attrs ->
        TreeVersionElement treeVersionElement = attrs.element
        if (treeVersionElement) {
            TreeElement treeElement = treeVersionElement.treeElement
            Instance instance = attrs.instance ?: treeElement.instance
            if (treeElement && instance && treeElement.instance.id == instance.id) {
                String link = g.createLink(absolute: true, controller: 'apcFormat', action: 'display', id: treeElement.name.id)
                String tree = treeVersionElement.treeVersion.tree.name
                Boolean previous = treeVersionElement.treeVersion.published && (treeVersionElement.treeVersion != treeVersionElement.treeVersion.tree.currentTreeVersion)
                if (!treeVersionElement.treeVersion.published) {
                    tree += ": ${treeVersionElement.treeVersion.draftName}"
                }

                out << """<a href="${link}">""".toString()

                if (treeElement.excluded) {
                    out << """<apc title="excluded from $tree"><i class="fa fa-ban"></i> ${tree}</apc>""".toString()
                } else {
                    out << """<apc title="$tree concept"><i class="fa fa-check"></i>${tree}</apc>""".toString()
                }
                out << "</a>"
                if (previous) {
                    out << "&nbsp;<span class=\"text-muted\">(version ${treeVersionElement.treeVersion.publishedAt.format('dd/MM/yyyy')})</span>"
                }
            }
        }
    }

    def legacyAPCInstanceNotes = { attrs, body ->
        Instance instance = attrs.instance as Instance
        Map notes = [:]
        notes.comment = instance.instanceNotes.find { InstanceNote note ->
            note.instanceNoteKey.name == 'APC Comment'
        }
        notes.dist = instance.instanceNotes.find { InstanceNote note ->
            note.instanceNoteKey.name == 'APC Dist.'
        }
        if (notes.comment || notes.dist) {
            out << body(notes: notes)
        }
    }

    def nameResources = { attrs, body ->
        Name name = attrs.name as Name
        name.resources.each { Resource r ->
            if (r.resourceType.display) {
                out << body(res: r)
            }
        }
    }

    def resourceIcon = {attrs ->
        Resource r = attrs.resource as Resource
        if(r.resourceType.cssIcon){
            out << "<i class='${r.resourceType.cssIcon}'></i>"
        }
    }
}
