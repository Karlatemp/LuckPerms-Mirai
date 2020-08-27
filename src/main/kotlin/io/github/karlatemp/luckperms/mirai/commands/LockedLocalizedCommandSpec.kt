package io.github.karlatemp.luckperms.mirai.commands

import me.lucko.luckperms.common.locale.LocaleManager
import me.lucko.luckperms.common.locale.command.Argument
import me.lucko.luckperms.common.locale.command.LocalizedCommandSpec
import java.util.*

class LockedLocalizedCommandSpec(
    localeManager: LocaleManager,
    private val desc: String,
    private val usage: String
) : LocalizedCommandSpec(null, localeManager) {
    override fun description(): String {
        return desc
    }

    override fun args(): MutableList<Argument> {
        return Collections.emptyList()
    }


    override fun usage(): String {
        return usage
    }
}