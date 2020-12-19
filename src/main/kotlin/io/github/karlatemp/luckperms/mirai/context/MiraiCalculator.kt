/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/MiraiCalculator.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.context

import net.luckperms.api.context.ContextCalculator
import net.luckperms.api.context.ContextConsumer
import net.luckperms.api.context.ContextSet
import net.luckperms.api.context.ImmutableContextSet
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermitteeId
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator

object MiraiCalculator : ContextCalculator<PermitteeId> {
    private fun scanMember(group: Long, member: Long): Member? {
        Bot.instancesSequence.forEach { bot ->
            bot.getGroup(group)?.get(member)?.let { return it }
        }
        return null
    }

    override fun estimatePotentialContexts(): ContextSet = ImmutableContextSet.builder()
        .add("contract", "user")
        .add("contract", "group")
        //.add("contract", "console")

        .add("type", "user")
        .add("type", "friend")
        .add("type", "group")
        //.add("type", "console")

        // Group
        .add("level", "member")
        .add("level", "administrator")
        .add("level", "owner")

        .add("admin", "true")
        .add("admin", "false")

        .build()

    override fun calculate(target: PermitteeId, consumer: ContextConsumer) {
        when (target) {
            is AbstractPermitteeId -> {
                when (target) {
                    is AbstractPermitteeId.ExactUser -> {
                        consumer.accept("contract", "user")
                    }
                    is AbstractPermitteeId.ExactGroup -> {
                        consumer.accept("contract", "group")
                        consumer.accept("type", "group")
                        consumer.accept("group", target.groupId.toString())
                    }
                    is AbstractPermitteeId.AnyMember -> {
                        consumer.accept("contract", "user")
                        consumer.accept("group", target.groupId.toString())
                        consumer.accept("type", "group")
                    }
                    is AbstractPermitteeId.ExactMember -> {
                        consumer.accept("contract", "user")
                        consumer.accept("type", "group")
                        consumer.accept("group", target.groupId.toString())
                        scanMember(target.groupId, target.memberId)?.let { member ->
                            consumer.accept("level", member.permission.name.toLowerCase())
                            consumer.accept("admin", member.permission.isOperator().toString())
                        }
                    }
                    is AbstractPermitteeId.ExactFriend -> {
                        consumer.accept("type", "friend")
                        consumer.accept("contract", "user")
                    }
                    is AbstractPermitteeId.ExactTemp -> {
                        consumer.accept("contract", "user")
                        consumer.accept("type", "temp")
                        consumer.accept("group", target.groupId.toString())
                        scanMember(target.groupId, target.memberId)?.let { member ->
                            consumer.accept("level", member.permission.name.toLowerCase())
                            consumer.accept("admin", member.permission.isOperator().toString())
                        }
                    }
                    AbstractPermitteeId.AnyGroup -> {
                        consumer.accept("contract", "group")
                    }
                    AbstractPermitteeId.AnyMemberFromAnyGroup -> {
                        consumer.accept("mode", "group")
                        consumer.accept("contract", "user")
                    }
                    AbstractPermitteeId.AnyFriend -> {
                        consumer.accept("mode", "friend")
                        consumer.accept("contract", "user")
                    }
                    is AbstractPermitteeId.AnyTemp -> {
                        consumer.accept("contract", "user")
                        consumer.accept("mode", "temp")
                        consumer.accept("group", target.groupId.toString())
                    }
                    AbstractPermitteeId.AnyTempFromAnyGroup -> {
                        consumer.accept("mode", "temp")
                        consumer.accept("contract", "user")
                    }
                    AbstractPermitteeId.AnyUser -> {
                        consumer.accept("mode", "user")
                        consumer.accept("contract", "user")
                    }
                    AbstractPermitteeId.AnyContact -> {
                    }
                    AbstractPermitteeId.Console -> {
                        consumer.accept("type", "console")
                        consumer.accept("contract", "console")
                    }
                }
            }
        }
    }
}