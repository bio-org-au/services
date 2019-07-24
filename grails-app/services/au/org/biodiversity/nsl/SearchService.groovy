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

class SearchService {

    def suggestService
    def treeService
    def linkService
    def configService

    Map searchForName(Map params, Integer max) {

        Map queryParams = [:]

        Set<String> from = ['Name n']
        Set<String> and = []

        if (params.withNoInstances == 'on') {
            and << 'n.instances.size = 0'
        } else {
            and << 'n.instances.size > 0'
        }

        queryNameParams(params, queryParams, and)

        if (params.nameStatus) {
            queryParams.nameStatus = params.nameStatus
            and << "n.nameStatus.name in (:nameStatus)"
        }

        if (params.author) {
            queryParams.author = params.author.toLowerCase()
            and << "lower(n.author.abbrev) like :author"
        }


        if (params.ofRank?.id) {
            queryParams.ofRankId = params.ofRank.id as Long

            and << "n.nameRank.id = :ofRankId"
        }

        if (params.nameType?.id) {
            queryParams.nameTypeId = params.nameType.id as Long
            and << "n.nameType.id = :nameTypeId"
        }

        if (params.protologue == 'on') {
            from.add('Instance i')
            and << "i.instanceType.protologue is true"
            and << "i.name = n"
        }

        if (params.publication) {
            queryParams.publication = "${regexTokenizeReferenceQueryString((params.publication as String).trim().toLowerCase(), true)}"
            from.add('Instance i')
            from.add('Reference r')
            and << "iregex(r.citation, :publication) = true"
            and << "i.reference = r"
            and << "i.name = n"
        }

        if (params.year) {
            queryParams.year = params.year.toInteger()
            from.add('Instance i')
            from.add('Reference r')
            and << "r.year = :year"
            and << "i.reference = r"
            and << "i.name = n"
        }

        if (params.inc) {
            List<String> ors = []
            params.inc.each { k, v ->
                if (v == 'on') {
                    if (k == 'other') {
                        ors << "(n.nameType.scientific = false and n.nameType.cultivar = false)"
                        if (params.containsKey('tree')) {
                            params.remove('tree.id')
                            params.remove('tree')
                        }
                    } else {
                        ors << "n.nameType.${k} = true".toString()
                    }
                }
            }
            if (ors.size()) {
                and << "(${ors.join(' or ')})".toString()
            }
        }

        if (params.advanced && params.ex) {
            params.ex.each { k, v ->
                if (v == 'on') {
                    and << "n.nameType.${k} = false".toString()
                }
            }
        }

        if (params.advanced && params.nameTag) {
            from.add('NameTagName ntn')
            queryParams.nameTag = params.nameTag
            and << "ntn.tag.name = :nameTag and ntn.name = n"
        }

        Map fail = queryTreeParams(params, queryParams, from, and)
        if (fail) {
            return fail
        }

        String fromClause = "from ${from.join(',')}"
        String whereClause = "where ${and.join(' and ')}"

        String countQuery = "select count(distinct n) as count, n.nameRank.name as rank, n.nameRank.sortOrder $fromClause $whereClause group by n.nameRank.name, n.nameRank.sortOrder order by n.nameRank.sortOrder"
        String query = "select distinct(n), n.sortName, n.nameRank.sortOrder $fromClause $whereClause order by n.namePath"

        log.debug query
        log.debug queryParams.toString()
        Long start = System.currentTimeMillis()
        List<List> counter = (Name.executeQuery(countQuery, queryParams, [max: max])) as List<List>
        Integer total = 0
        Map count = [:]
        counter.each { c ->
            total += c[0] as Integer
            count.put(c[1], c[0])
        }
        List<List> nameResults = (Name.executeQuery(query, queryParams, [max: max])) as List<List>
        log.debug "query took ${System.currentTimeMillis() - start}ms"
        //filter for just names.
        List<Name> names = nameResults.collect { result ->
            result[0] as Name
        }

        return [count: count, total: total, names: names, queryTime: (System.currentTimeMillis() - start)]
    }

