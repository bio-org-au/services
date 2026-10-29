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
/**
 * This is best used as a Category e.g.
 *
 * use(RankUtils) {*     name.nameAtRankOrLower('genus');
 *}*
 * User: pmcneil
 * Date: 3/12/14
 *
 */
class RankUtils {

    private static Integer rankOrder(String rdfId){
        NameRank.findByRdfId(rdfId).sortOrder
    }

    /**
     * Checks if the NameRank provided is higher than the rank of Name rankName.
     * Higher ranks have a lower rank sort order, so Genus is higher that Species.
     *
     * assert rankHigherThan(NameRank.findByRdfId('genus'), 'species') == true
     * assert rankHigherThan(NameRank.findByRdfId('species'), 'genus') == false
     * assert rankHigherThan(NameRank.findByRdfId('genus'), 'genus') == false
     *
     * @param rank
     * @param rdfId
     * @return true if rank is higher than the rank with the name rdfId
     */
    static Boolean rankHigherThan(NameRank rank, String rdfId) {
        return rankOrder(rdfId) > rank.sortOrder
    }

    static Boolean rankHigherThan(NameRank rankA, NameRank rankB) {
        return rankB.sortOrder > rankA.sortOrder
    }

    static Boolean rankLowerThan(NameRank rank, String rdfId) {
        return rankOrder(rdfId) < rank.sortOrder
    }

    /**
     * Checks if the NameRank provided is lower than the rank of Name rdfId.
     * Lower ranks have a higher rank sort order, so Species is lower that Genus.
     *
     * assert rankLowerThan(NameRank.findByRdfId('genus'), 'gpecies') == false
     * assert rankLowerThan(NameRank.findByRdfId('species'), 'senus') == true
     *
     * This ranks with a sort order of 500 (or above) will always return false
     * as they are effectively unranked.
     *
     * @param rank
     * @param rdfId
     * @return true if rank is lower than the rank with the name rdfId
     */
    static Boolean nameAtRankOrHigher(Name name, String rdfId) {
        return rankOrder(rdfId) >= name.nameRank.sortOrder
    }

    static Boolean nameAtRankOrLower(Name name, String rdfId) {
        return rankOrder(rdfId) <= name.nameRank.sortOrder
    }

    static Boolean nameLowerThanRank(Name name, String rdfId) {
        return rankOrder(rdfId) < name.nameRank.sortOrder
    }

    static Boolean nameLowerThanRank(Name name, NameRank rank) {
        return rank.sortOrder < name.nameRank.sortOrder
    }

    static Boolean nameHigherThanRank(Name name, String rdfId) {
        return rankOrder(rdfId) > name.nameRank.sortOrder
    }

    static nextMajorRank(NameRank rank, Boolean up = true) {
        return NameRank.findBySortOrderLessThanAndMajor(rank.sortOrder, true, [sort: 'sortOrder', order: up ? 'desc' : 'asc'])
    }

    static parentOrMajor(NameRank rank) {
        rank.parentRank ?: nextMajorRank(rank, true)
    }

    static isRankedHigherThan(Name a, Name b) {
        a.nameRank.sortOrder < b.nameRank.sortOrder
    }

    static isRankedLowerThan(Name a, Name b) {
        !isRankedHigherThan(a, b)
    }
}
