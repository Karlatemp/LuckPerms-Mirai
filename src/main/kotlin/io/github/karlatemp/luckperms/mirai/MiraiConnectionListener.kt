/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/MiraiConnectionListener.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai

import io.github.karlatemp.luckperms.mirai.logging.DebugKit
import me.lucko.luckperms.common.plugin.util.AbstractConnectionListener
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.At
import java.util.*

class MiraiConnectionListener : AbstractConnectionListener(LPMiraiPlugin) {
    companion object {
        lateinit var instance: MiraiConnectionListener
    }

    init {
        instance = this
    }

    fun recUsr(id: Long) {
        val uid = UUID(MAGIC_UUID_HIGH_BITS, id)
        DebugKit.log { "Checking user object for $id..." }
        val loaded = LPMiraiPlugin.apiProvider.userManager.getUser(uid)
        if (loaded == null) {
            DebugKit.log { "User object of $id not create. Creating..." }
            loadUser(uid, id.toString())
            recordConnection(uid)
        }
    }

    fun registerListeners() {
        LPMiraiBootstrap.subscribeAlways<MessageEvent>(
            priority = Listener.EventPriority.HIGHEST
        ) {
            recUsr(sender.id)
            message.forEach { elm ->
                if (elm is At) {
                    recUsr(elm.target)
                }
            }
        }
    }
}
