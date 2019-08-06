package au.org.biodiversity.nsl

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.plugins.codecs.HTMLCodec
import spock.lang.Specification

class NameConstructionServiceSpec extends Specification implements ServiceUnitTest<NameConstructionService>, DataTest {

    void setupSpec() {
        mockDomains NameGroup, NameRank, Name
    }

    def setup() {
        String.metaClass.encodeAsHTML = {
            HTMLCodec.xml_encoder.encode(delegate)
        }
        String.metaClass.decodeHTML = {
            HTMLCodec.decoder.decode(delegate)
        }

        //create basic nameRanks
        List<Map> data = [
                [useVerbatimRank: false, abbrev: 'reg.', name: 'Regnum', displayName: 'Regnum', sortOrder: 10, descriptionHtml: '[description of <b>Regnum</b>]', rdfId: 'regnum'],
                [useVerbatimRank: false, abbrev: 'div.', name: 'Division', displayName: 'Division', sortOrder: 20, descriptionHtml: '[description of <b>Division</b>]', rdfId: 'division'],
                [useVerbatimRank: false, abbrev: 'cl.', name: 'Classis', displayName: 'Classis', sortOrder: 30, descriptionHtml: '[description of <b>Classis</b>]', rdfId: 'classis'],
                [useVerbatimRank: false, abbrev: 'subcl.', name: 'Subclassis', displayName: 'Subclassis', sortOrder: 40, descriptionHtml: '[description of <b>Subclassis</b>]', rdfId: 'subclassis'],
                [useVerbatimRank: false, abbrev: 'superordo', name: 'Superordo', displayName: 'Superordo', sortOrder: 50, descriptionHtml: '[description of <b>Superordo</b>]', rdfId: 'superordo'],
                [useVerbatimRank: false, abbrev: 'ordo', name: 'Ordo', displayName: 'Ordo', sortOrder: 60, descriptionHtml: '[description of <b>Ordo</b>]', rdfId: 'ordo'],
                [useVerbatimRank: false, abbrev: 'subordo', name: 'Subordo', displayName: 'Subordo', sortOrder: 70, descriptionHtml: '[description of <b>Subordo</b>]', rdfId: 'subordo'],
                [useVerbatimRank: false, abbrev: 'fam.', name: 'Familia', displayName: 'Familia', sortOrder: 80, descriptionHtml: '[description of <b>Familia</b>]', rdfId: 'familia'],
                [useVerbatimRank: false, abbrev: 'subfam.', name: 'Subfamilia', displayName: 'Subfamilia', sortOrder: 90, descriptionHtml: '[description of <b>Subfamilia</b>]', rdfId: 'subfamilia'],
                [useVerbatimRank: false, abbrev: 'trib.', name: 'Tribus', displayName: 'Tribus', sortOrder: 100, descriptionHtml: '[description of <b>Tribus</b>]', rdfId: 'tribus'],
                [useVerbatimRank: false, abbrev: 'subtrib.', name: 'Subtribus', displayName: 'Subtribus', sortOrder: 110, descriptionHtml: '[description of <b>Subtribus</b>]', rdfId: 'subtribus'],
                [useVerbatimRank: false, abbrev: 'gen.', name: 'Genus', displayName: 'Genus', sortOrder: 120, descriptionHtml: '[description of <b>Genus</b>]', rdfId: 'genus'],
                [useVerbatimRank: false, abbrev: 'subg.', name: 'Subgenus', displayName: 'Subgenus', sortOrder: 130, descriptionHtml: '[description of <b>Subgenus</b>]', rdfId: 'subgenus'],
                [useVerbatimRank: false, abbrev: 'sect.', name: 'Sectio', displayName: 'Sectio', sortOrder: 140, descriptionHtml: '[description of <b>Sectio</b>]', rdfId: 'sectio'],
                [useVerbatimRank: false, abbrev: 'subsect.', name: 'Subsectio', displayName: 'Subsectio', sortOrder: 150, descriptionHtml: '[description of <b>Subsectio</b>]', rdfId: 'subsectio'],
                [useVerbatimRank: false, abbrev: 'ser.', name: 'Series', displayName: 'Series', sortOrder: 160, descriptionHtml: '[description of <b>Series</b>]', rdfId: 'series'],
                [useVerbatimRank: false, abbrev: 'subser.', name: 'Subseries', displayName: 'Subseries', sortOrder: 170, descriptionHtml: '[description of <b>Subseries</b>]', rdfId: 'subseries'],
                [useVerbatimRank: false, abbrev: 'supersp.', name: 'Superspecies', displayName: 'Superspecies', sortOrder: 180, descriptionHtml: '[description of <b>Superspecies</b>]', rdfId: 'superspecies'],
                [useVerbatimRank: false, abbrev: 'sp.', name: 'Species', displayName: 'Species', sortOrder: 190, descriptionHtml: '[description of <b>Species</b>]', rdfId: 'species'],
                [useVerbatimRank: false, abbrev: 'subsp.', name: 'Subspecies', displayName: 'Subspecies', sortOrder: 200, descriptionHtml: '[description of <b>Subspecies</b>]', rdfId: 'subspecies'],
                [useVerbatimRank: false, abbrev: 'nothovar.', name: 'Nothovarietas', displayName: 'Nothovarietas', sortOrder: 210, descriptionHtml: '[description of <b>Nothovarietas</b>]', rdfId: 'nothovarietas'],
                [useVerbatimRank: false, abbrev: 'var.', name: 'Varietas', displayName: 'Varietas', sortOrder: 210, descriptionHtml: '[description of <b>Varietas</b>]', rdfId: 'varietas'],
                [useVerbatimRank: false, abbrev: 'subvar.', name: 'Subvarietas', displayName: 'Subvarietas', sortOrder: 220, descriptionHtml: '[description of <b>Subvarietas</b>]', rdfId: 'subvarietas'],
                [useVerbatimRank: false, abbrev: 'f.', name: 'Forma', displayName: 'Forma', sortOrder: 230, descriptionHtml: '[description of <b>Forma</b>]', rdfId: 'forma'],
                [useVerbatimRank: false, abbrev: 'subf.', name: 'Subforma', displayName: 'Subforma', sortOrder: 240, descriptionHtml: '[description of <b>Subforma</b>]', rdfId: 'subforma'],
                [useVerbatimRank: false, abbrev: 'form taxon', name: 'form taxon', displayName: 'form taxon', sortOrder: 250, descriptionHtml: '[description of <b>form taxon</b>]', rdfId: 'form-taxon'],
                [useVerbatimRank: false, abbrev: 'morph.', name: 'morphological var.', displayName: 'morphological var.', sortOrder: 260, descriptionHtml: '[description of <b>morphological var.</b>]', rdfId: 'morphological-var'],
                [useVerbatimRank: false, abbrev: 'nothomorph', name: 'nothomorph.', displayName: 'nothomorph.', sortOrder: 270, descriptionHtml: '[description of <b>nothomorph.</b>]', rdfId: 'nothomorph'],
                [useVerbatimRank: true, abbrev: '[unranked]', name: '[unranked]', displayName: '[unranked]', sortOrder: 500, descriptionHtml: '[description of <b>[unranked]</b>]', rdfId: 'unranked'],
                [useVerbatimRank: true, abbrev: '[infrafamily]', name: '[infrafamily]', displayName: '[infrafamily]', sortOrder: 500, descriptionHtml: '[description of <b>[infrafamily]</b>]', rdfId: 'infrafamily'],
                [useVerbatimRank: true, abbrev: '[infragenus]', name: '[infragenus]', displayName: '[infragenus]', sortOrder: 500, descriptionHtml: '[description of <b>[infragenus]</b>]', rdfId: 'infragenus'],
                [useVerbatimRank: true, abbrev: '[infrasp.]', name: '[infraspecies]', displayName: '[infraspecies]', sortOrder: 500, descriptionHtml: '[description of <b>[infraspecies]</b>]', rdfId: 'infraspecies'],
                [useVerbatimRank: true, abbrev: '[unknown]', name: '[unknown]', displayName: '[unknown]', sortOrder: 500, descriptionHtml: '[description of <b>[unknown]</b>]', rdfId: 'unknown'],
                [useVerbatimRank: false, abbrev: 'regio', name: 'Regio', displayName: 'Regio', sortOrder: 8, descriptionHtml: '[description of <b>Regio</b>]', rdfId: 'regio'],
                [useVerbatimRank: false, abbrev: '[n/a]', name: '[n/a]', displayName: '[n/a]', sortOrder: 500, descriptionHtml: '[description of <b>[n/a]</b>]', rdfId: 'n-a']
        ]
        NameGroup group = new NameGroup([name: 'group'])
        data.each { Map d ->
            NameRank r = new NameRank(d)
            r.nameGroup = group
            r.save()
        }

    }

