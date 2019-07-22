package au.org.biodiversity.nsl

import grails.test.spock.IntegrationSpec
import grails.transaction.Rollback

/**
 * User: pmcneil
 * Date: 10/07/18
 *
 */

@Rollback
class InstanceServiceSpec extends IntegrationSpec {

    def instanceService

    def setup() {
    }

    def cleanup() {
        
    }

    def "Test Sort Instances order"() {
        when: "CHAH 2011 instance of Hibbertia hirticalyx Toelken https://id.biodiversity.org.au/instance/apni/709624"
        Instance hibbertia = Instance.get(709624)
        List<Instance> sortedSynonyms = instanceService.sortInstances(hibbertia.instancesForCitedBy as List)
        println sortedSynonyms.collect {
            "$it.instanceType.id, $it.instanceType.name $it.id: $it.name.fullName"
        }.join("\n")

        then: "instances will be in this order"
        sortedSynonyms.size() == 18
        sortedSynonyms[0].id == 916201
        sortedSynonyms[1].id == 916202
        sortedSynonyms[2].id == 916203
        sortedSynonyms[3].id == 916206
        sortedSynonyms[4].id == 916189
        sortedSynonyms[5].id == 916198
        sortedSynonyms[6].id == 916197
        sortedSynonyms[7].id == 916196
        sortedSynonyms[8].id == 916194
        sortedSynonyms[9].id == 916193
        sortedSynonyms[10].id == 916199
    }

}
