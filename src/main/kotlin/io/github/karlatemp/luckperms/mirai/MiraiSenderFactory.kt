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
import me.lucko.luckperms.common.locale.TranslationManager
import me.lucko.luckperms.common.sender.Sender
import me.lucko.luckperms.common.sender.SenderFactory
import me.lucko.luckperms.common.verbose.event.PermissionCheckEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.query.QueryOptions
import net.luckperms.api.util.Tristate
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.permission.Permittee
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import java.util.*

class MiraiSenderFactory : SenderFactory<LPMiraiPlugin, Permittee>(
    LPMiraiPlugin
) {
    @OptIn(ConsoleExperimentalApi::class)
    override fun getName(sender: Permittee?): String {
        if (sender is UserCommandSender) return sender.user.id.toString()
        return Sender.CONSOLE_NAME
    }

    override fun getUniqueId(sender: Permittee): UUID {
        return sender.permitteeId.uuid()
    }

    public override fun sendMessage(sender: Permittee, message: Component) {
        sendMessage(sender, LegacyComponentSerializer.legacySection().serialize(TranslationManager.render(message)))
    }

    public fun sendMessage(sender: Permittee, message: String) {
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
        sender: Permittee,
        node: String
    ): Tristate = getPermissionValue0(sender, node)

    companion object {
        internal fun getPermissionValue0(
            sender: Permittee,
            node: String
        ): Tristate {
            LPMiraiPlugin.permissionRegistry.offer(node)
            if (sender is ConsoleCommandSender) {
                LPMiraiPlugin.verboseHandler.offerPermissionCheckEvent(
                    PermissionCheckEvent.Origin.PLATFORM_PERMISSION_CHECK, "internal/console",
                    QueryOptions.nonContextual(), node, TristateResult.of(Tristate.TRUE)
                )
                return Tristate.TRUE
            }
            val id = sender.permitteeId
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

    override fun hasPermission(sender: Permittee, node: String): Boolean {
        return getPermissionValue0(sender, node) == Tristate.TRUE
    }

    @OptIn(ExperimentalCommandDescriptors::class,ConsoleExperimentalApi::class)
    public override fun performCommand(sender: Permittee, command: String) {
        runBlocking {
            val result = (sender as? CommandSender ?: error("Not a command sender."))
                .executeCommand(CommandManager.commandPrefix + command, true)
            when (result) {
                is CommandExecuteResult.Success -> {
                }
                is CommandExecuteResult.ExecutionFailed -> {
                    sender.sendMessage("Exception in executing command.")
                }
                is CommandExecuteResult.UnresolvedCall -> {
                    sender.sendMessage("Command not found.")
                }
                is CommandExecuteResult.PermissionDenied -> {
                    sender.sendMessage("Permission denied...")
                }
                is CommandExecuteResult.IllegalArgument -> {
                    sender.sendMessage(result.exception.message ?: "[Execute] Illegal Argument")
                }
            }
        }
    }
}
