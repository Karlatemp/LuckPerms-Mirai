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
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.info
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

    internal fun loadInternalUsers() {
        fun rec(uid: UUID, name: String) {
            // UUID_ANY_MEMBER_SELECTOR
            val loaded = LPMiraiPlugin.userManager.getIfLoaded(uid)
            if (loaded == null) {
                loadUser(uid, name)
                recordConnection(uid)
                LPMiraiBootstrap.logger.debug { "Registered $name with $uid" }
            }
        }
        rec(UUID_ANY_MEMBER_SELECTOR, "MemberSelector")
        rec(UUID_ANY_GROUP_SELECTOR, "GroupSelector")
        rec(UUID_ANY_CONTEXT_SELECTOR, "ContextSelector")
        rec(UUID_OTHER_CLIENT, "OtherClient")
    }

    fun registerListeners() {
        LPMiraiBootstrap.globalEventChannel().subscribeAlways<MessageEvent>(
            priority = EventPriority.HIGHEST
        ) {
            loadInternalUsers()
            recUsr(sender.id)
            message.forEach { elm ->
                if (elm is At) {
                    recUsr(elm.target)
                }
            }
        }
    }
}