    private static Map queryTreeParams(Map params, Map queryParams, Set<String> from, Set<String> and) {
        if (params.tree?.id) {
            Tree tree = Tree.get(params.tree.id as Long)
            TreeVersion treeVersion = tree.currentTreeVersion
            queryParams.treeVersion = treeVersion
            from.add('TreeVersionElement treeVersionElement')
            from.add('TreeElement treeElement')
            and << "treeVersionElement.treeVersion = :treeVersion and treeElement = treeVersionElement.treeElement"

            if (params.exclSynonym == 'on') {
                and << "n.id = treeElement.nameId"
            } else {
                from.add('Instance i')
                from.add('Instance s')
                and << "n = s.name and (s.citedBy = i or s = i) and i.id = treeElement.instanceId"
            }

            if (params.inRank?.id) {
                NameRank inRank = NameRank.get(params.inRank?.id as Long)
                String rankNameString = params.rankName.trim()
                if (rankNameString) {
                    Set<String> rankNames = Name.findAllByFullNameIlikeAndNameRank("${rankNameString}%", inRank).collect {
                        it.nameElement
                    }
                    if (rankNames && !rankNames.empty) {
                        Set<String> pathOr = []
                        rankNames.eachWithIndex { String nameElement, int i ->
                            queryParams["path$i"] = "%/${nameElement}%"
                            pathOr << "treeVersionElement.namePath like :path$i".toString()
                        }
                        and << "(${pathOr.join(' or ')})".toString()
                    } else {
                        return [count: 0, names: [], message: "${params.rankName} is not a ${inRank.displayName} in ${tree.name}"]
                    }
                } else {
                    params.remove('inRank') //blank name so set it to any
                }
            }
        }
        return null
    }

    private static void queryNameParams(Map params, Map queryParams, Set<String> and) {
        if ((params.name as String)?.trim()) {
            List<String> nameStrings = (params.name as String).trim().split('\n').collect {
                cleanUpName(it).toLowerCase()
            }
            List<String> ors = []
            nameStrings.findAll { it }.eachWithIndex { n, i ->
                queryParams["name${i}"] = regexTokenizeNameQueryString(n)
                ors << "iregex(n.simpleName, :name${i}) = true".toString()
                ors << "iregex(n.fullName, :name${i}) = true".toString()
            }
            and << "(${ors.join(' or ')})".toString()
        }
    }

    private static String cleanUpName(String name) {
        name
                .replaceAll('\u2013', '-')
                .replaceAll('\u2014', '-')
                .replaceAll('\u2015', '-')
                .replaceAll('\u2017', '_')
                .replaceAll('\u2018', '\'')
                .replaceAll('\u2019', '\'')
                .replaceAll('\u201a', ',')
                .replaceAll('\u201b', '\'')
                .replaceAll('\u201c', '\"')
                .replaceAll('\u201d', '\"')
                .replaceAll('\u201e', '\"')
                .replaceAll("\u2026", "...")
                .replaceAll('\u2032', '\'')
                .replaceAll('\u2033', '\"')
                .replaceAll('[\\s]+', ' ')
                .trim()
    }

    static String tokenizeQueryString(String query, boolean leadingWildCard = false) {
        use(SearchQueryCategory) {
            if (query.startsWith('"') && query.endsWith('"')) {
                return query.topAndTail()
            }
            (leadingWildCard ? '%' : '') + query.compressSpaces() + '%'
        }
    }

    static String regexTokenizeReferenceQueryString(String query, boolean leadingWildCard = false) {
        use(SearchQueryCategory) {
            if (query.startsWith('"') && query.endsWith('"')) {
                return '^' +
                        query.topAndTail()
                             .escapeRegexSpecialChars()
                             .sqlToRegexWildCard() + '$'
            }
            return (leadingWildCard ? '.*' : '^') +
                    query.escapeRegexSpecialChars()
                         .sqlToRegexWildCard()
                         .multiSpaceRegex() + '.*'
        }
    }


    static String regexTokenizeNameQueryString(String query, boolean leadingWildCard = false) {
        use(SearchQueryCategory) {
            if (query.startsWith('"') && query.endsWith('"')) {
                return '^' +
                        query.topAndTail()
                             .escapeRegexSpecialChars()
                             .sqlToRegexWildCard()
                             .replaceMultiplicationSignWithX() + '$'
            }

            Boolean previousTokenWasX = false
            String[] tokens = query.escapeRegexSpecialChars()
                                   .sqlToRegexWildCard()
                                   .replaceMultiplicationSignWithX()
                                   .compressSpaces()
                                   .split(' ')
                                   .collect { String token ->
                if (token.startsWith('x\\s')) {
                    previousTokenWasX = true
                    return token
                }
                if (token.size() > 1 && token.startsWith('x')) {
                    previousTokenWasX = false
                    return "($token|x ${token.substring(1)})"
                }
                if (token == '.*') {
                    previousTokenWasX = false
                    return token
                }
                if (previousTokenWasX) {
                    previousTokenWasX = false
                    return token
                }
                previousTokenWasX = false
                return "(x )?$token"
            }

            String tokenizedString = (leadingWildCard ? '.*' : '^') + tokens.join(' +')
            return tokenizedString
        }
    }

