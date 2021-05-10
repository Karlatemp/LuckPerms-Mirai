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

import io.github.karlatemp.luckperms.mirai.LPMConfigs
import io.github.karlatemp.luckperms.mirai.LPMiraiPlugin
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

    @Suppress("ObjectPropertyName")
    private val _contact
        get() = if (LPMiraiPlugin.configuration[LPMConfigs.FIX_CONTEXT_TYPO]) {
            "contact"
        } else {
            "contract"
        }

    override fun estimatePotentialContexts(): ContextSet = estimatePotentialContexts(
        _contact
    )

    private fun estimatePotentialContexts(contact: String): ContextSet = ImmutableContextSet.builder()
        .add(contact, "user")
        .add(contact, "group")
        //.add("contact", "console")

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
        val contact = _contact
        when (target) {
            is AbstractPermitteeId -> {
                when (target) {
                    is AbstractPermitteeId.ExactUser -> {
                        consumer.accept(contact, "user")
                    }
                    is AbstractPermitteeId.ExactGroup -> {
                        consumer.accept(contact, "group")
                        consumer.accept("type", "group")
                        consumer.accept("group", target.groupId.toString())
                    }
                    is AbstractPermitteeId.AnyMember -> {
                        consumer.accept(contact, "user")
                        consumer.accept("group", target.groupId.toString())
                        consumer.accept("type", "group")
                    }
                    is AbstractPermitteeId.ExactMember -> {
                        consumer.accept(contact, "user")
                        consumer.accept("type", "group")
                        consumer.accept("group", target.groupId.toString())
                        scanMember(target.groupId, target.memberId)?.let { member ->
                            consumer.accept("level", member.permission.name.toLowerCase())
                            consumer.accept("admin", member.permission.isOperator().toString())
                        }
                    }
                    is AbstractPermitteeId.ExactFriend -> {
                        consumer.accept("type", "friend")
                        consumer.accept(contact, "user")
                    }
                    is AbstractPermitteeId.ExactStranger -> {
                        consumer.accept("type", "stranger")
                        consumer.accept(contact, "user")
                    }
                    is AbstractPermitteeId.ExactGroupTemp -> {
                        consumer.accept(contact, "user")
                        consumer.accept("type", "temp")
                        consumer.accept("group", target.groupId.toString())
                        scanMember(target.groupId, target.memberId)?.let { member ->
                            consumer.accept("level", member.permission.name.toLowerCase())
                            consumer.accept("admin", member.permission.isOperator().toString())
                        }
                    }
                    AbstractPermitteeId.AnyGroup -> {
                        consumer.accept(contact, "group")
                    }
                    AbstractPermitteeId.AnyMemberFromAnyGroup -> {
                        consumer.accept("mode", "group")
                        consumer.accept(contact, "user")
                    }
                    AbstractPermitteeId.AnyStranger -> {
                        consumer.accept("mode", "stranger")
                        consumer.accept(contact, "user")
                    }
                    AbstractPermitteeId.AnyFriend -> {
                        consumer.accept("mode", "friend")
                        consumer.accept(contact, "user")
                    }
                    is AbstractPermitteeId.AnyGroupTemp -> {
                        consumer.accept(contact, "user")
                        consumer.accept("mode", "temp")
                        consumer.accept("group", target.groupId.toString())
                    }
                    AbstractPermitteeId.AnyTempFromAnyGroup -> {
                        consumer.accept("mode", "temp")
                        consumer.accept(contact, "user")
                    }
                    AbstractPermitteeId.AnyUser -> {
                        consumer.accept("mode", "user")
                        consumer.accept(contact, "user")
                    }
                    AbstractPermitteeId.AnyContact -> {
                    }
                    AbstractPermitteeId.Console -> {
                        consumer.accept("type", "console")
                        consumer.accept(contact, "console")
                    }
                }
            }
        }
    }
}