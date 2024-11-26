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

package au.org.biodiversity.nsl.api

import au.org.biodiversity.nsl.*
import grails.converters.JSON
import grails.converters.XML
import org.apache.shiro.SecurityUtils

import javax.servlet.http.Cookie

class SearchController implements RequestUtil {
    ConfigService configService
    SearchService searchService
    FlatViewService flatViewService
    def maxResults = 100

    def index() {
        log.warn "SearchController: tree: ${params?.tree}"
        redirect ([action: 'taxonomy', permanent: true])
    }

    def product(String product) {
        log.debug "Search product $product"
        String prod = product.toLowerCase()
        Tree productTree = Tree.list().find { Tree t ->
            t.name.toLowerCase() == prod
        }
        if (productTree) {
            redirect(action: 'taxonomy')
        } else {
            redirect(action: 'names')
        }
    }

    def taxonomy(Integer max) {

        log.debug "Taxon search"

        String referer = request.getHeader('Referer')
        String remoteIP = remoteAddress(request)
        log.info "Search params $params, Referer: ${referer}, Remote: ${remoteIP}"
        max = max ?: maxResults
        params.display = 'apc'
        //cater for searches that dont use the form
        if (params.name && params.search != 'true' && params.advanced != 'true' && params.nameCheck != 'true') {
            params.search = 'true'
        }

        List<Tree> trees = Tree.list()

        String lowerProductName = (params.product as String)?.toLowerCase()

        Tree productTree = Tree.list().find { Tree t ->
            t.name.toLowerCase() == lowerProductName
        }

        Tree tree = productTree ?: Tree.findByAcceptedTree(true)

        String productName = params.product = tree.name

        if (!tree) {
            redirect(view: '404')
            return
        }
        params.tree = [id: tree.id]

        Map incMap = searchService.checked(params, 'inc')

        if (incMap.isEmpty()) {
            incMap = defaultIncMap()
        }

        params.inc = incMap
        saveIncludeCookie(incMap)

        List displayFormats = ['apni', 'apc']
        if (params.search == 'true' || params.advanced == 'true' || params.nameCheck == 'true') {
            log.debug "doing search"
            Map results = searchService.searchForName(params, max)
            List models = results.names
            if (results.message) {
                flash.message = results.message
            }
            withFormat {
                html {
                    String viewName = models.size() ? 'taxonomy-results' : 'taxonomy-no-results'
                    return render(view: viewName,
                            model: [names         : models,
                                    query         : params,
                                    treeSearch    : tree != null,
                                    count         : results.count,
                                    total         : results.total,
                                    queryTime     : results.queryTime,
                                    max           : max,
                                    displayFormats: displayFormats,
                                    trees         : trees
                            ]
                    )
                }
                json {
                    return render(contentType: 'application/json') { models }
                }
                xml {
                    return render(models as XML)
                }
            }
        }
        return [query: params, max: max, displayFormats: displayFormats, treeSearch: false, trees: []]

    }

    def names(Integer max) {
        log.debug "Name search"

        String referer = request.getHeader('Referer')
        String remoteIP = remoteAddress(request)
        log.info "Search params $params, Referer: ${referer}, Remote: ${remoteIP}"

        max = max ?: maxResults

        params.product = configService.nameTreeName
        params.display = 'apni'
        //cater for searches that dont use the form
        if (params.name && params.search != 'true' && params.advanced != 'true' && params.nameCheck != 'true') {
            params.search = 'true'
        }

        Map incMap = searchService.checked(params, 'inc')

        if (incMap.isEmpty()) {
            incMap = defaultIncMap()
        }

        params.inc = incMap

        saveIncludeCookie(incMap)

        List displayFormats = ['apni', 'apc']
        if (params.search == 'true' || params.advanced == 'true' || params.nameCheck == 'true') {
            log.debug "doing search"
            Map results = searchService.searchForName(params, max)
            List models = results.names
            if (results.message) {
                flash.message = results.message
            }
            withFormat {
                html {
                    String viewName = models.size() ? 'names-results' : 'names-no-results'
                    return render(view: viewName,
                            model: [names         : models,
                                    query         : params,
                                    count         : results.count,
                                    total         : results.total,
                                    queryTime     : results.queryTime,
                                    max           : max,
                                    displayFormats: displayFormats
                            ]
                    )
                }
                json {
                    return render(contentType: 'application/json') { models }
                }
                xml {
                    return render(models as XML)
                }
            }
        }
        return [query: params, max: max, displayFormats: displayFormats]
    }