    /**
     * Name Check - take a list of names and check if they exist in the database. Possibly check names that don't exists
     * against close matches.
     * @return
     */
    List<Map> nameCheck(Map params, Integer max) {
        use(SearchQueryCategory) {
            if ((params.name as String)?.trim()) {
                LinkedHashSet<String> strings = (params.name as String).trim().split('\n').collect { String nameString ->
                    String queryString = cleanUpName(nameString)
                            .replaceMultiplicationSignWithX()
                            .replaceAll('(.*) , .*', '$1') //remove stuff after a space comma, so cut paste from web pages (e.g. apni) work
                    queryString ?: null
                }
                strings.remove(null)
                List<Map> results = strings.collect { String nameString ->
                    List<Name> names = Name.executeQuery('''
select n
from Name n
where ((lower(simpleName) like :q) or (lower(fullName) like :q))
and n.nameStatus.name in ('legitimate', 'nom. cons.', '[n/a]', '[default]', 'nom. alt.',
'nom. cult.', 'nom. cons.', 'nom. cons., orth. cons.', 'nom. cons., nom. alt.', 'nom. cult., nom. alt.',
'nom. et typ. cons.', 'nom. et orth. cons.', 'typ. cons.', 'orth. cons.', 'manuscript')
and n.nameType.name <> 'common'
and n.instances.size > 0
order by sortName
''', [q: nameString.toLowerCase()], [max: max])
                    Boolean found = (names != null && !names.empty)
                    List<Map> r = names.collect { Name name ->
                        TreeVersionElement treeVersionElement = treeService.findCurrentElementForName(name, treeService.getAcceptedTree())
                        Name family = name.family
                        [treeVersionElement: treeVersionElement, name: name, family: family]
                    }
                    [query: nameString, found: found, names: r, count: names.size()]
                }
                return results
            }
            return null
        }
    }

    private static Integer getRankSuggestionParentSortOrder(NameRank rank, Boolean allRanksAbove) {
        if (allRanksAbove) {
            return 0
        }
        if (rank.name == 'Genus') {
            return NameRank.findByName('Familia').sortOrder
        }
        return RankUtils.parentOrMajor(rank)?.sortOrder ?: 0
    }