    def cleanup() {
    }

    void "test makeSortName returns expected results"() {
        when: "we convert a simple name string"
        Name name = new Name(simpleName: test, nameRank: NameRank.findByName(rank))
        String output = service.makeSortName(name, name.simpleName)

        then:
        output == result

        where:
        test                                          | rank         | result
        "Ptilotus"                                    | 'Genus'      | "ptilotus"
        "Ptilotus sect. Ptilotus"                     | 'Sectio'     | "ptilotus ptilotus"
        "Ptilotus ser. Ptilotus"                      | 'Series'     | "ptilotus ptilotus"
        "Ptilotus aervoides"                          | 'Species'    | "ptilotus aervoides"
        "Ptilotus albidus"                            | 'Species'    | "ptilotus albidus"
        "Ptilotus alexandri"                          | 'Species'    | "ptilotus alexandri"
        "Ptilotus alopecuroides"                      | 'Species'    | "ptilotus alopecuroides"
        "Ptilotus alopecuroideus"                     | 'Species'    | "ptilotus alopecuroideus"
        "Ptilotus alopecuroideus f. alopecuroideus"   | 'Forma'      | "ptilotus alopecuroideus alopecuroideus"
        "Ptilotus alopecuroideus var. alopecuroideus" | 'Varietas'   | "ptilotus alopecuroideus alopecuroideus"
        "Ptilotus alopecuroideus f. rubriflorus"      | 'Forma'      | "ptilotus alopecuroideus rubriflorus"
        "Ptilotus alopecuroideus var. longistachyus"  | 'Varietas'   | "ptilotus alopecuroideus longistachyus"
        "Ptilotus alopecuroideus var. rubriflorum"    | 'Varietas'   | "ptilotus alopecuroideus rubriflorum"
        "Ptilotus alopecuroideus var. rubriflorus"    | 'Varietas'   | "ptilotus alopecuroideus rubriflorus"
        "Ptilotus amabilis"                           | 'Species'    | "ptilotus amabilis"
        "Ptilotus andersonii"                         | 'Species'    | "ptilotus andersonii"
        "Ptilotus aphyllus"                           | 'Species'    | "ptilotus aphyllus"
        "Ptilotus appendiculatus"                     | 'Species'    | "ptilotus appendiculatus"
        "Ptilotus appendiculatus var. appendiculatus" | 'Varietas'   | "ptilotus appendiculatus appendiculatus"
        "Ptilotus appendiculatus var. minor"          | 'Varietas'   | "ptilotus appendiculatus minor"
        "Ptilotus aristatus"                          | 'Species'    | "ptilotus aristatus"
        "Ptilotus aristatus subsp. aristatus"         | 'Subspecies' | "ptilotus aristatus aristatus"
        "Ptilotus aristatus var. aristatus"           | 'Varietas'   | "ptilotus aristatus aristatus"
        "Ptilotus aristatus subsp. micranthus"        | 'Subspecies' | "ptilotus aristatus micranthus"
        "Ptilotus aristatus var. eichlerianus"        | 'Varietas'   | "ptilotus aristatus eichlerianus"
        "Ptilotus aristatus var. exilis"              | 'Varietas'   | "ptilotus aristatus exilis"
        "Ptilotus aristatus var. stenophyllus"        | 'Varietas'   | "ptilotus aristatus stenophyllus"

    }

