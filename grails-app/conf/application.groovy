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

dataSource {
    username = "nsl"
    password = "nsl"
    url = "jdbc:postgresql://mylocalhost:5432/somedb"

    pooled = true
    jmxExport = true
    driverClassName = "org.postgresql.Driver"
    dialect = "au.org.biodiversity.nsl.ExtendedPostgreSQLDialect"
    formatSql = false
    logSql = false
    properties {
        // See http://grails.org/doc/latest/guide/conf.html#dataSource for documentation
        jmxEnabled = true
        initialSize = 5
        maxActive = 50
        minIdle = 5
        maxIdle = 25
        maxWait = 10000
        maxAge = 10 * 60000
        timeBetweenEvictionRunsMillis = 5000
        minEvictableIdleTimeMillis = 60000
        validationQuery = "SELECT 1"
        validationQueryTimeout = 3
        validationInterval = 15000
        testOnBorrow = true
        testWhileIdle = true
        testOnReturn = false
        jdbcInterceptors = "ConnectionState"
        defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
    }
}
