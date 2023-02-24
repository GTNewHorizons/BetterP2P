package com.projecturanus.betterp2p.client.gui

/**
 * Extend the default filtering. Holds information about the search filter.
 *
 * There are 3 modes of filtering:
 * - By input/output: Using `@in/@out` filters by input/output respectively.
 * - By bound/unbound: Use `@b` or `@u` filters by input/output respectively.
 * - By name: Use the name
 * If someone comes and says "sort by freq pls" then we can add it at that time
 */
class InfoFilter {

    /**
     * Active filters to use when filtering entries.
     */
    val activeFilters: MutableMap<Filter, MutableList<String>?> = mutableMapOf()

    /**
     * Parse the query string for filters and update the active
     * filter list.
     */
    fun updateFilter(query: String) {
        val tokens = query.split("\\s+".toRegex()) // split by whitespace
        activeFilters.clear()
        tokens.forEach {
            when {
                it.matches(Filter.INPUT.pattern) -> {
                    activeFilters.putIfAbsent(Filter.INPUT, null)
                }
                it.matches(Filter.OUTPUT.pattern) -> {
                    activeFilters.putIfAbsent(Filter.OUTPUT, null)
                }
                it.matches(Filter.BOUND.pattern) -> {
                    activeFilters.putIfAbsent(Filter.BOUND, null)
                }
                it.matches(Filter.UNBOUND.pattern) -> {
                    activeFilters.putIfAbsent(Filter.UNBOUND, null)
                }
                else -> {
                    activeFilters.putIfAbsent(Filter.NAME, mutableListOf())
                    activeFilters[Filter.NAME]!!.add(it)
                }
            }
        }
    }
}

/**
 * The different filter types. Probably will let these be adjustable in the
 * config.
 */
enum class Filter(val pattern: Regex, val filter: (InfoWrapper, List<String>?) -> Boolean) {
    INPUT("\\A@in\\z".toRegex(), { it, _ -> !it.output }),
    OUTPUT("\\A@out\\z".toRegex(), { it, _ -> it.output }),
    BOUND("\\A@b\\z".toRegex(), { it, _ -> it.frequency != 0L }),
    UNBOUND("\\A@u\\z".toRegex(), { it, _ -> it.frequency == 0L || it.error }),
    NAME("\"?.+\"?".toRegex(), filter@{ it, strs ->
        val name = it.name.lowercase()
        for (f in strs!!) {
            // Ppl better not troll and use double quotes in their P2P tunnel names
            val query = f.removeSurrounding("\"")
            if (name.contains(query)) {
                return@filter true
            }
        }
        false
    });

}

//fun parseQuery(search: String): InfoFilter {
//
//}
