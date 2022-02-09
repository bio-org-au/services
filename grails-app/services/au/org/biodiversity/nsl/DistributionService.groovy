package au.org.biodiversity.nsl

import grails.gorm.transactions.Transactional

import java.sql.Timestamp

@Transactional
class DistributionService {

    /**
     * Constructs a distribution string from the DistEntries associated with a TreeElement
     * @param element
     */
    String constructDistributionString(TreeElement element) {
        element.distributionEntries.collect {it.distEntry }.sort { it.sortOrder }.collect { it.display }.join(', ')
    }

    /**
     * Deconstruct a distribtion string into a set of distribution entries <DistEntry> which can then be
     * associated with a TreeElement
     * @param dist
     * @return
     */
    List<DistEntry> deconstructDistributionString(String dist, Boolean ignoreErrors = false) {
        List<DistEntry> entries = []
        dist.split(',').collect { it.trim() }.findAll{it}.each { String desc ->
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

//    boolean removeDistributionEntries(TreeElement element) {
//        boolean rtn = false
//        if (element.distributionEntries) {
//            List<DistEntry> entries = new LinkedList<DistEntry>(element.distributionEntries)
//            entries.each { DistEntry entry ->
//                element.removeFromDistributionEntries(entry)
//                rtn = true
//                // From the documentation: "isDirty() does not currently check collection associations, but it does check all other persistent properties and associations."
//                // I have no idea why the save(flush) is needed to force a save
////                element.save(flush: true)
////                element.markDirty()
//            }
//        }
//        return rtn
//    }
//
//    void reconstructDistribution(TreeElement element, String dist, Boolean ignoreErrors = false) {
//        removeDistributionEntries(element)
//        if (dist) {
//            deconstructDistributionString(dist, ignoreErrors).each { DistEntry entry ->
//                element.addToDistributionEntries(entry)
//                // From the documentation: "isDirty() does not currently check collection associations, but it does check all other persistent properties and associations."
//                // I have no idea why the save(flush) is needed to force a save
//                element.save(flush: true)
//                element.markDirty()
//            }
//        }
//    }

    void reconstructDistribution(TreeElement element, String dist, String userName, Boolean ignoreErrors = false) {
        Set<DistEntry> oldEntries = element.distributionEntries.collect{it.distEntry} ?: new HashSet<>()
        List<DistEntry> newEntryList = deconstructDistributionString(dist, ignoreErrors)
        Set<DistEntry> newEntries = new HashSet<>(newEntryList)
        oldEntries.minus(newEntries).each { DistEntry entry ->
            TreeElementDistEntry ent = element.distributionEntries.find {it.distEntry == entry }
            element.removeFromDistributionEntries(ent)
            ent.delete()
        }
        newEntries.minus(oldEntries).each { DistEntry entry ->
            TreeElementDistEntry ent = new TreeElementDistEntry(
                    treeElement: element,
                    distEntry: entry,
                    updatedAt: new Timestamp(System.currentTimeMillis()),
                    updatedBy: userName
            )
            ent.save()
            element.addToDistributionEntries(ent)
        }
    }
}
