/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/WrappedLPSender.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.commands

import io.github.karlatemp.luckperms.mirai.LPMiraiPlugin
import me.lucko.luckperms.common.command.access.CommandPermission
import me.lucko.luckperms.common.plugin.LuckPermsPlugin
import me.lucko.luckperms.common.sender.Sender
import net.kyori.adventure.text.Component
import net.luckperms.api.util.Tristate
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

open class WrappedLPSender(
    val delegate: Sender,
    val real: CommandSender
) : Sender {
    object WrappedConsole : WrappedLPSender(
        ConsoleSender,
        ConsoleCommandSender
    ) {
        override fun flush() {}
        override fun isConsole(): Boolean = true
        override fun sendMessage(message: Component?) {
            ConsoleSender.sendMessage(message ?: return)
        }
    }

    companion object {
        fun wrap(sender: CommandSender): WrappedLPSender = if (sender is ConsoleCommandSender) {
            WrappedConsole
        } else WrappedLPSender(LPMiraiPlugin.senderFactory0.wrap(sender), sender)

    }

    override fun getPlugin(): LuckPermsPlugin = delegate.plugin

    override fun getName(): String = delegate.name

    override fun getNameWithLocation(): String = delegate.nameWithLocation

    override fun getUniqueId(): UUID = delegate.uniqueId

    private val cachedMessages = ArrayList<Component>(16)
    private val cache = AtomicBoolean(true)
    private val writingLock = AtomicBoolean()

    override fun sendMessage(message: Component?) {
        message ?: return
        @Suppress("ControlFlowWithEmptyBody")
        while (writingLock.compareAndSet(false, true)) {
        }
        if (cache.get()) {
            cachedMessages.add(message)
            writingLock.set(false)
        } else {
            writingLock.set(false)
            delegate.sendMessage(message)
        }
    }

    override fun getPermissionValue(permission: String?): Tristate = delegate.getPermissionValue(permission)

    override fun hasPermission(permission: String?) = delegate.hasPermission(permission)

    override fun hasPermission(permission: CommandPermission?) = delegate.hasPermission(permission)

    override fun performCommand(commandLine: String?) = delegate.performCommand(commandLine)

    override fun isConsole() = delegate.isConsole

    override fun isValid() = delegate.isValid

    override fun flush() {
        @Suppress("ControlFlowWithEmptyBody")
        while (writingLock.compareAndSet(false, true)) {
        }
        if (cache.get()) {
            cache.set(false)
            val msg = cachedMessages
            writingLock.set(false)
            if (msg.isNotEmpty()) {
                delegate.sendMessage(Component.join({ Component.text('\n') }, msg))
            }
        } else {
            writingLock.set(false)
        }
    }
}