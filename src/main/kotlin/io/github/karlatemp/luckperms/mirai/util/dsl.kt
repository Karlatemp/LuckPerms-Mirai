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

import io.github.karlatemp.luckperms.mirai.MiraiSenderFactory
import net.luckperms.api.util.Tristate
import net.mamoe.mirai.console.permission.Permittee

infix fun Permittee.hasPermission(
    permission: String
): Boolean {
    return MiraiSenderFactory.getPermissionValue0(this, permission) == Tristate.TRUE
}

//fun permission(permission: String) = CommandPermission { hasPermission(permission) }
