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

import me.lucko.luckperms.common.command.access.CommandPermission
import me.lucko.luckperms.common.sender.Sender
import me.lucko.luckperms.common.util.TextUtils
import net.kyori.text.Component
import net.mamoe.mirai.console.command.CommandSender
import java.util.concurrent.atomic.AtomicBoolean

class WrappedLPSender(
    val delegate: Sender,
    val real: CommandSender
) : Sender {
    override fun getPlugin() = delegate.plugin

    override fun getName() = delegate.name

    override fun getNameWithLocation() = delegate.nameWithLocation

    override fun getUniqueId() = delegate.uniqueId

    private val cachedMessages = ArrayList<String>(16)
    private val cache = AtomicBoolean(true)
    private val writingLock = AtomicBoolean()
    override fun sendMessage(message: String) {
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

    override fun sendMessage(message: Component?) {
        sendMessage(TextUtils.toLegacy(message ?: return))
    }

    override fun getPermissionValue(permission: String?) = delegate.getPermissionValue(permission)

    override fun hasPermission(permission: String?) = delegate.hasPermission(permission)

    override fun hasPermission(permission: CommandPermission?) = delegate.hasPermission(permission)

    override fun performCommand(commandLine: String?) = delegate.performCommand(commandLine)

    override fun isConsole() = delegate.isConsole

    override fun isValid() = delegate.isValid
    fun flush() {
        @Suppress("ControlFlowWithEmptyBody")
        while (writingLock.compareAndSet(false, true)) {
        }
        if (cache.get()) {
            cache.set(false)
            val msg = cachedMessages
            writingLock.set(false)
            if (msg.isNotEmpty()) {
                delegate.sendMessage(msg.joinToString("\n"))
            }
        } else {
            writingLock.set(false)
        }
    }
}