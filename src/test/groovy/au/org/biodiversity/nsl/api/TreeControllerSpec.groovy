package au.org.biodiversity.nsl.api

import au.org.biodiversity.nsl.*
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.validation.ValidationException
import org.apache.shiro.authz.AuthorizationException
import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import spock.lang.Specification

import java.sql.Timestamp

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(TreeController)
@TestMixin(DomainClassUnitTestMixin)
@Mock([Tree, TreeVersion, TreeElement])
class TreeControllerSpec extends Specification {

    def treeService = Mock(TreeService)

    def setup() {
        controller.jsonRendererService = new JsonRendererService()
        controller.jsonRendererService.treeService = treeService
        controller.jsonRendererService.registerObjectMashallers()
        /*
        Note treeService authorizeTreeOperation throws an unauthorized exception if not authorized. The mock returns
        null by default for methods that aren't mocked out, so you won't get an AuthorizationException unless you mock
        authorizeTreeOperation to throw one. e.g.

          1 * treeService.authorizeTreeOperation(tree) >> {Tree tree1 ->
            throw new AuthorizationException('Black Hatz')
          }

         */
        controller.treeService = treeService
    }

    def cleanup() {
    }

    void "test creating a tree"() {
        given:
        Map req = [treeName       : 'aTree',
                   groupName      : 'aGroup',
                   referenceId    : null,
                   descriptionHtml: '<p>description</p>'
        ]

        when: 'I create a new tree'
        def data = jsonCall('PUT', req) { controller.createTree() }

        then: 'I get an OK response'
        1 * treeService.createNewTree(req.treeName, req.groupName, req.referenceId, req.descriptionHtml, null, null) >> {
            new Tree(name: req.treeName, groupName: req.groupName, referenceId: req.referenceId, descriptionHtml: req.descriptionHtml)
        }
        response.status == 200
        data.ok == true
        data.payload.name == req.treeName
        data.payload.groupName == req.groupName
    }

    void "test creating a tree sans group name"() {
        given:
        Map req = [treeName       : 'aTree',
                   groupName      : null,
                   referenceId    : null,
                   descriptionHtml: '<p>description</p>'
        ]

        when: 'I create a new tree without a groupName'
        def data = jsonCall('PUT', req) { controller.createTree() }

        then: 'It replaces the null group with username'
        1 * treeService.authorizeTreeBuilder() >> 'username'
        1 * treeService.createNewTree(req.treeName, 'username', req.referenceId, req.descriptionHtml, null, null) >> {
            new Tree(name: req.treeName, groupName: 'username', referenceId: req.referenceId, descriptionHtml: req.descriptionHtml)
        }
        response.status == 200
        data.ok == true
        data.payload.name == req.treeName
        data.payload.groupName == 'username'
    }

    void "test creating a tree sans tree name"() {
        given:
        Map req = [treeName       : null,
                   groupName      : 'aGroup',
                   referenceId    : null,
                   descriptionHtml: '<p>description</p>'
        ]

        when: 'I create a new tree without a treeName'
        def data = jsonCall('PUT', req) { controller.createTree() }

        then: 'I get a fail'
        response.status == 400
        data.ok == false
        data.error == 'treeName not supplied. You must supply treeName.'
    }

    void "test creating a tree sans description html"() {
        given:
        Map req = [treeName       : 'aTree',
                   groupName      : 'aGroup',
                   referenceId    : null,
                   descriptionHtml: null
        ]

        when: 'I create a new tree without a treeName'
        def data = jsonCall('PUT', req) { controller.createTree() }

        then: 'I get a fail'
        response.status == 400
        data.ok == false
        data.error == 'descriptionHtml not supplied. You must supply descriptionHtml.'
    }

    void "test creating a tree that exists"() {
        given:
        Map req = [treeName       : 'aTree',
                   groupName      : 'aGroup',
                   referenceId    : null,
                   descriptionHtml: 'desc'
        ]
        treeService.createNewTree(_, _, _, _, _, _) >> {
            throw new ObjectExistsException('Object exists')
        }

        when: 'I create a new tree with a name that exists'
        def data = jsonCall('PUT', req) { controller.createTree() }

        then: 'I get a CONFLICT fail response with object exists'
        response.status == 409
        data.ok == false
        data.error == 'Object exists'
    }

    void "test creating a tree that clashes with db validation"() {
        given:
        Map req = [treeName       : 'aTree',
                   groupName      : 'aGroup',
                   referenceId    : null,
                   descriptionHtml: 'desc'
        ]
        treeService.createNewTree(_, _, _, _, _, _) >> {
            Errors errors = mockErrors()
            throw new ValidationException('validation error', errors)
        }

        when: 'I create a new tree with a name that exists'
        def data = jsonCall('PUT', req) { controller.createTree() }

        then: 'I get an Fail response with object exists'
        response.status == 500
        data.ok == false
        data.error == 'validation error:\n'
    }

