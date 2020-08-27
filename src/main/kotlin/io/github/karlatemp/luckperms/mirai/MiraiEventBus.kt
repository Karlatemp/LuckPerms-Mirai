/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/MiraiEventBus.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai

import me.lucko.luckperms.common.api.LuckPermsApiProvider
import me.lucko.luckperms.common.event.AbstractEventBus
import net.mamoe.mirai.console.plugin.Plugin

class MiraiEventBus(
    apiProvider: LuckPermsApiProvider
) : AbstractEventBus<Plugin>(LPMiraiPlugin, apiProvider) {
    override fun checkPlugin(plugin: Any?): Plugin {
        return plugin as Plugin
    }
}