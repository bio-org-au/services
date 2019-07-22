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

import spock.lang.Specification

class SearchQueryCategorySpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "escapeRegexSpecialChars works on examples"() {
        when:
        String output = SearchQueryCategory.escapeRegexSpecialChars(test)

        then:
        println output
        output == result

        where:
        test                                                       | result
        ".[]()+?*\\"                                               | "\\.\\[\\]\\(\\)\\+\\?\\*\\\\"
        "This is a sentence. [we] f(23x2) a+b=? *this* pigs\\cats" | "This is a sentence\\. \\[we\\] f\\(23x2\\) a\\+b=\\? \\*this\\* pigs\\\\cats"
        "oh. that. is. 2.34"                                       | "oh\\. that\\. is\\. 2\\.34"
    }

    void "sqlToRegexWildCard works on examples"() {
        when:
        String output = SearchQueryCategory.sqlToRegexWildCard(test)

        then:
        println output
        output == result

        where:
        test              | result
        "hail mar%"       | "hail mar.*"
        "%var.%"          | ".*var..*"
        "the quick%var.%" | "the quick.*var..*"
    }

    void "replaceMultiplicationSignWithX works on examples"() {
        when:
        String output = SearchQueryCategory.replaceMultiplicationSignWithX(test)

        then:
        println output
        output == result

        where:
        test           | result
        "floop × flop" | "floop x\\sflop"
        "floop ×flop"  | "floop x\\sflop"
    }

    void "compressSpaces works on examples"() {
        when:
        String output = SearchQueryCategory.compressSpaces(test)

        then:
        println output
        output == result

        where:
        test                                        | result
        "floop  flop"                               | "floop flop"
        "floop flop"                                | "floop flop"
        "this   \n    is a little \t\t tab\n space" | "this is a little tab space"
    }

    void "multiSpaceRegex works on examples"() {
        when:
        String output = SearchQueryCategory.multiSpaceRegex(test)

        then:
        println output
        output == result

        where:
        test                                        | result
        "floop  flop"                               | "floop +flop"
        "floop flop"                                | "floop +flop"
        "this   \n    is a little \t\t tab\n space" | "this +is +a +little +tab +space"
    }

    void "topAndTail works on examples"() {
        when:
        String output = SearchQueryCategory.topAndTail(test)

        then:
        println output
        output == result

        where:
        test                             | result
        "\"floop flop\""                | "floop flop"
    }
}