    void "test editing a tree"() {
        given:
        Tree tree = new Tree(name: 'aTree', groupName: 'aGroup', descriptionHtml: '<p>description</p>', hostName: 'localhost').save()
        Long treeId = tree.id
        Map req = [id             : treeId,
                   treeName       : 'A New Name',
                   referenceId    : 123456,
                   groupName      : 'aGroup',
                   descriptionHtml: '<p>description</p>',
                   linkToHomePage : 'http://something.com',
                   acceptedTree   : true
        ]

        expect:
        tree
        treeId
        tree.name == 'aTree'

        when: 'I change the name of a tree'
        def data = jsonCall('POST', req) { controller.editTree() }

        then: 'It works'

        1 * treeService.editTree(tree, req.treeName,
                req.groupName,
                req.referenceId,
                req.descriptionHtml,
                req.linkToHomePage,
                req.acceptedTree) >> { Tree tree2, String name, String group, Long refId, String desc, String link, Boolean acc ->
            tree2.name = name
            tree2.referenceId = refId
            tree2.groupName = group
            tree2.descriptionHtml = desc
            tree2.linkToHomePage = link
            tree2.acceptedTree = acc
            tree2.save()
            return tree2
        }
        response.status == 200
        data.ok
        data.payload
        data.payload.name == req.treeName
        data.payload.groupName == req.groupName
        data.payload.referenceId == req.referenceId
        data.payload.linkToHomePage == req.linkToHomePage
        data.payload.descriptionHtml == req.descriptionHtml
        data.payload.acceptedTree == req.acceptedTree

        when: 'Im not authorized'
        data = jsonCall('POST', req) { controller.editTree() }

        then: 'I get a Authorization exception'
        1 * treeService.authorizeTreeOperation(tree) >> { Tree tree1 ->
            throw new AuthorizationException('Black Hatz')
        }
        response.status == 403
        data.ok == false
        data.error == 'You are not authorised to null. Black Hatz'

    }

    void "test validation error editing a tree"() {
        given:
        Tree tree = new Tree(name: 'aTree', groupName: 'aGroup', descriptionHtml: '<p>description</p>', hostName: 'localhost').save()
        Long treeId = tree.id
        Map req = [id             : treeId,
                   treeName       : 'A New Name',
                   referenceId    : 123456,
                   groupName      : 'aGroup',
                   descriptionHtml: '<p>description</p>',
                   linkToHomePage : 'http://something.com',
                   acceptedTree   : true
        ]

        treeService.editTree(_, _, _, _, _, _, _) >> { Tree tree2, String name, String group, Long refId, String desc, String link, Boolean acc ->
            Errors errors = mockErrors()
            throw new ValidationException('validation error', errors)
        }

        when: 'I do something that gets a validation exception'
        def data = jsonCall('POST', req) { controller.editTree() }

        then: 'It gives a validation error'
        response.status == 500
        data.ok == false
        data.error == 'validation error:\n'
    }

    void "test non existent tree editing a tree"() {
        given:
        Map req = [id             : 23,
                   treeName       : 'A New Name',
                   referenceId    : 123456,
                   groupName      : 'aGroup',
                   descriptionHtml: '<p>description</p>',
                   linkToHomePage : 'http://something.com',
                   acceptedTree   : true
        ]
        treeService.editTree(_, _, _, _, _, _, _) >> { Tree tree2, String name, String group, Long refId, String desc, String link, Boolean acc ->
            fail('shouldn\'t call edit tree')
        }

        when: 'I edit a non existent tree'
        def data = jsonCall('POST', req) { controller.editTree() }

        then: 'It gives a not found error'
        response.status == 404
        data.ok == false
        data.error == 'No Tree with id: 23 found'
    }

    void "test creating a new version"() {
        given:
        Tree tree = new Tree(name: 'aTree', groupName: 'aGroup', descriptionHtml: '<p>description</p>', hostName: 'localhost').save()
        Map req = [treeId       : tree.id.toString(),
                   fromVersionId: '',
                   draftName    : 'my draft tree',
                   defaultDraft : false
        ]

        when: 'I create a new version for a tree with no version'
        def data = jsonCall('PUT', req) { controller.createVersion() }

        then: 'I should get an OK response'
        1 * treeService.authorizeTreeOperation(tree) >> { Tree tree1 ->
            "irma"
        }
        1 * treeService.createTreeVersion(_, _, _, _, _) >> { Tree tree1, TreeVersion version, String draftName, String userName, String logEntry ->
            TreeVersion v = new TreeVersion(tree: tree1, draftName: draftName, logEntry: logEntry, createdBy: userName, createdAt: new Timestamp(System.currentTimeMillis()))
            v.save()
            return v
        }
        response.status == 200
        data.ok == true
        data.payload
        data.payload.draftName == 'my draft tree'
    }

