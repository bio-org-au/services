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

import au.org.biodiversity.nsl.Name
import au.org.biodiversity.nsl.RankUtils
import au.org.biodiversity.nsl.TreeVersion
import grails.converters.JSON
import org.grails.plugins.metrics.groovy.Timed

/**
 * This controller replaces the cgi-bin/apni output. It takes a name and prints out informaiton in the APNI format as
 * it is.
 */
class ApniFormatController {

    def apniFormatService
    def jsonRendererService
    def photoService
    def configService

    static responseFormats = [
            display: ['html'],
            name   : ['html']
    ]

    def index() {
        redirect(action: 'search')
    }

    /**
     * Display a name in APNI format
     * @param Name
     */
    @Timed()
    display(Name name, Long versionId, Boolean drafts) {
        if (name) {
            params.product = configService.nameTreeName
            String inc = g.cookie(name: 'searchInclude')
            if (inc) {
                params.inc = JSON.parse(inc) as Map
            } else {
                params.inc = [scientific: 'on']
            }

            String photoSearch = null
            if (RankUtils.nameAtRankOrLower(name, 'Species') && photoService.hasPhoto(name.simpleName)) {
                photoSearch = photoService.searchUrl(name.simpleName)
            }

            apniFormatService.getNameModel(name, TreeVersion.get(versionId), drafts) << [
                    drafts   : drafts,
                    versionId: versionId,
                    query    : [name: "$name.fullName", product: configService.nameTreeName, inc: params.inc],
                    stats    : [:],
                    names    : [name],
                    photo    : photoSearch,
                    count    : 1, max: 100]
        } else {
            flash.message = "Name not found."
            redirect(action: 'search')
        }
    }

    @Timed()
    name(Name name, Long versionId, Boolean drafts) {
        if (name) {
            log.info "getting ${configService.nameTreeName} name $name"
            ResultObject model = new ResultObject(apniFormatService.getNameModel(name, TreeVersion.get(versionId), drafts))
            String photoSearch = null
            if (RankUtils.nameAtRankOrLower(name, 'Species') && photoService.hasPhoto(name.simpleName)) {
                photoSearch = photoService.searchUrl(name.simpleName)
            }
            model << [drafts   : drafts,
                      versionId: versionId,
                      photo    : photoSearch]
            render(view: '_name', model: model)
        } else {
            render(status: 404, text: 'Name not found.')
        }
    }


}
