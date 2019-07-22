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

import grails.converters.XML
import org.grails.web.converters.exceptions.ConverterException
import org.grails.web.converters.marshaller.NameAwareMarshaller
import org.grails.web.converters.marshaller.ObjectMarshaller

/**
 * User: pmcneil
 * Date: 23/02/15
 *
 */
class XmlMapMarshaller implements ObjectMarshaller<XML>, NameAwareMarshaller {

    @Override
    String getElementName(Object o) {
        return 'data'
    }

    @Override
    boolean supports(Object object) {
        return object instanceof Map
    }

    @Override
    void marshalObject(Object object, XML converter) throws ConverterException {
        Map map = (Map) object

        for(entry in map) {
            converter.startNode(entry.key as String)
            if(entry.value instanceof String) {
                converter.chars(entry.value as String)
            } else if(entry.value instanceof Boolean) {
                converter.attribute('is', entry.value as String)
            } else {
                converter.convertAnother(entry.value)
            }
            converter.end()
        }
    }
}
