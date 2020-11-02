/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.test-plugin.main/OpenLPTest.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.test

import com.google.auto.service.AutoService
import io.github.karlatemp.luckperms.mirai.LPMiraiPlugin
import io.github.karlatemp.luckperms.mirai.util.hasPermission
import net.mamoe.mirai.console.command.AbstractCommand
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.permission.ExperimentalPermission
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.message.data.MessageChain
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@AutoService(JvmPlugin::class)
object OpenLPTest : KotlinPlugin(
    JvmPluginDescriptionBuilder(
        name = "LuckPerms Mirai OpenTest",
        version = "1.0.0", // tester un-need version.
    )
        .id("luckperms.luckperms.tester")
        // .dependsOn("io.github.karlatemp.luckperms-mirai", null as String?, false)
        .build()
// dependencies = listOf(
//            PluginDependency(
//                name = "LuckPerms"
//            )
//        )
) {
    @OptIn(ExperimentalPermission::class)
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    override fun onEnable() {
        object : AbstractCommand(
            owner = this,
            names = arrayOf("lpp"),
            parentPermission = PermissionService.INSTANCE[PermissionId("", "")]!!,
            prefixOptional = true
        ) {
            override val usage: String
                get() = ""

            override suspend fun CommandSender.onCommand(args: MessageChain) {
                // Mirai-Console M4-dev-5
                val perm = args.contentToString()
                sendMessage("$perm -> " + (this hasPermission perm))
            }

        }.register(true)
        invokeCatching {
            println(io.github.karlatemp.luckperms.mirai.internal.Magic_NO_PERMISSION_CHECK)
        }
        invokeCatching {
//            LPMiraiPlugin.senderFactory0.wrap(
//                ConsoleCommandSender.INSTANCE
//            ).sendMessage("§cHello, Open test!")
        }
    }


    @Retention(AnnotationRetention.SOURCE)
    @DslMarker
    private annotation class InvokeTest

    @OptIn(ExperimentalContracts::class)
    @InvokeTest
    private fun invokeCatching(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        runCatching(block).onFailure { logger.debug(it) }
    }
}