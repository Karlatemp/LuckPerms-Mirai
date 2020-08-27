# LuckPerms Mirai

经典权限管理系统

## Dev

判断使用者是否拥有某权限:
```kotlin
fun CommandSender.hasPermission(permission: String): Boolean
```

接入 Mirai-Console 的权限控制系统
```kotlin
val perm: CommandPermission = permission("my.permission")
```


