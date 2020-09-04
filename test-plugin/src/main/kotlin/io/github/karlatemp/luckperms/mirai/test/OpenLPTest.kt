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
import io.github.karlatemp.luckperms.mirai.util.hasPermission
import net.mamoe.mirai.console.command.AbstractCommand
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandPermission
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.plugin.description.PluginDependency
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.plugin.jvm.SimpleJvmPluginDescription

@AutoService(JvmPlugin::class)
object OpenLPTest : KotlinPlugin(
    SimpleJvmPluginDescription(
        "LuckPerms Mirai OpenTest",
        "1.0.0", // tester un-need version.
        dependencies = listOf(
            PluginDependency(
                name = "LuckPerms"
            )
        )
    )
) {
    override fun onEnable() {
        object : AbstractCommand(
            owner = this,
            names = arrayOf("lpp"),
            permission = CommandPermission.Any,
            prefixOptional = true
        ) {
            override val usage: String
                get() = ""

            override suspend fun CommandSender.onCommand(args: Array<out Any>) {
                val perm = args.joinToString(
                    separator = " ", prefix = "", postfix = ""
                ) {
                    it.toString()
                }
                sendMessage("$perm -> " + (this hasPermission ""))
            }
        }.register()
    }
}