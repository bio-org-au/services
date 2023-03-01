package au.org.biodiversity.nsl.api

import org.postgresql.util.PSQLException

class ExportController {

    def flatViewService
    def configService

    def index() {
        def exports = []
        if (flatViewService.nameViewExists()) {
            exports.add([label: "${configService.nameTreeName} - Australian ${configService.getMenuLabel()} Names as CSV", url: 'namesCsv'])
        }
        if (flatViewService.taxonViewExists()) {
            exports.add([label: "${configService.classificationTreeName} - Australian ${configService.getMenuLabel()} Taxa as CSV", url: 'taxonCsv'])
        }
        if (flatViewService.commonViewExists()) {
            exports.add([label: "Australian ${configService.getMenuLabel()} Common Names as CSV", url: 'commonCsv'])
        }
        [ exports: exports ]
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
