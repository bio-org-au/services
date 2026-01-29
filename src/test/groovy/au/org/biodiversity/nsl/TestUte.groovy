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

import java.sql.Timestamp

/**
 * User: pmcneil
 * Date: 15/01/15
 *
 */
class TestUte {

    /**
     * you need to mock
     * @mock ([NameGroup,NameCategory,NameType,NameStatus,NameRank])
     *
     * to use in unit tests
     */
    static void setUpNameInfrastructure() {
        setUpNameGroups()
        setUpNameCategories()
        setUpNameTypes()
        setUpNameStatus()
        setUpNameRanks()
    }

    static String setUpNameGroups() {
        NameGroup.withTransaction { status ->
            [
                    [name: '[unknown]'],
                    [name: '[n/a]'],
                    [name: 'botanical'],
                    [name: 'zoological']
            ].each { data ->
                NameGroup nameGroup = new NameGroup(data)
                nameGroup.save()
            }
            NameGroup.withSession { s -> s.flush() }
        }
        return "Added name group records"
    }

    static String setUpNameCategories() {
        NameCategory.withTransaction { status ->
            [
                    [name                      : 'cultivar',
                     sortOrder                 : 50,
                     descriptionHtml           : 'names entered and edited as cultivar names',
                     rdfId                     : 'cultivar',
                     maxParentsAllowed         : 1,
                     minParentsRequired        : 1,
                     parent1HelpText           : "cultivar - genus and below, or unranked if unranked",
                     parent2HelpText           : null,
                     requiresFamily            : true,
                     requiresHigherRankedParent: false,
                     requiresNameElement       : true,
                     takesAuthorOnly           : false,
                     takesAuthors              : false,
                     takesCultivarScopedParent : true,
                     takesHybridScopedParent   : false,
                     takesNameElement          : true,
                     takesVerbatimRank         : true],

                    [name                      : 'scientific',
                     sortOrder                 : 10,
                     descriptionHtml           : 'names entered and edited as scientific names',
                     rdfId                     : 'scientific',
                     maxParentsAllowed         : 1,
                     minParentsRequired        : 1,
                     parent1HelpText           : "ordinary - restricted by rank, or unranked if unranked",
                     parent2HelpText           : null,
                     requiresFamily            : true,
                     requiresHigherRankedParent: true,
                     requiresNameElement       : true,
                     takesAuthorOnly           : false,
                     takesAuthors              : true,
                     takesCultivarScopedParent : false,
                     takesHybridScopedParent   : false,
                     takesNameElement          : true,
                     takesVerbatimRank         : true],

                    [name                      : 'cultivar hybrid',
                     sortOrder                 : 60,
                     descriptionHtml           : 'names entered and edited as cultivar hybrid names',
                     rdfId                     : 'NULL',
                     maxParentsAllowed         : 2,
                     minParentsRequired        : 2,
                     parent1HelpText           : "cultivar - genus and below, or unranked if unranked",
                     parent2HelpText           : "cultivar - genus and below, or unranked if unranked",
                     requiresFamily            : true,
                     requiresHigherRankedParent: false,
                     requiresNameElement       : true,
                     takesAuthorOnly           : false,
                     takesAuthors              : false,
                     takesCultivarScopedParent : true,
                     takesHybridScopedParent   : false,
                     takesNameElement          : true,
                     takesVerbatimRank         : true],

                    [name                      : 'other',
                     sortOrder                 : 70,
                     descriptionHtml           : 'names entered and edited as other names',
                     rdfId                     : 'NULL',
                     maxParentsAllowed         : 0,
                     minParentsRequired        : 0,
                     parent1HelpText           : "ordinary - restricted by rank, or unranked if unranked",
                     parent2HelpText           : null,
                     requiresFamily            : false,
                     requiresHigherRankedParent: false,
                     requiresNameElement       : true,
                     takesAuthorOnly           : false,
                     takesAuthors              : false,
                     takesCultivarScopedParent : true,
                     takesHybridScopedParent   : false,
                     takesNameElement          : true,
                     takesVerbatimRank         : true],

                    [name                      : 'phrase name',
                     sortOrder                 : 20,
                     descriptionHtml           : 'names entered and edited as scientific phrase names',
                     rdfId                     : 'NULL',
                     maxParentsAllowed         : 1,
                     minParentsRequired        : 1,
                     parent1HelpText           : "ordinary - restricted by rank, or unranked if unranked",
                     parent2HelpText           : null,
                     requiresFamily            : true,
                     requiresHigherRankedParent: false,
                     requiresNameElement       : false,
                     takesAuthorOnly           : true,
                     takesAuthors              : false,
                     takesCultivarScopedParent : false,
                     takesHybridScopedParent   : false,
                     takesNameElement          : true,
                     takesVerbatimRank         : false],

                    [name                      : 'scientific hybrid formula',
                     sortOrder                 : 30,
                     descriptionHtml           : 'names entered and edited as scientific hybrid formulae',
                     rdfId                     : 'NULL',
                     maxParentsAllowed         : 2,
                     minParentsRequired        : 2,
                     parent1HelpText           : 'hybrid - species and below or unranked if unranked',
                     parent2HelpText           : 'hybrid - species and below or unranked if unranked',
                     requiresFamily            : true,
                     requiresHigherRankedParent: false,
                     requiresNameElement       : false,
                     takesAuthorOnly           : false,
                     takesAuthors              : false,
                     takesCultivarScopedParent : false,
                     takesHybridScopedParent   : true,
                     takesNameElement          : false,
                     takesVerbatimRank         : true],

                    [name                      : 'scientific hybrid formula unknown 2nd parent',
                     sortOrder                 : 40,
                     descriptionHtml           : 'names entered and edited as scientific hybrid formulae with unknown 2nd parent',
                     rdfId                     : 'NULL',
                     maxParentsAllowed         : 1,
                     minParentsRequired        : 1,
                     parent1HelpText           : 'hybrid - species and below or unranked if unranked',
                     parent2HelpText           : null,
                     requiresFamily            : true,
                     requiresHigherRankedParent: false,
                     requiresNameElement       : false,
                     takesAuthorOnly           : false,
                     takesAuthors              : false,
                     takesCultivarScopedParent : true,
                     takesHybridScopedParent   : true,
                     takesNameElement          : false,
                     takesVerbatimRank         : true]
            ].each { data ->
                NameCategory nameCategory = new NameCategory(data)
                nameCategory.save()
            }
            NameCategory.withSession { s -> s.flush() }
        }
        return "Added name category records"

    }

