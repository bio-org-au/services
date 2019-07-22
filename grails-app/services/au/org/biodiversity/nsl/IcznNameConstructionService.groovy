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

class IcznNameConstructionService implements NameConstructor {

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

    private Map constructAutonymScientificName(Name name) {
        use(NameConstructionUtils) {
            Name nameParent = name.nameParent()
            String precedingName = constructPrecedingNameString(nameParent, name)
            String rank = nameParent ? makeRankString(name) : ''
            String connector = makeConnectorString(name, rank)
            String element = "<element>${name.nameElement.encodeAsHTML()}</element>"
            String manuscript = (name.nameStatus.name == 'manuscript') ? '<manuscript>MS</manuscript>' : ''

            List<String> simpleNameParts = [precedingName, rank, connector, element, manuscript]

            String fullMarkedUpName = "<scientific><name data-id='$name.id'>${join(simpleNameParts)}</name></scientific>"
            //need to remove Authors below from simple name because preceding name includes author in autonyms
            return [fullMarkedUpName: fullMarkedUpName, simpleMarkedUpName: fullMarkedUpName.removeAuthors()]
        }
    }

    private Map constructScientificName(Name name) {
        use(NameConstructionUtils) {
            Name nameParent = (RankUtils.rankLowerThan(name.nameRank, 'Genus')) ? name.parent : null
            String precedingName = constructPrecedingNameString(nameParent, name)

            if (nameParent && !precedingName) {
                log.error "parent $nameParent, but didn't construct name."
            }

            String rank = nameParent ? makeRankString(name) : ''
            String connector = makeConnectorString(name, rank)
            String element
            if (name.nameRank.name == 'Subgenus') {
                element = "(<element>${name.nameElement.encodeAsHTML()}</element>)"
            } else {
                element = "<element>${name.nameElement.encodeAsHTML()}</element>"
            }
            String author = constructAuthor(name)
            String manuscript = (name.nameStatus.name == 'manuscript') ? '<manuscript>MS</manuscript>' : ''

            List<String> fullNameParts = [precedingName, rank, connector, element, author, manuscript]
            List<String> simpleNameParts = [precedingName, rank, connector, element, manuscript]

            String fullMarkedUpName = "<scientific><name data-id='$name.id'>${join(fullNameParts)}</name></scientific>"
            String simpleMarkedUpName = "<scientific><name data-id='$name.id'>${join(simpleNameParts)}</name></scientific>"
            return [fullMarkedUpName: fullMarkedUpName, simpleMarkedUpName: simpleMarkedUpName]
        }
    }

    private String constructPrecedingNameString(Name parent, Name child) {
        use(NameConstructionUtils) {
            if (parent) {
                Map constructedName = constructName(parent)
                if (child.nameType.autonym) {
                    return constructedName.fullMarkedUpName.removeManuscript()
                }
                if (child.nameType.formula) {
                    if (parent.nameType.formula) {
                        return "(${constructedName.fullMarkedUpName.removeManuscript()})"
                    }
                    return constructedName.fullMarkedUpName.removeManuscript()
                }
                return constructedName.simpleMarkedUpName.removeManuscript()
            }
            return ''
        }
    }

    private static String makeConnectorString(Name name, String rank) {
        if (name.nameType.connector &&
                !(rank && name.nameType.connector == 'x' && name.nameRank.abbrev.startsWith('notho'))) {
            return "<hybrid data-id='$name.nameType.id' title='$name.nameType.name'>$name.nameType.connector</hybrid>"
        } else {
            return ''
        }
    }

    private static String makeRankString(Name name) {
        if (name.nameRank?.visibleInName) {
            if (name.nameRank.useVerbatimRank && name.verbatimRank) {
                return "<rank data-id='${name.nameRank?.id}'>${name.verbatimRank}</rank>"
            }
            return "<rank data-id='${name.nameRank?.id}'>${name.nameRank?.abbrev}</rank>"
        }
        return ''
    }

    String constructAuthor(Name name) {
        List<String> bits = []
        if (name.author) {
            if (name.changedCombination) {
                if (name.publishedYear) {
                    bits << "(<author data-id='$name.author.id' title='${name.author.name.encodeAsHTML()}'>$name.author.abbrev</author>, $name.publishedYear)"
                } else {
                    bits << "(<author data-id='$name.author.id' title='${name.author.name.encodeAsHTML()}'>$name.author.abbrev</author>)"
                }
            } else {
                if (name.publishedYear) {
                    bits << "<author data-id='$name.author.id' title='${name.author.name.encodeAsHTML()}'>$name.author.abbrev</author>, $name.publishedYear"
                } else {
                    bits << "<author data-id='$name.author.id' title='${name.author.name.encodeAsHTML()}'>$name.author.abbrev</author>"
                }
            }
        }
        return bits.size() ? "<authors>${join(bits)}</authors>" : ''
    }
}

