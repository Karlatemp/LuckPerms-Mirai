/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/MiraiPluginLogger.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.logging

import me.lucko.luckperms.common.plugin.logging.PluginLogger
import net.mamoe.mirai.utils.MiraiLogger

class MiraiPluginLogger(
    private val logger: MiraiLogger
) : PluginLogger {
    override fun info(s: String?) {
        logger.info(s)
    }

    override fun warn(s: String?) {
        logger.warning(s)
    }

    override fun severe(s: String?) {
        logger.error(s)
    }
}