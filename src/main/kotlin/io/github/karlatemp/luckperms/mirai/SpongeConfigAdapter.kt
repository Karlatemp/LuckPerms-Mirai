/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/SpongeConfigAdapter.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

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
