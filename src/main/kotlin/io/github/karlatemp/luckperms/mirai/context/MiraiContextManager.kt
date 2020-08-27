/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/MiraiContextManager.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.context

import com.github.benmanes.caffeine.cache.LoadingCache
import io.github.karlatemp.luckperms.mirai.LPMiraiPlugin
import io.github.karlatemp.luckperms.mirai.MAGIC_UUID_HIGH_BITS
import io.github.karlatemp.luckperms.mirai.WrappedCommandSender
import me.lucko.luckperms.common.context.ContextManager
import me.lucko.luckperms.common.context.QueryOptionsSupplier
import me.lucko.luckperms.common.sender.Sender
import me.lucko.luckperms.common.util.CaffeineFactory
import net.luckperms.api.context.ImmutableContextSet
import net.luckperms.api.query.QueryOptions
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import java.util.*
import java.util.concurrent.TimeUnit

class MiraiContextManager : ContextManager<CommandSender, CommandSender>(
    LPMiraiPlugin, CommandSender::class.java, CommandSender::class.java
) {

    private val contextsCache = CaffeineFactory.newBuilder()
        .expireAfterWrite(50, TimeUnit.MILLISECONDS)
        .build { subject: CommandSender ->
            calculate(subject)
        }

    override fun getUniqueId(sender: CommandSender?): UUID {
        return if (sender is WrappedCommandSender)
            sender.uuid
        else Sender.CONSOLE_UUID
    }

    override fun getCacheFor(subject: CommandSender): QueryOptionsSupplier {
        return InlineQueryOptionsSupplier(subject, this.contextsCache)
    }

    override fun formQueryOptions(subject: CommandSender?, contextSet: ImmutableContextSet?): QueryOptions {
        return formQueryOptions(contextSet);
    }

    override fun getContext(subject: CommandSender): ImmutableContextSet {
        return getQueryOptions(subject).context()
    }

    override fun invalidateCache(subject: CommandSender?) {
        contextsCache.invalidate(subject ?: return)
    }

    override fun getQueryOptions(subject: CommandSender): QueryOptions {
        return this.contextsCache.get(subject)!!
    }

    private class InlineQueryOptionsSupplier(
        private val sender: CommandSender,
        private val cache: LoadingCache<CommandSender, QueryOptions>
    ) : QueryOptionsSupplier {
        override fun getQueryOptions(): QueryOptions = cache[sender]!!
    }
}