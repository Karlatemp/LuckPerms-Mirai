/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.public-api.main/CustomPermitteeId.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.openapi

import io.github.karlatemp.luckperms.mirai.openapi.anno.ExperimentalLpApi
import net.mamoe.mirai.console.permission.PermitteeId
import java.util.*

@ExperimentalLpApi
interface CustomPermitteeId : PermitteeId {
    val uuid: UUID
}

