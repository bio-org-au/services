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

class ServiceTagLib {

    def grailsApplication
    def linkService
    def instanceService
    def configService

    @SuppressWarnings("GroovyUnusedDeclaration")
    static defaultEncodeAs = 'raw'
    @SuppressWarnings("GroovyUnusedDeclaration")
    static encodeAsForTags = [encodeHTML: 'raw']

    static namespace = "st"

    def displayMap = { attrs ->

        def map = attrs.map

        out << '<ul>'
        map.each { k, v ->
            out << "<li>"
            out << "<b>${k.encodeAsHTML()}:</b>&nbsp;"
            if (k =~ /(?i)html/) {
                out << v
                out << '<br><pre>' + v.encodeAsHTML() + '</pre>'
            } else {
                out << displayValue(v: v)
            }
            out << '</li>'
        }
        out << '</ul>'
    }

    private static String anchorLinks(String value) {
        value.replaceAll(/(https?:\/\/[^ )]*)/, '<a href="$1">$1</a>')
    }

    def displayValue = { attrs ->
        def v = attrs.v
        if (v instanceof Map) {
            out << displayMap(map: v)
        } else if (v instanceof List) {

            out << '<ul>'
            v.each { sv ->
                out << "<li>"
                out << displayValue(v: sv)
                out << "</li>"
            }
            out << '</ul>'
        } else {
            if (v.toString().contains('http')) {
                out << anchorLinks(v.toString())
            } else {
                out << v.encodeAsHTML()
            }
        }
    }

    def systemNotification = { attrs ->
        String messageFileName = configService.systemMessageFilename
        if (messageFileName) {
            File message = new File(messageFileName)
            if (message.exists()) {
                String text = message.text
                if (text) {
                    out << """<div class="alert alert-danger" role="alert">
  <span class="fa fa-warning" aria-hidden="true"></span>
  $text</div>"""
                }
            }
        } else {
            out << 'configure message filename.'
        }
    }

    def scheme = { attrs ->
        String colourScheme = configService.colourScheme
        if (colourScheme) {
            out << colourScheme
        }
    }

    def preferredUrl = { attrs ->
        def target = attrs.target
        if (target) {
            target = HibernateDomainUtils.initializeAndUnproxy(target)
            try {
                String link = linkService.getPreferredLinkForObject(target)
                if (link) {
                    out << link
                }
            } catch (e) {
                log.debug e.message
            }
        }
    }

    def preferredLink = { attrs, body ->
        def target = attrs.target
        def api = attrs.api
        if (target) {
            target = HibernateDomainUtils.initializeAndUnproxy(target)
            try {
                String link = linkService.getPreferredLinkForObject(target)
                if (link) {
                    if (api) {
                        link += "/$api"
                    }
                    out << "<a href='${link}'>".toString()
                    out << body(link: link)
                    out << "</a>"
                } else {
                    out << body(link: '/')
                }
            } catch (e) {
                log.debug e.message
            }
        } else {
            out << '<span class="text-danger">Link not available.</span>'
        }
    }

    def editorLink = { attrs, body ->
        def nameId = attrs.nameId
        if (nameId) {
            try {
                String link = configService.editorlink +
                        "/search?query=id:${nameId}&query_field=name-instances&query_on=instance"
                if (link) {
                    out << "<a href='${link}'>"
                    out << body(link: link)
                    out << "</a>"
                }
            } catch (e) {
                log.debug e.message
            }
        } else {
            out << '<span class="text-danger">Link not available.</span>'
        }
    }

    def productName = { attrs ->
        String product = grailsApplication?.config?.nslServices?.product
        if (product) {
            out << product.encodeAsHTML()
        } else {
            out << 'APNI'
        }
    }

    def mapperUrl = { attrs ->
        String url = grailsApplication?.config?.services?.link?.mapperURL
        out << url
    }

    def googleAnalytics = { attrs ->
        String script = grailsApplication?.config?.services?.scriptAddOns
        if (script) {
            out << script
        }
    }

    def linkedData = { attr ->
        if (attr.val != null) {
            String description = ''
            def data = null
            if (attr.val instanceof Map) {
                description = attr.val.desc
                data = attr.val.data
            } else {
                data = attr.val
            }

            if (data instanceof String) {
                if (description) {
                    out << "&nbsp;<strong>${data}</strong>"
                    out << "&nbsp;<span class='text text-muted'>$description</span>"
                } else {
                    out << "<strong>${attr.val}</strong>"
                }
                return
            }

            if (data instanceof Collection) {
                out << "<a href='#' data-toggle='collapse' data-target='#${data.hashCode()}'>(${data.size()})</a>"
                out << "&nbsp;<span class='text text-muted'>$description</span>"
                out << "<ol id='${data.hashCode()}' class='collapse'>"
                Integer top = Math.min(data.size(), 99)
                (0..top).each { idx ->
                    Object obj = data[idx]
                    if (obj) {
                        if (obj instanceof Collection) {
                            out << "<li>&nbsp;"
                            out << '['
                            obj.eachWithIndex { Object subObj, i ->
                                if (i) {
                                    out << ', '
                                }
                                printObject(subObj)
                            }
                            out << ']</li>'
                        } else {
                            out << '<li>'
                            printObject(obj)
                            out << '</li>'
                        }
                    }
                }
                if (top < data.size()) {
                    out << '<li>...</li>'
                }
                out << '</ol>'

            } else {
                out << "<strong>${attr.val}</strong>"
            }

        }
    }

    private void printObject(obj) {
        if (obj.properties.containsKey('id')) {
            def target = HibernateDomainUtils.initializeAndUnproxy(obj)
            try {
                String link = linkService.getPreferredLinkForObject(target)
                if (link) {
                    out << "<a href='${link}'>${obj}</a>"
                } else {
                    out << "<strong>$obj</strong>"
                }
            } catch (e) {
                out << "<strong>$obj</strong>"
                log.debug e.message
            }
        } else {
            out << "<strong>$obj</strong>"
        }
    }

    def camelToLabel = { attrs ->
        String label = attrs.camel
        if (label) {
            label = label.replaceAll(/([a-z]+)([A-Z])/, '$1 $2').toLowerCase()
            out << label.capitalize()
        }
    }

    private static String toCamelCase(String text) {
        return text.toLowerCase().replaceAll("(_)([A-Za-z0-9])", { String[] it -> it[2].toUpperCase() })
    }

    def primaryInstance = { attrs, body ->
        Name name = attrs.name
        String var = attrs.var ?: 'primaryInstance'
        if (name) {
            List<Instance> primaryInstances = instanceService.findPrimaryInstance(name)
            if (primaryInstances && primaryInstances.size() > 0) {
                out << body((var): primaryInstances.first())
            } else {
                out << body((var): null)
            }
        }
    }

    def documentationLink = { attrs ->
        String serverURL = grailsApplication?.config?.grails?.serverURL
        if (serverURL) {
            serverURL -= '/services'
            out << "<a class=\"doco\" href=\"$serverURL/docs/main.html\">"
            out << '<i class="fa fa-book"></i> docs </a>'
        }
    }

    def primaryClassification = { attrs ->
        out << configService.getClassificationTreeName()
    }

    def nameTree = { attrs ->
        out << configService.getNameTreeName()
    }

    def productLabel = { attrs, body ->
        String product = attrs.product
        if (product) {
            out << body(label: configService.getProductLabel(product))
        }
    }

    def productDescription = { attrs ->
        out << configService.getProductDescription(attrs.product)
    }

    def shardDescription = { attrs ->
        out << configService.getShardDescriptionHtml()
    }

    def bannerText = { attrs ->
        out << configService.getBannerText()
    }

    def bannerImage = { attrs ->
        out << configService.getBannerImage()
    }

    def pageTitle = { attrs ->
        out << configService.getPageTitle()
    }

    def cardImage = { attrs ->
        out << configService.getCardImage()
    }

    def panelClass = { attrs ->
        String product = attrs.product
        out << (product == configService.classificationTreeName ? 'panel-success' : 'panel-info')
    }

    def alertClass = { attrs ->
        String product = attrs.product
        out << (product == configService.classificationTreeName ? 'alert-success' : 'alert-info')
    }

    def randomName = { attrs ->
        String q = attrs.q
        if (q) {
            q = q[0]
        }
        String simpleName = Name.findBySimpleNameIlike("$q%")?.simpleName
        if (!simpleName) {
            List simpleNameList = Name.list(max: 1)
            if(simpleNameList && !simpleNameList.empty) {
                simpleName = simpleNameList.first().simpleName
            }
        }
        if (!simpleName) {
            simpleName = 'Doodia'
        }
        out << simpleName
    }

    /**
     * encodes HTML output converting chars to entities and cleaning invalid tags.
     * Use sparingly as this is slowish at ~1ms for a call.
     */
    def encodeWithHTML = { attrs ->
        String text = attrs.text
        Set<String> allowedTags = []
        out << HTMLSanitiser.encodeInvalidMarkup(text, allowedTags).trim()
    }

    def nicerDomainString = { attrs ->
        String str = attrs.domainObj.toString()
        int index = str.indexOf(':')
        out << "<div><b>${str[0..index]}</b></div>"
        out << str[index + 1..-1]
    }

    def diffValue = { attrs ->
        def val = attrs.value
        if (val) {
            switch (val.class.simpleName) {
                case 'Name':
                    Name name = (Name) val
                    String link = linkService.getPreferredLinkForObject(name) + '/api/apni-format'
                    out << "<div class='title'><a href='$link' target='audit'>Name ($name.id)</a></div>"
                    out << "<div>${name.fullNameHtml}</div>"
                    break
                case 'Author':
                    Author author = (Author) val
                    String link = linkService.getPreferredLinkForObject(author)
                    out << "<div class='title'><a href='$link' target='audit'>Author ($author.id)</a></div>"
                    out << "<div>${author.name} (${author.abbrev})</div>"
                    break
                case 'Reference':
                    Reference reference = (Reference) val
                    String link = linkService.getPreferredLinkForObject(reference)
                    out << "<div class='title'><a href='$link' target='audit'>Reference ($reference.id)</a></div>"
                    out << "<div>${reference.citationHtml}</div>"
                    break
                case 'Instance':
                    Instance instance = (Instance) val
                    String link = linkService.getPreferredLinkForObject(instance)
                    out << "<div class='title'><a href='$link' target='audit'>Instance ($instance.id)</a></div>"
                    out << "<ul><li>${instance.instanceType.name}</li>"
                    out << "<li>${instance.name.fullNameHtml}</li>"
                    out << "<li>${instance.reference.citationHtml}</li></ul>"
                    break
                case 'InstanceNote':
                    InstanceNote note = (InstanceNote) val
                    String link = linkService.getPreferredLinkForObject(note)
                    out << "<div class='title'><a href='$link' target='audit'>Instance Note ($note.id)</a></div>"
                    out << "<div><b>${note.instanceNoteKey.name}:</b></div>"
                    out << "<div>${note.value}</div>"
                    Instance instance = note.instance
                    String instLink = linkService.getPreferredLinkForObject(instance)
                    out << "<div><a href='$instLink'>Instance ($instance.id)</a></div>"
                    out << "<ul><li>${instance.instanceType.name}</li>"
                    out << "<li>${instance.name.fullNameHtml}</li>"
                    out << "<li>${instance.reference.citationHtml}</li></ul>"
                    break
                case 'Comment':
                    Comment comment = (Comment) val
                    out << "<div class='title'>Comment ($comment.id)</div>"
                    out << "<div>${comment.text}</div>"
                    out << "<div>on:</div>"
                    if (comment.name) {
                        out << diffValue(value: comment.name)
                    }
                    if (comment.author) {
                        out << diffValue(value: comment.author)
                    }
                    if (comment.instance) {
                        out << diffValue(value: comment.instance)
                    }
                    if (comment.reference) {
                        out << diffValue(value: comment.reference)
                    }
                    break

                case 'NameType':
                    NameType nameType = (NameType) val
                    out << nameType.name
                    break
                case 'NameStatus':
                    NameStatus nameStatus = (NameStatus) val
                    out << nameStatus.name
                    break
                case 'RefAuthorRole':
                    RefAuthorRole refAuthorRole = (RefAuthorRole) val
                    out << refAuthorRole.name
                    break
                case 'RefType':
                    RefType refType = (RefType) val
                    out << refType.name
                    break
                case 'InstanceType':
                    InstanceType instanceType = (InstanceType) val
                    out << instanceType.name
                    break

                default:
                    out << val ? val.toString() : '-'
            }
        } else {
            out << '-'
        }
    }

}
