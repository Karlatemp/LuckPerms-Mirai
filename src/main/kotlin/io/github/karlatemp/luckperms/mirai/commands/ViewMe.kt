/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/ViewMe.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.commands

import io.github.karlatemp.luckperms.mirai.LPMiraiPlugin
import me.lucko.luckperms.common.command.CommandResult
import me.lucko.luckperms.common.command.abstraction.SingleCommand
import me.lucko.luckperms.common.command.access.CommandPermission
import me.lucko.luckperms.common.command.utils.ArgumentList
import me.lucko.luckperms.common.locale.LocaleManager
import me.lucko.luckperms.common.plugin.LuckPermsPlugin
import me.lucko.luckperms.common.sender.Sender
import me.lucko.luckperms.common.util.Predicates
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI

class ViewMe(
    localeManager: LocaleManager,
) : SingleCommand(
    LockedLocalizedCommandSpec(
        localeManager,
        "Lookup user's contexts in a group",
        "/%s view [target]"
    ),
    "view",
    CommandPermission.USER_INFO,
    Predicates.alwaysFalse()
) {
    @OptIn(ConsoleExperimentalAPI::class)
    override fun execute(
        plugin: LuckPermsPlugin?,
        sender: Sender,
        args: ArgumentList,
        label: String?
    ): CommandResult {
        if (sender is WrappedLPSender) {
            val real = sender.real
            if (args.isEmpty()) {
                // dump me
                val context = LPMiraiPlugin.contextManager.getQueryOptions(real.identifier)
                    .context()
                sender.sendMessage(context.joinToString(", ", "(", ")") {
                    "${it.key}=${it.value}"
                })
            } else {
                val target = args[0].toLongOrNull()
                if (target == null) {
                    sender.sendMessage("Sorry, $target is not a valid number.")
                    return CommandResult.INVALID_ARGS
                }
                val unboxed = real
                if (unboxed is MemberCommandSender) {
                    val member = unboxed.group.getOrNull(target)
                    if (member == null) {
                        sender.sendMessage("Sorry, $target is not a valid account.")
                        return CommandResult.INVALID_ARGS
                    }
                    val context = LPMiraiPlugin.contextManager.getQueryOptions(
                        @Suppress("INVISIBLE_MEMBER")
                        MemberCommandSender(member).identifier
                    ).context()
                    sender.sendMessage(context.joinToString(", ", "(", ")") {
                        "${it.key}=${it.value}"
                    })
                } else {
                    sender.sendMessage("Must use in a group.")
                }
            }
        } else {
            sender.sendMessage("§cNot from LuckPerms mirai console.")
        }
        return CommandResult.SUCCESS
    }
}