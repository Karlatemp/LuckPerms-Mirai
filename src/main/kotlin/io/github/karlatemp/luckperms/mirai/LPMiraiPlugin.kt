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


import io.github.karlatemp.luckperms.mirai.commands.WrappedLPSender
import io.github.karlatemp.luckperms.mirai.context.MiraiCalculator
import io.github.karlatemp.luckperms.mirai.context.MiraiContextManager
import io.github.karlatemp.luckperms.mirai.internal.LPPermissionService
import io.github.karlatemp.luckperms.mirai.internal.LPPermissionService.uuid
import io.github.karlatemp.luckperms.mirai.internal.Magic_NO_PERMISSION_CHECK
import io.github.karlatemp.luckperms.mirai.util.hasPermission
import me.lucko.luckperms.common.api.LuckPermsApiProvider
import me.lucko.luckperms.common.calculator.CalculatorFactory
import me.lucko.luckperms.common.command.CommandManager
import me.lucko.luckperms.common.config.generic.adapter.ConfigurationAdapter
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
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.CommandSender.Companion.asMemberCommandSender
import net.mamoe.mirai.console.extensions.PostStartupExtension
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.getFriendOrNull
import net.mamoe.mirai.message.data.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import kotlin.collections.ArrayList

object LPMiraiPlugin : AbstractLuckPermsPlugin() {
    private val commandCaller = ThreadLocal<CommandSender>()

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
        @OptIn(ConsoleExperimentalApi::class)
        LPMiraiBootstrap.componentStorage.contribute(PostStartupExtension.ExtensionPoint) {
            PostStartupExtension {
                val commands: MutableList<net.mamoe.mirai.console.command.Command> = ArrayList()
                commands.add(object : AbstractCommand(
                    owner = LPMiraiBootstrap,
                    primaryName = "luckperms",
                    secondaryNames = arrayOf("lp", "llp", "lplp"),
                    description = "LuckPerms",
                    // permission = CommandPermission.Any,
                    prefixOptional = false
                ) {
                    init {
                        @Suppress("UNCHECKED_CAST")
                        val sec = secondaryNames as Array<String>
                        sec[0] = "lp"
                        sec[1] = "luckperms:lp"
                        sec[2] = "luckperms:luckperms"
                    }

                    override val usage: String
                        get() = "/lp"

                    override val permission: Permission
                        get() = Magic_NO_PERMISSION_CHECK

                    override suspend fun CommandSender.onCommand(args: MessageChain) {
                        onCommand(args.asSequence()
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
                    }

                    fun CommandSender.onCommand(args: MutableList<String>) {
                        val sender = WrappedLPSender(
                            senderFactory0.wrap(
                                this@onCommand
                            ), this@onCommand
                        )
                        cm.executeCommand(sender, "lp", object : MutableList<String> by args {
                            override fun isEmpty(): Boolean {
                                commandCaller.set(this@onCommand)
                                return args.isEmpty()
                            }
                        })
                            .thenAccept {
                                sender.flush()
                            }
                    }

                })
                commands.add(object : AbstractCommand(
                    owner = LPMiraiBootstrap,
                    primaryName = "lpcheck",
                    secondaryNames = arrayOf(),
                    description = "LuckPerms - Checker",
                    // permission = CommandPermission.Any,
                    prefixOptional = false
                ) {
                    override val usage: String
                        get() = ""
                    override val permission: Permission = LPPermissionService.register(
                        PermissionId("luckperms", "lpcheck"), "", Magic_NO_PERMISSION_CHECK
                    )

                    override suspend fun CommandSender.onCommand(args: MessageChain) {
                        val perm = args.contentToString().trim()
                        val id = permitteeId
                        val data = id.uuid()
                        val usr = LPMiraiPlugin.userManager.getOrMake(data)

                        val options = LPMiraiPlugin.contextManager.getQueryOptions(id)
                        sendMessage(
                            "$perm -> " + hasPermission(perm) + '\n' +
                                    options.context().toString() + '\n' +
                                    usr.cachedData.getPermissionData(options).permissionMap.toString()
                        )

                    }
                })

                if (BuiltInCommands.PermissionCommand.unregister()) {
                    object : AbstractCommand(
                        owner = LPMiraiBootstrap,
                        primaryName = BuiltInCommands.PermissionCommand.primaryName,
                        secondaryNames = BuiltInCommands.PermissionCommand.secondaryNames,
                        prefixOptional = BuiltInCommands.PermissionCommand.prefixOptional,
                        description = BuiltInCommands.PermissionCommand.description
                    ) {
                        override val usage: String
                            get() = BuiltInCommands.PermissionCommand.usage

                        override suspend fun CommandSender.onCommand(args: MessageChain) {
                            sendMessage(
                                "" +
                                        "Please use /lp to manage permissions.\n" +
                                        "请使用 /lp 管理权限."
                            )
                        }

                        override val permission: Permission
                            get() = BuiltInCommands.PermissionCommand.permission
                    }.register(true)
                }
                commands.forEach { it.register(override = true) }
            }
        }
    }

    private lateinit var connectionListener0: AbstractConnectionListener
    override fun getConnectionListener(): AbstractConnectionListener = connectionListener0

    override fun getQueryOptionsForUser(user: User): Optional<QueryOptions> {
        commandCaller.get()?.takeIf { it.isUser() }?.let { contract ->
            val uid = user.uniqueId
            if (uid.mostSignificantBits == MAGIC_UUID_HIGH_BITS) {
                val qid = uid.leastSignificantBits
                when (val subject = contract.subject) {
                    is net.mamoe.mirai.contact.Group -> {
                        subject.getOrNull(qid)?.let { member ->
                            return Optional.of(contextManager0.getQueryOptions(member.permitteeId))
                        }
                    }
                    is Friend -> {
                        subject.bot.getFriendOrNull(qid)?.let { friend ->
                            return Optional.of(contextManager0.getQueryOptions(friend.permitteeId))
                        }
                    }
                }
            }
        }
        return Optional.empty()

    }

    override fun getOnlineSenders(): Stream<Sender> {
        val consoleX = Stream.of(console)
        // MAGIC_UUID_HIGH_BITS
        commandCaller.get()?.takeIf { it.isUser() }?.let { contract ->
            val subject = contract.subject!!
            if (subject is net.mamoe.mirai.contact.Group) {
                return Stream.concat(consoleX,
                    Stream.concat(subject.members.stream(), Stream.of(subject.botAsMember)).map {
                        val mcs = it.asMemberCommandSender()
                        WrappedLPSender(senderFactory0.wrap(mcs), mcs)
                    }
                )
            }
        }
        return consoleX
    }

    private val console by lazy {
        senderFactory0.wrap(ConsoleCommandSender)
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

    @OptIn(ConsoleExperimentalApi::class)
    override fun setupContextManager() {
        contextManager0 = MiraiContextManager().apply {
            registerCalculator(MiraiCalculator)
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