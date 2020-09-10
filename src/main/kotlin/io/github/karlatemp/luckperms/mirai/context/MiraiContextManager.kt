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
import net.mamoe.mirai.console.permission.Permissible
import net.mamoe.mirai.console.permission.PermissibleIdentifier
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPermission::class)
class MiraiContextManager : ContextManager<PermissibleIdentifier, PermissibleIdentifier>(
    LPMiraiPlugin, PermissibleIdentifier::class.java, PermissibleIdentifier::class.java
) {
    private val contextsCache = CaffeineFactory.newBuilder()
        .expireAfterWrite(50, TimeUnit.MILLISECONDS)
        .build { subject: PermissibleIdentifier ->
            calculate(subject)
        }

    override fun getUniqueId(sender: PermissibleIdentifier): UUID {
        return sender.uuid()
    }

    override fun getCacheFor(subject: PermissibleIdentifier): QueryOptionsSupplier {
        return InlineQueryOptionsSupplier(subject, this.contextsCache)
    }

    override fun formQueryOptions(subject: PermissibleIdentifier?, contextSet: ImmutableContextSet?): QueryOptions {
        return formQueryOptions(contextSet)
    }

    override fun getContext(subject: PermissibleIdentifier): ImmutableContextSet {
        return getQueryOptions(subject).context()
    }


    override fun invalidateCache(subject: PermissibleIdentifier?) {
        contextsCache.invalidate(subject ?: return)
    }

    override fun getQueryOptions(subject: PermissibleIdentifier): QueryOptions {
        return contextsCache[subject]!!
    }

    private class InlineQueryOptionsSupplier(
        private val sender: PermissibleIdentifier,
        private val cache: LoadingCache<PermissibleIdentifier, QueryOptions>
    ) : QueryOptionsSupplier {
        override fun getQueryOptions(): QueryOptions = cache[sender]!!
    }
}