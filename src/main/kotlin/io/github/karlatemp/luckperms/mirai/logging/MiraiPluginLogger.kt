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