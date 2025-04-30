*
    Copyright 2015 Australian National Botanic Gardens

    This file is part of NSL tree services plugin project.

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

class TreeServicesTagLib {
    @SuppressWarnings("GroovyUnusedDeclaration")
    static defaultEncodeAs = [taglib: 'raw']
    static namespace = "tree"

    TreeService treeService

    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    def elementPath = { attrs, body ->
        TreeVersionElement element = attrs.element
        Boolean excludeThis = attrs.excludeThis ?: false
        String var = attrs.var
        List<TreeVersionElement> path = treeService.getElementPath(element)
        log.debug "Path $path"
        if (excludeThis) {
            path.remove(element)
        }
        String separator = ''
        path.each { TreeVersionElement pathElement ->
            out << separator
            out << body("$var": pathElement)
            separator = attrs.separator
        }
    }

    def diffProfiles = { attrs, body ->
        Map profileA = (attrs.a ?: [:]) as Map
        Map profileB = (attrs.b ?: [:]) as Map
        List<String> allKeys = (profileA.keySet() + profileB.keySet()).sort() as List<String>
        allKeys.each { String key ->
            String valueA = profileA[key]?.value
            String valueB = profileB[key]?.value

            TextDiff diffForward = new TextDiff(valueA, valueB)
            TextDiff diffBackward = new TextDiff(valueB, valueA)
            out << body(key: key, diffProfileA: diffBackward.diffHtml('span', 'missing'), diffProfileB: diffForward.diffHtml('span', 'added'))
        }
    }

    def profile = { attrs ->
        Map profileData = attrs.profile as Map
        if (profileData) {
            out << "<dl class='dl-horizontal'>"
            profileData.each { k, v ->
                if (k) {
                    out << "<dt>$k</dt><dd>${v.value}"
                }
                if (v.previous) {
                    out << '&nbsp;<span class="toggleNext"><i class="fa fa-clock-o"></i><i style="display: none" class="fa fa-circle"></i></span>' +
                            '<div style="display: none" class="previous"><ul>'
                    previous(v.previous).each {
                        out << "<li>${it.value ?: '(Blank)'} <span class='small text-muted'><date>${it.updated_at}</date></span></li>"
                    }
                    out << '</ul></div>'
                }
                out << "</dd>"
            }
            out << "</dl>"
        }
    }

    def withDiffList = { attrs, body ->
        TreeChangeSet changeSet = attrs.changeSet as TreeChangeSet
        TreeVersionElement lastFamily = null
        TreeVersionElement family
        for (TreeVersionElement tve in changeSet.all) {
            family = null
            if (!(lastFamily && tve.treePath.startsWith(lastFamily.treePath))) {
                lastFamily = treeService.getFamily(tve)
                family = lastFamily
            }
            String action = 'modified'
            Boolean added = changeSet.added.contains(tve)
            Boolean removed = changeSet.removed.contains(tve)
            if (added) {
                action = 'added'
            }
            if (removed) {
                action = 'removed'
            }

            TreeVersionElement prev = changeSet.was(tve)

            out << body(tve: tve,
                    prev: prev,
                    depth: lastFamily ? (tve.depth - lastFamily.depth) : tve.depth,
                    higherRank: lastFamily == null,
                    family: family,
                    added: added,
                    removed: removed,
                    action: action)
        }
    }

    private List previous(Map data, List results = []) {
        results << data
        if (data.previous) {
            previous(data.previous, results)
        }
        results
    }

    def versionStatus = { attrs ->
        TreeVersion treeVersion = attrs.version
        if (treeVersion == treeVersion.tree.currentTreeVersion) {
            out << "current"
        } else if (!treeVersion.published) {
            out << "<span class=\"draftStamp\"></span>draft"
        } else {
            out << "old"
        }
    }

    def versionStats = { attrs, body ->
        TreeVersion treeVersion = attrs.version
        Integer count = TreeVersionElement.countByTreeVersion(treeVersion)
        out << body([elements: count])
    }

