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
import grails.gorm.transactions.Transactional

import static org.springframework.http.HttpStatus.NOT_FOUND

@Transactional(readOnly = true)
class IdMapperController {

    static responseFormats = ['json', 'xml']

    def checkId(Long id) {
        Name name = Name.get(id)
        if (name) {
            respond name
            return
        }
        Instance instance = Instance.get(id)
        if (instance) {
            respond instance
            return
        }
        Author author = Author.get(id)
        if (author) {
            respond author
            return
        }
        Reference reference = Reference.get(id)
        if (reference) {
            respond reference
            return
        }
        render status: NOT_FOUND
    }

    def apni() {
        log.debug "cgi-bin/apni params: $params"
        if (params.taxon_id && (params.taxon_id as String).isLong()) {
            Long taxon_id = params.taxon_id as Long
            IdMapper idMapper = IdMapper.findByFromIdAndSystem(taxon_id, 'PLANT_NAME')
            if (!idMapper?.toId) {
                flash.message = "Old taxon ID ${taxon_id} was not found. Please try a search."
                redirect(controller: 'search')
                return
            }
            flash.message = "You have been redirected here from an old Link. Please use the APNI search directly for best results."
            return redirect(controller: 'apniFormat', action: 'display', id: idMapper.toId)
        }
        // OK look for any parameter with name in it and search it
        String name = params.taxon_name ?: (params['00taxon_name'] ?: params.TAXON_NAME)

        if(name) {
            flash.message = "You have been redirected here from an old Link. We may have missed something in your search request. Please use the APNI search directly for best results."
            return redirect(uri:'/apni', params: [name: name, max: 100, display: 'apni', search: true])
        } else {
            flash.message = "You have been redirected here from an old Link. We couldn't figure out what you were looking for, try searching here."
            return redirect(uri:'/apni')
        }
    }

}