    void "test strip markup"() {
        when: "We strip HTML from a name"

        String result = service.stripMarkUp(source)

        then:

        result == expected

        where:

        source                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   | expected
        "<scientific><name id='63600'><scientific><name id='102507'><element class='Eucalyptus'>Eucalyptus</element></name></scientific> <rank id='54419'>[infragen.]</rank> <element class='&#39;Terminales&#39;'>&#39;Terminales&#39;</element> <authors><author id='2328' title='Maiden, J,H,'>Maiden</author></authors></name></scientific>"                                                                                                                                                 | "Eucalyptus [infragen.] 'Terminales' Maiden"
        "<scientific><name id='80260'><scientific><name id='80197'><scientific><name id='80102'><element class='Gentiana'>Gentiana</element></name></scientific> <element class='montana'>montana</element></name></scientific> <rank id='54412'>var.</rank> <element class='&delta; pleurogynoides'>&delta; pleurogynoides</element> <authors>(<base id='7130' title='Grisebach, A.H.R.'>Griseb.</base>) <author id='6847' title='Hooker, J.D.'>Hook.f.</author></authors></name></scientific>" | "Gentiana montana var. δ pleurogynoides (Griseb.) Hook.f."
        "<scientific><name id='111716'><scientific><name id='111692'><scientific><name id='92530'><element class='Scirpus'>Scirpus</element></name></scientific> <element class='costatus'>costatus</element></name></scientific> <rank id='54412'>var.</rank> <element class='&beta; gracilis'>&beta; gracilis</element> <authors><author id='6990' title='Boeckeler, J.O.'>Boeckeler</author></authors></name></scientific>"                                                                   | "Scirpus costatus var. β gracilis Boeckeler"
        "<scientific><name id='112268'><scientific><name id='112237'><scientific><name id='92530'><element class='Scirpus'>Scirpus</element></name></scientific> <element class='gaudichaudii'>gaudichaudii</element></name></scientific> <rank id='54412'>var.</rank> <element class='&beta; tenuis'>&beta; tenuis</element> <authors><author id='6990' title='Boeckeler, J.O.'>Boeckeler</author></authors></name></scientific>"                                                               | "Scirpus gaudichaudii var. β tenuis Boeckeler"
        "<scientific><name id='114258'><scientific><name id='114164'><scientific><name id='92530'><element class='Scirpus'>Scirpus</element></name></scientific> <element class='pungens'>pungens</element></name></scientific> <rank id='54412'>var.</rank> <element class='&beta; polyphyllus'>&beta; polyphyllus</element> <authors><author id='6990' title='Boeckeler, J.O.'>Boeckeler</author></authors></name></scientific>"                                                               | "Scirpus pungens var. β polyphyllus Boeckeler"
        "<scientific><name id='119357'><scientific><name id='77505'><element class='Helichrysum'>Helichrysum</element></name></scientific> <element class='sp. Point Lookout&#39;(I.R.Telford 10758)'>sp. Point Lookout&#39;(I.R.Telford 10758)</element></name></scientific>"                                                                                                                                                                                                                   | "Helichrysum sp. Point Lookout'(I.R.Telford 10758)"
        "<scientific><name id='119824'><scientific><name id='119823'><element class='Diplora'>Diplora</element></name></scientific> <element class='d&#39;urvillie'>d&#39;urvillie</element> <authors>(<base id='7416' title='Bory de Saint-Vincent, J.B.G.G.M.'>Bory</base>) <author id='7073' title='Christensen, C.F.A.'>C.Chr.</author></authors></name></scientific>"                                                                                                                       | "Diplora d'urvillie (Bory) C.Chr."
        "<scientific><name id='119827'><scientific><name id='119823'><element class='Diplora'>Diplora</element></name></scientific> <element class='d&#39;urvillei'>d&#39;urvillei</element> <authors>(<base id='7416' title='Bory de Saint-Vincent, J.B.G.G.M.'>Bory</base>) <author id='7073' title='Christensen, C.F.A.'>C.Chr.</author></authors></name></scientific>"                                                                                                                       | "Diplora d'urvillei (Bory) C.Chr."
        "<scientific><name id='119828'><scientific><name id='119823'><element class='Diplora'>Diplora</element></name></scientific> <element class='d&#39;urvillei'>d&#39;urvillei</element> <authors>(<base id='7416' title='Bory de Saint-Vincent, J.B.G.G.M.'>Bory</base>) <author id='7073' title='Christensen, C.F.A.'>C.Chr.</author></authors></name></scientific>"                                                                                                                       | "Diplora d'urvillei (Bory) C.Chr."
        "<cultivar><name id='120074'><scientific><name id='85267'><scientific><name id='85248'><element class='Goniophlebium'>Goniophlebium</element></name></scientific> <element class='subauriculatum'>subauriculatum</element></name></scientific> <element class='Knightiae'>&lsquo;Knightiae&rsquo;</element></name></cultivar>"                                                                                                                                                           | "Goniophlebium subauriculatum 'Knightiae'"
        "<scientific><name id='120959'><scientific><name id='120958'><element class='Phragmipedium'>Phragmipedium</element></name></scientific> <element class='caudatum &#39;Sanderae&#39;'>caudatum &#39;Sanderae&#39;</element></name></scientific>"                                                                                                                                                                                                                                          | "Phragmipedium caudatum 'Sanderae'"
        "<scientific><name id='121105'><scientific><name id='98936'><element class='Calochilus'>Calochilus</element></name></scientific> <element class='paludosus &quot;albino&quot;'>paludosus &quot;albino&quot;</element></name></scientific>"                                                                                                                                                                                                                                               | "Calochilus paludosus \"albino\""
        "<scientific><name id='121106'><scientific><name id='78645'><element class='Diuris'>Diuris</element></name></scientific> <element class='punctata &quot;albino&quot;'>punctata &quot;albino&quot;</element></name></scientific>"                                                                                                                                                                                                                                                         | "Diuris punctata \"albino\""
        "<scientific><name id='121337'><scientific><name id='86062'><element class='Styphelia'>Styphelia</element></name></scientific> <element class='sp. 1 (Deua N.P. &amp; Nalbaugh N.P.)'>sp. 1 (Deua N.P. &amp; Nalbaugh N.P.)</element></name></scientific>"                                                                                                                                                                                                                               | "Styphelia sp. 1 (Deua N.P. & Nalbaugh N.P.)"
        "<scientific><name id='121424'><scientific><name id='120603'><element class='Paphiopedilum'>Paphiopedilum</element></name></scientific> <element class='&#39;Deedmannianum&#39;'>&#39;Deedmannianum&#39;</element></name></scientific>"                                                                                                                                                                                                                                                  | "Paphiopedilum 'Deedmannianum'"
        "<scientific><name id='121941'><scientific><name id='78706'><element class='Leptospermum'>Leptospermum</element></name></scientific> <element class='sp. nov. &quot;M&quot;'>sp. nov. &quot;M&quot;</element></name></scientific>"                                                                                                                                                                                                                                                       | "Leptospermum sp. nov. \"M\""
        "<scientific><name id='121944'><scientific><name id='78706'><element class='Leptospermum'>Leptospermum</element></name></scientific> <element class='sp. nov. &quot;N&quot;'>sp. nov. &quot;N&quot;</element></name></scientific>"                                                                                                                                                                                                                                                       | "Leptospermum sp. nov. \"N\""
        "<scientific><name id='121947'><scientific><name id='78706'><element class='Leptospermum'>Leptospermum</element></name></scientific> <element class='sp. nov. &quot;K&quot;'>sp. nov. &quot;K&quot;</element></name></scientific>"                                                                                                                                                                                                                                                       | "Leptospermum sp. nov. \"K\""
        "<scientific><name id='121949'><scientific><name id='78706'><element class='Leptospermum'>Leptospermum</element></name></scientific> <element class='sp.n. &quot;A&quot;'>sp.n. &quot;A&quot;</element></name></scientific>"                                                                                                                                                                                                                                                             | "Leptospermum sp.n. \"A\""
        "<scientific><name id='122088'><scientific><name id='83527'><element class='Telopea'>Telopea</element></name></scientific> <element class='sp. &quot;Gibraltar Range&quot;'>sp. &quot;Gibraltar Range&quot;</element> <manuscript>MS</manuscript></name></scientific>"                                                                                                                                                                                                                   | "Telopea sp. \"Gibraltar Range\" MS"
        "<scientific><name id='122089'><scientific><name id='74468'><element class='Hakea'>Hakea</element></name></scientific> <element class='sp. 2 (Stanley &amp; Ross 1986)'>sp. 2 (Stanley &amp; Ross 1986)</element></name></scientific>"                                                                                                                                                                                                                                                   | "Hakea sp. 2 (Stanley & Ross 1986)"
        "<scientific><name id='123088'><scientific><name id='84495'><element class='Dendrobium'>Dendrobium</element></name></scientific> <element class='&#39;pitpit&#39;'>&#39;pitpit&#39;</element> <authors><author id='8957' title='c.f. #Veersteegh &amp; Vink in BW 8275'>[MS]</author></authors></name></scientific>"                                                                                                                                                                     | "Dendrobium 'pitpit' [MS]"
        "<scientific><name id='123103'><scientific><name id='82809'><element class='Oberonia'>Oberonia</element></name></scientific> <element class='&#39;tep tep&#39;'>&#39;tep tep&#39;</element></name></scientific>"                                                                                                                                                                                                                                                                         | "Oberonia 'tep tep'"
        "<scientific><name id='123385'><scientific><name id='89061'><element class='Ctenitis'>Ctenitis</element></name></scientific> <element class='sp. nov. &quot;croftii&quot;'>sp. nov. &quot;croftii&quot;</element></name></scientific>"                                                                                                                                                                                                                                                   | "Ctenitis sp. nov. \"croftii\""
    }
}
