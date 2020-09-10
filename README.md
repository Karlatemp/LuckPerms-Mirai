# LuckPerms Mirai

LuckPerms, 经典的权限系统, 已经接入Mirai-Console Permission Service.

## 实现细节

对于 Mirai-Console Permission System. Permission有以下内容
- Permission本身
- Permission的父Permission

对于 MC Default PermissionSystem. 他会有以下行为

- 如果可以拥有权限的对象(`Permissible`) 拥有
 `Permission` 或者 `Permission的任何一级parent`,
  那么代表此 `Permissible` 拥有这个权限

---

对于 LuckPerms-Mirai, 会有以下行为
- 获取当前权限节点状态
- 如果权限节点是已设置的, 返回该权限节点的状态
- 如果当前节点已经是 Root 权限节点, 中断, 判断为没有权限
- 重新检查该权限节点的parent
- 特别的, 对于 `Console`, `Console` 拥有全部权限

LuckPerms 采用拦截式的权限判断.
- 设: 当前需要判断的权限对象拥有的权限为 `*=true`, `deny=false`
- 那么该对象拥有**除了** `deny` 之外的全部权限

# Dev

## 特别权限节点
LuckPerms Mirai 提供了一些特别的权限节点(权限ID), `namespace:id` 在对应代码中为 `PermissionId(namespace, id)`
- 对于 `*:*`, LuckPerms-Mirai会直接识别成 `*`, 代表 ROOT
- 对于 `:`(`PermissionId("","")`), LuckPerms-Mirai 会直接返回true, 代表没有权限检查
- 对于 `namespace:`(`PermissionId(namespace, "")`),
  LuckPerms-Mirai 会识别成 `namespace` 而不是 `namespace.`
- PermissionService#register
    - 对于 parentId为 `<lp>:#` 的权限,
      LuckPerms-Mirai 将直接提供权限节点并不进行任何注册检查,
      父权限指定

## 如何使用
- 正常接入Mirai-Console Permission System即可
- 请不要在 `namespace` 中使用 `.` 号