    private saveIncludeCookie(Map incMap) {
        String cookieData = (incMap as JSON).toString()
        Cookie incCookie = new Cookie("searchInclude", (String) cookieData.encodeAsBase64())
        incCookie.maxAge = 3600 //1 hour
        response.addCookie(incCookie)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private Map defaultIncMap() {
        Map incMap = [scientific: 'on']
        String inc = g.cookie(name: 'searchInclude')
        if (inc) {
            if (!inc.startsWith('{')) {
                inc = new String(inc.decodeBase64())
            }
            log.debug "cookie $inc"
            if (inc == '{}') {
                return incMap
            }
            try {
                incMap = JSON.parse(inc) as Map

            } catch (Exception e) {
                log.error "cookie $inc caused error $e.message"
            }
        }
        return incMap
    }

    private static Tree determineTree(String treeId) {
        if (!treeId) {
            return null
        }
        if (treeId.isLong()) {
            return Tree.get(treeId as Long)
        }
        Tree.findByNameIlike(treeId)
    }

    def searchForm() {
        if (!params.product && !SecurityUtils.subject?.authenticated) {
            params.product = configService.nameTreeName
        }

        Boolean treeSearch = params.product && params.product != configService.nameTreeName

        if (treeSearch) {
            Tree tree = Tree.findByNameIlike(params.product as String)
            if (tree) {
                params.product = tree.name //force to the correct case for a product label
                params.tree = [id: tree.id]
                params.display = 'apc'
            } else {
                flash.message = "Unknown product ${params.product}"
                return redirect(url: '/search')
            }
        } else {
            params.display = params.display ?: 'apni'
        }

        render([template: 'advanced-search-form', model: [query: params, max: 100]])
    }

    def nameCheck(Integer max) {
        List<Map> results = searchService.nameCheck(params, max)
        params.product = configService.nameTreeName
        String treeName = configService.classificationTreeName
        if (params.csv) {
            render(file: renderCsvResults(results).bytes, contentType: 'text/csv', fileName: 'name-check.csv')
        } else {
            String viewName = results ? 'name-check-results' : 'name-check'
            render(view: viewName, model: [results: results, query: params, max: max, treeName: treeName])
        }
    }

    private String renderCsvResults(List<Map> results) {
        List<List> csvResults = []
        List<String> flatViewExportFields = [
                'canonicalName',
                'scientificNameID',
                'scientificNameAuthorship',
                'taxonRank',
                'taxonRankSortOrder',
                'taxonRankAbbreviation',
                'kingdom',
                'class',
                'subclass',
                'family',
                'genericName',
                'specificEpithet',
                'infraspecificEpithet',
                'nameElement',
                'firstHybridParentName',
                'firstHybridParentNameID',
                'secondHybridParentName',
                'secondHybridParentNameID'
        ]
        List<String> headers = ['Found?', 'Search term', 'Census', 'Matched name(s)', 'Name status', 'Name type', 'Tags']
        headers.addAll(flatViewExportFields)
        results.each { Map result ->
            if (result.names.empty) {
                csvResults.add([result.found,
                                result.query,
                                '',
                                'not found',
                                '',
                                '',
                                ''
                ])
            } else {
                result.names.each { Map nameData ->
                    Map flatViewRow = flatViewService.findNameRow(nameData.name as Name)
                    String census = ''
                    if (nameData.treeVersionElement?.treeElement) {
                        census = nameData.treeVersionElement?.treeElement?.excluded ? 'APC Excluded' : 'APC'
                    }
                    List values = [result.found,
                                   result.query,
                                   census,
                                   nameData.name.fullName,
                                   nameData.name.nameStatus.name,
                                   nameData.name.nameType.name,
                                   (nameData.name.tags.collect { NameTagName tag -> tag.tag.name }).toString(),

                    ]
                    flatViewExportFields.each { fieldName ->
                        if (flatViewRow) {
                            values.add(flatViewRow[fieldName] ?: '')
                        } else {
                            values.add('Not available')
                        }
                    }
                    csvResults.add(values)
                }
            }
        }
        return CsvRenderer.renderAsCsv(headers, csvResults)
    }

}
