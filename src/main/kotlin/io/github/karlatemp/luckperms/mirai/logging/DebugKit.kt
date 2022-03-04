/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/DebugKit.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.logging

import io.github.karlatemp.luckperms.mirai.LPMiraiBootstrap
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermitteeId
import net.mamoe.mirai.utils.debug
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal object DebugKit {
    @JvmField
    var isDebugEnabled = false

    @JvmField
    var trustedUsers: MutableList<Long>? = null

    @OptIn(ExperimentalContracts::class)
    inline fun log(msg: () -> String) {
        contract { callsInPlace(msg, InvocationKind.AT_MOST_ONCE) }
        if (isDebugEnabled) {
            LPMiraiBootstrap.logger.debug { "[DEBUG] ${msg()}" }
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun ifDebug(act: () -> Unit) {
        contract { callsInPlace(act, InvocationKind.AT_MOST_ONCE) }
        if (isDebugEnabled) {
            act()
        }
    }

    fun isTrusted(sender: PermitteeId): Boolean {
        return isTrusted(
            when (sender) {
                is AbstractPermitteeId.ExactUser -> sender.id
                is AbstractPermitteeId.ExactFriend -> sender.id
                is AbstractPermitteeId.ExactStranger -> sender.id
                is AbstractPermitteeId.ExactMember -> sender.memberId
                is AbstractPermitteeId.ExactGroupTemp -> sender.memberId
                else -> return false
            }
        )
    }

    fun isTrusted(id: Long): Boolean {
        return (trustedUsers ?: return false).contains(id)
    }
}
