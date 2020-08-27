package io.github.karlatemp.luckperms.mirai

import io.github.karlatemp.luckperms.mirai.util.ChatColor
import io.github.karlatemp.luckperms.mirai.util.colorTranslator
import kotlinx.coroutines.runBlocking
import me.lucko.luckperms.common.sender.Sender
import me.lucko.luckperms.common.sender.SenderFactory
import me.lucko.luckperms.common.util.TextUtils
import me.lucko.luckperms.common.verbose.event.PermissionCheckEvent
import net.kyori.text.Component
import net.luckperms.api.util.Tristate
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender
import java.util.*

class MiraiSenderFactory : SenderFactory<LPMiraiPlugin, CommandSender>(
    LPMiraiPlugin
) {
    override fun getName(sender: CommandSender?): String {
        if (sender is WrappedCommandSender)
            return sender.parent.user.id.toString()
        return Sender.CONSOLE_NAME
    }

    override fun getUniqueId(sender: CommandSender?): UUID {
        return if (sender is WrappedCommandSender)
            sender.uuid
        else Sender.CONSOLE_UUID
    }

    override fun sendMessage(sender: CommandSender, message: Component) {
        sendMessage(sender, TextUtils.toLegacy(message))
    }

    override fun sendMessage(sender: CommandSender, message: String) {
        runBlocking {
            if (sender is ConsoleCommandSender) {
                sender.sendMessage(colorTranslator.translate(message))
            } else {
                sender.sendMessage(ChatColor.stripColor(message))
            }
        }
    }

    override fun getPermissionValue(
        sender: CommandSender,
        node: String
    ): Tristate {
        if (sender !is WrappedCommandSender) {
            return Tristate.TRUE
        }
        val usr = plugin.userManager.getIfLoaded(sender.uuid) ?: return Tristate.UNDEFINED

        val options = LPMiraiPlugin.contextManager.getQueryOptions(sender)

        return usr.cachedData.getPermissionData(options)
            .checkPermission(
                node,
                PermissionCheckEvent.Origin.PLATFORM_LOOKUP_CHECK
            )
            .result()
    }

    override fun hasPermission(sender: CommandSender, node: String): Boolean {
        return getPermissionValue(sender, node) == Tristate.TRUE
    }

    override fun performCommand(sender: CommandSender, command: String) {
        runBlocking { sender.executeCommand(command) }
    }
}