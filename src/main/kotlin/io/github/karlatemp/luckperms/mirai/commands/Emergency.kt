/*
 * Copyright (c) 2018-2021 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/Emergency.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package io.github.karlatemp.luckperms.mirai.commands

import io.github.karlatemp.luckperms.mirai.LPMiraiBootstrap
import io.github.karlatemp.luckperms.mirai.internal.LuckPermsPermission
import io.github.karlatemp.luckperms.mirai.internal.Magic_NO_PERMISSION_CHECK
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionId

object Emergency : CompositeCommand(
    LPMiraiBootstrap,
    "lpe",
    description = "LuckPerms - Mirai - Emergency",
    parentPermission = Magic_NO_PERMISSION_CHECK,
) {
    override val permission: Permission by lazy {
        LuckPermsPermission(
            Magic_NO_PERMISSION_CHECK,
            "",
            PermissionId("luckperms", "emergency"),
            "luckperms.emergency",
            null
        )
    }

    @Description("fast shutdown")
    @SubCommand
    suspend fun CommandSender.shutdown() {
        EmergencyOptions.shutdown = true
        sendMessage("Service shutdown.... Type `/lpe restart` in console for restart service")
    }

    @Description("restart")
    @SubCommand
    suspend fun CommandSender.startup() {
        EmergencyOptions.shutdown = false
        sendMessage("Service unlocked")
    }
}

object EmergencyOptions {
    @JvmField
    var shutdown = false
}
