/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/LPconstants.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai

import me.lucko.luckperms.common.sender.Sender
import java.util.*
import java.util.concurrent.ConcurrentHashMap

const val MAGIC_UUID_HIGH_BITS: Long =
    0x14768AEEFFA88746L

val CACHED_USERS = ConcurrentHashMap<Long, Unit>()
val UUID_CONSOLE = Sender.CONSOLE_UUID!!
val UUID_ANY_MEMBER_SELECTOR = UUID(0, 1)
val UUID_ANY_GROUP_SELECTOR = UUID(0, 2)
val UUID_ANY_CONTEXT_SELECTOR = UUID(0, 3)

