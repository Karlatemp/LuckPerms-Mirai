/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/WrappedCommandSender.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.command.GroupAwareCommandSender
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.Message
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext

//@JvmDefault

@OptIn(ConsoleExperimentalAPI::class)
class WrappedCommandSender(
    val parent: UserCommandSender
) : CommandSender {
    override val coroutineContext: CoroutineContext
        get() = parent.coroutineContext
    override val subject: Contact?
        get() = parent.subject
    override val user: User?
        get() = parent.user

    override suspend fun catchExecutionException(e: Throwable) {
        parent.catchExecutionException(e)
    }

    override val bot: Bot
        get() = parent.bot
    override val name: String
        get() = parent.name
    val group: Group? =
        (bot as? GroupAwareCommandSender)?.group
    val uuid = UUID(MAGIC_UUID_HIGH_BITS, parent.user.id)

    override fun hashCode(): Int {
        return (parent.user.id * parent.bot.id).toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is WrappedCommandSender) return false
        if (parent.bot.id != other.bot.id) return false
        if (group == null) {
            if (other.group != null) return false
        } else if (other.group == null) return false
        else if (other.group.id != group.id) return false
        return parent.user.id == other.parent.user.id
    }

    var bufferedMessages: ConcurrentLinkedQueue<String>? = ConcurrentLinkedQueue()
    override suspend fun sendMessage(message: Message): Nothing? {
        sendMessage(message.contentToString())
        return null
    }

    override suspend fun sendMessage(message: String): Nothing? {
        val m = bufferedMessages
        if (m == null) {
            parent.sendMessage(message)
        } else {
            m.add(message)
        }
        return null
    }
}