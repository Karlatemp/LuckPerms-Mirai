/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/internalDsl.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.util

import io.github.karlatemp.luckperms.mirai.LPMiraiPlugin
import io.github.karlatemp.luckperms.mirai.commands.WrappedLPSender
import me.lucko.luckperms.common.sender.Sender
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun Sender.sendmsg(msg: String): Unit {
    LPMiraiPlugin.senderFactory0.sendMessage((this as WrappedLPSender).real, msg)
}

inline fun StringBuilder.newLine() {
    append('\n')
}

inline fun StringBuilder.newLine(msg: String) {
    append(msg).append('\n')
}

@OptIn(ExperimentalContracts::class)
inline fun StringBuilder.newLine(msg: StringBuilder.() -> Unit) {
    contract { callsInPlace(msg, InvocationKind.EXACTLY_ONCE) }
    msg()
    append('\n')
}

