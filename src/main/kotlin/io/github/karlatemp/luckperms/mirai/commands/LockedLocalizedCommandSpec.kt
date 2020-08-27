/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/LockedLocalizedCommandSpec.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.commands

import me.lucko.luckperms.common.locale.LocaleManager
import me.lucko.luckperms.common.locale.command.Argument
import me.lucko.luckperms.common.locale.command.LocalizedCommandSpec
import java.util.*

class LockedLocalizedCommandSpec(
    localeManager: LocaleManager,
    private val desc: String,
    private val usage: String
) : LocalizedCommandSpec(null, localeManager) {
    override fun description(): String {
        return desc
    }

    override fun args(): MutableList<Argument> {
        return Collections.emptyList()
    }


    override fun usage(): String {
        return usage
    }
}