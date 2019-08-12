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
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.plugins.codecs.HTMLCodec
import spock.lang.Specification

class NameControllerSpec extends Specification implements ControllerUnitTest<NameController>, DataTest {
         
    @SuppressWarnings("GroovyUnusedDeclaration")
    static transactional = true

    def linkService = Mock(LinkService)
    Name doodia

    void setupSpec() {
        mockDomains Name, NameGroup, NameCategory, NameStatus, NameRank, NameType, Namespace
    }
    
    def setup() {
        controller.jsonRendererService = new JsonRendererService()
        controller.jsonRendererService.registerObjectMashallers()
        controller.jsonRendererService.linkService = linkService
        linkService.getLinksForObject(_) >> { Object thing -> ["first $thing link", "second $thing link"] }
        linkService.getPreferredLinkForObject(_) >> { Object thing -> "Link for $thing" }
        controller.nameConstructionService = new NameConstructionService()
        controller.nameConstructionService.icnNameConstructionService = new IcnNameConstructionService()
        controller.nameConstructionService.icnNameConstructionService.htmlEncoderInst = new HTMLCodec().encoder

        Namespace namespace = new Namespace(name: 'test', rfId: 'blah', descriptionHtml: '<p>blah</p>')
        TestUte.setUpNameInfrastructure()
        doodia = TestUte.makeName('Doodia', 'Genus', null, namespace)
        doodia.save()
    }

    def cleanup() {
    }

    void "Test nameStrings accepts get and put only"() {
        when: "no parameters are passed"
        controller.request.method = method
        controller.nameStrings(null)

        then: "? is returned"
        controller.response.status == status

        where:
        method   | status
        'GET'    | 404
        'PUT'    | 404
        'POST'   | 405
        'DELETE' | 405
        'PATCH'  | 405
    }

    void "Test nameStrings"() {
        when: "no parameters are passed"
        controller.request.contentType = "application/json"
        controller.request.format = 'json'
        controller.response.format = 'json'
        controller.request.method = 'GET'
        controller.nameStrings(null)

        then: "not found is returned"
        controller.response.status == 404
        controller.response.text == '{"action":null,"error":"Object not found."}'

        when: "Doodia is provided"
        controller.response.reset()
        controller.response.format = 'json'
        controller.nameStrings(doodia)

        then: "expected name strings are returned"
        controller.response.status == 200
        controller.response.text == '{"action":null,"name":{"class":"au.org.biodiversity.nsl.Name","_links":{"permalink":{"link":"Link for [Name 1: Doodia]","preferred":true,"resources":1}},"nameElement":"Doodia","fullNameHtml":null},"result":{"fullMarkedUpName":"<scientific><name data-id=\'1\'><element>Doodia<\\/element><\\/name><\\/scientific>","simpleMarkedUpName":"<scientific><name data-id=\'1\'><element>Doodia<\\/element><\\/name><\\/scientific>","fullName":"Doodia","simpleName":"Doodia","sortName":"doodia"}}'
        controller.response.json.result.fullName == 'Doodia'
    }

}