    static String setUpNameTypes() {
        Map<String, NameCategory> nameCategories = [:]
        NameCategory.listOrderById().each { NameCategory nameCategory ->
            nameCategories.put(nameCategory.name, nameCategory)
        }

        Map<String, NameGroup> nameGroups = [:]
        NameGroup.listOrderById().each { NameGroup nameGroup ->
            nameGroups.put(nameGroup.name, nameGroup)
        }

        NameType.withTransaction { status ->
            [
                    [scientific: false, cultivar: false, formula: false, hybrid: false, autonym: false, connector: '', sortOrder: 1, name: '[default]', nameGroup: '[n/a]', nameCategory: 'other'],
                    [scientific: false, cultivar: false, formula: false, hybrid: false, autonym: false, connector: '', sortOrder: 2, name: '[unknown]', nameGroup: '[n/a]', nameCategory: 'other'],
                    [scientific: false, cultivar: false, formula: false, hybrid: false, autonym: false, connector: '', sortOrder: 3, name: '[n/a]', nameGroup: '[n/a]', nameCategory: 'other'],
                    [scientific: true, cultivar: false, formula: false, hybrid: false, autonym: false, connector: '', sortOrder: 4, name: 'scientific', nameGroup: 'botanical', nameCategory: 'scientific'],
                    [scientific: true, cultivar: false, formula: false, hybrid: false, autonym: false, connector: '', sortOrder: 5, name: 'sanctioned', nameGroup: 'botanical', nameCategory: 'scientific'],
                    [scientific: true, cultivar: false, formula: false, hybrid: false, autonym: false, connector: '', sortOrder: 6, name: 'phrase name', nameGroup: 'botanical', nameCategory: 'phrase name'],
                    [scientific: true, cultivar: false, formula: true, hybrid: true, autonym: false, connector: 'x', sortOrder: 7, name: 'hybrid formula parents known', nameGroup: 'botanical', nameCategory: 'scientific hybrid formula'],
                    [scientific: true, cultivar: false, formula: true, hybrid: true, autonym: false, connector: 'x', sortOrder: 8, name: 'hybrid formula unknown 2nd parent', nameGroup: 'botanical', nameCategory: 'scientific hybrid formula unknown 2nd parent'],
                    [scientific: true, cultivar: false, formula: false, hybrid: true, autonym: false, connector: 'x', sortOrder: 9, name: 'named hybrid', nameGroup: 'botanical', nameCategory: 'scientific'],
                    [scientific: true, cultivar: false, formula: false, hybrid: true, autonym: true, connector: 'x', sortOrder: 10, name: 'named hybrid autonym', nameGroup: 'botanical', nameCategory: 'scientific'],
                    [scientific: true, cultivar: false, formula: false, hybrid: true, autonym: true, connector: 'x', sortOrder: 11, name: 'hybrid autonym', nameGroup: 'botanical', nameCategory: 'scientific hybrid formula'],
                    [scientific: true, cultivar: false, formula: true, hybrid: true, autonym: false, connector: '-', sortOrder: 12, name: 'intergrade', nameGroup: 'botanical', nameCategory: 'scientific hybrid formula'],
                    [scientific: true, cultivar: false, formula: false, hybrid: false, autonym: true, connector: '', sortOrder: 13, name: 'autonym', nameGroup: 'botanical', nameCategory: 'scientific'],

                    [scientific: false, cultivar: true, formula: false, hybrid: false, autonym: false, connector: '', sortOrder: 16, name: 'cultivar', nameGroup: 'botanical', nameCategory: 'cultivar'],
                    [scientific: false, cultivar: true, formula: false, hybrid: true, autonym: false, connector: '', sortOrder: 17, name: 'cultivar hybrid', nameGroup: 'botanical', nameCategory: 'cultivar hybrid'],
                    [scientific: false, cultivar: true, formula: true, hybrid: true, autonym: false, connector: '', sortOrder: 18, name: 'cultivar hybrid formula', nameGroup: 'botanical', nameCategory: 'scientific hybrid formula'],
                    [scientific: false, cultivar: true, formula: false, hybrid: false, autonym: false, connector: '', sortOrder: 19, name: 'ACRA', nameGroup: 'botanical', nameCategory: 'cultivar'],
                    [scientific: false, cultivar: true, formula: false, hybrid: true, autonym: false, connector: '', sortOrder: 20, name: 'ACRA hybrid', nameGroup: 'botanical', nameCategory: 'cultivar hybrid'],
                    [scientific: false, cultivar: true, formula: false, hybrid: false, autonym: false, connector: '', sortOrder: 21, name: 'PBR', nameGroup: 'botanical', nameCategory: 'cultivar'],
                    [scientific: false, cultivar: true, formula: false, hybrid: true, autonym: false, connector: '', sortOrder: 22, name: 'PBR hybrid', nameGroup: 'botanical', nameCategory: 'cultivar hybrid'],
                    [scientific: false, cultivar: true, formula: false, hybrid: false, autonym: false, connector: '', sortOrder: 23, name: 'trade', nameGroup: 'botanical', nameCategory: 'cultivar'],
                    [scientific: false, cultivar: true, formula: false, hybrid: true, autonym: false, connector: '', sortOrder: 24, name: 'trade hybrid', nameGroup: 'botanical', nameCategory: 'cultivar hybrid'],
                    [scientific: false, cultivar: true, formula: true, hybrid: false, autonym: false, connector: '+', sortOrder: 25, name: 'graft / chimera', nameGroup: 'botanical', nameCategory: 'scientific hybrid formula'],
                    [scientific: false, cultivar: false, formula: false, hybrid: false, autonym: false, connector: '', sortOrder: 26, name: 'informal', nameGroup: 'botanical', nameCategory: 'other'],
                    [scientific: false, cultivar: false, formula: false, hybrid: false, autonym: false, connector: '', sortOrder: 15, name: 'common', nameGroup: 'botanical', nameCategory: 'other'],
            ].each { data ->
                //noinspection GroovyAssignabilityCheck
                data.nameCategory = nameCategories[data.nameCategory as String]
                //noinspection GroovyAssignabilityCheck
                data.nameGroup = nameGroups[data.nameGroup as String]
                NameType nameType = new NameType(data)
                nameType.save()
            }
            NameType.withSession { s -> s.flush() }
        }
        return "Added name type records"

    }

