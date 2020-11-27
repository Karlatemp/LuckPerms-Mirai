/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.public-api.main/BackendImpl.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.openapi.internal

import io.github.karlatemp.luckperms.mirai.openapi.anno.InternalLpApi
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.permission.Permittee
import java.util.*

@InternalLpApi
interface BackendImpl {
    companion object {
        lateinit var INSTANCE: BackendImpl
    }

    fun Permittee.hasPerm(perm: String): Boolean
    suspend fun sendBcMsg(sender: CommandSender, msg: String)
    val lpmver: String
    val lpver: String
    val lpmgver: String
    val lpgver: String
    val buildtime: Long
    fun isConsoleUUID(target: UUID): Boolean
    fun isUsrUid(target: UUID): Boolean
    fun getId(source: UUID): Long
}
