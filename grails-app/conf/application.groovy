/*
    Copyright 2015 Australian National Botanic Gardens

    This file is part of NSL-domain-plugin.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy
    of the License at http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

hibernate {
    cache {
        queries = false
        use_second_level_cache = false
        use_query_cache = false
    }
}

//grails.cache.config = {
//    provider {
//        name "ehcache-${appName}-${appVersion}"
//    }
//
//    defaults {
//        maxElementsInMemory 10000
//        eternal false
//        overflowToDisk false
//        maxElementsOnDisk 0
//        timeToLiveSeconds 120
//        memoryStoreEvictionPolicy 'LRU'
//    }
//
//    defaultCache {
//        maxElementsInMemory 10000
//        eternal false
//        timeToIdleSeconds 120
//        timeToLiveSeconds 120
//        overflowToDisk true
//        maxElementsOnDisk 10000000
//        diskPersistent false
//        diskExpiryThreadIntervalSeconds 120
//        memoryStoreEvictionPolicy 'LRU'
//    }
//
//    cache {
//        name "linkcache"
//        maxElementsInMemory 10000
//        eternal false
//        timeToIdleSeconds 600
//        timeToLiveSeconds 3600
//        overflowToDisk true
//        maxElementsOnDisk 10000000
//        diskPersistent false
//        diskExpiryThreadIntervalSeconds 600
//        memoryStoreEvictionPolicy 'LRU'
//    }
//
//    cache {
//        name "identitycache"
//        maxElementsInMemory 10000
//        eternal false
//        timeToIdleSeconds 600
//        timeToLiveSeconds 3600
//        overflowToDisk true
//        maxElementsOnDisk 10000000
//        diskPersistent false
//        diskExpiryThreadIntervalSeconds 600
//        memoryStoreEvictionPolicy 'LRU'
//    }
//
//    cache {
//        name "linkscache"
//        maxElementsInMemory 10000
//        eternal false
//        timeToIdleSeconds 600
//        timeToLiveSeconds 3600
//        overflowToDisk true
//        maxElementsOnDisk 10000000
//        diskPersistent false
//        diskExpiryThreadIntervalSeconds 600
//        memoryStoreEvictionPolicy 'LRU'
//    }
//}

shard {
    system.message.file = "${userHome}/.nsl/broadcast.txt"
    temp.file.directory = "/tmp"
}

services {
    mapper.apikey = 'not set'
    link {
        mapperURL = 'http://localhost:7070/nsl-mapper'
        internalMapperURL = 'http://localhost:7070/nsl-mapper'
        editor = 'https://biodiversity.org.au/test-nsl-editor'
    }
}

updates.dir = "${userHome}/.nsl/updates"