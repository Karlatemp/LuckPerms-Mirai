/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/dsl.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.util

import io.github.karlatemp.luckperms.mirai.LPMiraiBootstrap
import io.github.karlatemp.luckperms.mirai.LPMiraiPlugin
import io.github.karlatemp.luckperms.mirai.MiraiSenderFactory
import io.github.karlatemp.luckperms.mirai.WrappedCommandSender
import net.luckperms.api.util.Tristate
import net.mamoe.mirai.console.command.CommandPermission.Any.hasPermission
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI

infix fun CommandSender.hasPermission(
    permission: String
): Boolean {
    @OptIn(ConsoleExperimentalAPI::class)
    if (this is UserCommandSender) {
        val wrapped = WrappedCommandSender(this)
        return MiraiSenderFactory.getPermissionValue0(wrapped, "permission") == Tristate.TRUE
    }
    return true
}