/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/ansi.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.util

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.Attribute
import java.util.*

fun interface ColorTranslator {
    fun translate(source: String): String
}

val colorTranslator: ColorTranslator = run {
    runCatching {
        val reset = Ansi.ansi().a(Attribute.RESET).toString()
        val replacements: Map<ChatColor, String> = EnumMap<ChatColor, String>(
            ChatColor::class.java
        ).apply {
            put(ChatColor.BLACK, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.BLACK).boldOff().toString())
            put(ChatColor.DARK_BLUE, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.BLUE).boldOff().toString())
            put(ChatColor.DARK_GREEN, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.GREEN).boldOff().toString())
            put(ChatColor.DARK_AQUA, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.CYAN).boldOff().toString())
            put(ChatColor.DARK_RED, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.RED).boldOff().toString())
            put(ChatColor.DARK_PURPLE, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.MAGENTA).boldOff().toString())
            put(ChatColor.GOLD, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.YELLOW).boldOff().toString())
            put(ChatColor.GRAY, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.WHITE).boldOff().toString())
            put(ChatColor.DARK_GRAY, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.BLACK).bold().toString())
            put(ChatColor.BLUE, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.BLUE).bold().toString())
            put(ChatColor.GREEN, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.GREEN).bold().toString())
            put(ChatColor.AQUA, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.CYAN).bold().toString())
            put(ChatColor.RED, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.RED).bold().toString())
            put(ChatColor.LIGHT_PURPLE, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.MAGENTA).bold().toString())
            put(ChatColor.YELLOW, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString())
            put(ChatColor.WHITE, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.WHITE).bold().toString())
            put(ChatColor.MAGIC, Ansi.ansi().a(Attribute.BLINK_SLOW).toString())
            put(ChatColor.BOLD, Ansi.ansi().a(Attribute.UNDERLINE_DOUBLE).toString())
            put(ChatColor.STRIKETHROUGH, Ansi.ansi().a(Attribute.STRIKETHROUGH_ON).toString())
            put(ChatColor.UNDERLINE, Ansi.ansi().a(Attribute.UNDERLINE).toString())
            put(ChatColor.ITALIC, Ansi.ansi().a(Attribute.ITALIC).toString())
            put(ChatColor.RESET, Ansi.ansi().a(Attribute.RESET).toString())
        }
        val regex = "\\u00a7.".toRegex()
        ColorTranslator {
            it.replace(regex) { result ->
                val color = ChatColor.getByChar(result.value[1])
                if (color == null) {
                    ""
                } else {
                    replacements[color] ?: ""
                }
            } + reset
        }
    }.getOrElse {
        ColorTranslator { ChatColor.stripColor(it) }
    }
}
