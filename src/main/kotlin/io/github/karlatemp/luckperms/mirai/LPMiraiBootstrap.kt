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

import io.github.karlatemp.luckperms.mirai.cp.RCP
import io.github.karlatemp.luckperms.mirai.gui.guiSender
import io.github.karlatemp.luckperms.mirai.internal.LPPermissionService
import io.github.karlatemp.luckperms.mirai.internal.LPPermissionService.uuid
import io.github.karlatemp.luckperms.mirai.internal.OpenApiImpl
import io.github.karlatemp.luckperms.mirai.logging.MiraiPluginLogger
import io.github.karlatemp.luckperms.mirai.openapi.internal.BackendImpl
import me.lucko.luckperms.common.config.ConfigKeys
import me.lucko.luckperms.common.config.generic.key.SimpleConfigKey
import me.lucko.luckperms.common.plugin.bootstrap.LuckPermsBootstrap
import me.lucko.luckperms.common.plugin.classpath.ClassPathAppender
import me.lucko.luckperms.common.plugin.logging.PluginLogger
import me.lucko.luckperms.common.plugin.scheduler.SchedulerAdapter
import net.luckperms.api.platform.Platform
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.hocon.HoconConfigurationLoader
import java.io.Closeable
import java.io.InputStream
import java.lang.invoke.MethodHandles
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.reflect.full.memberProperties

private val versionInfo: ConfigurationNode = HoconConfigurationLoader.builder().setSource {
    MethodHandles.lookup().lookupClass().classLoader.getResourceAsStream("metainfo.conf")!!
        .bufferedReader()
}
    .build()
    .load()
internal val version0 by lazy {
    versionInfo.getNode("pluginVersion").string!!
}
internal val buildTime by lazy {
    Date(versionInfo.getNode("buildTime").long)
}
internal val gitVersionLuckPerms by lazy {
    versionInfo.getNode("gitversion-luckperms").string!!
}
internal val gitVersionLuckPermsMirai by lazy {
    versionInfo.getNode("gitversion-plugin").string!!
}
internal val luckPermsMiraiVersion by lazy {
    versionInfo.getNode("plugin").string!!
}
internal val luckPermVersion by lazy {
    versionInfo.getNode("luckperms").string!!
}


@Suppress("unused")
object LPMiraiBootstrap : KotlinPlugin(
    JvmPluginDescriptionBuilder(
        "LuckPerms", version0
    )
        .id("io.github.karlatemp.luckperms-mirai")
        // .kind(PluginLoadPriority.BEFORE_EXTENSIONS)
        .author("lucko & Karlatemp")
        .build()
), LuckPermsBootstrap {

    private val pluginLogger0 by lazy { MiraiPluginLogger(logger) }
    private val schedulerAdapter by lazy { MiraiSchedulerAdapter(this) }

    override fun getPluginLogger(): PluginLogger = pluginLogger0

    override fun getScheduler(): SchedulerAdapter = schedulerAdapter

    override fun getClassPathAppender(): ClassPathAppender = classPathAppender
    private val classPathAppender = RCP(this)


    // load/enable latches
    private val loadLatch0 = CountDownLatch(1)
    private val enableLatch0 = CountDownLatch(1)

    override fun getLoadLatch(): CountDownLatch = loadLatch0

    override fun getEnableLatch(): CountDownLatch = enableLatch0

    override fun getVersion(): String = version0
    override fun getVersionLuckPerms(): String = luckPermVersion

    private lateinit var startupTime0: Instant
    override fun getStartupTime(): Instant = startupTime0

    override fun getType(): Platform.Type {
        return Platform.Type.MIRAI_CONSOLE
    }

    override fun getServerBrand(): String {
        return "Mirai"
    }

    private val consoleVersion by lazy {
        kotlin.runCatching {
            return@lazy MiraiConsole.version.toString()
        }
        MiraiConsole::class.memberProperties.first { it.name == "version" }
            .get(MiraiConsole.INSTANCE).toString()
    }

    private val serverVersion0 by lazy {
        buildString {
            append("     MiraiConsole/").append(consoleVersion)
            append(" LuckPerms-Mirai/").append(luckPermsMiraiVersion)
            append(" LuckPerms-Core/").append(luckPermVersion)
            append("\n              ")
            append("BuildTime: ").append(buildTime.toInstant().atZone(ZoneId.systemDefault()))
            append("\n              ")
            append("LuckPerms-Core / ").append(gitVersionLuckPerms)
            append("\n              ")
            append("LuckPerms-Mirai/ ").append(gitVersionLuckPermsMirai)
            append("\n              ")
            append("LuckPerms-Mirai provided by Karlatemp. LuckPerms provided by Luck.")
        }
    }

    override fun versionOnCommandRender(): String = serverVersion0
    override fun getServerVersion(): String {
        return "MiraiConsole/$consoleVersion"
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
            return Optional.of(UUID(MAGIC_UUID_HIGH_BITS, username.toLong()))
        }
        kotlin.runCatching {
            return Optional.of(AbstractPermitteeId.parseFromString(username).uuid())
        }
        return Optional.empty()
    }

    override fun lookupUsername(uniqueId: UUID): Optional<String> {
        if (uniqueId.mostSignificantBits != MAGIC_UUID_HIGH_BITS)
            return Optional.empty()
        return Optional.of(uniqueId.leastSignificantBits.toString())
    }

    override fun getPlayerCount(): Int {
        return 0
    }

    override fun getPlayerList(): MutableCollection<String> {
        return mutableListOf()
    }

    override fun getOnlinePlayers(): MutableCollection<UUID> {
        return mutableListOf()
    }

    override fun isPlayerOnline(uniqueId: UUID): Boolean {
        if (uniqueId.mostSignificantBits == 0L) { // System
            return true
        }
        return uniqueId.mostSignificantBits == MAGIC_UUID_HIGH_BITS
    }

    private var incompatibleVersion: Boolean = false

    internal lateinit var pcs: PluginComponentStorage
    override fun PluginComponentStorage.onLoad() {
        pcs = this
        if (checkIncompatibleVersion()) {
            incompatibleVersion = true
            return
        }
        this.contributePermissionService {
            object : PermissionServiceProvider {
                override val instance: PermissionService<*>
                    get() = LPPermissionService
            }
        }
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
            BackendImpl.INSTANCE = OpenApiImpl
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
        (guiSender as? Closeable)?.close()
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
        ConfigKeys::class.java.getDeclaredField("KEYS").let { keysField ->
            keysField.isAccessible = true
            val keys = ConfigKeys.getKeys().toMutableList()
            var od = keys.maxOf { it.ordinal() } + 1
            val additions = LPMConfigs::class.java.declaredFields.asSequence().onEach {
                it.isAccessible = true
            }.map { it[LPMConfigs] }.mapNotNull { it as? SimpleConfigKey<*> }.toList()
            additions.forEach { it.setOrdinal(od); od++ }
            keysField.set(null, keys.also { it.addAll(additions) }.toList())
        }
    }
}