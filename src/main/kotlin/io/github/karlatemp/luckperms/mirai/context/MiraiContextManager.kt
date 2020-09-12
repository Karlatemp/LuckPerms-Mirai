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
import io.github.karlatemp.luckperms.mirai.internal.LPPermissionService.uuid
import me.lucko.luckperms.common.context.ContextManager
import me.lucko.luckperms.common.context.QueryOptionsSupplier
import me.lucko.luckperms.common.util.CaffeineFactory
import net.luckperms.api.context.ImmutableContextSet
import net.luckperms.api.query.QueryOptions
import net.mamoe.mirai.console.permission.ExperimentalPermission
import net.mamoe.mirai.console.permission.PermitteeId
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPermission::class)
class MiraiContextManager : ContextManager<PermitteeId, PermitteeId>(
    LPMiraiPlugin, PermitteeId::class.java, PermitteeId::class.java
) {
    private val contextsCache = CaffeineFactory.newBuilder()
        .expireAfterWrite(50, TimeUnit.MILLISECONDS)
        .build { subject: PermitteeId ->
            calculate(subject)
        }

    override fun getUniqueId(sender: PermitteeId): UUID {
        return sender.uuid()
    }

    override fun getCacheFor(subject: PermitteeId): QueryOptionsSupplier {
        return InlineQueryOptionsSupplier(subject, this.contextsCache)
    }

    override fun formQueryOptions(subject: PermitteeId?, contextSet: ImmutableContextSet?): QueryOptions {
        return formQueryOptions(contextSet)
    }

    override fun getContext(subject: PermitteeId): ImmutableContextSet {
        return getQueryOptions(subject).context()
    }


    override fun invalidateCache(subject: PermitteeId?) {
        contextsCache.invalidate(subject ?: return)
    }

    override fun getQueryOptions(subject: PermitteeId): QueryOptions {
        return contextsCache[subject]!!
    }

    private class InlineQueryOptionsSupplier(
        private val sender: PermitteeId,
        private val cache: LoadingCache<PermitteeId, QueryOptions>
    ) : QueryOptionsSupplier {
        override fun getQueryOptions(): QueryOptions = cache[sender]!!
    }
}