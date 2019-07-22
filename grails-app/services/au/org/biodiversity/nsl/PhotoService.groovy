package au.org.biodiversity.nsl

import grails.plugins.rest.client.RestResponse
import groovy.transform.Synchronized

import java.util.concurrent.atomic.AtomicBoolean

class PhotoService {

    def restCallService
    def configService

    private List<String> photoNames = null
    private AtomicBoolean updating = new AtomicBoolean(false)

    boolean hasPhoto(String simpleName) {
        if (!updating.get()) {
            if (!photoNames) {
                log.debug "calling refresh"
                refresh()
            }
            return photoNames?.contains(simpleName)
        }
        return false
    }

    String searchUrl(String simpleName) {
        configService.getPhotoSearch(simpleName)
    }

    @Synchronized
    refresh() {
        getPhotoMatchList()
    }

    private getPhotoMatchList() {
        try {
            if (updating.compareAndSet(false, true)) {
                log.debug "updating set"
                List<String> photoNames = []
                String url = configService.getPhotoServiceUri()
                if (url) { //no photo service so no photos
                    log.debug(url)
                    RestResponse response = restCallService.nakedJsonGet(url)
                    if (response.status == 200) {
                        String csvText = response.text
                        if (csvText) {
                            csvText.eachLine { String line ->
                                String name = line.replaceAll(/^"([^"]*).*$/, '$1')
                                photoNames.add(name.trim())
                            }
                        } else {
                            log.error "No data from $url"
                        }
                    } else {
                        log.error "Error from $url ${response.status}"
                    }
                }
                log.debug "got ${photoNames.size()} photo names"
                this.photoNames = photoNames
            }
        } catch (e) {
            log.error "Error getting photos $e"
        } finally {
            updating.set(false)
            log.debug "updating unset"
        }
    }

}
