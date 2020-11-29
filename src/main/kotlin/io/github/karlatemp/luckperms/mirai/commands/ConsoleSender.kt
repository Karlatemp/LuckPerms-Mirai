/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/ConsoleSender.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.commands

import io.github.karlatemp.luckperms.mirai.LPMiraiPlugin
import io.github.karlatemp.luckperms.mirai.MiraiSenderFactory
import io.github.karlatemp.luckperms.mirai.UUID_CONSOLE
import me.lucko.luckperms.common.plugin.LuckPermsPlugin
import me.lucko.luckperms.common.sender.Sender
import net.kyori.adventure.text.Component
import net.luckperms.api.util.Tristate
import net.mamoe.mirai.console.command.ConsoleCommandSender
import java.util.*

object ConsoleSender : Sender {
    private inline val console get() = ConsoleCommandSender
    override fun getPlugin(): LuckPermsPlugin = LPMiraiPlugin
    override fun getName(): String = Sender.CONSOLE_NAME
    override fun getUniqueId(): UUID = UUID_CONSOLE
    override fun isConsole(): Boolean = true
    override fun sendMessage(message: Component) {
        LPMiraiPlugin.senderFactory0.sendMessage(console, message)
    }
    override fun getPermissionValue(permission: String): Tristate =
        MiraiSenderFactory.getPermissionValue0(console, permission)
    override fun hasPermission(permission: String): Boolean =
        MiraiSenderFactory.getPermissionValue0(console, permission) == Tristate.TRUE
    override fun performCommand(commandLine: String) {
        LPMiraiPlugin.senderFactory0.performCommand(console, commandLine)
    }
}