    static String setUpNameStatus() {

        Map<String, NameGroup> nameGroups = [:]
        NameGroup.listOrderById().each { NameGroup nameGroup ->
            nameGroups.put(nameGroup.name, nameGroup)
        }

        NameStatus.withTransaction { status ->
            [
                    [nomIlleg: false, nomInval: false, name: '[default]', nameGroup: '[n/a]'],
                    [nomIlleg: false, nomInval: false, name: '[unknown]', nameGroup: '[n/a]'],
                    [nomIlleg: false, nomInval: false, name: '[n/a]', nameGroup: '[n/a]'],
                    [nomIlleg: false, nomInval: false, name: '[deleted]', nameGroup: '[n/a]'],
                    [nomIlleg: false, nomInval: false, name: 'legitimate', nameGroup: 'botanical'],

                    [nomIlleg: false, nomInval: true, name: 'nom. inval.', nameGroup: 'botanical'],
                    [nomIlleg: false, nomInval: true, name: 'nom. inval., pro syn.', nameGroup: 'botanical', parent: 'nom. inval.'],
                    [nomIlleg: false, nomInval: true, name: 'nom. inval., nom. nud.', nameGroup: 'botanical', parent: 'nom. inval.'],
                    [nomIlleg: false, nomInval: true, name: 'nom. inval., nom. subnud.', nameGroup: 'botanical', parent: 'nom. inval.'],
                    [nomIlleg: false, nomInval: true, name: 'nom. inval., pro. syn.', nameGroup: 'botanical', parent: 'nom. inval.'],
                    [nomIlleg: false, nomInval: true, name: 'nom. inval., nom. ambig.', nameGroup: 'botanical', parent: 'nom. inval.'],
                    [nomIlleg: false, nomInval: true, name: 'nom. inval., nom. confus.', nameGroup: 'botanical', parent: 'nom. inval.'],
                    [nomIlleg: false, nomInval: true, name: 'nom. inval., nom. prov.', nameGroup: 'botanical', parent: 'nom. inval.'],
                    [nomIlleg: false, nomInval: true, name: 'nom. inval., nom. alt.', nameGroup: 'botanical', parent: 'nom. inval.'],
                    [nomIlleg: false, nomInval: true, name: 'nom. inval., nom. dub.', nameGroup: 'botanical', parent: 'nom. inval.'],
                    [nomIlleg: false, nomInval: true, name: 'nom. inval., opera utique oppressa', nameGroup: 'botanical', parent: 'nom. inval.'],
                    [nomIlleg: false, nomInval: true, name: 'nom. inval., tautonym', nameGroup: 'botanical', parent: 'nom. inval.'],

                    [nomIlleg: true, nomInval: false, name: 'nom. illeg.', nameGroup: 'botanical'],
                    [nomIlleg: true, nomInval: false, name: 'nom. illeg., nom. superfl.', nameGroup: 'botanical', parent: 'nom. illeg.'],
                    [nomIlleg: true, nomInval: false, name: 'nom. illeg., nom. rej.', nameGroup: 'botanical', parent: 'nom. illeg.'],

                    [nomIlleg: false, nomInval: false, name: 'isonym', nameGroup: 'botanical'],
                    [nomIlleg: false, nomInval: false, name: 'nom. superfl.', nameGroup: 'botanical'],
                    [nomIlleg: false, nomInval: false, name: 'nom. rej.', nameGroup: 'botanical'],
                    [nomIlleg: false, nomInval: false, name: 'nom. alt.', nameGroup: 'botanical'],
                    [nomIlleg: false, nomInval: false, name: 'nom. cult.', nameGroup: 'botanical'],
                    [nomIlleg: false, nomInval: false, name: 'nom. cons.', nameGroup: 'botanical'],
                    [nomIlleg: false, nomInval: false, name: 'nom. cons., orth. cons.', nameGroup: 'botanical'],
                    [nomIlleg: false, nomInval: false, name: 'nom. cons., nom. alt.', nameGroup: 'botanical'],
                    [nomIlleg: false, nomInval: false, name: 'nom. cult., nom. alt.', nameGroup: 'botanical', parent: 'nom. cult.'],
                    [nomIlleg: false, nomInval: false, name: 'nom. et typ. cons.', nameGroup: 'botanical'],
                    [nomIlleg: false, nomInval: false, name: 'nom. et orth. cons.', nameGroup: 'botanical'],
                    [nomIlleg: false, nomInval: false, name: 'nomina utique rejicienda', nameGroup: 'botanical'],
                    [nomIlleg: false, nomInval: false, name: 'typ. cons.', nameGroup: 'botanical'],
                    [nomIlleg: false, nomInval: false, name: 'orth. var.', nameGroup: 'botanical'],
                    [nomIlleg: false, nomInval: false, name: 'orth. cons.', nameGroup: 'botanical']
            ].each { data ->
                //noinspection GroovyAssignabilityCheck
                data.nameGroup = nameGroups[data.nameGroup as String]
                if (data.parent) {
                    //noinspection GroovyAssignabilityCheck
                    data.parent = NameStatus.findByName(data.parent as String)
                }
                NameStatus nameStatus = new NameStatus(data)
                nameStatus.save()
            }
            NameStatus.withSession { s -> s.flush() }
        }
        return "Added name status records"
    }

