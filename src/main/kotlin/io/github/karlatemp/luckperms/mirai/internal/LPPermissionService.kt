/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/LPPermissionService.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

@file:OptIn(ConsoleExperimentalApi::class)
@file:Suppress("ClassName")

package io.github.karlatemp.luckperms.mirai.internal

import io.github.karlatemp.luckperms.mirai.*
import io.github.karlatemp.luckperms.mirai.commands.EmergencyOptions
import io.github.karlatemp.luckperms.mirai.logging.DebugKit
import io.github.karlatemp.luckperms.mirai.openapi.CustomPermitteeId
import io.github.karlatemp.luckperms.mirai.util.InspectPermissionProcessor
import me.lucko.luckperms.common.cacheddata.type.PermissionCache
import me.lucko.luckperms.common.calculator.result.TristateResult
import me.lucko.luckperms.common.verbose.VerboseCheckTarget
import me.lucko.luckperms.common.verbose.event.PermissionCheckEvent
import net.luckperms.api.query.QueryOptions
import net.luckperms.api.util.Tristate
import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import net.mamoe.mirai.console.permission.*
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

internal object LPPP : PermissionServiceProvider {
    override val instance: PermissionService<*>
        get() = LPPermissionService

}

internal open class LuckPermsPermission(
    override val parent: Permission,
    override val description: String,
    override val id: PermissionId,
    val internalId: String,
    val parentPermission: LuckPermsPermission?
) : Permission {
    override fun toString(): String {
        return "<$internalId>"
    }
}

internal object TempPermissionInitPlaceholder : Permission {
    override val description: String
        get() = "TEMP INITIALIZE PLACEHOLDER"
    override val id: PermissionId
        get() = PermissionId("<lp>", "##TEMP INIT PLACEHOLDER##")
    override val parent: Permission
        get() = this

    override fun toString(): String {
        return "TempPermissionInitPlaceholder"
    }
}

private val AnyID = PermissionId("<lp>", "%")

internal object Magic_NO_PERMISSION_CHECK : LuckPermsPermission(
    ROOT, "", AnyID, ".", null
) {
    override fun toString(): String {
        return "Magic[No Permission Check]"
    }
}

internal val ROOT_IND = PermissionId("*", "*")

internal object ROOT : LuckPermsPermission(
    TempPermissionInitPlaceholder, "The root of permissions.", ROOT_IND, "*", null
) {
    override val parent: Permission
        get() = this

    override fun toString(): String {
        return "Magic[Root]"
    }
}

internal object Magic_NO_REGISTER_CHECK : LuckPermsPermission(
    ROOT, "Magic code of LuckPerms Mirai",
    PermissionId("<lp>", "#"),
    "<lp>.#", null
) {
    override fun toString(): String {
        return "Magic[No Register Check]"
    }
}

internal object LPPermissionService : PermissionService<LuckPermsPermission> {
    private val permissions = ConcurrentHashMap<String, LuckPermsPermission>()

    init {
        // PermissionServiceProvider.registerExtension(LPMiraiBootstrap, LPPP)
        permissions["."] = Magic_NO_PERMISSION_CHECK
        permissions[""] = Magic_NO_PERMISSION_CHECK
        permissions["*"] = ROOT
        permissions["<lp>.#"] = Magic_NO_REGISTER_CHECK
        permissions["<lp>.%"] = Magic_NO_PERMISSION_CHECK
        permissions["<lp>.<void>"] = Magic_NO_PERMISSION_CHECK
    }


    override val permissionType: KClass<LuckPermsPermission> = LuckPermsPermission::class

    private fun PermissionId.lp(): String {
        val id = this.name
        val namespace = this.namespace
        if (id.isEmpty()) return namespace
        if (namespace == "*" && id == "*") return "*"
        return "$namespace.$id"
    }

    override fun get(id: PermissionId): LuckPermsPermission? = permissions[id.lp()]

    override fun getRegisteredPermissions(): Sequence<LuckPermsPermission> {
        return Sequence { permissions.values.iterator() }
    }

