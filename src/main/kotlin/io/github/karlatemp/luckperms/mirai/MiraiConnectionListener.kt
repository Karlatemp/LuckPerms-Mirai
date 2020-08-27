package io.github.karlatemp.luckperms.mirai

import me.lucko.luckperms.common.plugin.util.AbstractConnectionListener
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.At
import java.util.*
import kotlin.math.min

class MiraiConnectionListener : AbstractConnectionListener(LPMiraiPlugin) {
    fun registerListeners() {
        LPMiraiBootstrap.subscribeAlways<MessageEvent>(
            priority = Listener.EventPriority.HIGHEST
        ) {
            val uid = UUID(MAGIC_UUID_HIGH_BITS, sender.id)
            val loaded = LPMiraiPlugin.apiProvider.userManager.getUser(uid)
            if (loaded == null) {
                loadUser(uid, sender.id.toString())
                recordConnection(uid)
            }
            message.forEach { elm ->
                if (elm is At) {
                    val tuid = UUID(MAGIC_UUID_HIGH_BITS, elm.target)
                    val tusr = LPMiraiPlugin.apiProvider.userManager.getUser(uid)
                    if (tusr == null) {
                        loadUser(uid, tusr)
                        recordConnection(tuid)
                    }
                }
            }
        }
    }
}