/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/LPMiraiPlugin.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.github.karlatemp.luckperms.mirai


import com.google.common.collect.ImmutableMap
import io.github.karlatemp.luckperms.mirai.commands.ViewMe
import io.github.karlatemp.luckperms.mirai.commands.WrappedLPSender
import io.github.karlatemp.luckperms.mirai.context.MiraiContextManager
import io.github.karlatemp.luckperms.mirai.util.hasPermission
import io.github.karlatemp.luckperms.mirai.util.permission
import kotlinx.coroutines.runBlocking
import me.lucko.luckperms.common.api.LuckPermsApiProvider
import me.lucko.luckperms.common.calculator.CalculatorFactory
import me.lucko.luckperms.common.command.CommandManager
import me.lucko.luckperms.common.command.abstraction.Command
import me.lucko.luckperms.common.config.generic.adapter.ConfigurationAdapter
import me.lucko.luckperms.common.context.ContextManager
import me.lucko.luckperms.common.dependencies.Dependency
import me.lucko.luckperms.common.event.AbstractEventBus
import me.lucko.luckperms.common.messaging.MessagingFactory
import me.lucko.luckperms.common.model.Group
import me.lucko.luckperms.common.model.Track
import me.lucko.luckperms.common.model.User
import me.lucko.luckperms.common.model.manager.group.GroupManager
import me.lucko.luckperms.common.model.manager.group.StandardGroupManager
import me.lucko.luckperms.common.model.manager.track.StandardTrackManager
import me.lucko.luckperms.common.model.manager.track.TrackManager
import me.lucko.luckperms.common.model.manager.user.StandardUserManager
import me.lucko.luckperms.common.model.manager.user.UserManager
import me.lucko.luckperms.common.plugin.AbstractLuckPermsPlugin
import me.lucko.luckperms.common.plugin.util.AbstractConnectionListener
import me.lucko.luckperms.common.sender.Sender
import me.lucko.luckperms.common.tasks.CacheHousekeepingTask
import me.lucko.luckperms.common.tasks.ExpireTemporaryTask
import me.lucko.luckperms.common.util.MoreFiles
import net.luckperms.api.LuckPerms
import net.luckperms.api.query.QueryOptions
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.message.data.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import kotlin.collections.LinkedHashMap

object LPMiraiPlugin : AbstractLuckPermsPlugin() {

    override fun getBootstrap(): LPMiraiBootstrap = LPMiraiBootstrap

    private lateinit var userManager0: UserManager<out User>
    private lateinit var groupManager0: GroupManager<out Group>
    private lateinit var trackManager0: TrackManager<out Track>
    override fun getUserManager(): UserManager<out User> = userManager0
    override fun getGroupManager(): GroupManager<out Group> = groupManager0
    override fun getTrackManager(): TrackManager<out Track> = trackManager0

    override fun setupManagers() {
        this.userManager0 = StandardUserManager(this)
        this.groupManager0 = StandardGroupManager(this)
        this.trackManager0 = StandardTrackManager(this)
    }

    private lateinit var commandManager0: CommandManager
    override fun getCommandManager(): CommandManager = commandManager0
    override fun registerCommands() {
        val cm = CommandManager(this)
        commandManager0 = cm

        object : AbstractCommand(
            owner = LPMiraiBootstrap,
            names = arrayOf("lp", "luckperms"),
            description = "LuckPerms",
            permission = CommandPermission.Any,
            prefixOptional = true
        ) {
            override val usage: String
                get() = "/lp"

            @OptIn(ConsoleExperimentalAPI::class)
            override suspend fun CommandSender.onCommand(args: Array<out Any>) {
                val target =
                    if (this@onCommand is UserCommandSender) {
                        WrappedCommandSender(this@onCommand)
                    } else this@onCommand
                val sender = WrappedLPSender(
                    senderFactory0.wrap(
                        target
                    ), target
                )
                cm.executeCommand(sender, "lp", args.asSequence()
                    .flatMap {
                        when (it) {
                            is PlainText -> it.content.trim().split(' ')
                            is MessageSource -> emptyList()
                            is At -> listOf(it.target.toString())
                            is MessageContent -> listOf(it.content)
                            else -> it.toString().split(' ')
                        }
                    }
                    .filter { it.isNotBlank() }
                    .toMutableList())
                    .thenAccept {
                        if (target is WrappedCommandSender) {
                            val ms = target.bufferedMessages
                            target.bufferedMessages = null
                            if (ms != null && !ms.isEmpty()) {
                                val msg = ms.joinToString("\n").also {
                                    ms.clear()
                                }
                                runBlocking {
                                    sendMessage(msg)
                                }
                            }
                        }
                    }
            }
        }.register(true)
        object : AbstractCommand(
            owner = LPMiraiBootstrap,
            names = arrayOf("lpcheck"),
            description = "LuckPerms - Checker",
            permission = permission("luckperms.check"),
            prefixOptional = true
        ) {
            override val usage: String
                get() = ""

            override suspend fun CommandSender.onCommand(args: Array<out Any>) {
                val perm = args.joinToString(" ").trim()
                sendMessage("$perm -> " + hasPermission(perm))
            }
        }.register(true)
        // inject command
        @Suppress("UNCHECKED_CAST")
        CommandManager::class.java.getDeclaredField("mainCommands").apply {
            isAccessible = true
            val old = this[cm] as Map<String, Command<*>>
            val copied = LinkedHashMap(old)
            copied["view"] = ViewMe(localeManager)
            this[cm] = ImmutableMap.copyOf(copied)
            // Map<String, Command<?>>
        }
    }

