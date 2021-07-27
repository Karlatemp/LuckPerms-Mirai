/*
 * Copyright (c) 2018-2021 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/InspectPermissionProcessor.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.util

import me.lucko.luckperms.common.calculator.processor.PermissionProcessor
import me.lucko.luckperms.common.calculator.result.TristateResult
import net.luckperms.api.util.Tristate

object InspectPermissionProcessor : PermissionProcessor {
    val RESULT_FACTORY = TristateResult.Factory(InspectPermissionProcessor::class.java)
    val shutdown = RESULT_FACTORY.result(Tristate.FALSE, "Permission service was shut-down. Restart service by type `/lpe startup` in console")
    override fun hasPermission(prev: TristateResult?, permission: String?): TristateResult {
        return TristateResult.UNDEFINED
    }

}