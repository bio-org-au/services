package au.org.biodiversity.nsl

import grails.transaction.Transactional

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
    List<DistEntry> deconstructDistributionString(String dist) {
        List<DistEntry> entries = []
        dist.split(',').collect { it.trim() }.each { String desc ->
            DistEntry entry = DistEntry.findByDisplay(desc)
            if (entry == null) {
                String validEntries = DistEntry.list().sort{(it as DistEntry).sortOrder}.collect { e -> (e as DistEntry).display }.join(', ')
                throw new IllegalArgumentException("Distribution entry ${desc} is not valid. Try one of $validEntries")
            } else {
                entries.add(entry)
            }
        }
        return entries
    }

    TreeElement removeDistributionEntries(TreeElement element) {
        if(element.distributionEntries) {
            List<DistEntry> entries = new LinkedList<DistEntry>(element.distributionEntries)
            entries.each { DistEntry entry ->
                element.removeFromDistributionEntries(entry)
            }
        }
        return element
    }

    void reconstructDistribution(TreeElement element, String dist) {
        removeDistributionEntries(element)
        if (dist) {
            deconstructDistributionString(dist).each { DistEntry entry ->
                element.addToDistributionEntries(entry)
            }
        }
    }
}
