/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/MiraiSchedulerAdapter.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai

import me.lucko.luckperms.common.plugin.bootstrap.LuckPermsBootstrap
import me.lucko.luckperms.common.plugin.scheduler.AbstractJavaScheduler
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class MiraiSchedulerAdapter(bootstrap: LuckPermsBootstrap?) : AbstractJavaScheduler(bootstrap) {
    private val service = Executors.newSingleThreadExecutor {
        Thread(it, "Mirai LuckPerms Sync")
    }

    override fun sync(): Executor = service

    override fun shutdownScheduler() {
        super.shutdownScheduler()
        service.shutdown()
        try {
            service.awaitTermination(1, TimeUnit.MINUTES)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}