    void "test creating a new version no draft name"() {
        given:
        Tree tree = new Tree(name: 'aTree', groupName: 'aGroup', descriptionHtml: '<p>description</p>', hostName: 'localhost').save()
        Map req = [treeId       : tree.id,
                   fromVersionId: null,
                   draftName    : null,
                   defaultDraft : false
        ]

        when: 'I forget something like draftName'
        def data = jsonCall('PUT', req) { controller.createVersion() }

        then: 'I get a bad argument response'
        response.status == 400
        data.ok == false
        data.error == 'draftName not supplied. You must supply draftName.'
    }

    void "test creating a new version unauthorized"() {
        given:
        Tree tree = new Tree(name: 'aTree', groupName: 'aGroup', descriptionHtml: '<p>description</p>', hostName: 'localhost').save()
        Map req = [treeId       : tree.id,
                   fromVersionId: null,
                   draftName    : 'blargh',
                   defaultDraft : false
        ]

        when: 'Im not authorized'
        def data = jsonCall('PUT', req) { controller.createVersion() }

        then: 'I get a Authorization exception'
        1 * treeService.authorizeTreeOperation(_) >> { Tree tree1 ->
            throw new AuthorizationException('Black Hatz')
        }
        response.status == 403
        data.ok == false
        data.error == 'You are not authorised to null. Black Hatz'
    }

    //todo move this to treeVersionControllerSpec
//    void "test validating a version"() {
//        given:
//        Tree tree = new Tree(name: 'aTree', groupName: 'aGroup').save()
//        TreeVersion version = new TreeVersion(tree: tree, draftName: 'draft tree')
//        version.save()
//        request.method = 'GET'
//
//        expect:
//        tree
//        version
//
//        when: 'I validate this version'
//        def data = jsonCall { controller.validateTreeVersion(version.id) }
//
//        then: 'I should get an OK response'
//        1 * treeService.validateTreeVersion(_) >> { TreeVersion version1 ->
//            return [:]
//        }
//        response.status == 200
//        data.ok == true
//        data.payload instanceof Map
//        data.payload.keySet().empty
//
//        when: 'tree service hasnt implemented validation'
//        data = jsonCall { controller.validateTreeVersion(version.id) }
//
//        then: 'I get a not implemented response'
//        1 * treeService.validateTreeVersion(_) >> { TreeVersion version1 ->
//            throw new NotImplementedException('oops')
//        }
//        response.status == 501
//        data.ok == false
//        data.error == 'oops'
//    }

    private def jsonCall(String method, Map dataMap, Closure action) {
        response.reset()
        String jsonData = (dataMap as JSON).toString()
        println "JSON data sent: $jsonData"
        request.method = method
        request.json = jsonData
        response.format = 'json'
        action()
        println "response: ${response.text}"
        return response.json
    }

    private static Errors mockErrors() {
        new Errors() {
            @Override
            String getObjectName() {
                return null
            }

            @Override
            void setNestedPath(String nestedPath) {}

            @Override
            String getNestedPath() {}

            @Override
            void pushNestedPath(String subPath) {}

            @Override
            void popNestedPath() throws IllegalStateException {}

            @Override
            void reject(String errorCode) {}

            @Override
            void reject(String errorCode, String defaultMessage) {}

            @Override
            void reject(String errorCode, Object[] errorArgs, String defaultMessage) {}

            @Override
            void rejectValue(String field, String errorCode) {}

            @Override
            void rejectValue(String field, String errorCode, String defaultMessage) {}

            @Override
            void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {}

            @Override
            void addAllErrors(Errors errors) {}

            @Override
            boolean hasErrors() {
                return false
            }

            @Override
            int getErrorCount() {
                return 0
            }

            @Override
            List<ObjectError> getAllErrors() {
                return []
            }

            @Override
            boolean hasGlobalErrors() {
                return false
            }

            @Override
            int getGlobalErrorCount() {
                return 0
            }

            @Override
            List<ObjectError> getGlobalErrors() {
                return []
            }

            @Override
            ObjectError getGlobalError() {
                return null
            }

            @Override
            boolean hasFieldErrors() {
                return false
            }

            @Override
            int getFieldErrorCount() {
                return 0
            }

            @Override
            List<FieldError> getFieldErrors() {
                return []
            }

            @Override
            FieldError getFieldError() {
                return null
            }

            @Override
            boolean hasFieldErrors(String field) {
                return false
            }

            @Override
            int getFieldErrorCount(String field) {
                return 0
            }

            @Override
            List<FieldError> getFieldErrors(String field) {
                return []
            }

            @Override
            FieldError getFieldError(String field) {
                return null
            }

            @Override
            Object getFieldValue(String field) {
                return null
            }

            @Override
            Class<?> getFieldType(String field) {
                return null
            }
        }
    }
}
