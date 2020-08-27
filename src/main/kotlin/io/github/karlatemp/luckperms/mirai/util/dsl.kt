package io.github.karlatemp.luckperms.mirai.util

import io.github.karlatemp.luckperms.mirai.LPMiraiBootstrap
import io.github.karlatemp.luckperms.mirai.LPMiraiPlugin
import io.github.karlatemp.luckperms.mirai.MiraiSenderFactory
import io.github.karlatemp.luckperms.mirai.WrappedCommandSender
import net.luckperms.api.util.Tristate
import net.mamoe.mirai.console.command.CommandPermission.Any.hasPermission
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI

infix fun CommandSender.hasPermission(
    permission: String
): Boolean {
    @OptIn(ConsoleExperimentalAPI::class)
    if (this is UserCommandSender) {
        val wrapped = WrappedCommandSender(this)
        return MiraiSenderFactory.getPermissionValue0(wrapped, "permission") == Tristate.TRUE
    }
    return true
}