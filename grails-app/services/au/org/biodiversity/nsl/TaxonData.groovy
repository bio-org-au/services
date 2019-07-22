package au.org.biodiversity.nsl

class TaxonData {

    Long nameId
    Long instanceId
    String simpleName
    String nameElement
    String displayHtml
    String synonymsHtml
    String sourceShard
    String rank
    Map profile
    String nameLink
    String instanceLink
    Boolean nomInval
    Boolean nomIlleg
    Boolean excluded
    Synonyms synonyms

    TaxonData() {}

    Map asMap() {
        [
                nameId      : nameId,
                instanceId  : instanceId,
                simpleName  : simpleName,
                nameElement : nameElement,
                displayHtml : displayHtml,
                synonymsHtml: synonymsHtml,
                sourceShard : sourceShard,
                synonyms    : synonyms.asMap(),
                rank        : rank,
                profile     : profile,
                nameLink    : nameLink,
                instanceLink: instanceLink,
                nomInval    : nomInval,
                nomIlleg    : nomIlleg,
                excluded    : excluded
        ]
    }

    Boolean equalsElement(TreeElement treeElement) {
        treeElement.nameId == nameId &&
                treeElement.instanceId == instanceId &&
                treeElement.simpleName == simpleName &&
                treeElement.nameElement == nameElement &&
                treeElement.displayHtml == displayHtml &&
                treeElement.synonymsHtml == synonymsHtml &&
                treeElement.sourceShard == sourceShard &&
                treeElement.synonyms == synonyms.asMap() &&
                treeElement.rank == rank &&
                treeElement.profile == profile &&
                treeElement.nameLink == nameLink &&
                treeElement.instanceLink == instanceLink &&
                treeElement.excluded == excluded
    }
}