    private lateinit var connectionListener0: AbstractConnectionListener
    override fun getConnectionListener(): AbstractConnectionListener = connectionListener0

    override fun getQueryOptionsForUser(user: User): Optional<QueryOptions> {
        return Optional.empty()

    }

    override fun getOnlineSenders(): Stream<Sender> {
        return Stream.of(console)
    }

    private val console by lazy {
        senderFactory0.wrap(ConsoleCommandSender.INSTANCE)
    }

    override fun getConsoleSender(): Sender = console

    internal lateinit var senderFactory0: MiraiSenderFactory
    override fun setupSenderFactory() {
        senderFactory0 = MiraiSenderFactory()
    }

    override fun provideConfigurationAdapter(): ConfigurationAdapter {
        return SpongeConfigAdapter(this, resolveConfig())
    }

    private fun resolveConfig(): Path? {
        val path = this.bootstrap.configDirectory.resolve("luckperms.conf")
        if (!Files.exists(path)) {
            try {
                MoreFiles.createDirectoriesIfNotExists(this.bootstrap.configDirectory)
                javaClass.classLoader.getResourceAsStream("luckperms.conf").use { `is` ->
                    Files.copy(
                        `is`,
                        path
                    )
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        return path
    }


    override fun registerPlatformListeners() {
        this.connectionListener0 = MiraiConnectionListener().apply {
            registerListeners()
        }
    }

    override fun provideMessagingFactory(): MessagingFactory<*> = MessagingFactory(this)


    override fun provideCalculatorFactory(): CalculatorFactory = MiraiCalculatorFactory

    override fun getGlobalDependencies(): MutableSet<Dependency> {
        return EnumSet.noneOf(Dependency::class.java)
    }

    private lateinit var contextManager0: MiraiContextManager
    override fun getContextManager(): MiraiContextManager = contextManager0

    @OptIn(ConsoleExperimentalAPI::class)
    override fun setupContextManager() {
        contextManager0 = MiraiContextManager().apply {
            registerCalculator { target, consumer ->
                if (target is WrappedCommandSender) {
                    val real = target.parent
                    if (real is MemberCommandSender) {
                        consumer.accept("type", "group")
                        consumer.accept("level", real.user.permission.name.toLowerCase())
                        consumer.accept("group", real.group.id.toString())
                    } else {
                        consumer.accept("type", "direct")
                    }
                }
            }
        }
    }

    override fun setupPlatformHooks() {
    }

    override fun provideEventBus(apiProvider: LuckPermsApiProvider): AbstractEventBus<*> {
        return MiraiEventBus(apiProvider)
    }

    override fun registerApiOnPlatform(api: LuckPerms?) {
        // mirai has no services manager.
    }

    override fun registerHousekeepingTasks() {
        this.bootstrap.scheduler.asyncRepeating(ExpireTemporaryTask(this), 3, TimeUnit.SECONDS)
        this.bootstrap.scheduler.asyncRepeating(CacheHousekeepingTask(this), 2, TimeUnit.MINUTES)
    }

    override fun performFinalSetup() {
    }
}