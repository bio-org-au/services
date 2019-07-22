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

class IcvcnNameConstructionService implements NameConstructor {

    static transactional = false

    static String join(List<String> bits) {
        bits.findAll { it }.join(' ')
    }

    Map constructName(Name name) {
        if (!name) {
            throw new NullPointerException("Name can't be null.")
        }

        if (name.nameType.scientific) {

            if (name.nameType.autonym) {
                return constructAutonymScientificName(name)
            }
            return constructScientificName(name)
        }

        if (name.nameType.name == 'informal') {
            return constructInformalName(name)
        }

        if (name.nameType.nameCategory?.name == 'common') {
            String htmlNameElement = name.nameElement.encodeAsHTML()
            String markedUpName = "<common><name data-id='$name.id'><element>${htmlNameElement}</element></name></common>"
            return [fullMarkedUpName: markedUpName, simpleMarkedUpName: markedUpName]
        }

        return [fullMarkedUpName: (name.nameElement?.encodeAsHTML() ?: '?'), simpleMarkedUpName: (name.nameElement.encodeAsHTML() ?: '?')]
    }

    private static Map constructInformalName(Name name) {
        List<String> bits = ["<element>${name.nameElement.encodeAsHTML()}</element>", constructAuthor(name)]

        String markedUpName = "<informal><name data-id='$name.id'>${join(bits)}</name></informal>"
        return [fullMarkedUpName: markedUpName, simpleMarkedUpName: markedUpName]
    }

    //TODO remove after removing autonym type from the virus database.
    private Map constructAutonymScientificName(Name name) {
        use(NameConstructionUtils) {
            String element = "<element>${name.nameElement.encodeAsHTML()}</element>"
            String manuscript = (name.nameStatus.name == 'manuscript') ? '<manuscript>MS</manuscript>' : ''

            List<String> simpleNameParts = [element, manuscript]

            String fullMarkedUpName = "<scientific><name data-id='$name.id'>${join(simpleNameParts)}</name></scientific>"
            return [fullMarkedUpName: fullMarkedUpName, simpleMarkedUpName: fullMarkedUpName]
        }
    }

    private Map constructScientificName(Name name) {
        use(NameConstructionUtils) {

            String element = "<element>${name.nameElement.encodeAsHTML()}</element>"
            String manuscript = (name.nameStatus.name == 'manuscript') ? '<manuscript>MS</manuscript>' : ''

            List<String> fullNameParts = [element, manuscript]
            List<String> simpleNameParts = [element, manuscript]

            String fullMarkedUpName = "<scientific><name data-id='$name.id'>${join(fullNameParts)}</name></scientific>"
            String simpleMarkedUpName = "<scientific><name data-id='$name.id'>${join(simpleNameParts)}</name></scientific>"
            return [fullMarkedUpName: fullMarkedUpName, simpleMarkedUpName: simpleMarkedUpName]
        }
    }

    String constructAuthor(Name name) {
        List<String> bits = []
        if (name.author) {
            if (name.baseAuthor) {
                if (name.exBaseAuthor) {
                    bits << "(<ex-base data-id='$name.exBaseAuthor.id' title='${name.exBaseAuthor.name.encodeAsHTML()}'>$name.exBaseAuthor.abbrev</ex-base> ex <base data-id='$name.baseAuthor.id' title='${name.baseAuthor.name.encodeAsHTML()}'>$name.baseAuthor.abbrev</base>)"
                } else {
                    bits << "(<base data-id='$name.baseAuthor.id' title='${name.baseAuthor.name.encodeAsHTML()}'>$name.baseAuthor.abbrev</base>)"
                }
            }
            if (name.exAuthor) {
                bits << "<ex data-id='$name.exAuthor.id' title='${name.exAuthor.name.encodeAsHTML()}'>$name.exAuthor.abbrev</ex> ex"
            }
            bits << "<author data-id='$name.author.id' title='${name.author.name.encodeAsHTML()}'>$name.author.abbrev</author>"
            if (name.sanctioningAuthor) {
                bits << ": <sanctioning data-id='$name.sanctioningAuthor.id' title='${name.sanctioningAuthor.name.encodeAsHTML()}'>$name.sanctioningAuthor.abbrev</sanctioning>"
            }
        }
        return bits.size() ? "<authors>${join(bits)}</authors>" : ''
    }
}

