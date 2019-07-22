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

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(ApniFormatService)
class ApniFormatServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test xics elements are replaced correctly"() {
        when:
        String output = ApniFormatService.transformXics(test)

        println output

        then:
        output == result

        where:
        test                                                                                                                 | result
        "Substitute name for WILLEMETIA Marklin ^t KOCHIA Roth according to <IT>Index Nominum Genericorum <RO>1 (1979) 594." | 'Substitute name for WILLEMETIA Marklin = KOCHIA Roth according to <i>Index Nominum Genericorum </i>1 (1979) 594.'
        "<IT>nom. illeg. <RO>non Endl. (1848). Published as `^u <IT>sericea<RO>'."                                           | '<i>nom. illeg. </i>non Endl. (1848). Published as `&alpha; <i>sericea</i>\'.'
        "<IT>Cactus opuntia <RO>L. ^t <IT>Opuntia vulgaris <RO>Miller"                                                       | '<i>Cactus opuntia </i>L. = <i>Opuntia vulgaris </i>Miller'
        "Published as `^v <IT>megaphylla <RO>'."                                                                             | 'Published as `&beta; <i>megaphylla </i>\'.'
        "<IT>Stomoisia cornuta <(Michx.) RO>Raf."                                                                            | '&lt;i&gt;Stomoisia cornuta &lt;(Michx.) RO&gt;Raf.'
        "D.E.Anderson, loc. cit. [i.e. <i>Contributions from the United States National Herbarium</i> 2: 42  (1892)]"        | 'D.E.Anderson, loc. cit. [i.e. <i>Contributions from the United States National Herbarium</i> 2: 42  (1892)]'
        "<p>This is a paragraph</p> <ol><li>list item1</li><li>two<li>three</ol>"                                            | '<p>This is a paragraph</p> <ol><li>list item1</li><li>two</li><li>three</li></ol>'
        "<p>This is a paragraph</p> <ul><li>list item1</li><li>two<li>three</ul>"                                            | '<p>This is a paragraph</p> <ul><li>list item1</li><li>two</li><li>three</li></ul>'
        "I can't use a <div> to break things up but <br> is OK</div>"                                                        | 'I can\'t use a &lt;div&gt; to break things up but <br /> is OK&lt;/div&gt;'
        "look a <a href='http://google.com'>Link</a>"                                                                        | 'look a <a href="http://google.com">Link</a>'
        "this will <b>emboldenate</b>"                                                                                       | 'this will <b>emboldenate</b>'
        "Let it snow ☃"                                                                                                      | 'Let it snow &#9731;'
    }

    void "test all xics elements are replaced"() {

        when:
        String output = ApniFormatService.transformXics(test)

        then:
        output == result

        where:
        test             | result
        '~J'             | '&Aacute;'
        '~c'             | '&Agrave;'
        '~T'             | '&Acirc;'
        '~p'             | '&Auml;'
        '<AOU>'          | '&Aring;'
        '~K'             | '&Eacute;'
        '~d'             | '&Egrave;'
        '~U'             | '&Ecirc;'
        '~q'             | '&Euml;'
        '~L'             | '&Iacute;'
        '~3'             | '&Igrave;'
        '~V'             | '&Icirc;'
        '~r'             | '&Iuml;'
        '~M'             | '&Oacute;'
        '~e'             | '&Ograve;'
        '~W'             | '&Ocirc;'
        '~s'             | '&Ouml;'
        '~N'             | '&Uacute;'
        '~f'             | '&Ugrave;'
        '\\'             | '&Ucirc;'
        '~t'             | '&Uuml;'
        '~z'             | '&Ccedil;'
        '~1'             | '&Ntilde;'
        '~O'             | '&aacute;'
        '~g'             | '&agrave;'
        '~X'             | '&acirc;'
        '~u'             | '&auml;'
        '<AOL>'          | '&aring;'
        '<ATL>'          | '&atilde;'
        '~P'             | '&eacute;'
        '~h'             | '&egrave;'
        '~Y'             | '&ecirc;'
        '~v'             | '&euml;'
        '~Q'             | '&iacute;'
        '~i'             | '&igrave;'
        '~Z'             | '&icirc;'
        '~w'             | '&iuml;'
        '~R'             | '&oacute;'
        '~D'             | '&ograve;'
        '~a'             | '&ocirc;'
        '~x'             | '&ouml;'
        '<OTL>'          | '&otilde;'
        '~S'             | '&uacute;'
        '~E'             | '&ugrave;'
        '~b'             | '&ucirc;'
        '~y'             | '&uuml;'
        '~0'             | '&ccedil;'
        '~2'             | '&ntilde;'
        '<PM>'           | '&plusmn;'
        '<MR>'           | '&#151;'
        '<NR>'           | '&#150;'
        '<BY>'           | '&times;'
        '<DEG>'          | '&deg;'
        '<MIN>'          | '&prime;'
        '^I'             | '&#134;'
        '^K'             | '&#138;'
        '^k'             | '1/2'
        '^l'             | '1/3'
        '^m'             | '2/3'
        '^n'             | '1/4'
        '^o'             | '3/4'
        '^u'             | '&alpha;'
        '^v'             | '&beta;'
        '^w'             | '&gamma;'
        '^x'             | '&delta;'
        '^t'             | '='
        '<13>'           | '1/3'
        '<23>'           | '2/3'
        '<IT><RO>'       | '<i></i>'
        '<IT><RRO>'      | '<i></i>'
        '<MALE>'         | '&#9794;'
        '<M>'            | '&#9794;'
        '<FM>'           | '&#9792;'
        '<F,8>~S<F,4>'   | '&Delta;'
        '<F,17>~u<F,4>'  | '&#10218;'
        '<F,17>~u <F,4>' | '&#10219;'
        '<F,17>^u<F,4>'  | '&#10219;'
    }

    void "test xics elements are replaced with UTF8 correctly"() {
        when:
        String output = ApniFormatService.transformXicsToUTF8(test)

        println output

        then:
        output == result

        where:
        test                                                                                                                 | result
        "Substitute name for WILLEMETIA Marklin ^t KOCHIA Roth according to <IT>Index Nominum Genericorum <RO>1 (1979) 594." | 'Substitute name for WILLEMETIA Marklin = KOCHIA Roth according to <i>Index Nominum Genericorum </i>1 (1979) 594.'
        "<IT>nom. illeg. <RO>non Endl. (1848). Published as `^u <IT>sericea<RO>'."                                           | '<i>nom. illeg. </i>non Endl. (1848). Published as `α <i>sericea</i>\'.'
        "<IT>Cactus opuntia <RO>L. ^t <IT>Opuntia vulgaris <RO>Miller"                                                       | '<i>Cactus opuntia </i>L. = <i>Opuntia vulgaris </i>Miller'
        "Published as `^v <IT>megaphylla <RO>'."                                                                             | 'Published as `β <i>megaphylla </i>\'.'
        "<IT>Stomoisia cornuta <(Michx.) RO>Raf."                                                                            | '<i>Stomoisia cornuta <(Michx.) RO>Raf.'
        "D.E.Anderson, loc. cit. [i.e. <i>Contributions from the United States National Herbarium</i> 2: 42  (1892)]"        | 'D.E.Anderson, loc. cit. [i.e. <i>Contributions from the United States National Herbarium</i> 2: 42  (1892)]'
        "Let it snow ☃"                                                                                                      | 'Let it snow ☃'
        "Lake Takapo, S. Isl., 83, CHEESEMAN (hb. Stockholm.) <F,17>~u<F,4>alt. 2500 feet<F,17>~u<F,4>Det. & comm. AR. BENNETT. Tasmania, R.C. GUNN (hb. Stockholm.)," | "Lake Takapo, S. Isl., 83, CHEESEMAN (hb. Stockholm.) ⟪alt. 2500 feet⟪Det. & comm. AR. BENNETT. Tasmania, R.C. GUNN (hb. Stockholm.),"
    }

    void "test all xics elements are replaced with UTF8"() {

        when:
        String output = ApniFormatService.transformXicsToUTF8(test)

        then:
        output == result

        where:
        test             | result
        '~J'             | 'Á'
        '~c'             | 'À'
        '~T'             | 'Â'
        '~p'             | 'Ä'
        '<AOU>'          | 'Å'
        '~K'             | 'É'
        '~d'             | 'È'
        '~U'             | 'Ê'
        '~q'             | 'Ë'
        '~L'             | 'Í'
        '~3'             | 'Ì'
        '~V'             | 'Î'
        '~r'             | 'Ï'
        '~M'             | 'Ó'
        '~e'             | 'Ò'
        '~W'             | 'Ô'
        '~s'             | 'Ö'
        '~N'             | 'Ú'
        '~f'             | 'Ù'
        '\\'             | 'Û'
        '~t'             | 'Ü'
        '~z'             | 'Ç'
        '~1'             | 'Ñ'
        '~O'             | 'á'
        '~g'             | 'à'
        '~X'             | 'â'
        '~u'             | 'ä'
        '<AOL>'          | 'å'
        '<ATL>'          | 'ã'
        '~P'             | 'é'
        '~h'             | 'è'
        '~Y'             | 'ê'
        '~v'             | 'ë'
        '~Q'             | 'í'
        '~i'             | 'ì'
        '~Z'             | 'î'
        '~w'             | 'ï'
        '~R'             | 'ó'
        '~D'             | 'ò'
        '~a'             | 'ô'
        '~x'             | 'ö'
        '<OTL>'          | 'õ'
        '~S'             | 'ú'
        '~E'             | 'ù'
        '~b'             | 'û'
        '~y'             | 'ü'
        '~0'             | 'ç'
        '~2'             | 'ñ'
        '<PM>'           | '±'
        '<MR>'           | '—'
        '<NR>'           | '–'
        '<BY>'           | '×'
        '<DEG>'          | '°'
        '<MIN>'          | '′'
        '^I'             | '†'
        '^K'             | 'Š'
        '^k'             | '½'
        '^l'             | '⅓'
        '^m'             | '⅔'
        '^n'             | '¼'
        '^o'             | '¾'
        '^u'             | 'α'
        '^v'             | 'β'
        '^w'             | 'γ'
        '^x'             | 'δ'
        '^t'             | '='
        '<13>'           | '⅓'
        '<23>'           | '⅔'
        '<IT><RO>'       | '<i></i>'
        '<IT><RRO>'      | '<i></i>'
        '<MALE>'         | '♂'
        '<M>'            | '♂'
        '<FM>'           | '♀'
        '<F,8>~S<F,4>'   | 'Δ'
        '<F,17>~u<F,4>'  | '⟪'
        '<F,17>^u<F,4>'  | '⟫'
    }

}
