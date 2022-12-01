import grails.util.BuildSettings
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

import java.nio.charset.Charset

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')

        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm:ss}){magenta} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr([%4.5t]){green} ' + // Thread
                        '%clr(%-15.20logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%msg %ex{1}%nopex%n' // Message
    }
}

appender("dailyFileAppender", RollingFileAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')

        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm:ss}){magenta} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr([%4.5t]){green} ' + // Thread
                        '%clr(%-15.20logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%msg %ex{1}%nopex%n' // Message
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "${(System.getProperty('catalina.base') ?: (System.getProperty('user.home') ?: 'target'))}/logs/services-%d{yyyy-MM-dd}.zip"
    }
}

println "ENV: ${Environment.getCurrent()} DEV: ${Environment.isDevelopmentMode()}"
if (Environment.isDevelopmentMode()) {
    root(WARN, ['STDOUT'])
    logger("au.org.biodiversity", DEBUG, ['STDOUT'], false)
    logger("services3", DEBUG, ['STDOUT'], false)
} else if (Environment.getCurrent() == Environment.PRODUCTION) {
    root(ERROR, ['STDOUT'])
    logger("au.org.biodiversity", DEBUG, ['STDOUT'], false)
    logger("services3", DEBUG, ['STDOUT'], false)
} else {
    root(ERROR, ['STDOUT'])
    logger("au.org.biodiversity", DEBUG, ['STDOUT'], false)
    logger("services3", DEBUG, ['STDOUT'], false)
}