    static String setUpNameRanks() {
        NameGroup botanicalNameGroup = NameGroup.findByName('botanical')

        NameRank.withTransaction { status ->
            [
                    [hasParent: false, major: true, sortOrder: 10, visibleInName: false, name: 'Regnum', displayName: 'Regnum', abbrev: 'reg.', parentRank: ''],
                    [hasParent: false, major: true, sortOrder: 20, visibleInName: false, name: 'Division', displayName: 'Division', abbrev: 'div.', parentRank: ''],
                    [hasParent: false, major: true, sortOrder: 30, visibleInName: false, name: 'Classis', displayName: 'Classis', abbrev: 'cl.', parentRank: ''],
                    [hasParent: false, major: false, sortOrder: 40, visibleInName: false, name: 'Subclassis', displayName: 'Subclassis', abbrev: 'subcl.', parentRank: ''],
                    [hasParent: false, major: false, sortOrder: 50, visibleInName: false, name: 'Superordo', displayName: 'Superordo', abbrev: 'superordo', parentRank: ''],
                    [hasParent: false, major: true, sortOrder: 60, visibleInName: false, name: 'Ordo', displayName: 'Ordo', abbrev: 'ordo', parentRank: ''],
                    [hasParent: false, major: false, sortOrder: 70, visibleInName: false, name: 'Subordo', displayName: 'Subordo', abbrev: 'subordo', parentRank: ''],
                    [hasParent: false, major: true, sortOrder: 80, visibleInName: false, name: 'Familia', displayName: 'Familia', abbrev: 'fam.', parentRank: ''],
                    [hasParent: true, major: false, sortOrder: 90, visibleInName: true, name: 'Subfamilia', displayName: 'Subfamilia', abbrev: 'subfam.', parentRank: 'Familia'],
                    [hasParent: true, major: true, sortOrder: 100, visibleInName: true, name: 'Tribus', displayName: 'Tribus', abbrev: 'trib.', parentRank: 'Familia'],
                    [hasParent: true, major: false, sortOrder: 110, visibleInName: true, name: 'Subtribus', displayName: 'Subtribus', abbrev: 'subtrib.', parentRank: 'Familia'],
                    [hasParent: false, major: true, sortOrder: 120, visibleInName: false, name: 'Genus', displayName: 'Genus', abbrev: 'gen.', parentRank: ''],
                    [hasParent: true, major: false, sortOrder: 130, visibleInName: true, name: 'Subgenus', displayName: 'Subgenus', abbrev: 'subg.', parentRank: 'Genus'],
                    [hasParent: true, major: false, sortOrder: 140, visibleInName: true, name: 'Sectio', displayName: 'Sectio', abbrev: 'sect.', parentRank: 'Genus'],
                    [hasParent: true, major: false, sortOrder: 150, visibleInName: true, name: 'Subsectio', displayName: 'Subsectio', abbrev: 'subsect.', parentRank: 'Genus'],
                    [hasParent: true, major: false, sortOrder: 160, visibleInName: true, name: 'Series', displayName: 'Series', abbrev: 'ser.', parentRank: 'Genus'],
                    [hasParent: true, major: false, sortOrder: 170, visibleInName: true, name: 'Subseries', displayName: 'Subseries', abbrev: 'subser.', parentRank: 'Genus'],
                    [hasParent: true, major: false, sortOrder: 180, visibleInName: true, name: 'Superspecies', displayName: 'Superspecies', abbrev: 'supersp.', parentRank: 'Genus'],
                    [hasParent: true, major: true, sortOrder: 190, visibleInName: false, name: 'Species', displayName: 'Species', abbrev: 'sp.', parentRank: 'Genus'],
                    [hasParent: true, major: false, sortOrder: 200, visibleInName: true, name: 'Subspecies', displayName: 'Subspecies', abbrev: 'subsp.', parentRank: 'Species'],
                    [hasParent: true, major: false, sortOrder: 210, visibleInName: true, name: 'Nothovarietas', displayName: 'Nothovarietas', abbrev: 'nothovar.', parentRank: 'Species'],
                    [hasParent: true, major: false, sortOrder: 210, visibleInName: true, name: 'Varietas', displayName: 'Varietas', abbrev: 'var.', parentRank: 'Species'],
                    [hasParent: true, major: false, sortOrder: 220, visibleInName: true, name: 'Subvarietas', displayName: 'Subvarietas', abbrev: 'subvar.', parentRank: 'Species'],
                    [hasParent: true, major: false, sortOrder: 230, visibleInName: true, name: 'Forma', displayName: 'Forma', abbrev: 'f.', parentRank: 'Species'],
                    [hasParent: true, major: false, sortOrder: 240, visibleInName: true, name: 'Subforma', displayName: 'Subforma', abbrev: 'subf.', parentRank: 'Species'],
                    [hasParent: true, major: false, sortOrder: 250, visibleInName: false, name: 'form taxon', displayName: 'form taxon', abbrev: 'form taxon', deprecated: true, parentRank: 'Species'],
                    [hasParent: true, major: false, sortOrder: 260, visibleInName: false, name: 'morphological var.', displayName: 'morphological var.', abbrev: 'morph.', deprecated: true, parentRank: 'Species'],
                    [hasParent: true, major: false, sortOrder: 270, visibleInName: false, name: 'nothomorph.', displayName: 'nothomorph.', abbrev: 'nothomorph', deprecated: true, parentRank: 'Species'],
                    [hasParent: true, major: false, sortOrder: 500, visibleInName: true, name: '[unranked]', displayName: '[unranked]', abbrev: '[unranked]', parentRank: ''],
                    [hasParent: true, major: false, sortOrder: 500, visibleInName: true, name: '[infrafamily]', displayName: '[infrafamily]', abbrev: '[infrafamily]', parentRank: 'Familia'],
                    [hasParent: true, major: false, sortOrder: 500, visibleInName: true, name: '[infragenus]', displayName: '[infragenus]', abbrev: '[infragenus]', parentRank: 'Genus'],
                    [hasParent: true, major: false, sortOrder: 500, visibleInName: true, name: '[infraspecies]', displayName: '[infraspecies]', abbrev: '[infrasp.]', parentRank: 'Species'],
                    [hasParent: false, major: false, sortOrder: 500, visibleInName: false, name: '[n/a]', displayName: '[n/a]', abbrev: '[n/a]', parentRank: ''],
                    [hasParent: false, major: false, sortOrder: 500, visibleInName: false, name: '[unknown]', displayName: '[unknown]', abbrev: '[unknown]', deprecated: true, parentRank: '']

            ].each { values ->
                if (values.parentRank) {
                    //noinspection GroovyAssignabilityCheck
                    values.parentRank = NameRank.findByName(values.parentRank as String)
                }
                NameRank nameRank = new NameRank(values)
                nameRank.nameGroup = botanicalNameGroup
                nameRank.save()
            }
            NameRank.withSession { s -> s.flush() }
        }

        return "Added name rank records"
    }

