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

import io.github.karlatemp.luckperms.mirai.logging.DebugKit
import io.github.karlatemp.luckperms.mirai.util.sendmsg
import me.lucko.luckperms.common.command.CommandResult
import me.lucko.luckperms.common.command.abstraction.SingleCommand
import me.lucko.luckperms.common.command.access.CommandPermission
import me.lucko.luckperms.common.command.spec.CommandSpec
import me.lucko.luckperms.common.command.utils.ArgumentList
import me.lucko.luckperms.common.plugin.LuckPermsPlugin
import me.lucko.luckperms.common.sender.Sender
import me.lucko.luckperms.common.util.Predicates

internal object SwitchDebugCommand:SingleCommand(
    CommandSpec.LPM_DEBUG,
    "LpmDebug",
    CommandPermission.LPM_DEBUG,
    Predicates.alwaysFalse()
) {
    override fun execute(
        plugin: LuckPermsPlugin?,
        sender: Sender,
        args: ArgumentList?,
        label: String?
    ): CommandResult {
        DebugKit.isDebugEnabled = !DebugKit.isDebugEnabled
        sender.sendmsg("[LuckPerms Mirai] Debug mode switch to ${DebugKit.isDebugEnabled}")
        return CommandResult.SUCCESS
    }
}
