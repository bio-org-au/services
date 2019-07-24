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

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.runtime.IOGroovyMethods
import org.postgresql.PGConnection
import org.postgresql.copy.CopyManager

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Transactional
class DataExportService {

    def grailsApplication
    def configService

    /**
     * export Darwin Core Archive
     *
     * This uses the postgresql function to dump it to a file then returns the file
     *
     */
    @Deprecated
    File exportDarwinCoreArchiveFilesToCSV() {
        Date date = new Date()
        String tempFileDir = getBaseDir()
        String dateString = date.format('yyyy-MM-dd-mmss')

        File outputDirectory = new File(tempFileDir, "dca-$dateString")
        outputDirectory.mkdir()
        File taxaCsvFile = new File(outputDirectory, "taxa-${dateString}.csv")
        File relationshipCsvFile = new File(outputDirectory, "relationships-${dateString}.csv")
        File metadataFile = new File(outputDirectory, "metadata.xml")
        File outputFile = new File(tempFileDir, "darwin-core-archive-${dateString}.zip")

        withSql { sql ->
            metadataFile.write(dcaMetaDataXml(taxaCsvFile, relationshipCsvFile))
            makeDCAStandaloneInstanceExport(sql, taxaCsvFile)
            makeDCARelationshipInstanceExportTable(sql, relationshipCsvFile)
        }
        zip(outputDirectory, outputFile)
        outputDirectory.deleteDir() //clean up
        return outputFile
    }

    File getBaseDir() {
        String tempFileDir = configService.tempFileDir
        File baseDir = new File(tempFileDir, 'nsl-tmp')
        if (baseDir.exists()) {
            return baseDir
        }
        if (baseDir.mkdirs()) {
            return baseDir
        }
        return null
    }

    private withSql(Closure work) {
        Sql sql = configService.getSqlForNSLDB()
        try {
            work(sql)
        } finally {
            sql.close()
        }
    }

    private static zip(File dir, File outputFile) {
        ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(outputFile))
        dir.eachFile { file ->
            println file.name
            zipFile.putNextEntry(new ZipEntry(file.getName()))
            file.withInputStream { i ->
                def buffer = new byte[1024]
                Integer length = 0
                while ((length = i.read(buffer)) > 0) {
                    zipFile.write(buffer, 0, length)
                }
            }
            zipFile.closeEntry()
        }
        zipFile.close()
    }

    /**
     * Wrap a select statement with COPY statement TO STDOUT and save in the local file passed in.
     * This streams the output from the copy to a local file over the JDBC connection using the postgresql CopyManager
     * which is part of the postgresql driver:
     * see https://jdbc.postgresql.org/documentation/publicapi/org/postgresql/copy/CopyManager.html#copyOut%28java.lang.String,%20java.io.Writer%29
     * @param sql
     * @return the file you passed in
     */
    static File sqlCopyToCsvFile(String sqlStatement, File file, Sql sql) {

        String statement = "COPY ($sqlStatement) TO STDOUT WITH ENCODING 'UTF8' CSV HEADER"
        println statement
        CopyManager copyManager = ((PGConnection) sql.connection).getCopyAPI()
        PrintWriter printWriter = new PrintWriter(file,'UTF-8')
        IOGroovyMethods.withWriter(printWriter) { writer ->
            copyManager.copyOut(statement, writer)
        }
        return file
    }

    //Note this metadata is for old export tables - it's saved here to convert to the new exports from the flat view service
    String dcaMetaDataXml(File standalone, File relationship) {
        String TERMS = "http://rs.tdwg.org/dwc/terms"
        String TDWG_TERMS = "http://rs.tdwg.org/ontology/voc"
        String OUR_TERMS = "http://www.biodiversity.org.au/voc/boa"

        def writer = new StringWriter()
        def builder = new MarkupBuilder(writer)
        builder.archive(
                xmlns: "http://rs.tdwg.org/dwc/text/",
                metadata: "description.xml",
                'xmlns:xsi': "http://www.w3.org/2001/XMLSchema-instance",
                'xsi:schemaLocation': "http://rs.tdwg.org/dwc/text/ http://rs.tdwg.org/dwc/text/tdwg_dwc_text.xsd"
        ) {
            core(
                    ignoreHeaderLines: "1",
                    fieldsTerminatedBy: ",",
                    fieldsEnclosedBy: '"',
                    rowType: "$TERMS/Taxa"
            ) {
                files {
                    location standalone.name
                }
                id(index: 0)                                              // this instance id
                field(index: 1, term: "$TERMS/taxonID")                   // the name id
                field(index: 2, term: "$TERMS/acceptedNameUsageID")       // APC accepted name id
                field(index: 3, term: "$TERMS/parentNameUsageID")         // parent name id
                field(index: 4, term: "$TERMS/scientificName")            // the full name - if scientific Name
                field(index: 5, term: "$TERMS/vernacularName")            // common name - if common name instance
                field(index: 6, term: "$TDWG_TERMS/TaxonName#cultivarNameGroup") // cultivar name - if not scientific
                field(index: 7, term: "$TERMS/acceptedNameUsage")         // the APC accepted full name
                field(index: 8, term: "$TERMS/parentNameUsage")          // the parent full name
                field(index: 9, term: "$TERMS/namePublishedIn")          // reference citation
                field(index: 10, term: "$TERMS/namePublishedInYear")      // reference year
                //tree branch
                field(index: 11, term: "$TERMS/class")                    //
                field(index: 12, term: "$TERMS/family")                   //
                field(index: 13, term: "$TERMS/genus")                    //
                field(index: 14, term: "$TERMS/specificEpithet")          // species
                field(index: 15, term: "$TERMS/infraspecificEpithet")     //
                field(index: 16, term: "$TERMS/taxonRank")                // Rank name
                field(index: 17, term: "$TERMS/verbatimTaxonRank")        // verbatim rank
                field(index: 18, term: "$TERMS/scientificNameAuthorship") // name author
                field(index: 19, term: "$TERMS/nomenclaturalCode")        // "ICBN"
                field(index: 20, term: "$TERMS/taxonomicStatus")          // instance type
                field(index: 21, term: "$TERMS/nomenclaturalStatus")      // name_status
                field(index: 22, term: "$OUR_TERMS/Name.rdf#type")        // name_type
                field(index: 23, term: "$TERMS/taxonRemarks")       // instance notes - concatenate?

            }

            extension(
                    ignoreHeaderLines: "1",
                    fieldsTerminatedBy: ",",
                    fieldsEnclosedBy: '"',
                    rowType: "$TERMS/ResourceRelationship"
            ) {
                files {
                    location relationship.name
                }
                id(index: 0) // this instance id
                field(index: 0, term: "$TERMS/resourceRelationshipID ")       // this instance ID
                field(index: 1, term: "$TERMS/resourceID ")                   // cited by instance ID
                field(index: 2, term: "$TERMS/relatedResourceID ")            // cites instance ID
                field(index: 3, term: "$TERMS/relationshipOfResource ")       // instance type
                field(index: 4, term: "$TERMS/relationshipAccordingTo ")      // cited by reference citation
                field(index: 5, term: "$TERMS/relationshipEstablishedDate ")  // cited by reference year
                field(index: 6, term: "$TERMS/relationshipRemarks")           // instance notes - concatenate?
            }
        }
        return writer.toString()
    }

}
