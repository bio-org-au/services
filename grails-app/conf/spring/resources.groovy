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

import au.org.biodiversity.nsl.ApiSessionStorageEvaluator
import org.apache.shiro.mgt.DefaultSubjectDAO

// Place your Spring DSL code here
beans = {
    shiroSessionStorageEvaluator(ApiSessionStorageEvaluator)

    shiroSubjectDAO(DefaultSubjectDAO) { bean ->
        sessionStorageEvaluator = ref("shiroSessionStorageEvaluator")
    }
}
