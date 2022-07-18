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

import groovy.transform.CompileStatic
import org.grails.plugins.codecs.HTMLCodec

import java.util.regex.Matcher
import java.util.regex.Pattern

class NameConstructionService {

    NameConstructor icnNameConstructionService
    NameConstructor icznNameConstructionService
    NameConstructor icnpNameConstructionService
    NameConstructor icvcnNameConstructionService

    static transactional = false

    private static HTMLCodec htmlCodec = new HTMLCodec()

    @CompileStatic
    static String stripMarkUp(String string) {
        if (string) {
            String s = htmlCodec.decoder.decode(string.replaceAll(/<[^>]*>/, '')?.replaceAll(/(&lsquo;|&rsquo;)/, "'")).toString()
            return s.trim()
        }
        return string
    }

    @CompileStatic
    static String join(List<String> bits) {
        bits.findAll { it }.join(' ')
    }

    static Pattern wordSplitterPattern = Pattern.compile(/(\S+)\s+(\S+)\s*(.*)/)
    static Pattern wordSplitterPattern2 = Pattern.compile(/(\S+)\s+(.*)/)

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
        String sortName = simpleName.toLowerCase() + ' '
        if (sortName.matches(/.*\bcf\.\B.*/) || sortName.matches(/.*\baff\.\B.*/)) {
            sortName = sortName.trim() + ' 0 ' // make it sort last
        }
        sortName = sortName
                .replaceAll(/^x /, '') //remove hybrid marks
                .replaceAll(/ [x+-] /, ' ') //remove hybrid marks
                .replaceAll(/ MS$/, '') //remove manuscript
                .replaceAll(/\s*\bcf\.\B\s*/,' ')
                .replaceAll(/\s*\baff\.\B\s*/,' ')
                .trim()
        if (name.nameType.name != 'phrase name') {
            String abbrev = name.nameRank.abbrev.toLowerCase().replaceAll(/([\]\.\[])/, /\\$1/)
            sortName = sortName.replaceAll(/(\s+)${abbrev}(\s+|$)/, ' ') //remove rank abbreviations
        }
        if (name.verbatimRank) {
            String abbrev = name.verbatimRank.toLowerCase().replaceAll(/([\]\.\[])/, /\\$1/)
            sortName = sortName.replaceAll(/(\s+)${abbrev}(\s+|$)/, ' ') //remove rank abbreviations
        }
        NameRank subsp = NameRank.findByAbbrev('subsp.')
        if (name.nameRank.parentRank && name.nameRank.sortOrder >= subsp.sortOrder) {
            Matcher m = wordSplitterPattern.matcher(sortName)
            if (m.matches()) {
                String first = m.group(1)
                String second = m.group(2)
                String rest = m.group(3)
                sortName = "$first ${String.format('%04d', name.nameRank.parentRank.sortOrder)} $second ${String.format('%04d', name.nameRank.sortOrder)} $rest"
            }
        } else if (name.nameRank.parentRank) {
            Matcher m = wordSplitterPattern2.matcher(sortName)
            if (m.matches()) {
                String first = m.group(1)
                String rest = m.group(2)
                sortName = "$first ${String.format('%04d', name.nameRank.sortOrder)} $rest"
            }
        }
        return sortName.trim()
    }

    @CompileStatic
    ConstructedName constructName(Name name) {
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

    @CompileStatic
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