    static Name makeName(String element, String rank, Name parent, Namespace namespace) {
        new Name(
                nameType: NameType.findByName('scientific'),
                nameStatus: NameStatus.findByName('legitimate'),
                nameRank: NameRank.findByName(rank),
                createdBy: 'tester',
                updatedBy: 'tester',
                createdAt: new Timestamp(System.currentTimeMillis()),
                updatedAt: new Timestamp(System.currentTimeMillis()),
                nameElement: element,
                parent: parent,
                namespace: namespace
        )
    }

    static Namespace namespace() {
        Namespace ns = Namespace.first()
        ns ?: new Namespace(name: 'test', rfId: 'blah', descriptionHtml: '<p>blah</p>')
    }

    static Reference genericReference(Author author, String refTitle) {
        saveReference(title: refTitle, author: author, isoPublicationDate: '1999', year: 1999, namespace())
    }

    static Author saveAuthor(Map params) {
        Author a = Author.findByAbbrev(params.abbrev)
        if (a) {
            return a
        }
        saveAuthor(params, namespace())
    }

    static Author saveAuthor(Map params, Namespace namespace) {
        Map base = [
                updatedAt: new Timestamp(System.currentTimeMillis()),
                updatedBy: 'test',
                createdAt: new Timestamp(System.currentTimeMillis()),
                createdBy: 'test',
                namespace: namespace
        ] << params
        Author a = new Author(base)
        a.save(flush: true, failOnError: true)
        return a
    }

