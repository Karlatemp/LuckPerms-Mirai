package io.github.karlatemp.luckperms.mirai

import me.lucko.luckperms.common.plugin.scheduler.AbstractJavaScheduler
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object MiraiSchedulerAdapter : AbstractJavaScheduler() {
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