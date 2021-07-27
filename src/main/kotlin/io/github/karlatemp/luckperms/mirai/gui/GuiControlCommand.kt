/*
 * Copyright (c) 2018-2021 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/GuiControlCommand.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.gui

import io.github.karlatemp.luckperms.mirai.LPMiraiBootstrap
import io.github.karlatemp.luckperms.mirai.internal.Magic_NO_PERMISSION_CHECK
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.permission.Permission

@Suppress("unused")
object GuiControlCommand : CompositeCommand(LPMiraiBootstrap, "lpg") {
    override val permission: Permission get() = Magic_NO_PERMISSION_CHECK

    @SubCommand
    fun CommandSender.clear() {
        if (this !is ConsoleCommandSender) return

        guiSender.clearScreen()
    }
}