    def findCurrentVersion = { attrs, body ->
        TreeVersionElement tve = attrs.element as TreeVersionElement
        if (tve && tve.treeVersion != tve.treeVersion.tree.currentTreeVersion) { //blank if current version
            TreeVersionElement currentTve = treeService.findCurrentTreeVersionElement(tve)
            if (currentTve) {
                out << body(synonym: false, currentElement: currentTve)
            } else {
                List<TreeVersionElement> currentTves = tve ? treeService.findCurrentTreeVersionElementAsSynonym(tve) : null
                out << body(synonym: true, elements: currentTves)
            }
        }
    }

    def previously = { attrs, body ->
        TreeVersionElement tve = attrs.element
        TreeVersionElement lastChanged = treeService.lastChangeVersion(tve)
        if (lastChanged) {
            out << body(lastChanged: lastChanged)
        }
    }

    def history = { attrs, body ->
        TreeVersionElement tve = attrs.element
        List<TreeVersionElement> history = treeService.historyForName(tve.treeElement.nameId, tve.treeVersion.tree)
        Integer index = 0
        for (element in history) {
            out << body(historyElement: element, currentPos: tve.elementLink == element.elementLink, index: index++)
        }
    }

    def drafts = { attrs, body ->
        Tree tree = attrs.tree
        List<TreeVersion> drafts = TreeVersion.findAllWhere(tree: tree, published: false)
        drafts.each { TreeVersion draft ->
            out << body(draft: draft, defaultDraft: draft.id == tree.defaultDraftTreeVersion?.id)
        }
    }

    def commonSynonyms = { attrs, body ->
        List<Map> results = attrs.results
        if (results) {
            for (Map result in results) {
                Name synonym = result.commonSynonym
                List<Map> names = result.elements as List<Map>
                if (names.size() > 2) {
                    log.debug "multiple common synonyms for ${result.commonSynonym}"
                    List<Map> commonSyns = names.groupBy { it.tree_link }.collect { it.value.first() }
                    for (int i = 1; i < commonSyns.size(); i++) {
                        out << body(namePath: synonym.namePath, synonym: synonym.fullNameHtml, name1: commonSyns[0], name2: commonSyns[i])
                    }
                } else {
                    out << body(namePath: synonym.namePath, synonym: synonym.fullNameHtml, name1: names[0], name2: names[1])
                }
            }
        }
    }

    def diffSynonyms = { attrs, body ->
        use(SynonymDiffMarker) {

            String synA = attrs.a ?: ''
            String synB = attrs.b ?: ''

            // split synonyms onto new lines
            ABPair input = new ABPair(splitSynonyms(synA), splitSynonyms(synB))
            ABPair output = input.markUpTagChanges('name', true)
                                 .markUpTagChanges('type')
                                 .markUpTagChanges('year')
                                 .markUpTagChanges('citation')
                                 .markUpTagChanges('name-status')

            String diffA = '<synonyms>' + output.a.join('\n') + '</synonyms>'
            String diffB = '<synonyms>' + output.b.join('\n') + '</synonyms>'

            out << body(diffA: diffA, diffB: diffB)
        }
    }

    private static List<String> splitSynonyms(String syn) {
        return (syn)?.replaceAll('</?synonyms>', '')
                    ?.replaceAll('<(tax|nom|mis|syn)>', '::<$1>')
                    ?.split('::')
    }

    def diffPath = { attrs, body ->

        String pathA = attrs.a ?: ''
        String pathB = attrs.b ?: ''

        // split path onto new lines
        List<String> a = (pathA)?.split('/')
        List<String> b = (pathB)?.split('/')

        int size = Math.max(a.size(), b.size())
        0.upto(size - 1) { i ->
            String oldLine = a[i]
            String newLine = b[i]
            if (oldLine && !b.contains(oldLine)) {
                a[i] = '<span class="targetHighlight">' + oldLine + '</span>'
            }

            if (newLine && !a.contains(newLine)) {
                b[i] = '<span class="targetHighlight">' + newLine + '</span>'
            }
        }
        out << body(pathA: a.join(' / '), pathB: b.join(' / '))
    }

    def prettyNamePath = { attrs ->
        String path = attrs.path
        out << path.split('/').join(' / ')
    }

    def children = { attrs ->
        TreeVersionElement tve = attrs.tve
        if (tve) {
            out << treeService.countAllChildElements(tve)
        }
    }

}
