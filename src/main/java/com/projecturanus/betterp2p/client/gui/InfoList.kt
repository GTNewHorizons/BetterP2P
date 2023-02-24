package com.projecturanus.betterp2p.client.gui

import com.projecturanus.betterp2p.network.NONE
import com.projecturanus.betterp2p.network.hashP2P
import kotlin.math.absoluteValue
import kotlin.reflect.KProperty0

/**
 * InfoList
 * Dedicated list to hold "InfoWrappers". Internally, stores
 * them in a HashMap. This is an opaque type, and access to
 * it is restricted. External access is instead directed to
 * a sorted view and a filtered view of the internal map.
 */
class InfoList (initList: Collection<InfoWrapper>,
                private val search: KProperty0<String>) {

    /**
     * The master map, acts as the source of truth for all items
     * in this list.
     */
    private val masterMap: HashMap<Long, InfoWrapper> = hashMapOf()

    /**
     * Sorted view of the master map. This is resorted whenever
     * the map is updated.
     */
    val sorted: MutableList<InfoWrapper> = mutableListOf()

    /**
     * Filtered view of the sorted view (not the master map)
     */
    var filtered: List<InfoWrapper> = listOf()

    private val filter: InfoFilter = InfoFilter()

    /**
     * Binding to the search string in the text box
     */
    private val searchStr: String
        get() = search.get()

    val selectedInfo: InfoWrapper?
        get() = masterMap[selectedEntry]

    var selectedEntry: Long = NONE

    val size: Int
        get() = masterMap.size

    init {
        initList.forEach { masterMap[it.code] = it }
    }

    fun resort() {
        sorted.sortBy {
            if (it.code == selectedEntry) {
                -2 // Put the selected p2p in the front
                // Non-Zero frequencies
            } else if (it.frequency != 0L && it.frequency == selectedInfo?.frequency && !it.output) {
                -3 // Put input in the beginning
            } else if (it.frequency != 0.toLong() && it.frequency == selectedInfo?.frequency) {
                -1 // Put same frequency in the front
            } else {
                // Frequencies from lowest to highest
                it.frequency + Short.MAX_VALUE
            }
        }
    }

    /**
     * Updates the filtered list.
     */
    fun refilter() {
        filter.updateFilter(searchStr.lowercase())
        filtered = sorted.filter {
            if (it.code == selectedEntry) {
                return@filter true
            }
            for ((f, strs) in filter.activeFilters) {
                if(!f.filter(it, strs?.toList())) {
                    return@filter false
                }
            }
            true
        }.sortedBy {
            when {
                it.code == selectedEntry -> Long.MIN_VALUE
                it.frequency != 0L && it.frequency == selectedInfo?.frequency && !it.output -> Long.MIN_VALUE + 1
                it.frequency != 0.toLong() && it.frequency == selectedInfo?.frequency -> Long.MIN_VALUE + 2 // Put same frequency in the front
                filter.activeFilters.containsKey(Filter.NAME) -> {
                    var hits = 0L
                    var name = it.name
                    for (f in filter.activeFilters[Filter.NAME]!!) {
                        if (name.contains(f, true)) {
                            hits += 1
                            name = name.replaceFirst(f, "", true)
                        }
                    }
                    -(hits * hits) + name.length
                }
                else -> it.frequency + Short.MAX_VALUE
            }
        }
//        filtered = sorted.filter {
//            it.frequency.toHexString().contains(searchStr.uppercase()) ||
//                it.frequency.toHexString().format4().contains(searchStr.uppercase()) ||
//                it.name.lowercase().contains(searchStr.lowercase()) ||
//                it.code == selectedEntry
//        }
    }

    /**
     * Updates the sorted list and applies the filter again.
     */
    fun refresh() {
        sorted.addAll(masterMap.values)
        resort()
        refilter()
    }

    /**
     * Completely refresh the master list.
     */
    fun rebuild(updateList: Collection<InfoWrapper>) {
        masterMap.clear()
        updateList.forEach { masterMap[it.code] = it }
        sorted.clear()
        sorted.addAll(masterMap.values)
        resort()
        // TODO: Extend the filtering mechanism.
        refilter()
    }

    /**
     * Update the master list, and send the changes downstream to sorted/filtered
     */
    fun update(updateList: Collection<InfoWrapper>) {
        updateList.forEach { masterMap[it.code] = it }
        sorted.clear()
        sorted.addAll(masterMap.values)
        resort()
        // TODO: Extend the filtering mechanism.
        refilter()
    }

    fun select(hashCode: Long) {
        if (masterMap.containsKey(hashCode)) {
            selectedEntry = hashCode
        } else {
            selectedEntry = NONE
        }
    }
}
