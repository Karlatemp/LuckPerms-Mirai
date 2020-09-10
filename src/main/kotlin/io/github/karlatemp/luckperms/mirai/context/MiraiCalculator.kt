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
import net.mamoe.mirai.console.permission.AbstractPermissibleIdentifier
import net.mamoe.mirai.console.permission.ExperimentalPermission
import net.mamoe.mirai.console.permission.PermissibleIdentifier
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.getGroupOrNull

@OptIn(ExperimentalPermission::class)
object MiraiCalculator : ContextCalculator<PermissibleIdentifier> {
    private fun scanMember(group: Long, member: Long): Member? {
        Bot.botInstancesSequence.forEach { bot ->
            bot.getGroupOrNull(group)?.getOrNull(member)?.let { return it }
        }
        return null
    }

    override fun calculate(target: PermissibleIdentifier, consumer: ContextConsumer) {
        when (target) {
            is AbstractPermissibleIdentifier -> {
                when (target) {
                    is AbstractPermissibleIdentifier.ExactUser -> {
                    }
                    is AbstractPermissibleIdentifier.ExactGroup -> {
                        consumer.accept("type", "group")
                        consumer.accept("group", target.groupId.toString())
                    }
                    is AbstractPermissibleIdentifier.AnyMember -> {
                        consumer.accept("group", target.groupId.toString())
                        consumer.accept("type", "member")
                    }
                    is AbstractPermissibleIdentifier.ExactMember -> {
                        consumer.accept("type", "group")
                        consumer.accept("group", target.groupId.toString())
                        scanMember(target.groupId, target.memberId)?.let { member ->
                            consumer.accept("level", member.permission.name.toLowerCase())
                            consumer.accept("admin", member.permission.isOperator().toString())
                        }
                    }
                    is AbstractPermissibleIdentifier.ExactFriend -> {
                        consumer.accept("type", "friend")
                    }
                    is AbstractPermissibleIdentifier.ExactTemp -> {
                        consumer.accept("type", "temp")
                        consumer.accept("group", target.groupId.toString())
                        scanMember(target.groupId, target.memberId)?.let { member ->
                            consumer.accept("level", member.permission.name.toLowerCase())
                            consumer.accept("admin", member.permission.isOperator().toString())
                        }
                    }
                    AbstractPermissibleIdentifier.AnyGroup -> {
                    }
                    AbstractPermissibleIdentifier.AnyMemberFromAnyGroup -> {
                        consumer.accept("mode", "group")
                    }
                    AbstractPermissibleIdentifier.AnyFriend -> {
                        consumer.accept("mode", "friend")
                    }
                    is AbstractPermissibleIdentifier.AnyTemp -> {
                        consumer.accept("mode", "temp")
                        consumer.accept("group", target.groupId.toString())
                    }
                    AbstractPermissibleIdentifier.AnyTempFromAnyGroup -> {
                        consumer.accept("mode", "temp")
                    }
                    AbstractPermissibleIdentifier.AnyUser -> {
                        consumer.accept("mode", "user")
                    }
                    AbstractPermissibleIdentifier.AnyContact -> {
                    }
                    AbstractPermissibleIdentifier.Console -> {
                        consumer.accept("type", "console")
                    }
                }
            }
        }
    }
}