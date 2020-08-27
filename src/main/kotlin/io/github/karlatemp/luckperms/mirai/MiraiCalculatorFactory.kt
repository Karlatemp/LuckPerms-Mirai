/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/MiraiCalculatorFactory.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai

import com.google.common.collect.ImmutableList
import me.lucko.luckperms.common.cacheddata.CacheMetadata
import me.lucko.luckperms.common.calculator.CalculatorFactory
import me.lucko.luckperms.common.calculator.PermissionCalculator
import me.lucko.luckperms.common.calculator.processor.*
import me.lucko.luckperms.common.config.ConfigKeys
import net.luckperms.api.query.QueryOptions

object MiraiCalculatorFactory : CalculatorFactory {

    override fun build(queryOptions: QueryOptions?, metadata: CacheMetadata?): PermissionCalculator? {
        val processors = ImmutableList.builder<PermissionProcessor>()
        processors.add(MapProcessor())
        val plugin = LPMiraiPlugin
        if (plugin.getConfiguration().get<Boolean>(ConfigKeys.APPLYING_REGEX)) {
            processors.add(RegexProcessor())
        }
        if (plugin.getConfiguration().get<Boolean>(ConfigKeys.APPLYING_WILDCARDS)) {
            processors.add(WildcardProcessor())
        }
        if (plugin.getConfiguration().get<Boolean>(ConfigKeys.APPLYING_WILDCARDS_SPONGE)) {
            processors.add(SpongeWildcardProcessor())
        }
        return PermissionCalculator(plugin, metadata, processors.build())
    }
}