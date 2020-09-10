/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/MiraiSenderFactory.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai

import io.github.karlatemp.luckperms.mirai.internal.LPPermissionService.uuid
import io.github.karlatemp.luckperms.mirai.util.ChatColor
import io.github.karlatemp.luckperms.mirai.util.colorTranslator
import kotlinx.coroutines.runBlocking
import me.lucko.luckperms.common.calculator.result.TristateResult
import me.lucko.luckperms.common.sender.Sender
import me.lucko.luckperms.common.sender.SenderFactory
import me.lucko.luckperms.common.util.TextUtils
import me.lucko.luckperms.common.verbose.event.PermissionCheckEvent
import net.kyori.text.Component
import net.luckperms.api.query.QueryOptions
import net.luckperms.api.util.Tristate
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.console.permission.ExperimentalPermission
import net.mamoe.mirai.console.permission.Permissible
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import java.util.*

@OptIn(ExperimentalPermission::class)
class MiraiSenderFactory : SenderFactory<LPMiraiPlugin, Permissible>(
    LPMiraiPlugin
) {
    @OptIn(ConsoleExperimentalAPI::class)
    override fun getName(sender: Permissible?): String {
        if (sender is UserCommandSender) return sender.user.id.toString()
        return Sender.CONSOLE_NAME
    }

    override fun getUniqueId(sender: Permissible): UUID {
        return sender.identifier.uuid()
    }

    override fun sendMessage(sender: Permissible, message: Component) {
        sendMessage(sender, TextUtils.toLegacy(message))
    }

    override fun sendMessage(sender: Permissible, message: String) {
        runBlocking {
            when (sender) {
                is ConsoleCommandSender -> {
                    sender.sendMessage(colorTranslator.translate(message))
                }
                is CommandSender -> {
                    sender.sendMessage(ChatColor.stripColor(message))
                }
                else -> {
                }
            }
        }
    }

    override fun getPermissionValue(
        sender: Permissible,
        node: String
    ): Tristate = getPermissionValue0(sender, node)

    companion object {
        internal fun getPermissionValue0(
            sender: Permissible,
            node: String
        ): Tristate {
            if (sender is ConsoleCommandSender) {
                LPMiraiPlugin.verboseHandler.offerPermissionCheckEvent(
                    PermissionCheckEvent.Origin.PLATFORM_PERMISSION_CHECK, "internal/console",
                    QueryOptions.nonContextual(), node, TristateResult.of(Tristate.TRUE)
                )
                LPMiraiPlugin.permissionRegistry.offer(node)
                return Tristate.TRUE
            }
            val id = sender.identifier
            val data = id.uuid()
            val usr = LPMiraiPlugin.userManager.getOrMake(data)

            val options = LPMiraiPlugin.contextManager.getQueryOptions(id)

            return usr.cachedData.getPermissionData(options)
                .checkPermission(
                    node,
                    PermissionCheckEvent.Origin.PLATFORM_LOOKUP_CHECK
                )
                .result()
        }
    }

    override fun hasPermission(sender: Permissible, node: String): Boolean {
        return getPermissionValue0(sender, node) == Tristate.TRUE
    }

    override fun performCommand(sender: Permissible, command: String) {
        runBlocking {
            val result = (sender as? CommandSender ?: error("Not a command sender."))
                .executeCommand(CommandManager.commandPrefix + command, true)
            when (result) {
                is CommandExecuteResult.Success -> {
                }
                is CommandExecuteResult.ExecutionFailed -> {
                    sender.sendMessage("Exception in executing command.")
                }
                is CommandExecuteResult.CommandNotFound -> {
                    sender.sendMessage("Command not found.")
                }
                is CommandExecuteResult.PermissionDenied -> {
                    sender.sendMessage("Permission denied...")
                }
            }
        }
    }
}