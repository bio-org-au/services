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

class NameConstructionService {

    def icnNameConstructionService
    def icznNameConstructionService
    def icnpNameConstructionService
    def icvcnNameConstructionService

    static transactional = false

    static String stripMarkUp(String string) {
        string?.replaceAll(/<[^>]*>/, '')?.replaceAll(/(&lsquo;|&rsquo;)/, "'")?.decodeHTML()?.trim()
    }

    static String join(List<String> bits) {
        bits.findAll { it }.join(' ')
    }

    /**
     * Make the sortName from the passed in simple name and name object.
     * We pass in simple name because it may not have been set on the name yet for new names.
     * NSL-1837
     *
     * @param name
     * @param simpleName
     * @return sort name string
     */
    String makeSortName(Name name, String simpleName) {

        String abbrev = name.nameRank.abbrev
        String sortName = simpleName.toLowerCase()
                                    .replaceAll(/^x /, '') //remove hybrid marks
                                    .replaceAll(/ [x+-] /, ' ') //remove hybrid marks
                                    .replaceAll(" $abbrev ", ' ') //remove rank abreviations
                                    .replaceAll(/ MS$/, '') //remove manuscript
        return sortName
    }

    Map constructName(Name name) {
        if (!name) {
            throw new NullPointerException("Name can't be null.")
        }

        if (name.nameType.nameGroup.name == 'botanical') {
            return icnNameConstructionService.constructName(name)
        }

        if (name.nameType.nameGroup.name == 'zoological') {
            return icznNameConstructionService.constructName(name)
        }

        if (name.nameType.nameGroup.name == 'prokaryotes') {
            return icnpNameConstructionService.constructName(name)
        }

        if (name.nameType.nameGroup.name == 'virus') {
            return icvcnNameConstructionService.constructName(name)
        }

        throw new UnsupportedNomCode("Unsupported Nomenclatural code for name construction $name.nameType.nameGroup.name")
    }

    String constructAuthor(Name name) {
        if (!name) {
            throw new NullPointerException("Name can't be null.")
        }

        if (name.nameType.nameGroup.name == 'botanical') {
            return icnNameConstructionService.constructAuthor(name)
        }

        if (name.nameType.nameGroup.name == 'zoological') {
            return icznNameConstructionService.constructAuthor(name)
        }

        if (name.nameType.nameGroup.name == 'prokaryotes') {
            return icnpNameConstructionService.constructAuthor(name)
        }

        if (name.nameType.nameGroup.name == 'virus') {
            return icvcnNameConstructionService.constructAuthor(name)
        }

        throw new UnsupportedNomCode("Unsupported Nomenclatural code for name construction $name.nameType.nameGroup.name")

    }
}