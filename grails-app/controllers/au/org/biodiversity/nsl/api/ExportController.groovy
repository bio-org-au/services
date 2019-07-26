package au.org.biodiversity.nsl.api

import org.postgresql.util.PSQLException

class ExportController {

    def flatViewService
    def configService

    def index() {

        [
                exports: [
                        [label: "${configService.nameTreeName} Names as CSV", url: 'namesCsv'],
                        [label: "${configService.classificationTreeName} Taxon as CSV", url: 'taxonCsv'],
                        [label: "${configService.nameTreeName} Common Names as CSV", url: 'commonCsv'],
                ]
        ]
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    static responseFormats = [
            index   : ['html'],
            namesCsv: ['json', 'xml', 'html'],
            taxonCsv: ['json', 'xml', 'html'],
    ]

    static allowedMethods = [
            namesCsv: ["GET"],
            taxonCsv: ["GET"],
    ]


    def namesCsv() {
        File exportFile = null
        try {
            exportFile = flatViewService.exportNamesToCSV()
            render(file: exportFile, fileName: exportFile.name, contentType: 'text/csv')
        } finally {
            exportFile?.delete()
        }
    }


    def taxonCsv() {
        File exportFile = null
        try {
            exportFile = flatViewService.exportTaxonToCSV()
            render(file: exportFile, fileName: exportFile.name, contentType: 'text/csv')
        } finally {
            exportFile?.delete()
        }
    }


    def commonCsv() {
        File exportFile = null
        try {
            exportFile = flatViewService.exportCommonToCSV()
            render(file: exportFile, fileName: exportFile.name, contentType: 'text/csv')
        } finally {
            exportFile?.delete()
        }
    }

    def handlePSQLException(PSQLException e) {
        flash.message = "Error getting export. Please contact us via \"Provide Feedback\" tab."
        log.error("Error getting view $e.message")
        redirect(action: 'index', status: 404)
    }

    def handleException(Exception e) {
        flash.message = "Error getting export. Please contact us via \"Provide Feedback\" tab."
        log.error("Error getting view $e.message")
        redirect(action: 'index', status: 404)
    }
}
