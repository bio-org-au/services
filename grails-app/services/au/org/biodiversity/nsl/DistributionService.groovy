package au.org.biodiversity.nsl

import grails.gorm.transactions.Transactional

@Transactional
class DistributionService {

    /**
     * Constructs a distribution string from the DistEntries associated with a TreeElement
     * @param element
     */
    String constructDistributionString(TreeElement element) {
        element.distributionEntries.sort { it.sortOrder }.collect { it.display }.join(', ')
    }

    /**
     * Deconstruct a distribtion string into a set of distribution entries <DistEntry> which can then be
     * associated with a TreeElement
     * @param dist
     * @return
     */
    List<DistEntry> deconstructDistributionString(String dist, Boolean ignoreErrors = false) {
        List<DistEntry> entries = []
        dist.split(',').collect { it.trim() }.each { String desc ->
            DistEntry entry = DistEntry.findByDisplay(desc)
            if (entry == null) {
                if (ignoreErrors) {
                    log.info "Ignoring bad entry ${desc}"
                } else {
                    String validEntries = DistEntry.list().sort {
                        (it as DistEntry).sortOrder
                    }.collect { e -> (e as DistEntry).display }.join(', ')
                    throw new IllegalArgumentException("Distribution entry ${desc} is not valid. Try one of $validEntries")
                }
            } else {
                entries.add(entry)
            }
        }
        return entries
    }

    TreeElement removeDistributionEntries(TreeElement element) {
        boolean modified = false
        if (element.distributionEntries) {
            List<DistEntry> entries = new LinkedList<DistEntry>(element.distributionEntries)
            entries.each { DistEntry entry ->
                element.removeFromDistributionEntries(entry)
                modified = true
            }
        }
        if (modified) {
            // From the documentation: "isDirty() does not currently check collection associations, but it does check all other persistent properties and associations."
            // I have no idea why the save(flush) is needed to force a save
            element.save(flush: true)
            element.markDirty()
        }
        return element
    }

    void reconstructDistribution(TreeElement element, String dist, Boolean ignoreErrors = false) {
        removeDistributionEntries(element)
        boolean modified = false
        if (dist) {
            deconstructDistributionString(dist, ignoreErrors).each { DistEntry entry ->
                element.addToDistributionEntries(entry)
                modified = true
            }
        }
        if (modified) {
            // From the documentation: "isDirty() does not currently check collection associations, but it does check all other persistent properties and associations."
            // I have no idea why the save(flush) is needed to force a save
            element.save(flush: true)
            element.markDirty()
        }
    }
}