    def registerSuggestions() {
        // add apc name search
        suggestService.addSuggestionHandler('apc-search') { String subject, String query, Map params ->
            String treeName = configService.classificationTreeName

            log.debug "apc-search suggestion handler params: $params"
            Instance instance
            if (params.instanceId) {
                instance = Instance.get(params.instanceId as Long)
            }
            if (instance) {
                NameRank rank = instance.name.nameRank
                Integer parentSortOrder = getRankSuggestionParentSortOrder(rank, params.allRanksAbove == 'true')
                log.debug "This rank $rank, parent $rank.parentRank, parentSortOrder $parentSortOrder"

                return Name.executeQuery('''
select n from Name n, TreeElement element, TreeVersionElement tve, Tree tree
where (iregex(n.simpleName, :query) = true or iregex(n.fullName, :query) = true)
and n.nameRank.sortOrder < :sortOrder
and n.nameRank.sortOrder >= :parentSortOrder
and tree.name = :treeName
and element.nameId = n.id
and tve.treeElement = element
and tve.treeVersion = tree.currentTreeVersion
order by n.sortName asc''',
                        [
                                query          : regexTokenizeNameQueryString(query.toLowerCase()),
                                sortOrder      : rank.sortOrder,
                                parentSortOrder: parentSortOrder,
                                treeName       : treeName
                        ], [max: 15])
                           .collect { name -> name.fullName }

            } else {
                return Name.executeQuery('''
select n from Name n, TreeElement element, TreeVersionElement tve, Tree tree 
where (iregex(n.simpleName, :query) = true or iregex(n.fullName, :query) = true)
and tree.name = :treeName
and element.nameId = n.id
and tve.treeElement = element
and tve.treeVersion = tree.currentTreeVersion
order by n.sortName asc''',
                        [query   : regexTokenizeNameQueryString(query.toLowerCase()),
                         treeName: treeName], [max: 15])
                           .collect { name -> name.fullName }
            }
        }

        suggestService.addSuggestionHandler('apni-search') { String subject, String query, Map params ->
            if (!query) {
                return []
            }

            query = query.trim()
            log.debug "$query -> tokenized query ${regexTokenizeNameQueryString(query.toLowerCase())}"
            if (query.contains('\n')) {
                query = query.split('\n').last().trim()
            }
            NameRank rank = null
            if (params.context) {
                rank = NameRank.get(params.context as Long)
            }

            List<String> names

            if (rank) {
                names = Name.executeQuery('''select n.fullName
from Name n
where (iregex(n.simpleName, :query) = true or iregex(n.fullName, :query) = true)
and n.instances.size > 0
and n.nameType.scientific = true
and n.nameRank = :rank
order by n.sortName''', [query: regexTokenizeNameQueryString(query.toLowerCase()), rank: rank], [max: 15]) as List<String>
            } else {
                names = Name.executeQuery('''select n.fullName
from Name n
where (iregex(n.simpleName, :query) = true or iregex(n.fullName, :query) = true)
and n.instances.size > 0
and n.nameType.scientific = true
order by n.sortName''', [query: regexTokenizeNameQueryString(query.toLowerCase())], [max: 15]) as List<String>
            }

            if (names.size() == 15) {
                names.add('...')
            }

            return names
        }

        suggestService.addSuggestionHandler('simpleName') { String query ->
            return Name.executeQuery('''select n from Name n where iregex(n.simpleName, :query) = true and n.instances.size > 0 order by n.sortName''',
                    [query: regexTokenizeNameQueryString(query.toLowerCase())], [max: 15])
                       .collect { name -> name.simpleName }
        }

        suggestService.addSuggestionHandler('acceptableName') { String query ->
            List<String> status = ['legitimate', 'manuscript', 'nom. alt.', 'nom. cons.', 'nom. cons., nom. alt.', 'nom. cons., orth. cons.', 'nom. et typ. cons.', 'orth. cons.', 'typ. cons.']
            return Name.executeQuery('''select n from Name n where (iregex(n.fullName, :query) = true or iregex(n.simpleName, :query) = true) and n.instances.size > 0 and n.nameStatus.name in (:ns) order by n.sortName asc''',
                    [query: regexTokenizeNameQueryString(query.toLowerCase()), ns: status], [max: 15])
                       .collect { name -> [name: name.fullName, link: linkService.getPreferredLinkForObject(name)] }
        }

        suggestService.addSuggestionHandler('author') { String query ->
            return Author.executeQuery('''select a from Author a where lower(a.abbrev) like :query order by a.abbrev asc''',
                    [query: "${query.toLowerCase()}%"], [max: 15])
                         .collect { author -> author.abbrev }
        }

        suggestService.addSuggestionHandler('publication') { String query ->
            String qtokenized = regexTokenizeReferenceQueryString(query.trim().toLowerCase(), true)
            log.debug "Tokenized query: $qtokenized"
            return Reference.executeQuery('''select r from Reference r where iregex(r.citation, :query) = true order by r.citation asc''',
                    [query: "${qtokenized}"], [max: 15])
                            .collect { reference -> reference.citation }
        }

        suggestService.addSuggestionHandler('epithet') { String query ->
            return Name.executeQuery('''select distinct (n.nameElement) from Name n
where lower(n.nameElement) like :query and n.instances.size > 0 and n.nameType.cultivar = false order by n.nameElement asc''',
                    [query: "${query.toLowerCase()}%"], [max: 15])
        }

        suggestService.addSuggestionHandler('nameType') { String query ->
            return NameType.executeQuery('''select n from NameType n where n.deprecated = false and lower(n.name) like :query order by n.name asc''',
                    [query: "${query.toLowerCase()}%"], [max: 15])
                           .collect { type ->
                type.name
            }
        }

    }

    /**
     * checks the map of checkboxes to see what has been checked and returns a map of those checkboxes only
     * @param params - the params object
     * @param set - the set of checkboxes to check are checked
     * @return map of checked checkboxes as [key: 'on', ...]
     */
    Map checked(params, String set) {
        Map checked = [:]
        params[set].each { k, v ->
            if (v == 'on') {
                checked << [(k): v]
            }
        }
        return checked
    }

}

class SearchQueryCategory {

    static String escapeRegexSpecialChars(String query) {
        return query.replaceAll(/([.\[\]()+?*\\])/, '\\\\$1')
    }

    static String sqlToRegexWildCard(String query) {
        return query.replaceAll(/%/, '.*')
    }

    static String replaceMultiplicationSignWithX(String query) {
        return query.replaceAll(/× ?/, 'x\\\\s')
    }

    static String compressSpaces(String query) {
        return query.replaceAll(/[\s]+/, ' ')
    }

    static String topAndTail(String query) {
        return query.size() > 2 ? query[1..-2] : ""
    }

    static String multiSpaceRegex(String query) {
        return compressSpaces(query).replaceAll(/ /, ' +')
    }
}