/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/LPMiraiBootstrap.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai

import com.google.auto.service.AutoService
import io.github.karlatemp.luckperms.mirai.internal.LPPermissionService
import io.github.karlatemp.luckperms.mirai.logging.MiraiPluginLogger
import me.lucko.luckperms.common.dependencies.classloader.PluginClassLoader
import me.lucko.luckperms.common.dependencies.classloader.ReflectionClassLoader
import me.lucko.luckperms.common.plugin.bootstrap.LuckPermsBootstrap
import me.lucko.luckperms.common.plugin.logging.PluginLogger
import me.lucko.luckperms.common.plugin.scheduler.SchedulerAdapter
import net.luckperms.api.platform.Platform
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.hocon.HoconConfigurationLoader
import java.io.InputStream
import java.lang.invoke.MethodHandles
import java.nio.file.Path
import java.time.Instant
import java.util.*
import java.util.concurrent.CountDownLatch

private val versionInfo: ConfigurationNode = HoconConfigurationLoader.builder().setSource {
    MethodHandles.lookup().lookupClass().classLoader.getResourceAsStream("metainfo.conf")!!
        .bufferedReader()
}
    .build()
    .load()

@AutoService(JvmPlugin::class)
@Suppress("unused")
object LPMiraiBootstrap : KotlinPlugin(
    JvmPluginDescriptionBuilder(
        "LuckPerms", versionInfo.getNode("pluginVersion").string!!
    )
        .id("io.github.karlatemp.luckperms-mirai")
        // .kind(PluginLoadPriority.BEFORE_EXTENSIONS)
        .author("lucko & Karlatemp")
        .build()
), LuckPermsBootstrap {

    private val pluginLogger0 by lazy { MiraiPluginLogger(logger) }

    override fun getPluginLogger(): PluginLogger = pluginLogger0

    override fun getScheduler(): SchedulerAdapter = MiraiSchedulerAdapter

    private val classLoader = ReflectionClassLoader(this)

    override fun getPluginClassLoader(): PluginClassLoader = classLoader

    // load/enable latches
    private val loadLatch0 = CountDownLatch(1)
    private val enableLatch0 = CountDownLatch(1)

    override fun getLoadLatch(): CountDownLatch = loadLatch0

    override fun getEnableLatch(): CountDownLatch = enableLatch0

    override fun getVersion(): String = this.description.version.toString()

    private lateinit var startupTime0: Instant
    override fun getStartupTime(): Instant = startupTime0

    override fun getType(): Platform.Type {
        return Platform.Type.BUNGEECORD
    }

    override fun getServerBrand(): String {
        return "Mirai"
    }

    private val serverVersion0 by lazy {
        "MiraiConsole/" + MiraiConsole.version.toString() +
                " LuckPerms-Mirai/" + versionInfo.getNode("plugin").string +
                " LuckPerms-Core/" + versionInfo.getNode("luckperms").string
    }

    override fun getServerVersion(): String {
        return serverVersion0
    }

    override fun getDataDirectory(): Path = dataFolderPath

    override fun getResourceStream(path: String?): InputStream? {
        return javaClass.classLoader.getResourceAsStream(path)
    }

    override fun getPlayer(uniqueId: UUID): Optional<Long> {
        if (uniqueId.mostSignificantBits != MAGIC_UUID_HIGH_BITS)
            return Optional.empty()
        return Optional.of(uniqueId.leastSignificantBits)
    }

    override fun lookupUniqueId(username: String): Optional<UUID> {
        kotlin.runCatching {
            return Optional.of(UUID(0, username.toLong()))
        }
        return Optional.empty()
    }

    override fun lookupUsername(uniqueId: UUID): Optional<String> {
        if (uniqueId.mostSignificantBits != MAGIC_UUID_HIGH_BITS)
            return Optional.empty()
        return Optional.of(uniqueId.leastSignificantBits.toString())
    }

    override fun getPlayerCount(): Int {
        return CACHED_USERS.size
    }

    override fun getPlayerList(): MutableCollection<String> {
        return CACHED_USERS.keys().asSequence()
            .map { it.toString() }
            .toHashSet()
    }

    override fun getOnlinePlayers(): MutableCollection<UUID> {
        return CACHED_USERS.keys().asSequence()
            .map { UUID(MAGIC_UUID_HIGH_BITS, it) }
            .toHashSet()
    }

    override fun isPlayerOnline(uniqueId: UUID): Boolean {
        return uniqueId.mostSignificantBits == MAGIC_UUID_HIGH_BITS
    }

    private var incompatibleVersion: Boolean = false
    override fun PluginComponentStorage.onLoad() {
        if (checkIncompatibleVersion()) {
            incompatibleVersion = true
            return
        }
        this.contributePermissionService { LPPermissionService }
        try {
            LPMiraiPlugin.load()
        } finally {
            loadLatch.countDown()
        }
        onEnable0()
    }

    override fun onEnable() {
    }
    fun onEnable0() {
        if (incompatibleVersion) {
            logger.error("----------------------------------------------------------------------")
            logger.error("Your console is not compatible with this build of LuckPerms. :(")
            logger.error("")
            logger.error("You need add latest Gson Library to libraries.")
            logger.error("LuckPerms will not work...")
            logger.error("----------------------------------------------------------------------")
            return
        }
        startupTime0 = Instant.now()
        try {
            LPMiraiPlugin.enable()
        } finally {
            this.enableLatch.countDown()
        }
    }

    override fun onDisable() {
        if (this.incompatibleVersion) {
            return
        }

        LPMiraiPlugin.disable()
    }

    private fun checkIncompatibleVersion(): Boolean {
        return try {
            Class.forName("com.google.gson.internal.bind.TreeTypeAdapter")
            false
        } catch (e: ClassNotFoundException) {
            true
        }
    }

    init {
        Platform.Type::class.java
            .getDeclaredField("friendlyName")
            .apply {
                isAccessible = true
            }
            .set(Platform.Type.BUNGEECORD, "Mirai Console")

    }
}