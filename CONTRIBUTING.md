# CONTRIBUTING

感谢你来到这里，感谢你对 LuckPerms-Mirai 做的一切贡献。

## clone LuckPerms-Mirai

执行 `git clone git@github.com:Karlatemp/LuckPerms-Mirai.git`, 然后稍等片刻..

然后打开你的终端, 请不要马上使用idea打开该项目, 如果已经使用idea打开了该项目, 请立即中断 sync

```shell script
cd LuckPerms-Mirai
git submodule update --init --recursive
cd LuckPerms
git apply --reject ../patchs.patch
```

完成上述步骤, 即可使用 idea 开启本项目, 此前若没有立刻中断 sync, 那 gradle 会下载许多 LuckPerms-Mirai 用不上的各种依赖(属于上游 LuckPerms 的模块的各种依赖)

## 模块
```
LuckPerms-Mirai
  |- public-api  - LuckPerms-Mirai 暴露出去的公共api
  |- test-plugin - 测试用 plugin
  |- LuckPerms   - lucko/LuckPerms, 上游核心
  `- src         - LuckPerms-Mirai 核心 & 内部 代码实现
```

### public-api

LuckPerms-Mirai 公开的 api, 允许其他插件使用 LuckPerms-Mirai 的 api

若需要暴露内部实现为公开api, 请在 [BackendImpl.kt](public-api/src/main/kotlin/io/github/karlatemp/luckperms/mirai/openapi/internal/BackendImpl.kt)
添加一个新的接口方法/字段, 并在 [OpenApiImpl](src/main/kotlin/io/github/karlatemp/luckperms/mirai/internal/OpenApiImpl.kt) 实现

### src

LuckPerms-Mirai 内部核心实现

- 传入命令的 Sender 必须是 [WrappedLPSender](src/main/kotlin/io/github/karlatemp/luckperms/mirai/commands/WrappedLPSender.kt)
- MiraiConsole PermissionService 的实现 [LPPermissionService](src/main/kotlin/io/github/karlatemp/luckperms/mirai/internal/LPPermissionService.kt)

### LuckPerms

[lucko/LuckPerms](https://github.com/lucko/LuckPerms)

## 修改上游 LuckPerms

LuckPerms-Mirai 使用补丁的形式对 LuckPerms 进行修改, 存放于 [patchs.patch](patchs.patch)

由于使用的是 patch, 在对 LuckPerms 上游进行修改的时候必须遵守以下规则

- 不允许修改上游缩进, 不要在修改 LuckPerms 上游的时候乱按格式化快捷键

如:
```java
public class Example {
    public static void invoke() {
        System.out.println("Example text");
    }
}
```
只允许修改成这样

```java
public class Example {
    public static void invoke() {
        if (1 + 1 == 2)
        System.out.println("Example text");


        if (1 + 1 == 2) {
        System.out.println("Example text");
        }
    }
}
```

如果缩进发生了变化, 那你的修改很有不会被接受

缩进变化会影响整个上游更新,
并令patch文件变得臃肿庞大,
这是不希望看到的

但一点例外, imports, imports 的冲突是在可接受范围内的


完成修改后, 在 IDEA 双击 Ctrl 执行 `gradle :genPatch` (即执行 `:genPatch` task),
即可保存对 LuckPerms 的修改

## commit

为什么要说 commit 呢, 因为 LuckPerms-Mirai 在编辑的时候,
存在一个被修改过内容的 LuckPerms 子模块, 习惯直接全选
`Default Changelist` 的贡献者可能就这样直接把
LuckPerms 也一起 commit 进去了, 这是不正确的

在 IDEA 中, 打开 `Commit` 栏, 找到 `Group By` 按钮,
确保 `Repository` 已经勾选上, 然后选择 `LuckPerms-Mirai`,
切记不要把 `LuckPerms` 也勾选上, 否则你的修改很有可能会被拒绝
