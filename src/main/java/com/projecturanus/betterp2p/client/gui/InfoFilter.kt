package com.projecturanus.betterp2p.client.gui

import com.projecturanus.betterp2p.BetterP2P

/**
 * Extend the default filtering. Holds information about the search filter.
 *
 * There are 4 modes of filtering:
 * - By input/output: Using `@in/@out` filters by input/output respectively.
 * - By bound/unbound: Using `@b` or `@u` filters by bound/unbound respectively.
 * - By type: Using `@types=<type1>;<type2>;...` filters by type.
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
        val tokens = SEARCH_REGEX.findAll(query)
        activeFilters.clear()
        tokens.forEach {
            val token = it.value
            // If we don't start with a tag, skip all these regexes
            if (token.startsWith("@")) {
                when {
                    it.value.matches(Filter.TYPE.pattern) -> {
                        val result = Filter.TYPE.pattern.find(it.value)!!
                        val l = mutableListOf<String>()
                        activeFilters.putIfAbsent(Filter.TYPE, l)
                        val types = result.groupValues[1].split(";")
                        types.forEach(l::add)
                    }
                    it.value.isBlank() -> {}
                    else -> {
                    }
                }
            } else {
                val l = mutableListOf<String>()
                activeFilters.putIfAbsent(Filter.NAME, l)
                when {
                    it.groups[1] != null -> l.add(it.groups[1]!!.value)
                    it.groups[2] != null -> l.add(it.groups[2]!!.value)
                    else -> activeFilters[Filter.NAME]!!.add(it.value)
                }
            }
        }
    }
}

/**
 * Holds different filter types for use in the search bar
 */
enum class Filter(val pattern: Regex, val filter: (InfoWrapper, List<String>?) -> Boolean) {
    TYPE("\\A@types*=(.+)\\z".toRegex(), filter@{ it, strs ->
        val tags = BetterP2P.proxy.getP2PFromIndex(it.type)!!.dispName.lowercase()
        for (f in strs!!) {
            if (tags.contains(f.lowercase())) {
                return@filter true
            }
        }
        false
    }),
    NAME("\"?.+\"?".toRegex(), filter@{ it, strs ->
        val name = it.name.lowercase()
        for (f in strs!!) {
            // Double quotes will likely break P2P tunneling
            val query = f.removeSurrounding("\"")
            if (name.contains(query)) {
                return@filter true
            }
        }
        false
    });
}

// Splitting a string with regex is hard.
// https://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double
val SEARCH_REGEX = "[^\\s\"']+|\"([^\"]*)\"|'([^']*)'".toRegex()

