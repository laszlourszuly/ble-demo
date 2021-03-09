package com.echsylon.demo.extension

import kotlin.math.max
import kotlin.math.min

inline fun <T> List<T>.indexOfFirst(startIndex: Int, predicate: (T) -> Boolean): Int {
    var index = max(0, min(lastIndex, startIndex))
    for (item in this) {
        if (predicate(item))
            return index
        index++
    }
    return -1
}
