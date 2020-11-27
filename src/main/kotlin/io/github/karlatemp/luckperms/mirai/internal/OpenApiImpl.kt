/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/OpenApiImpl.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.internal

import io.github.karlatemp.luckperms.mirai.*
import io.github.karlatemp.luckperms.mirai.openapi.internal.BackendImpl
import io.github.karlatemp.luckperms.mirai.util.ChatColor
import io.github.karlatemp.luckperms.mirai.util.colorTranslator
import net.luckperms.api.util.Tristate
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.permission.Permittee
import java.util.*

internal object OpenApiImpl : BackendImpl {
    override fun Permittee.hasPerm(perm: String): Boolean {
        return MiraiSenderFactory.getPermissionValue0(this, perm) == Tristate.TRUE
    }

    override suspend fun sendBcMsg(sender: CommandSender, msg: String) {
        when (sender) {
            is ConsoleCommandSender -> {
                sender.sendMessage(colorTranslator.translate(msg))
            }
            else -> {
                sender.sendMessage(ChatColor.stripColor(msg))
            }
        }
    }

    override val lpmver: String get() = luckPermsMiraiVersion
    override val lpver: String get() = luckPermVersion
    override val lpmgver: String get() = gitVersionLuckPermsMirai
    override val lpgver: String get() = gitVersionLuckPerms
    override val buildtime: Long get() = buildTime.time
    override fun isConsoleUUID(target: UUID): Boolean = target == UUID_CONSOLE
    override fun isUsrUid(target: UUID): Boolean = target.mostSignificantBits == MAGIC_UUID_HIGH_BITS
    override fun getId(source: UUID): Long = if (source.mostSignificantBits == MAGIC_UUID_HIGH_BITS) {
        source.leastSignificantBits
    } else 0L
}
