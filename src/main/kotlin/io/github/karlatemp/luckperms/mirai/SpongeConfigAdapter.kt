package io.github.karlatemp.luckperms.mirai

import me.lucko.luckperms.common.config.generic.adapter.ConfigurateConfigAdapter
import me.lucko.luckperms.common.config.generic.adapter.ConfigurationAdapter
import me.lucko.luckperms.common.plugin.LuckPermsPlugin
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.hocon.HoconConfigurationLoader
import ninja.leaping.configurate.loader.ConfigurationLoader
import java.nio.file.Path

class SpongeConfigAdapter(plugin: LuckPermsPlugin?, path: Path?) :
    ConfigurateConfigAdapter(plugin, path), ConfigurationAdapter {
    override fun createLoader(path: Path): ConfigurationLoader<out ConfigurationNode?> {
        return HoconConfigurationLoader.builder().setPath(path).build()
    }
}
