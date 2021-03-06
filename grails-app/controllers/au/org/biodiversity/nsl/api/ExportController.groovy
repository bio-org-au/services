package au.org.biodiversity.nsl.api

import org.grails.plugins.metrics.groovy.Timed


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

    @Timed()
    namesCsv() {
        File exportFile = null
        try {
            exportFile = flatViewService.exportNamesToCSV()
            render(file: exportFile, fileName: exportFile.name, contentType: 'text/plain')
        } finally {
            exportFile?.delete()

        }
    }

    @Timed()
    taxonCsv() {
        File exportFile = null
        try {
            exportFile = flatViewService.exportTaxonToCSV()
            render(file: exportFile, fileName: exportFile.name, contentType: 'text/plain')
        } finally {
            exportFile?.delete()
        }
    }

    @Timed()
    commonCsv() {
        File exportFile = null
        try {
            exportFile = flatViewService.exportCommonToCSV()
            render(file: exportFile, fileName: exportFile.name, contentType: 'text/plain')
        } finally {
            exportFile?.delete()
        }
    }

}
