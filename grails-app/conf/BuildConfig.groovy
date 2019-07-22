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

grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.7
grails.project.source.level = 1.7
grails.project.war.file = "target/${appName}##${appVersion}.war"

//reloading
//disable.auto.recompile = false
//grails.gsp.enable.reload = true
//grails.reload.enabled = true

grails.project.fork = [
    // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
    //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

    // configure settings for the test-app JVM, uses the daemon by default
    test: false, //[maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
    // configure settings for the run-app JVM
    run: false, //[maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the run-war JVM
    war: false, //[maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the Console UI JVM
    console: false //[maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        mavenRepo("http://appsdev1-ibis.it.csiro.au:8085/repository/anbg-snapshot/")
        mavenRepo("http://appsdev1-ibis.it.csiro.au:8085/repository/anbg-release/")
        mavenRepo("http://appsdev1-ibis.it.csiro.au:8085/repository/maven-central/")
        mavenRepo("http://appsdev1-ibis.it.csiro.au:8085/repository/grails-plugins/")
        grailsHome()
        mavenLocal()
        grailsCentral()
        mavenCentral()
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes e.g.
        // runtime 'mysql:mysql-connector-java:5.1.29'
        runtime 'org.postgresql:postgresql:9.3-1101-jdbc41'
        runtime 'joda-time:joda-time:2.9.9'
        test "org.grails:grails-datastore-test-support:1.0-grails-2.4"
        compile 'net.htmlparser.jericho:jericho-html:3.2', {
            excludes 'log4j', 'commons-logging-api','slf4j-api'
        }
        compile "io.jsonwebtoken:jjwt:0.7.0"
        test "org.gebish:geb-spock:0.12.2"
        test 'cglib:cglib-nodep:2.2.2'       // For mocking classes
    }

    plugins {
        // plugins for the build system only
        build ":tomcat:7.0.55"

        // plugins for the compile step
        compile ":scaffolding:2.1.2"
        compile ':cache:1.1.8'
        compile "org.grails.plugins:cache-ehcache:1.0.5"
        compile ":asset-pipeline:2.1.0"
        compile "au.org.biodiversity.grails.plugins:nsl-domain-plugin:1.16"
        compile ':rest-client-builder:2.0.3'
        compile ":simple-suggestions:0.3"
		compile ":twitter-bootstrap:3.3.0"
        //noinspection GroovyAssignabilityCheck
        compile ":shiro:1.2.1", {
            excludes([name: 'quartz', group: 'org.opensymphony.quartz'])
        }
        compile ':quartz:1.0.2'
        compile ":executor:0.3"
        compile ":mail:1.0.7"
        compile ":yammer-metrics:3.0.1-2"
        compile "org.grails.plugins:slack-logger:1.0.1"
        compile "org.grails.plugins:jdbc-pool:7.0.47"
//        compile ":grails-melody:1.59.0"

        // plugins needed at runtime but not for compilation
        runtime ":hibernate4:4.3.5.5" // or ":hibernate:3.6.10.17"
        runtime ":database-migration:1.4.0"
        runtime ":jquery:1.11.1"
        runtime ":jquery-ui:1.10.4"
        runtime ":cors:1.1.6"
        // Uncomment these to enable additional asset-pipeline capabilities
        //compile ":sass-asset-pipeline:1.9.0"
        //compile ":less-asset-pipeline:1.10.0"
        //compile ":coffee-asset-pipeline:1.8.0"
        //compile ":handlebars-asset-pipeline:1.3.0.3"

        test "org.grails.plugins:geb:0.12.2"
    }
}
