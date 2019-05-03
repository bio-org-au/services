package au.org.biodiversity.nsl

/**
 * User: pmcneil
 * Date: 26/02/18
 *
 */
class Synonym {
    final String host
    final Instance instance
    final String instanceLink
    final String simpleName
    final String type
    final Long nameId
    final String nameLink
    final String fullNameHtml
    final Boolean nom
    final Boolean tax
    final Boolean mis
    final Boolean syn
    final String cites
    final String citesHtml
    final String citesLink
    final String conceptLink
    final Long year

    Synonym(Instance synonymInstance, LinkService linkService) {
        host = linkService.getPreferredHost()
        nameLink = (linkService.getPreferredLinkForObject(synonymInstance.name) ?: '') - host
        instanceLink = (linkService.getPreferredLinkForObject(synonymInstance) ?: '') - host
        conceptLink = (linkService.getPreferredLinkForObject(synonymInstance.cites) ?: '') - host
        citesLink = (linkService.getPreferredLinkForObject(synonymInstance.cites?.reference) ?: '') - host
        instance = synonymInstance
        year = instance.cites?.reference?.year
        simpleName = synonymInstance.name.simpleName
        type = synonymInstance.instanceType.name
        nameId = synonymInstance.name.id
        fullNameHtml = synonymInstance.name.fullNameHtml
        nom = synonymInstance.instanceType.nomenclatural
        tax = synonymInstance.instanceType.taxonomic
        mis = synonymInstance.instanceType.misapplied
        syn = synonymInstance.instanceType.synonym
        cites = synonymInstance.cites ? synonymInstance.cites.reference.citation : ''
        citesHtml = synonymInstance.cites ? synonymInstance.cites.reference.citationHtml : ''
    }

    Synonym(Map synonymMap) {
        instance = Instance.get(synonymMap.instance_id as Long)

        host = synonymMap.host
        nameLink = synonymMap.name_link as String
        instanceLink = synonymMap.instance_link as String
        conceptLink = synonymMap.concept_link as String
        citesLink = synonymMap.cites_link as String

        year = instance?.cites?.reference?.year
        simpleName = synonymMap.simple_name as String
        type = synonymMap.type as String
        nameId = synonymMap.name_id as Long
        fullNameHtml = synonymMap.full_name_html as String
        nom = synonymMap.nom as Boolean
        tax = synonymMap.tax as Boolean
        mis = synonymMap.mis as Boolean
        syn = synonymMap.syn as Boolean
        cites = synonymMap.cites as String
        citesHtml = synonymMap.citesHtml as String
    }

    Map asMap() {
        [
                host          : host,
                instance_id   : instance.id,
                instance_link : instanceLink,
                concept_link  : conceptLink,
                simple_name   : simpleName,
                type          : type,
                name_id       : nameId,
                name_link     : nameLink,
                full_name_html: fullNameHtml,
                nom           : nom,
                tax           : tax,
                mis           : mis,
                syn           : syn,
                cites         : cites,
                citesHtml     : citesHtml,
                cites_link    : citesLink,
                year          : year
        ]
    }
}

class Synonyms {
    @Delegate
    final List<Synonym> synonyms = []

    Synonyms() {}

    Synonyms(List<Map> synList) {
        synList.each {
            synonyms.add(new Synonym(it))
        }
    }

    List<Synonym> filtered() {
        synonyms.findAll { Synonym synonym ->
            !(synonym.type ==~ '.*(misapp|pro parte|common|vernacular).*')
        }
    }

    Map asMap() {
        [list: (synonyms.collect { it.asMap() } ?: [])]
    }

}