    override fun getPermittedPermissions(permitteeId: PermitteeId): Sequence<LuckPermsPermission> {
        return emptySequence()
    }

    override val rootPermission: LuckPermsPermission
        get() = ROOT

    override fun register(id: PermissionId, description: String, parent: Permission): LuckPermsPermission {
        val internalId = id.lp()
        if (internalId == "*") {
            return ROOT
        }
        val base = parent as LuckPermsPermission
        val perm = LuckPermsPermission(
            base, description, id, internalId, when (base) {
                Magic_NO_REGISTER_CHECK -> null
                else -> base
            }
        )
        // Magic code
        if (parent === Magic_NO_REGISTER_CHECK) return perm
        val old = permissions.putIfAbsent(internalId, perm)
        if (old !== null) {
            permissions[internalId] = old
            throw PermissionRegistryConflictException(perm, old)
        }
        LPMiraiPlugin.permissionRegistry.offer(internalId)
        return perm
    }

    fun PermitteeId.uuid(): UUID {
        return when (val identifier = this) {
            is AbstractPermitteeId -> {
                when (identifier) {
                    is AbstractPermitteeId.AnyContact -> {
                        UUID_ANY_CONTEXT_SELECTOR
                    }
                    is AbstractPermitteeId.AnyFriend -> {
                        UUID_ANY_MEMBER_SELECTOR
                    }
                    is AbstractPermitteeId.AnyGroupTemp -> {
                        UUID_ANY_MEMBER_SELECTOR
                    }
                    is AbstractPermitteeId.AnyUser -> {
                        UUID_ANY_MEMBER_SELECTOR
                    }
                    is AbstractPermitteeId.AnyMember -> {
                        UUID_ANY_MEMBER_SELECTOR
                    }
                    is AbstractPermitteeId.AnyMemberFromAnyGroup -> {
                        UUID_ANY_MEMBER_SELECTOR
                    }
                    is AbstractPermitteeId.Console -> {
                        UUID_CONSOLE
                    }
                    is AbstractPermitteeId.ExactFriend -> {
                        UUID(MAGIC_UUID_HIGH_BITS, identifier.id.also {
                            MiraiConnectionListener.instance.recUsr(it)
                        })
                    }
                    is AbstractPermitteeId.AnyGroup -> {
                        // TODO
                        UUID_ANY_GROUP_SELECTOR
                    }
                    is AbstractPermitteeId.ExactGroup -> {
                        // TODO
                        UUID_ANY_GROUP_SELECTOR
                    }
                    is AbstractPermitteeId.ExactMember -> {
                        UUID(MAGIC_UUID_HIGH_BITS, identifier.memberId.also {
                            MiraiConnectionListener.instance.recUsr(it)
                        })
                    }
                    is AbstractPermitteeId.ExactGroupTemp -> {
                        UUID(MAGIC_UUID_HIGH_BITS, identifier.memberId.also {
                            MiraiConnectionListener.instance.recUsr(it)
                        })
                    }
                    is AbstractPermitteeId.ExactUser -> {
                        UUID(MAGIC_UUID_HIGH_BITS, identifier.id.also {
                            MiraiConnectionListener.instance.recUsr(it)
                        })
                    }
                    is AbstractPermitteeId.ExactStranger -> {
                        UUID(MAGIC_UUID_HIGH_BITS, identifier.id.also {
                            MiraiConnectionListener.instance.recUsr(it)
                        })
                    }
                    AbstractPermitteeId.AnyTempFromAnyGroup -> UUID_ANY_MEMBER_SELECTOR
                    AbstractPermitteeId.AnyStranger -> UUID_ANY_MEMBER_SELECTOR
                    AbstractPermitteeId.AnyOtherClient -> UUID_OTHER_CLIENT
                    else -> throw AssertionError("Assertion Error: $identifier, ${identifier.javaClass}")
                }
            }
            is CustomPermitteeId -> identifier.uuid
            else -> {
                TODO()
            }
        }
    }

