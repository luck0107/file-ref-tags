# 开发文档

本文档将指导你如何参与 File Ref Tags 插件的开发。

## 目录

- [开发环境搭建](#开发环境搭建)
- [项目结构](#项目结构)
- [核心模块说明](#核心模块说明)
- [开发流程](#开发流程)
- [如何添加新功能](#如何添加新功能)
- [代码规范](#代码规范)
- [测试](#测试)
- [构建和打包](#构建和打包)
- [常见问题](#常见问题)

## 开发环境搭建

### 前置要求

- **JDK**: 17 或更高版本
- **IntelliJ IDEA**: 2023.1 或更高版本（推荐使用最新版本）
- **Gradle**: 项目已包含 Gradle Wrapper，无需单独安装
- **Git**: 用于版本控制

### 环境配置步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/your-username/file-ref-tags.git
   cd file-ref-tags/intellij-platform-plugin-template-main
   ```

2. **打开项目**
   - 使用 IntelliJ IDEA 打开项目根目录
   - 等待 Gradle 同步完成

3. **配置 JDK**
   - 打开 **File** → **Project Structure** → **Project Settings** → **Project**
   - 设置 **SDK** 为 Java 17 或更高版本
   - 设置 **Language level** 为 17 或更高

4. **验证环境**
   ```bash
   # 运行测试验证环境是否正确
   ./gradlew test
   
   # 运行插件验证是否能正常启动
   ./gradlew runIde
   ```

## 项目结构

```
intellij-platform-plugin-template-main/
├── src/
│   ├── main/
│   │   ├── kotlin/org/jetbrains/plugins/template/
│   │   │   ├── actions/          # 动作（Action）定义
│   │   │   │   ├── Actions.kt                    # 动作组定义
│   │   │   │   ├── AddCurrentFileAction.kt       # 添加当前文件
│   │   │   │   ├── AddFileAndSnippetAction.kt     # 添加文件和代码片段
│   │   │   │   ├── AddGlobalUniqueSnippetAction.kt # 添加全局唯一代码片段
│   │   │   │   ├── AddCommentAction.kt           # 添加注释
│   │   │   │   ├── AddGroupAction.kt              # 添加分组
│   │   │   │   ├── DeleteReferenceAction.kt       # 删除引用
│   │   │   │   ├── EditReferenceTitleAction.kt   # 编辑引用标题
│   │   │   │   ├── RefreshReferencesAction.kt    # 刷新引用
│   │   │   │   └── ShowStorageLocationAction.kt   # 显示存储位置
│   │   │   ├── model/            # 数据模型
│   │   │   │   ├── ReferenceItem.kt    # 引用项数据模型
│   │   │   │   └── ReferenceGroup.kt   # 分组数据模型
│   │   │   ├── services/         # 服务层
│   │   │   │   └── ReferenceDataService.kt  # 引用数据管理服务
│   │   │   ├── ui/               # UI 组件
│   │   │   │   └── ReferenceListPanel.kt     # 引用列表面板
│   │   │   ├── toolWindow/       # 工具窗口
│   │   │   │   └── MyToolWindowFactory.kt    # 工具窗口工厂
│   │   │   ├── utils/            # 工具类
│   │   │   │   ├── NotificationUtils.kt      # 通知工具
│   │   │   │   └── ReferenceNavigationUtils.kt # 导航工具
│   │   │   ├── MyBundle.kt       # 国际化支持
│   │   │   └── UriHandler.kt     # URI 处理器
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── plugin.xml     # 插件配置文件
│   │       └── messages/
│   │           └── MyBundle.properties  # 国际化资源文件
│   └── test/                      # 测试代码
│       └── kotlin/
├── build.gradle.kts               # Gradle 构建配置
├── gradle.properties             # Gradle 属性配置
├── settings.gradle.kts           # Gradle 项目设置
└── README.md                      # 项目说明文档
```

## 核心模块说明

### 1. 数据模型 (model/)

#### ReferenceItem.kt
定义引用项的数据结构，包括：
- `id`: 唯一标识符
- `title`: 显示标题
- `type`: 引用类型（FILE, FILE_SNIPPET, GLOBAL_SNIPPET, COMMENT）
- `filePath`: 文件路径
- `snippet`: 代码片段
- `groupId`: 所属分组ID

#### ReferenceGroup.kt
定义分组的数据结构，包括：
- `id`: 唯一标识符
- `name`: 分组名称

### 2. 服务层 (services/)

#### ReferenceDataService.kt
核心数据管理服务，负责：
- 加载和保存引用数据（JSON 格式）
- 添加、删除、更新引用项和分组
- 管理存储路径（基于项目路径的 MD5 哈希）
- 延迟加载机制（仅在工具窗口显示时加载数据）

**关键方法：**
- `getReferences()`: 获取所有引用项
- `getGroups()`: 获取所有分组
- `addReference()`: 添加引用项
- `deleteReference()`: 删除引用项
- `updateReferenceTitle()`: 更新引用项标题
- `addGroup()`: 添加分组
- `deleteGroup()`: 删除分组
- `saveReferences()`: 保存数据到文件

### 3. UI 组件 (ui/)

#### ReferenceListPanel.kt
工具窗口的主面板，负责：
- 显示引用项和分组列表
- 处理拖拽排序
- 处理点击事件（跳转到代码位置）
- 处理右键菜单
- 渲染不同类型的引用项（不同背景色和前景色）
- 显示空状态
- 显示存储位置按钮

**关键特性：**
- 自适应宽度（无滚动条）
- 根据背景色自动调整前景色（深色主题用白色，浅色主题用黑色）
- 支持分组折叠/展开
- 支持拖拽排序

### 4. 动作 (actions/)

所有用户操作都通过 Action 实现：

- **AddCurrentFileAction**: 添加当前打开的文件
- **AddFileAndSnippetAction**: 添加当前文件和选中的代码片段
- **AddGlobalUniqueSnippetAction**: 添加全局唯一的代码片段（自动搜索项目确保唯一性）
- **AddCommentAction**: 添加用户注释
- **AddGroupAction**: 创建新分组
- **DeleteReferenceAction**: 删除引用项
- **EditReferenceTitleAction**: 编辑引用项标题
- **RefreshReferencesAction**: 刷新引用列表
- **ShowStorageLocationAction**: 显示存储文件位置

### 5. 工具窗口 (toolWindow/)

#### MyToolWindowFactory.kt
负责创建和管理工具窗口：
- 创建工具窗口内容
- 注册标题栏动作（刷新按钮）
- 监听工具窗口显示事件，触发延迟加载

### 6. 工具类 (utils/)

#### NotificationUtils.kt
提供非阻塞通知功能：
- `showInfo()`: 显示信息通知
- `showWarning()`: 显示警告通知
- `showError()`: 显示错误通知

#### ReferenceNavigationUtils.kt
提供代码导航功能：
- `navigateToReference()`: 导航到引用项对应的代码位置
- 自动选择代码片段（如果存在）

## 开发流程

### 1. 创建功能分支

```bash
git checkout -b feature/your-feature-name
```

### 2. 开发新功能

1. **修改代码**
   - 根据功能需求修改相应模块
   - 遵循代码规范（见下文）

2. **添加测试**
   - 为新功能添加单元测试
   - 确保测试通过

3. **测试功能**
   ```bash
   # 运行插件进行测试
   ./gradlew runIde
   ```

4. **提交代码**
   ```bash
   git add .
   git commit -m "feat: 添加新功能描述"
   ```

### 3. 提交 Pull Request

1. 推送分支到远程仓库
2. 在 GitHub 上创建 Pull Request
3. 等待代码审查
4. 根据反馈修改代码

## 如何添加新功能

### 示例：添加新的引用类型

1. **修改数据模型**
   
   在 `ReferenceItem.kt` 中的 `ReferenceType` 枚举中添加新类型：
   ```kotlin
   enum class ReferenceType {
       FILE,
       FILE_SNIPPET,
       GLOBAL_SNIPPET,
       COMMENT,
       NEW_TYPE  // 新类型
   }
   ```

2. **修改 UI 渲染**
   
   在 `ReferenceListPanel.kt` 的 `ReferenceListCellRenderer` 中添加新类型的渲染逻辑：
   ```kotlin
   ReferenceType.NEW_TYPE -> {
       if (!isSelected) {
           val bgColor = adjustColor(baseBg, ...)
           background = bgColor
           foreground = getForegroundColor(bgColor)
       }
       AllIcons.General.SomeIcon
   }
   ```

3. **添加对应的 Action**
   
   创建新的 Action 类，例如 `AddNewTypeAction.kt`：
   ```kotlin
   class AddNewTypeAction : AnAction() {
       override fun actionPerformed(e: AnActionEvent) {
           // 实现添加逻辑
       }
   }
   ```

4. **注册 Action**
   
   在 `plugin.xml` 中注册新 Action：
   ```xml
   <action id="FileRefTags.AddNewType" 
           class="org.jetbrains.plugins.template.actions.AddNewTypeAction"
           text="添加新类型">
       <add-to-group group-id="FileRefTags.EditorPopupMenu" anchor="first"/>
   </action>
   ```

5. **添加国际化文本**
   
   在 `MyBundle.properties` 中添加文本：
   ```properties
   action.addNewType.text=添加新类型
   ```

### 示例：添加新的工具窗口按钮

1. **创建 Action**
   
   创建新的 Action 类，例如 `NewToolbarAction.kt`

2. **注册到工具栏**
   
   在 `plugin.xml` 中注册：
   ```xml
   <action id="FileRefTags.NewToolbarAction" 
           class="org.jetbrains.plugins.template.actions.NewToolbarAction"
           icon="AllIcons.General.Add">
       <add-to-group group-id="FileRefTags.ToolWindowToolbar" anchor="last"/>
   </action>
   ```

## 代码规范

### Kotlin 代码规范

1. **命名规范**
   - 类名：大驼峰（PascalCase），如 `ReferenceItem`
   - 函数名：小驼峰（camelCase），如 `getReferences()`
   - 常量：大写下划线分隔，如 `MAX_SIZE`
   - 私有属性：小驼峰，如 `private val dataService`

2. **代码格式**
   - 使用 4 个空格缩进（不是 Tab）
   - 行长度不超过 120 个字符
   - 函数参数过多时换行对齐

3. **注释规范**
   - 公共 API 必须添加 KDoc 注释
   - 复杂逻辑添加行内注释说明
   - 使用中文注释（项目统一使用中文）

4. **异常处理**
   - 使用 try-catch 捕获异常
   - 使用 `NotificationUtils` 向用户显示错误信息
   - 记录错误日志（如需要）

### 示例代码

```kotlin
/**
 * 添加新的引用项
 * 
 * @param item 要添加的引用项
 * @return 是否添加成功
 */
fun addReference(item: ReferenceItem): Boolean {
    return try {
        references.add(item)
        saveReferences()
        true
    } catch (e: Exception) {
        NotificationUtils.showError(
            project,
            "添加失败",
            "无法添加引用项: ${e.message}"
        )
        false
    }
}
```

## 测试

### 运行测试

```bash
# 运行所有测试
./gradlew test

# 运行特定测试类
./gradlew test --tests "org.jetbrains.plugins.template.services.ReferenceDataServiceTest"
```

### 编写测试

测试文件应放在 `src/test/kotlin/` 目录下，示例：

```kotlin
class ReferenceDataServiceTest {
    @Test
    fun testAddReference() {
        // 测试代码
    }
}
```

### 手动测试

1. 运行插件：
   ```bash
   ./gradlew runIde
   ```

2. 在打开的 IDE 中测试功能：
   - 打开工具窗口
   - 测试各种操作
   - 检查数据是否正确保存

## 构建和打包

### 构建插件

```bash
# 构建插件（生成 ZIP 文件）
./gradlew buildPlugin

# 构建产物位置
# build/distributions/File Ref Tags-1.0.0.zip
```

### 验证插件

```bash
# 验证插件兼容性
./gradlew verifyPlugin
```

### 本地安装测试

1. 构建插件：`./gradlew buildPlugin`
2. 在 IntelliJ IDEA 中：
   - **File** → **Settings** → **Plugins**
   - 点击齿轮图标 → **Install Plugin from Disk...**
   - 选择 `build/distributions/File Ref Tags-1.0.0.zip`
   - 重启 IDE

## 常见问题

### Q: 如何调试插件？

A: 
1. 在代码中设置断点
2. 运行 `./gradlew runIde --debug-jvm`
3. 在 IntelliJ IDEA 中附加调试器（端口 5005）

或者使用 IntelliJ IDEA 的 Run Configuration：
1. 创建新的 "Gradle" 运行配置
2. 任务：`runIde`
3. 使用 Debug 模式运行

### Q: 如何查看插件日志？

A: 
- 运行 `./gradlew runIde` 时，日志会输出到控制台
- 在沙盒 IDE 中：**Help** → **Show Log in Files**
- 日志位置：`build/idea-sandbox/IC-2024.1/log/idea.log`

### Q: 如何修改插件版本？

A: 
在 `gradle.properties` 中修改 `pluginVersion` 属性：
```properties
pluginVersion=1.0.1
```

### Q: 如何添加新的依赖？

A: 
在 `build.gradle.kts` 的 `dependencies` 块中添加：
```kotlin
dependencies {
    implementation("com.example:library:1.0.0")
}
```

### Q: 插件无法加载怎么办？

A: 
1. 检查 `plugin.xml` 配置是否正确
2. 检查是否有编译错误：`./gradlew build`
3. 查看日志文件中的错误信息
4. 确保 IntelliJ Platform 版本兼容

### Q: 如何实现国际化？

A: 
1. 在 `MyBundle.properties` 中添加键值对：
   ```properties
   my.key=我的文本
   ```
2. 在代码中使用：
   ```kotlin
   MyBundle.message("my.key")
   ```
3. 支持多语言：创建 `MyBundle_zh_CN.properties` 等文件

### Q: 如何调试 UI 问题？

A: 
1. 使用 IntelliJ IDEA 的 UI Inspector（在运行插件时）
2. 添加日志输出查看组件状态
3. 使用断点检查组件属性

## 贡献指南

### 提交代码前检查清单

- [ ] 代码遵循项目代码规范
- [ ] 所有测试通过
- [ ] 添加了必要的注释和文档
- [ ] 更新了相关文档（如 README）
- [ ] 提交信息清晰明确

### 提交信息格式

使用约定式提交格式：
- `feat:` 新功能
- `fix:` 修复 bug
- `docs:` 文档更新
- `style:` 代码格式调整
- `refactor:` 代码重构
- `test:` 测试相关
- `chore:` 构建/工具相关

示例：
```
feat: 添加新的引用类型支持
fix: 修复拖拽排序时的索引错误
docs: 更新开发文档
```

### Pull Request 要求

1. **描述清晰**：说明修改内容和原因
2. **关联 Issue**：如果修复了 Issue，在 PR 描述中关联
3. **测试充分**：确保新功能经过充分测试
4. **代码审查**：等待维护者审查通过后再合并

## 获取帮助

- **GitHub Issues**: 提交问题或功能请求
- **讨论区**: 在 GitHub Discussions 中讨论
- **文档**: 查看 [IntelliJ Platform SDK 文档](https://plugins.jetbrains.com/docs/intellij/)

---

**感谢你的贡献！** 🎉
