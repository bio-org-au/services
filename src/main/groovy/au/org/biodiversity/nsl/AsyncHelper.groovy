package au.org.biodiversity.nsl

import grails.async.Promise

import static grails.async.Promises.task

/**
 * User: pmcneil
 * Date: 9/8/19
 *
 */
trait AsyncHelper {
    @SuppressWarnings("GrMethodMayBeStatic")
    void doAsync(String description, Closure work) {
        long start = System.currentTimeMillis()
        log.info "Background Job: $description started."
        Promise p = task {
            Name.withNewTransaction { tx ->
                work()
            }
        }
        p.onError { Throwable err ->
            log.error "Error $description $err.message"
            err.printStackTrace()
        }
        p.onComplete { result ->
            long time = System.currentTimeMillis() - start
                    log.info "Background Job: $description complete. (Total time $time ms)"
        }
    }

}