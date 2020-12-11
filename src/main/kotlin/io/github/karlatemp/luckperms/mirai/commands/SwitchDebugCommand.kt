/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/SwitchDebugCommand.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.commands

import io.github.karlatemp.luckperms.mirai.internal.OpenApiImpl
import io.github.karlatemp.luckperms.mirai.logging.DebugKit
import io.github.karlatemp.luckperms.mirai.util.newLine
import io.github.karlatemp.luckperms.mirai.util.sendmsg
import me.lucko.luckperms.common.command.CommandResult
import me.lucko.luckperms.common.command.abstraction.SingleCommand
import me.lucko.luckperms.common.command.access.CommandPermission
import me.lucko.luckperms.common.command.spec.CommandSpec
import me.lucko.luckperms.common.command.utils.ArgumentList
import me.lucko.luckperms.common.plugin.LuckPermsPlugin
import me.lucko.luckperms.common.sender.Sender
import me.lucko.luckperms.common.util.Predicates

internal object SwitchDebugCommand : SingleCommand(
    CommandSpec.LPM_DEBUG,
    "LpmDebug",
    CommandPermission.LPM_DEBUG,
    Predicates.alwaysFalse()
) {
    override fun execute(
        plugin: LuckPermsPlugin?,
        sender: Sender,
        args: ArgumentList,
        label: String?
    ): CommandResult {
        when (args.getOrDefault(0, null)) {
            null -> return sender.sendmsg(buildString {
                newLine("LuckPerms Mirai - Dev Kit")
                newLine()
                newLine("Sub commands:")
                newLine("status - View LuckPermsMirai status")
                newLine("logger - Enable/Disable debug logger.")
                newLine("trust  - Temporarily trust full access to another user. (UNSAFE)")
            }).let { CommandResult.INVALID_ARGS }
            "status" -> {
                sender.sendmsg(buildString {
                    newLine("LuckPerms Mirai - Dev Kit")
                    newLine()
                    newLine {
                        append("LuckPerms - Core [").append(OpenApiImpl.lpver).append("] - ").append(OpenApiImpl.lpgver)
                    }
                    newLine {
                        append("LuckPerms - Mirai [").append(OpenApiImpl.lpmver).append("] - ")
                            .append(OpenApiImpl.lpmgver)
                    }
                    newLine()
                    newLine {
                        append("Source Code: https://github.com/Karlatemp/LuckPerms-Mirai")
                    }
                    newLine()
                    newLine("LPM DebugLogger: " + (if (DebugKit.isDebugEnabled) "ON" else "OFF"))
                    newLine("Trusted Users: " + DebugKit.trustedUsers)
                })
            }
            "trust" -> {
                when (args.getOrDefault(1, null)) {
                    null -> return sender.sendmsg(buildString {
                        newLine("LuckPerms Mirai - Dev Kit - Trust")
                        newLine()
                        newLine("enable  - Enable trust system")
                        newLine("disable - Disable trust system")
                        newLine("trust [users] - Trust full access to another user")
                        newLine("remove [users] - Drop full access")
                        newLine("reset - Reset trust system")
                    }).let { CommandResult.INVALID_ARGS }
                    "enable" -> {
                        DebugKit.trustedUsers = ArrayList()
                        sender.sendmsg("Enable and reset trust system.")
                    }
                    "disable" -> {
                        DebugKit.trustedUsers = null
                        sender.sendmsg("Trust system disabled")
                    }
                    "reset" -> {
                        (DebugKit.trustedUsers ?: sender.sendmsg("Trust system not enabled").let { null })
                            ?.clear()
                    }
                    "trust" -> {
                        (DebugKit.trustedUsers ?: sender.sendmsg("Trust system not enabled").let {
                            null
                        })?.let { trusted ->
                            val t = args.subList(2, args.size).map { it.toLong() }
                            trusted.addAll(t)
                            sender.sendmsg("Trusted " + t.joinToString(", ", prefix = "", postfix = ""))
                        }
                    }
                    "remove" -> {
                        (DebugKit.trustedUsers ?: sender.sendmsg("Trust system not enabled").let {
                            null
                        })?.let { trusted ->
                            val t = args.subList(2, args.size).map { it.toLong() }
                            trusted.removeIf { t.contains(it) }
                            sender.sendmsg("Dropped " + t.joinToString(", ", prefix = "", postfix = ""))
                        }
                    }
                }
            }
            "logger" -> {
                DebugKit.isDebugEnabled = !DebugKit.isDebugEnabled
                sender.sendmsg("Debug mode switch to ${DebugKit.isDebugEnabled}")
            }
        }
        return CommandResult.SUCCESS
    }
}
