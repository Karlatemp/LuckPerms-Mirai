/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai_main/MiraiCalculator.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.context

import net.luckperms.api.context.ContextCalculator
import net.luckperms.api.context.ContextConsumer
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermitteeId
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.getGroupOrNull

object MiraiCalculator : ContextCalculator<PermitteeId> {
    private fun scanMember(group: Long, member: Long): Member? {
        Bot.botInstancesSequence.forEach { bot ->
            bot.getGroupOrNull(group)?.getOrNull(member)?.let { return it }
        }
        return null
    }

    override fun calculate(target: PermitteeId, consumer: ContextConsumer) {
        when (target) {
            is AbstractPermitteeId -> {
                when (target) {
                    is AbstractPermitteeId.ExactUser -> {
                    }
                    is AbstractPermitteeId.ExactGroup -> {
                        consumer.accept("type", "group")
                        consumer.accept("group", target.groupId.toString())
                    }
                    is AbstractPermitteeId.AnyMember -> {
                        consumer.accept("group", target.groupId.toString())
                        consumer.accept("type", "member")
                    }
                    is AbstractPermitteeId.ExactMember -> {
                        consumer.accept("type", "group")
                        consumer.accept("group", target.groupId.toString())
                        scanMember(target.groupId, target.memberId)?.let { member ->
                            consumer.accept("level", member.permission.name.toLowerCase())
                            consumer.accept("admin", member.permission.isOperator().toString())
                        }
                    }
                    is AbstractPermitteeId.ExactFriend -> {
                        consumer.accept("type", "friend")
                    }
                    is AbstractPermitteeId.ExactTemp -> {
                        consumer.accept("type", "temp")
                        consumer.accept("group", target.groupId.toString())
                        scanMember(target.groupId, target.memberId)?.let { member ->
                            consumer.accept("level", member.permission.name.toLowerCase())
                            consumer.accept("admin", member.permission.isOperator().toString())
                        }
                    }
                    AbstractPermitteeId.AnyGroup -> {
                    }
                    AbstractPermitteeId.AnyMemberFromAnyGroup -> {
                        consumer.accept("mode", "group")
                    }
                    AbstractPermitteeId.AnyFriend -> {
                        consumer.accept("mode", "friend")
                    }
                    is AbstractPermitteeId.AnyTemp -> {
                        consumer.accept("mode", "temp")
                        consumer.accept("group", target.groupId.toString())
                    }
                    AbstractPermitteeId.AnyTempFromAnyGroup -> {
                        consumer.accept("mode", "temp")
                    }
                    AbstractPermitteeId.AnyUser -> {
                        consumer.accept("mode", "user")
                    }
                    AbstractPermitteeId.AnyContact -> {
                    }
                    AbstractPermitteeId.Console -> {
                        consumer.accept("type", "console")
                    }
                }
            }
        }
    }
}