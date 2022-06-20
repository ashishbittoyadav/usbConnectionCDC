package com.felhr.utils

import com.annimon.stream.Collectors
import com.annimon.stream.Stream
import com.annimon.stream.function.Predicate

object Utils {
    @JvmStatic
    fun <T> removeIf(c: Collection<T>, predicate: Predicate<in T>?): List<T> {
        return Stream.of(c.iterator())
            .filterNot(predicate)
            .collect(Collectors.toList())
    }
}