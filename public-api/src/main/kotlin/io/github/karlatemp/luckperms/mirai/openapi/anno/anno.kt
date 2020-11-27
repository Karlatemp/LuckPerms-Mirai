/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.public-api.main/LpInternalApi.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.openapi.anno

@RequiresOptIn(
    message = "This api is only for LuckPerms internal usage",
    level = RequiresOptIn.Level.ERROR
)
annotation class InternalLpApi

@RequiresOptIn(
    message = "Experimental Api",
    level = RequiresOptIn.Level.WARNING
)
annotation class ExperimentalLpApi
