package au.org.biodiversity.nsl

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

import javax.sql.DataSource

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@Rollback
@Integration
class TreeReportServiceSpec extends Specification {

    DataSource dataSource
    TreeReportService treeReportService
    TreeService treeService

    def setup() {
    }

    def cleanup() {
    }

    void "Test getting a diff v1=51357046&v2=51357890"() {
        when:
        TreeChangeSet changeSet = treeReportService.diffReport(TreeVersion.get(51357046), TreeVersion.get(51357890))
        println changeSet.modified

        then:
        changeSet.added.empty
        changeSet.removed.empty
        changeSet.changed
        !changeSet.modified.empty
        changeSet.modified.size() == 16
    }

    void "Test getting a diff v1=51352117&v2=51357890"() {
        when:
        TreeChangeSet changeSet = treeReportService.diffReport(TreeVersion.get(51352117), TreeVersion.get(51357890))

        then:
        changeSet.changed
        !changeSet.added.empty
        !changeSet.removed.empty
        !changeSet.modified.empty
        changeSet.added.size() == 1
        changeSet.removed.size() == 2
        changeSet.modified.size() == 220
        changeSet.all.size() == 223
        println ''
        TreeVersionElement lastFamily = null
        def lastParentId = null
        def lastId = null
        for (TreeVersionElement tve in changeSet.all) {
            if (!(lastFamily && tve.treePath.startsWith(lastFamily.treePath))) {
                lastFamily = treeService.getFamily(tve)
                println "\n$lastFamily.treeElement.name.fullName"
            } else if (tve.parentId != lastParentId && tve.parentId != lastId) {
                List<String> path = (tve.parent.namePath - lastFamily.namePath).split('/')
                path.remove('')
                if(path.size() > 1) {
                    println "... ${path.join('/')}"
                }
            }
            lastParentId = tve.parentId
            lastId = tve.elementLink
            print '|  '.multiply(tve.depth - lastFamily.depth)
            println "${tve.treeElement.simpleName}"
        }
    }
}