    private fun String.logConsole() {
        LPMiraiPlugin.verboseHandler.offerPermissionCheckEvent(
            PermissionCheckEvent.Origin.PLATFORM_PERMISSION_CHECK, VerboseCheckTarget.internal("console"),
            QueryOptions.nonContextual(), this, TristateResult.of(Tristate.TRUE)
        )
        LPMiraiPlugin.permissionRegistry.offer(this)
    }

    override fun testPermission(
        permitteeId: PermitteeId,
        permission: LuckPermsPermission
    ): Boolean {
        DebugKit.log { "Testing permission ${permission.internalId} with $permitteeId" }

        when (permission) {
            Magic_NO_PERMISSION_CHECK -> return true
            Magic_NO_REGISTER_CHECK -> {
                LPMiraiPlugin.logger.warn("Warning: Magic[No Register Check](<lp>:#) should is not checkable.")
                return false
            }
        }
        if (permitteeId is AbstractPermitteeId.Console) {
            permission.internalId.logConsole()
            return true
        }
        if (EmergencyOptions.shutdown) {
            LPMiraiPlugin.verboseHandler.offerPermissionCheckEvent(
                PermissionCheckEvent.Origin.PLATFORM_PERMISSION_CHECK, VerboseCheckTarget.of("mirai", permitteeId.asString()),
                QueryOptions.nonContextual(), permission.internalId, InspectPermissionProcessor.shutdown
            )
            return false
        }
        val user = permitteeId.uuid()
        val usr = LPMiraiPlugin.userManager.getIfLoaded(user) ?: kotlin.run {
            LPMiraiBootstrap.logger.error("WARMING: User $user{lnum=${user.leastSignificantBits}} not loaded. Please report to https://github.com/Karlatemp/LuckPerms-Mirai")
            return false
        }

        val permissionData = usr.cachedData.getPermissionData(
            LPMiraiPlugin.contextManager.getQueryOptions(permitteeId)
        )
        DebugKit.log { "UUID $user with lnum ${user.leastSignificantBits}, permissions = ${permissionData.permissionMap}" }
        return testPermission(permission, permissionData, permission)
                || DebugKit.isTrusted(permitteeId)
    }

    private fun testPermission(
        startPermission: LuckPermsPermission,
        permissionData: PermissionCache,
        permission: LuckPermsPermission
    ): Boolean {
        if (permission === Magic_NO_PERMISSION_CHECK) return true
        // It should be discarded at #register
        if (permission === Magic_NO_REGISTER_CHECK) {
            LPMiraiPlugin.logger.severe("Error: Oops. LuckPerms Mirai got a logic error.")
            LPMiraiPlugin.logger.severe("Please report it to https://github.com/Karlatemp/LuckPerms-Mirai")
            // gen report
            var p: LuckPermsPermission? = startPermission
            while (p != null) {
                val pw = p
                p = pw.parentPermission
                LPMiraiPlugin.logger.severe("  - $pw, lp-id = ${pw.internalId}, mc-id = ${pw.id}, desc = ${pw.description}")
            }
            return false
        }
        LPMiraiPlugin.permissionRegistry.offer(permission.internalId)
        val perm =
            permissionData.checkPermission(permission.internalId, PermissionCheckEvent.Origin.THIRD_PARTY_API)
        DebugKit.log { "Testing ${permission.internalId} -> ${perm.result()}" }
        if (perm.result() == Tristate.UNDEFINED) {
            val pp = permission.parentPermission
            if (pp != null) {
                return testPermission(startPermission, permissionData, pp)
            }
        }
        return perm.result().asBoolean()
    }

    override fun cancel(permitteeId: PermitteeId, permission: LuckPermsPermission, recursive: Boolean) {
        if (permitteeId is AbstractPermitteeId.Console)
            return
        // TODO Group Permissions
        throw UnsupportedOperationException("Only allowed CLI or Direct Cancel")
    }

    override fun permit(permitteeId: PermitteeId, permission: LuckPermsPermission) {
        if (permitteeId is AbstractPermitteeId.Console)
            return
        // TODO Group Permissions
        throw UnsupportedOperationException("Only allowed CLI or Direct Permit")
    }
}