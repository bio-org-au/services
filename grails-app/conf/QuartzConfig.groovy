quartz {
    jdbcStore = false
    waitForJobsToCompleteOnShutdown = true
    exposeSchedulerInRepository = false

    props {
        scheduler.skipUpdateCheck = true
        threadPool.threadCount = 2
    }
}

environments {
    development {
        quartz {
            autoStartup = false
        }
    }
    test {
        quartz {
            autoStartup = false
        }
    }
    production {
        quartz {
            autoStartup = false
        }
    }
}
