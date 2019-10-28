package au.org.biodiversity.nsl.api

import au.org.biodiversity.nsl.*
import org.springframework.http.HttpStatus

import static org.springframework.http.HttpStatus.OK

/**
 * User: pmcneil
 * Date: 18/01/17
 *
 */
class ResultObject {
    @Delegate
    Map data
    JsonRendererService jsonRendererService1

    ResultObject(Map data) {
        this.data = data
        data.status = data.status ?: OK
    }

    ResultObject(Map data, JsonRendererService jsonRendererService) {
        this.data = data
        this.jsonRendererService1 = jsonRendererService
        data.status = data.status ?: OK
    }

    def error(String error) {
        if (data.error) {
            data.error += "\n $error"
        } else {
            data.error = error
        }
    }

    def fail(String error, HttpStatus status) {
        data.status = status
        this.error(error)
    }

    def briefObject(Object target, String key = null) {
        if(!key) {
            key = target?.class?.simpleName?.toLowerCase()
        }
        if(!key) {
            return
        }
        if (jsonRendererService1) {
            switch (target.class.simpleName) {
                case 'Instance':
                    data[key] = jsonRendererService1.getBriefInstance(target as Instance)
                    break
                case 'Name':
                    data[key] = jsonRendererService1.getBriefName(target as Name)
                    break
                case 'Reference':
                    data[key] = jsonRendererService1.getBriefReference(target as Reference)
                    break
                case 'Author':
                    data[key] = jsonRendererService1.getBriefAuthor(target as Author)
                    break
                case 'InstanceNote':
                    data[key] = jsonRendererService1.marshallInstanceNote(target as InstanceNote)
                    break
                case 'String':
                    break
                default:
                    data[key] = jsonRendererService1.brief(target)
            }
        } else {
            throw new Exception("You need to set jsonRendererService on the result object to call addBriefObject")
        }
    }
}
