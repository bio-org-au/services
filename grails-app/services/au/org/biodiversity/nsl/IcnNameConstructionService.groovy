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
import org.grails.encoder.CodecLookup

class IcnNameConstructionService implements NameConstructor {

    CodecLookup codecLookup
    static transactional = false

    static String join(List<String> bits) {
        bits.findAll { it }.join(' ')
    }

    @CompileStatic
    ConstructedName constructName(Name name) {
        if (!name) {
            throw new NullPointerException("Name can't be null.")
        }

        if (name.nameType.scientific) {
            if (name.nameType.formula) {
                return constructHybridFormulaScientificName(name)
            }
            if (name.nameType.autonym) {
                return constructAutonymScientificName(name)
            }
            return constructScientificName(name)
        }

        if (name.nameType.cultivar) {
            if (name.nameType.formula && name.nameType.hybrid) {
                return constructHybridFormulaCultivarName(name)
            }
            if (name.nameType.formula) {
                return constructGraftChimeraName(name)
            }
            if (name.nameType.hybrid) {
                return constructHybridCultivarName(name)
            }
            return constructCultivarName(name)
        }

        if (name.nameType.name == 'informal') {
            return constructInformalName(name)
        }

        if (name.nameType.nameCategory?.name == 'common') {
            String htmlNameElement = htmlEncoder.encode(name.nameElement)
            String markedUpName = "<common><name data-id='$name.id'><element>${htmlNameElement}</element></name></common>"
            return new ConstructedName(fullMarkedUpName: markedUpName, simpleMarkedUpName: markedUpName)
        }
        String defaultName = (name.nameElement ? htmlEncoder.encode(name.nameElement) : '?')
        return new ConstructedName(fullMarkedUpName: defaultName, simpleMarkedUpName: defaultName)
    }

    @CompileStatic
    private ConstructedName constructInformalName(Name name) {
        List<String> bits = ["<element>${htmlEncoder.encode(name.nameElement)}</element>".toString(), constructAuthor(name)]
        String joined = join(bits)

        String markedUpName = "<informal><name data-id='$name.id'>${joined}</name></informal>"
        return new ConstructedName(fullMarkedUpName: markedUpName, simpleMarkedUpName: markedUpName)
    }

    @CompileStatic
    private ConstructedName constructHybridCultivarName(Name name) {
        List<String> bits = []
        String htmlNameElement = htmlEncoder.encode(name.nameElement)
        //NSL-856 cultivar hybrid display genus + epithet
        Name parent = NameConstructionUtils.nameParentOfRank(name, 'Genus')
        if (parent) {
            bits << NameConstructionUtils.removeManuscript(constructName(parent).simpleMarkedUpName)
            bits << (name.nameType.connector) ? "<hybrid data-id='$name.nameType.id' title='$name.nameType.name'>$name.nameType.connector</hybrid>" : ''
            bits << "<element>&lsquo;${htmlNameElement}&rsquo;</element>".toString()
        } else {
            bits << "<element>${htmlNameElement}</element>".toString()
        }
        String markedUpName = "<cultivar><name data-id='$name.id'>${join(bits)}</name></cultivar>"
        return new ConstructedName(fullMarkedUpName: markedUpName, simpleMarkedUpName: markedUpName)
    }

    @CompileStatic
    private ConstructedName constructGraftChimeraName(Name name) {
        List<String> bits = []
        Name parent = name.parent
        bits << NameConstructionUtils.removeManuscript(constructName(parent).simpleMarkedUpName)
        bits << (name.nameType.connector ? "<formula data-id='$name.nameType.id' title='$name.nameType.name'>$name.nameType.connector</formula>" : '').toString()
        bits << (name.secondParent ? NameConstructionUtils.removeManuscript(constructName(name.secondParent).simpleMarkedUpName) : '').toString()
        String markedUpName = "<cultivar><name data-id='$name.id'>${join(bits)}</name></cultivar>"
        return new ConstructedName(fullMarkedUpName: markedUpName, simpleMarkedUpName: markedUpName)
    }

    @CompileStatic
    private ConstructedName constructHybridFormulaCultivarName(Name name) {
        List<String> bits = []
        Name parent = name.parent
        bits << NameConstructionUtils.removeManuscript(constructName(parent).simpleMarkedUpName)
        bits << (name.nameType.connector ? "<hybrid data-id='$name.nameType.id' title='$name.nameType.name'>$name.nameType.connector</hybrid>" : '').toString()
        bits << (name.secondParent ? NameConstructionUtils.removeManuscript(constructName(name.secondParent).simpleMarkedUpName) : '').toString()
        String markedUpName = "<cultivar><name data-id='$name.id'>${join(bits)}</name></cultivar>"
        return new ConstructedName(fullMarkedUpName: markedUpName, simpleMarkedUpName: markedUpName)
    }

    @CompileStatic
    private ConstructedName constructCultivarName(Name name) {
        List<String> bits = []
        //NSL-927 cultivar display to lowest parent rank
        if (name.parent) {
            ConstructedName n = constructName(name.parent)
            bits << n.simpleSansMS
            bits << (name.nameType.connector ? "<hybrid data-id='$name.nameType.id' title='$name.nameType.name'>$name.nameType.connector</hybrid>" : '').toString()
            bits << "<element>&lsquo;${htmlEncoder.encode(name.nameElement)}&rsquo;</element>".toString()
        } else {
            bits << "'<element>${htmlEncoder.encode(name.nameElement)}</element>".toString()
        }
        String markedUpName = "<cultivar><name data-id='$name.id'>${join(bits)}</name></cultivar>"
        return new ConstructedName(fullMarkedUpName: markedUpName, simpleMarkedUpName: markedUpName)
    }