    static Reference saveReference(Map params, Namespace namespace) {
        Author unknownAuthor = saveAuthor(abbrev: '-', name: '-')
        RefAuthorRole authorRole = saveRefAuthorRole('Author')
        RefAuthorRole editorRole = saveRefAuthorRole('Editor')
        Language language = saveLanguage('au')
        RefType paper = saveRefType('Paper')

        Map base = [
                refType      : paper,
                published    : true,
                refAuthorRole: authorRole,
                language     : language,
                displayTitle : 'Not set',
                updatedAt    : new Timestamp(System.currentTimeMillis()),
                updatedBy    : 'test',
                createdAt    : new Timestamp(System.currentTimeMillis()),
                createdBy    : 'test',
                namespace    : namespace
        ] << params
        Reference reference = new Reference(base)
        reference.save(flush: true, failOnError: true)
        reference.citationHtml = ReferenceService.generateReferenceCitation(reference, unknownAuthor, editorRole)
        reference.citation = NameConstructionService.stripMarkUp(reference.citationHtml)
        reference.save(flush: true, failOnError: true)
        return reference
    }

    static saveRefAuthorRole(String name) {
        RefAuthorRole role = RefAuthorRole.findByName(name)
        if (!role) {
            role = new RefAuthorRole(name: name, rdfId: name,
                    descriptionHtml: name)
            role.save(flush: true, failOnError: true)
        }
        return role
    }

    static saveLanguage(String language) {
        Language lang = Language.findByIso6391Code(language)
        if (!lang) {
            lang = new Language(iso6391Code: language, iso6393Code: language, name: language)
            lang.save(flush: true, failOnError: true)
        }
        return lang
    }

    static saveRefType(String name) {
        RefType refType = RefType.findByName(name)
        if (!refType) {
            refType = new RefType(
                    name: name,
                    parentOptional: true,
                    parent: null,
                    rdfId: name.toLowerCase().replace(' ', '-'),
                    descriptionHtml: 'blah',
                    useParentDetails: false
            )
            refType.save(flush: true, failOnError: true)
        }
        return refType
    }

