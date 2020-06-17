package au.org.biodiversity.nsl

/**
 * User: pmcneil
 * Date: 22/11/18
 *
 */
class SynonymDiffMarker {

    private static markUpChanges(input, Boolean markMove, Closure filter, Closure marker) {
        ABPair output = new ABPair()
        ABPair comp = new ABPair(input.a.collect(filter), input.b.collect(filter))
        if (markMove) {
            markUpDifference(input, comp, output, marker)
        } else {
            markUpStaticDifference(input, comp, output, marker)
        }
        return output
    }

    static ABPair markUpTagChanges(ABPair input, String tag, Boolean markMove = false) {
        markUpChanges(input, markMove, { extractTagFromHtml(it, tag) }) { String line, String cssClass ->
            line.replaceFirst("<$tag([^>]*)>", "<$tag \$1 class='${cssClass}'>")
        }
    }

    private static markUpDifference(ABPair input, ABPair comp, ABPair output, Closure markUp) {

        Integer size = Math.max(input.a.size(), input.b.size())
        if (size) {
            0.upto(size - 1) { i ->
                String oldLine = input.a[i as Integer]
                String oldComp = comp.a[i as Integer]
                String newLine = input.b[i as Integer]
                String newComp = comp.b[i as Integer]
                if (oldLine && oldComp) {
                    if (!comp.b.contains(oldComp)) {
                        output.a << markUp(oldLine, 'target minus')
                    } else if (oldComp != newComp) {
                        output.a << '<div class="targetMoved">⇅ ' + oldLine + '</div>'
                    } else {
                        output.a << oldLine
                    }
                }

                if (newLine && newComp) {
                    if (!comp.a.contains(newComp)) {
                        output.b << markUp(newLine, 'target plus')
                    } else if (newComp != oldComp) {
                        output.b << '<div class="targetMoved">⇅ ' + newLine + '</div>'
                    } else {
                        output.b << newLine
                    }
                }
            }
        }
    }

    private static markUpStaticDifference(ABPair input, ABPair comp, ABPair output, Closure markUp) {

        Integer size = Math.max(input.a.size(), input.b.size())
        if (size) {
            0.upto(size - 1) { i ->
                String oldLine = input.a[i as Integer]
                String oldComp = comp.a[i as Integer]
                String newLine = input.b[i as Integer]
                String newComp = comp.b[i as Integer]
                if (oldLine && oldComp) {
                    //the div check stops the comparison if the name has moved.
                    if (!oldLine.startsWith('<div') && oldComp != newComp) {
                        output.a << markUp(oldLine, 'target minus')
                    } else {
                        output.a << oldLine
                    }
                }

                if (newLine && newComp) {
                    //the div check stops the comparison if the name has moved.
                    if (!newLine.startsWith('<div') && newComp != oldComp) {
                        output.b << markUp(newLine, 'target plus')
                    } else {
                        output.b << newLine
                    }
                }
            }
        }
    }

    static String extractTagFromHtml(String synonymHtml, String tag) {
        synonymHtml.replaceAll(".*<$tag[^>]*>(.*)</$tag>.*", '$1')
    }

}

class ABPair {

    final List<String> a
    final List<String> b

    ABPair(List<String> a, List<String> b) {
        this.a = a
        this.b = b
    }

    ABPair() {
        this.a = []
        this.b = []
    }
}