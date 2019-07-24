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

import org.hibernate.Hibernate
import org.hibernate.proxy.HibernateProxy

/**
 * User: pmcneil
 * Date: 31/03/15
 *
 */
class HibernateDomainUtils {

    static <T> T initializeAndUnproxy(T entity) {
        if (entity == null) {
            throw new NullPointerException("Entity passed for initialization is null")
        }

        Hibernate.initialize(entity)
//        if (entity instanceof HibernateProxy) {
//            entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer()
//                                                  .getImplementation()
//        }
        return entity
    }


}