    static String setUpInstanceTypes() {
        [
                [citing: false, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: false, secondaryInstance: false, sortOrder: 400, standalone: false, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: '[default]', ofLabel: '[default] of', bidirectional: false, name: '[default]'],
                [citing: false, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: false, secondaryInstance: false, sortOrder: 400, standalone: false, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: '[unknown]', ofLabel: '[unknown] of', bidirectional: false, name: '[unknown]'],
                [citing: false, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: false, secondaryInstance: false, sortOrder: 400, standalone: false, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: '[n/a]', ofLabel: '[n/a] of', bidirectional: false, name: '[n/a]'],
                [citing: false, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: true, proParte: false, protologue: false, relationship: false, secondaryInstance: false, sortOrder: 400, standalone: true, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'primary reference', ofLabel: 'primary reference of', bidirectional: false, name: 'primary reference'],
                [citing: false, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: true, proParte: false, protologue: true, relationship: false, secondaryInstance: false, sortOrder: 400, standalone: true, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'tax. nov.', ofLabel: 'tax. nov. of', bidirectional: false, name: 'tax. nov.'],
                [citing: false, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: true, proParte: false, protologue: true, relationship: false, secondaryInstance: false, sortOrder: 400, standalone: true, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'nom. nov.', ofLabel: 'nom. nov. of', bidirectional: false, name: 'nom. nov.'],
                [citing: false, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: true, proParte: false, protologue: true, relationship: false, secondaryInstance: false, sortOrder: 400, standalone: true, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'nom. et stat. nov.', ofLabel: 'nom. et stat. nov. of', bidirectional: false, name: 'nom. et stat. nov.'],
                [citing: false, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: true, proParte: false, protologue: true, relationship: false, secondaryInstance: false, sortOrder: 400, standalone: true, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'comb. nov.', ofLabel: 'comb. nov. of', bidirectional: false, name: 'comb. nov.'],
                [citing: false, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: true, proParte: false, protologue: true, relationship: false, secondaryInstance: false, sortOrder: 400, standalone: true, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'comb. et stat. nov.', ofLabel: 'comb. et stat. nov. of', bidirectional: false, name: 'comb. et stat. nov.'],
                [citing: false, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: false, secondaryInstance: false, sortOrder: 400, standalone: true, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'autonym', ofLabel: 'autonym of', bidirectional: false, name: 'autonym'],
                [citing: true, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 5, standalone: false, synonym: true, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'orthographic variant', ofLabel: 'orthographic variant of', bidirectional: false, name: 'orthographic variant'],
                [citing: false, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: false, secondaryInstance: false, sortOrder: 400, standalone: true, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'implicit autonym', ofLabel: 'implicit autonym of', bidirectional: false, name: 'implicit autonym'],
                [citing: true, deprecated: false, doubtful: false, misapplied: true, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 400, standalone: false, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'misapplication', ofLabel: 'misapplied to', bidirectional: false, name: 'misapplied'],
                [citing: true, deprecated: false, doubtful: false, misapplied: true, nomenclatural: false, primaryInstance: false, proParte: true, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 70, standalone: false, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'pro parte misapplication', ofLabel: 'pro parte misapplied to', bidirectional: false, name: 'pro parte misapplied'],
                [citing: true, deprecated: false, doubtful: true, misapplied: true, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 80, standalone: false, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'doubtful misapplication', ofLabel: 'doubtful misapplied to', bidirectional: false, name: 'doubtful misapplied'],
                [citing: true, deprecated: false, doubtful: true, misapplied: true, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 90, standalone: false, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'doubtful pro parte misapplication', ofLabel: 'doubtful pro parte misapplied to', bidirectional: false, name: 'doubtful pro parte misapplied'],
                [citing: false, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: false, secondaryInstance: true, sortOrder: 400, standalone: true, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'secondary reference', ofLabel: 'secondary reference of', bidirectional: false, name: 'secondary reference'],
                [citing: true, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 400, standalone: false, synonym: true, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'isonym', ofLabel: 'isonym of', bidirectional: false, name: 'isonym'],
                [citing: true, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 400, standalone: false, synonym: true, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'trade name', ofLabel: 'trade name of', bidirectional: false, name: 'trade name'],
                [citing: false, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: false, secondaryInstance: false, sortOrder: 400, standalone: false, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'excluded name', ofLabel: 'excluded name of', bidirectional: false, name: 'excluded name'],
                [citing: false, deprecated: false, doubtful: true, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: false, secondaryInstance: false, sortOrder: 400, standalone: false, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'doubtful invalid publication', ofLabel: 'doubtful invalid publication of', bidirectional: false, name: 'doubtful invalid publication'],
                [citing: true, deprecated: true, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 140, standalone: false, synonym: true, taxonomic: false, unsourced: true, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'synonym', ofLabel: 'synonym of', bidirectional: false, name: 'synonym'],
                [citing: true, deprecated: false, doubtful: false, misapplied: false, nomenclatural: true, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 30, standalone: false, synonym: true, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'nomenclatural synonym', ofLabel: 'nomenclatural synonym of', bidirectional: false, name: 'nomenclatural synonym'],
                [citing: true, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 100, standalone: false, synonym: true, taxonomic: true, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'taxonomic synonym', ofLabel: 'taxonomic synonym of', bidirectional: false, name: 'taxonomic synonym'],
                [citing: true, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 10, standalone: false, synonym: true, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'replaced synonym', ofLabel: 'replaced synonym of', bidirectional: false, name: 'replaced synonym'],
                [citing: true, deprecated: true, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: true, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 150, standalone: false, synonym: true, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'pro parte synonym', ofLabel: 'pro parte synonym of', bidirectional: false, name: 'pro parte synonym'],
                [citing: true, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: true, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 110, standalone: false, synonym: true, taxonomic: true, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'pro parte taxonomic synonym', ofLabel: 'pro parte taxonomic synonym of', bidirectional: false, name: 'pro parte taxonomic synonym'],
                [citing: true, deprecated: true, doubtful: true, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 160, standalone: false, synonym: true, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'doubtful synonym', ofLabel: 'doubtful synonym of', bidirectional: false, name: 'doubtful synonym'],
                [citing: false, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: false, secondaryInstance: false, sortOrder: 400, standalone: true, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'homonym', ofLabel: 'homonym of', bidirectional: false, name: 'homonym'],
                [citing: false, deprecated: true, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: false, secondaryInstance: false, sortOrder: 400, standalone: false, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'invalid publication', ofLabel: 'invalid publication of', bidirectional: false, name: 'invalid publication'],
                [citing: false, deprecated: true, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: false, secondaryInstance: false, sortOrder: 400, standalone: false, synonym: false, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'sens. lat.', ofLabel: 'sens. lat. of', bidirectional: false, name: 'sens. lat.'],
                [citing: true, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 400, standalone: false, synonym: false, taxonomic: false, unsourced: true, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'common name', ofLabel: 'common name of', bidirectional: false, name: 'common name'],
                [citing: true, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 400, standalone: false, synonym: false, taxonomic: false, unsourced: true, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'vernacular name', ofLabel: 'vernacular name of', bidirectional: false, name: 'vernacular name'],
                [citing: true, deprecated: false, doubtful: true, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 120, standalone: false, synonym: true, taxonomic: true, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'doubtful taxonomic synonym', ofLabel: 'doubtful taxonomic synonym of', bidirectional: false, name: 'doubtful taxonomic synonym'],
                [citing: true, deprecated: true, doubtful: true, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 170, standalone: false, synonym: true, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'doubtful pro parte synonym', ofLabel: 'doubtful pro parte synonym of', bidirectional: false, name: 'doubtful pro parte synonym'],
                [citing: true, deprecated: false, doubtful: true, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 130, standalone: false, synonym: true, taxonomic: true, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'doubtful pro parte taxonomic synonym', ofLabel: 'doubtful pro parte taxonomic synonym of', bidirectional: false, name: 'doubtful pro parte taxonomic synonym'],
                [citing: true, deprecated: false, doubtful: false, misapplied: false, nomenclatural: true, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 10, standalone: false, synonym: true, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'basionym', ofLabel: 'basionym of', bidirectional: false, name: 'basionym'],
                [citing: false, deprecated: false, doubtful: true, misapplied: false, nomenclatural: true, primaryInstance: false, proParte: false, protologue: false, relationship: false, secondaryInstance: false, sortOrder: 40, standalone: false, synonym: true, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'doubtful nomenclatural synonym', ofLabel: 'doubtful nomenclatural synonym of', bidirectional: false, name: 'doubtful nomenclatural synonym'],
                [citing: false, deprecated: false, doubtful: false, misapplied: false, nomenclatural: true, primaryInstance: false, proParte: true, protologue: false, relationship: false, secondaryInstance: false, sortOrder: 50, standalone: false, synonym: true, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'pro parte nomenclatural synonym', ofLabel: 'pro parte nomenclatural synonym of', bidirectional: false, name: 'pro parte nomenclatural synonym'],
                [citing: false, deprecated: false, doubtful: false, misapplied: false, nomenclatural: false, primaryInstance: false, proParte: true, protologue: false, relationship: false, secondaryInstance: false, sortOrder: 20, standalone: false, synonym: true, taxonomic: false, unsourced: false, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'pro parte replaced synonym', ofLabel: 'pro parte replaced synonym of', bidirectional: false, name: 'pro parte replaced synonym'],
                [citing: true, deprecated: false, doubtful: false, misapplied: true, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 400, standalone: false, synonym: false, taxonomic: false, unsourced: true, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'misapplication', ofLabel: 'misapplied to', bidirectional: false, name: 'unsourced misapplied'],
                [citing: true, deprecated: false, doubtful: false, misapplied: true, nomenclatural: false, primaryInstance: false, proParte: true, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 70, standalone: false, synonym: false, taxonomic: false, unsourced: true, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'pro parte misapplication', ofLabel: 'pro parte misapplied to', bidirectional: false, name: 'unsourced pro parte misapplied'],
                [citing: true, deprecated: false, doubtful: true, misapplied: true, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 80, standalone: false, synonym: false, taxonomic: false, unsourced: true, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'doubtful misapplication', ofLabel: 'doubtful misapplied to', bidirectional: false, name: 'unsourced doubtful misapplied'],
                [citing: true, deprecated: false, doubtful: true, misapplied: true, nomenclatural: false, primaryInstance: false, proParte: false, protologue: false, relationship: true, secondaryInstance: false, sortOrder: 90, standalone: false, synonym: false, taxonomic: false, unsourced: true, descriptionHtml: 'description', rdfId: 'rdf id', hasLabel: 'doubtful pro parte misapplication', ofLabel: 'doubtful pro parte misapplied to', bidirectional: false, name: 'unsourced doubtful pro parte misapplied'],
        ].each { values ->
            InstanceType instanceType = new InstanceType(values)
            instanceType.save()
        }
        InstanceType.withSession { s -> s.flush() }
        return "Instance Types created"
    }

}
