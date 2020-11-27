/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.public-api.main/util.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */
@file:JvmName("Utils")
@file:Suppress("unused")

package io.github.karlatemp.luckperms.mirai.openapi

import io.github.karlatemp.luckperms.mirai.openapi.internal.BackendImpl
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.permission.Permittee
import java.util.*

infix fun Permittee.hasPermission(perm: String): Boolean = BackendImpl.INSTANCE.run { hasPerm(perm) }

suspend fun CommandSender.sendBcMsg(msg: String): Unit = BackendImpl.INSTANCE.sendBcMsg(this, msg)


object LuckPermsUtil {
    // region metadata
    @JvmStatic
    val luckPermsCoreVersion: String
        get() = BackendImpl.INSTANCE.lpver

    @JvmStatic
    val luckPermsMiraiVersion: String
        get() = BackendImpl.INSTANCE.lpmver

    @JvmStatic
    val luckPermsCoreGitVersion: String
        get() = BackendImpl.INSTANCE.lpgver

    @JvmStatic
    val luckPermsMiraiGitVersion: String
        get() = BackendImpl.INSTANCE.lpmgver

    // endregion

    // region uuid
    @JvmStatic
    fun UUID.isLuckPermsConsole(): Boolean = BackendImpl.INSTANCE.isConsoleUUID(this)

    @JvmStatic
    fun UUID.isLuckPermsUserUid(): Boolean = BackendImpl.INSTANCE.isUsrUid(this)

    @JvmStatic
    val UUID.luckPermsUserId: Long
        get() = BackendImpl.INSTANCE.getId(this)

    // endregion
}
