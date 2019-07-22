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
    def configService
    def searchService
    FlatViewService flatViewService

    def search(Integer max, String product) {

        log.debug "Product set to $product"

        String referer = request.getHeader('Referer')
        String remoteIP = remoteAddress(request)
        log.info "Search params $params, Referer: ${referer}, Remote: ${remoteIP}"
        max = max ?: 100
        List<Tree> trees = Tree.list()

        String lowerProductName = (params.product as String)?.toLowerCase()
        String defaultProduct = configService.nameTreeName

        Map treeProducts = [:]
        Tree.list().each { Tree t ->
            treeProducts.put(t.name.toLowerCase(), t.name)
        }

        Boolean knownTreeProduct = treeProducts.keySet().contains(lowerProductName)

        // this is quick fix - ultimate fix is split the search into taxonomic and name
        params.display = params.display ?: (knownTreeProduct ? 'apc' : 'apni') //if not set and not a tree0 'product' set it to apni by default

        if (knownTreeProduct) {
            params.product = treeProducts[lowerProductName] //preserve case ??
        } else {
            if (!SecurityUtils.subject?.authenticated) {
                params.product = defaultProduct
            }
        }
        String productName = params.product

        Tree tree = determineTree(params.tree?.id ?: productName)

        if (tree) {
            params.tree = [id: tree.id]
            params.display = params.display ?: 'apc'
            if (productName != tree.name && !SecurityUtils.subject?.authenticated) {
                params.product = tree.name
            }
        } else {
            params.remove('tree.id')
            params.remove('tree')
        }

        Map incMap = searchService.checked(params, 'inc')

        //cater for searches that dont use the form
        if (params.name && params.search != 'true' && params.advanced != 'true' && params.nameCheck != 'true') {
            params.search = 'true'
        }

        if (incMap.isEmpty() && params.search != 'true' && params.advanced != 'true' && params.nameCheck != 'true') {
            String inc = g.cookie(name: 'searchInclude')
            if (inc) {
                if (!inc.startsWith('{')) {
                    inc = new String(inc.decodeBase64())
                }
                log.debug "cookie $inc"
                try {
                    incMap = JSON.parse(inc) as Map
                } catch (e) {
                    log.error "cookie $inc caused error $e.message"
                    incMap = [scientific: 'on']
                }
            } else {
                incMap = [scientific: 'on']
            }
        }

        params.inc = incMap

        String cookieData = (incMap as JSON).toString()
        Cookie incCookie = new Cookie("searchInclude", cookieData.encodeAsBase64())
        incCookie.maxAge = 3600 //1 hour
        response.addCookie(incCookie)

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
                    return render(view: 'search',
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


        if (params.sparql && !params.product) {
            // we do not do the searching here, instead we re-render the page in sparql mode
            // when the page is re-rendered like this, it will fire off its client-side search.
            log.debug "re-rendering in sparql mode"

            Map uriPrefixes = [
                    'http://www.w3.org/2001/XMLSchema#'                   : 'xs',
                    'http://www.w3.org/1999/02/22-rdf-syntax-ns#'         : 'rdf',
                    'http://www.w3.org/2000/01/rdf-schema#'               : 'rdfs',
                    'http://www.w3.org/2002/07/owl#'                      : 'owl',
                    'http://purl.org/dc/elements/1.1/'                    : 'dc',
                    'http://purl.org/dc/terms/'                           : 'dcterms',
                    'http://rs.tdwg.org/ontology/voc/TaxonName#'          : 'tdwg_tn',
                    'http://rs.tdwg.org/ontology/voc/TaxonConcept#'       : 'tdwg_tc',
                    'http://rs.tdwg.org/ontology/voc/PublicationCitation#': 'tdwg_pc',
                    'http://rs.tdwg.org/ontology/voc/Common#'             : 'tdwg_comm',
                    'http://biodiversity.org.au/voc/ibis/IBIS#'           : 'ibis',
                    'http://biodiversity.org.au/voc/afd/AFD#'             : 'afd',
                    'http://biodiversity.org.au/voc/apni/APNI#'           : 'apni',
                    'http://biodiversity.org.au/voc/apc/APC#'             : 'apc',
                    'http://biodiversity.org.au/voc/afd/profile#'         : 'afd_prf',
                    'http://biodiversity.org.au/voc/apni/profile#'        : 'apni_prf',
                    'http://biodiversity.org.au/voc/graph/GRAPH#'         : 'g',
                    'http://creativecommons.org/ns#'                      : 'cc',
                    'http://biodiversity.org.au/voc/boa/BOA#'             : 'boa',
                    'http://biodiversity.org.au/voc/boa/Name#'            : 'boa-name',
                    'http://biodiversity.org.au/voc/boa/Tree#'            : 'boa-tree',
                    'http://biodiversity.org.au/voc/boa/Instance#'        : 'boa-inst',
                    'http://biodiversity.org.au/voc/boa/Reference#'       : 'boa-ref',
                    'http://biodiversity.org.au/voc/boa/Author#'          : 'boa-auth',
                    'http://biodiversity.org.au/voc/nsl/NSL#'             : 'nsl',
                    'http://biodiversity.org.au/voc/nsl/Tree#'            : 'nsl-tree',
                    'http://biodiversity.org.au/voc/nsl/APC#'             : 'nsl-apc',
                    'http://biodiversity.org.au/voc/nsl/Namespace#'       : 'nsl-ns'
            ]

            // if we have an item in UriNs, then it will override these handy prefixes.

            UriNs.all.each { uriPrefixes.put(it.uri, it.label) }

            return [query: params, max: max, displayFormats: displayFormats, uriPrefixes: uriPrefixes, stats: [:]]
        }
        return [query: params, max: max, displayFormats: displayFormats, treeSearch: tree != null, trees: trees]

    }

    private static Tree determineTree(String treeId) {
        if (treeId?.isLong()) {
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
            render(view: 'search', model: [results: results, query: params, max: max, treeName: treeName])
        }
    }

    private String renderCsvResults(List<Map> results) {
        List<List> csvResults = []
        List<String> flatViewExportFields = [
                'canonicalName',
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
                        values.add(flatViewRow[fieldName] ?: '')
                    }

                    csvResults.add(values)
                }
            }
        }
        return CsvRenderer.renderAsCsv(headers, csvResults)
    }

}
