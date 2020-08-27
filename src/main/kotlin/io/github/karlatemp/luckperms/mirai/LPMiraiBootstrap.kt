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

import io.github.karlatemp.luckperms.mirai.logging.MiraiPluginLogger
import me.lucko.luckperms.common.dependencies.classloader.PluginClassLoader
import me.lucko.luckperms.common.dependencies.classloader.ReflectionClassLoader
import me.lucko.luckperms.common.plugin.bootstrap.LuckPermsBootstrap
import me.lucko.luckperms.common.plugin.logging.PluginLogger
import me.lucko.luckperms.common.plugin.scheduler.SchedulerAdapter
import net.luckperms.api.platform.Platform
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import java.io.InputStream
import java.nio.file.Path
import java.time.Instant
import java.util.*
import java.util.concurrent.CountDownLatch

@Suppress("unused")
object LPMiraiBootstrap : KotlinPlugin(), LuckPermsBootstrap {
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

    private val miraiVersion by lazy {
        "MiraiConsole/" + MiraiConsole.version.toString()
    }

    override fun getServerVersion(): String {
        return miraiVersion
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

    override fun onLoad() {
        if (checkIncompatibleVersion()) {
            this.incompatibleVersion = true
            return
        }

        try {
            LPMiraiPlugin.load()
        } finally {
            this.loadLatch.countDown()
        }
    }

    override fun onEnable() {
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
            LPMiraiPlugin.enable();
        } finally {
            this.enableLatch.countDown();
        }
    }

    override fun onDisable() {
        if (this.incompatibleVersion) {
            return;
        }

        LPMiraiPlugin.disable();
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