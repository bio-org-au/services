package au.org.biodiversity.nsl

class DisplayElement {

    @SuppressWarnings("GrFinalVariableAccess")
    public final String displayHtml
    @SuppressWarnings("GrFinalVariableAccess")
    public final String elementLink
    @SuppressWarnings("GrFinalVariableAccess")
    public final String nameLink
    @SuppressWarnings("GrFinalVariableAccess")
    public final String instanceLink
    @SuppressWarnings("GrFinalVariableAccess")
    public final Boolean excluded
    @SuppressWarnings("GrFinalVariableAccess")
    public final Integer depth
    @SuppressWarnings("GrFinalVariableAccess")
    public final String synonymsHtml

    DisplayElement(List data, String hostPart) {
        assert data.size() == 7
        this.displayHtml = data[0] as String
        this.elementLink = hostPart + data[1] as String
        this.nameLink = data[2] as String
        this.instanceLink = data[3] as String
        this.excluded = data[4] as Boolean
        this.depth = data[5] as Integer
        this.synonymsHtml = data[6] as String
    }

    Map asMap() {
        [
                displayHtml : displayHtml,
                elementLink : elementLink,
                nameLink    : nameLink,
                instanceLink: instanceLink,
                excluded    : excluded,
                depth       : depth,
                synonymsHtml: synonymsHtml
        ]
    }
}