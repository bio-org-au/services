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
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(DataExportService)
class DataExportServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test metadata creation"() {
        given:
        File taxaCsvFile = new File("taxa.csv")
        File relationshipCsvFile = new File("relationships.csv")
        String expectedXml = '''<archive xmlns='http://rs.tdwg.org/dwc/text/' metadata='description.xml' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://rs.tdwg.org/dwc/text/ http://rs.tdwg.org/dwc/text/tdwg_dwc_text.xsd'>
  <core ignoreHeaderLines='1' fieldsTerminatedBy=',' fieldsEnclosedBy='"' rowType='http://rs.tdwg.org/dwc/terms/Taxa'>
    <files>
      <location>taxa.csv</location>
    </files>
    <id index='0' />
    <field index='1' term='http://rs.tdwg.org/dwc/terms/taxonID' />
    <field index='2' term='http://rs.tdwg.org/dwc/terms/acceptedNameUsageID' />
    <field index='3' term='http://rs.tdwg.org/dwc/terms/parentNameUsageID' />
    <field index='4' term='http://rs.tdwg.org/dwc/terms/scientificName' />
    <field index='5' term='http://rs.tdwg.org/dwc/terms/vernacularName' />
    <field index='6' term='http://rs.tdwg.org/ontology/voc/TaxonName#cultivarNameGroup' />
    <field index='7' term='http://rs.tdwg.org/dwc/terms/acceptedNameUsage' />
    <field index='8' term='http://rs.tdwg.org/dwc/terms/parentNameUsage' />
    <field index='9' term='http://rs.tdwg.org/dwc/terms/namePublishedIn' />
    <field index='10' term='http://rs.tdwg.org/dwc/terms/namePublishedInYear' />
    <field index='11' term='http://rs.tdwg.org/dwc/terms/class' />
    <field index='12' term='http://rs.tdwg.org/dwc/terms/family' />
    <field index='13' term='http://rs.tdwg.org/dwc/terms/genus' />
    <field index='14' term='http://rs.tdwg.org/dwc/terms/specificEpithet' />
    <field index='15' term='http://rs.tdwg.org/dwc/terms/infraspecificEpithet' />
    <field index='16' term='http://rs.tdwg.org/dwc/terms/taxonRank' />
    <field index='17' term='http://rs.tdwg.org/dwc/terms/verbatimTaxonRank' />
    <field index='18' term='http://rs.tdwg.org/dwc/terms/scientificNameAuthorship' />
    <field index='19' term='http://rs.tdwg.org/dwc/terms/nomenclaturalCode' />
    <field index='20' term='http://rs.tdwg.org/dwc/terms/taxonomicStatus' />
    <field index='21' term='http://rs.tdwg.org/dwc/terms/nomenclaturalStatus' />
    <field index='22' term='http://www.biodiversity.org.au/voc/boa/Name.rdf#type' />
    <field index='23' term='http://rs.tdwg.org/dwc/terms/taxonRemarks' />
  </core>
  <extension ignoreHeaderLines='1' fieldsTerminatedBy=',' fieldsEnclosedBy='"' rowType='http://rs.tdwg.org/dwc/terms/ResourceRelationship'>
    <files>
      <location>relationships.csv</location>
    </files>
    <id index='0' />
    <field index='0' term='http://rs.tdwg.org/dwc/terms/resourceRelationshipID ' />
    <field index='1' term='http://rs.tdwg.org/dwc/terms/resourceID ' />
    <field index='2' term='http://rs.tdwg.org/dwc/terms/relatedResourceID ' />
    <field index='3' term='http://rs.tdwg.org/dwc/terms/relationshipOfResource ' />
    <field index='4' term='http://rs.tdwg.org/dwc/terms/relationshipAccordingTo ' />
    <field index='5' term='http://rs.tdwg.org/dwc/terms/relationshipEstablishedDate ' />
    <field index='6' term='http://rs.tdwg.org/dwc/terms/relationshipRemarks' />
  </extension>
</archive>'''

        expect:
        service.dcaMetaDataXml(taxaCsvFile, relationshipCsvFile) == expectedXml

    }

    void "test base output directory"() {
        given:
        service.configService = [tempFileDir: '/tmp']
        File dir = service.getBaseDir()
        if (dir.exists()) {
            //clean up
            dir.deleteDir()
            dir = service.getBaseDir()
        }

        expect:

        dir.exists()
        dir.directory

        when: "I create a file"
        File file = new File(dir, 'test.text')
        file.write('test')

        then: "It exists"

        file.exists()

        when: "I get the base dir again"
        dir = service.getBaseDir()

        then: "the file is still there (doesn't delete dir)"

        file.exists()

        when: "I can delete the file"
        file.delete()

        then: "and it doesn't exist"
        !file.exists()

        when: "I can delete the directory and contents"
        dir.deleteDir()

        then:
        !dir.exists()
    }

    //removed tests for currently unused or removed functions.
}