    @CompileStatic
    private ConstructedName constructHybridFormulaScientificName(Name name) {
        String firstParent = constructPrecedingNameString(name.parent, name)
        String connector = makeConnectorString(name, null)
        String secondParent = name.secondParent ? constructPrecedingNameString(name.secondParent, name) : '<element>?</element>'

        List<String> simpleNameParts = [firstParent, connector, secondParent] //manuscript

        String markedUpName = "<scientific><name data-id='$name.id'>${join(simpleNameParts)}</name></scientific>"
        //need to remove Authors below from simple name because preceding name includes author in parent
        return new ConstructedName(fullMarkedUpName: markedUpName, simpleMarkedUpName: NameConstructionUtils.removeAuthors(markedUpName))
    }

    @CompileStatic
    private ConstructedName constructAutonymScientificName(Name name) {
        Name nameParent = NameConstructionUtils.nameParent(name)
        String precedingName = constructPrecedingNameString(nameParent, name)
        String rank = nameParent ? makeRankString(name) : ''
        String connector = makeConnectorString(name, rank)
        String element = "<element>${htmlEncoder.encode(name.nameElement)}</element>"

        List<String> simpleNameParts = [precedingName, rank, connector, element] //manuscript

        String fullMarkedUpName = "<scientific><name data-id='$name.id'>${join(simpleNameParts)}</name></scientific>"
        //need to remove Authors below from simple name because preceding name includes author in autonyms
        return new ConstructedName(fullMarkedUpName: fullMarkedUpName, simpleMarkedUpName: NameConstructionUtils.removeAuthors(fullMarkedUpName))
    }

    @CompileStatic
    private ConstructedName constructScientificName(Name name) {
        Name nameParent = NameConstructionUtils.nameParent(name)
        String precedingName = constructPrecedingNameString(nameParent, name)

        if (nameParent && !precedingName) {
            log.error "parent $nameParent, but didn't construct name."
        }

        String rank = nameParent ? makeRankString(name) : ''
        String connector = makeConnectorString(name, rank)
        String element = "<element>${htmlEncoder.encode(name.nameElement)}</element>"
        String author = constructAuthor(name)

        List<String> fullNameParts = [precedingName, rank, connector, element, author] //manuscript
        List<String> simpleNameParts = [precedingName, rank, connector, element] //manuscript

        String fullMarkedUpName = "<scientific><name data-id='$name.id'>${join(fullNameParts)}</name></scientific>"
        String simpleMarkedUpName = "<scientific><name data-id='$name.id'>${join(simpleNameParts)}</name></scientific>"
        return new ConstructedName(fullMarkedUpName: fullMarkedUpName, simpleMarkedUpName: simpleMarkedUpName)
    }

    @CompileStatic
    private String constructPrecedingNameString(Name parent, Name child) {
        if (parent) {
            ConstructedName constructedName = constructName(parent)
            if (child.nameType.autonym) {
                return constructedName.fullSansMS
            }
            if (child.nameType.formula) {
                if (parent.nameType.formula) {
                    return "(${constructedName.fullSansMS})"
                }
                return constructedName.fullSansMS
            }
            return constructedName.simpleSansMS
        }
        return ''
    }

    @CompileStatic
    private static String makeConnectorString(Name name, String rank) {
        if (name.nameType.connector &&
                !(rank && name.nameType.connector == 'x' && name.nameRank.abbrev.startsWith('notho'))) {
            return "<hybrid data-id='$name.nameType.id' title='$name.nameType.name'>$name.nameType.connector</hybrid>"
        } else {
            return ''
        }
    }

    @CompileStatic
    private static String makeRankString(Name name) {
        if (name.nameRank?.visibleInName) {
            if (name.nameRank.useVerbatimRank && name.verbatimRank) {
                return "<rank data-id='${name.nameRank?.id}'>${name.verbatimRank}</rank>"
            }
            return "<rank data-id='${name.nameRank?.id}'>${name.nameRank?.abbrev}</rank>"
        }
        return ''
    }

    @CompileStatic
    String constructAuthor(Name name) {
        List<String> bits = []
        if (name.author) {
            if (name.baseAuthor) {
                if (name.exBaseAuthor) {
                    bits << "(<ex-base data-id='$name.exBaseAuthor.id' title='${encodeHtml(name.exBaseAuthor.name)}'>$name.exBaseAuthor.abbrev</ex-base> ex <base data-id='$name.baseAuthor.id' title='${htmlEncoder.encode(name.baseAuthor.name)}'>$name.baseAuthor.abbrev</base>)".toString()
                } else {
                    bits << "(<base data-id='$name.baseAuthor.id' title='${encodeHtml(name.baseAuthor.name)}'>$name.baseAuthor.abbrev</base>)".toString()
                }
            }
            if (name.exAuthor) {
                bits << "<ex data-id='$name.exAuthor.id' title='${encodeHtml(name.exAuthor.name)}'>$name.exAuthor.abbrev</ex> ex".toString()
            }
            bits << "<author data-id='$name.author.id' title='${encodeHtml(name.author.name)}'>$name.author.abbrev</author>".toString()
            if (name.sanctioningAuthor) {
                bits << ": <sanctioning data-id='$name.sanctioningAuthor.id' title='${encodeHtml(name.sanctioningAuthor.name)}'>$name.sanctioningAuthor.abbrev</sanctioning>".toString()
            }
        }
        return bits.size() ? "<authors>${join(bits)}</authors>" : ''
    }
}

