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