package io.github.karlatemp.luckperms.mirai

import java.util.concurrent.ConcurrentHashMap

const val MAGIC_UUID_HIGH_BITS: Long =
    0x14768AEEFFA88746L

val CACHED_USERS = ConcurrentHashMap<Long, Unit>()

