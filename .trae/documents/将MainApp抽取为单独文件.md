# 将MainApp抽取到ui/screen目录

## 现状分析
当前项目已经有了良好的目录结构：
- `cn.debubu.mytest` 包名顶层目录
- `data` 目录（数据层）
- `ui` 目录（UI层）
  - `screen` 目录（屏幕组件，已存在）
  - `theme` 目录（主题配置）

MainActivity.kt 包含了主界面逻辑，不符合当前的目录组织规范。

## 优化建议
将MainApp抽取到 `ui/screen` 目录下，理由如下：
1. **符合项目现有结构**：遵循已有的目录组织方式
2. **单一职责原则**：每个文件只负责一个主要功能
3. **代码可维护性**：分离关注点，便于后续扩展和修改
4. **符合Compose最佳实践**：将屏幕组件集中管理
5. **便于团队协作**：清晰的目录结构便于团队成员理解和维护

## 实现步骤
1. 创建新文件 `MainScreen.kt`（建议重命名为更清晰的名称）在 `ui/screen` 目录下
2. 将 `MainApp` Composable函数迁移到新文件，并更名为 `MainScreen`
3. 调整新文件的包名为 `cn.debubu.mytest.ui.screen`
4. 修改 `MainActivity.kt` 中的引用，直接调用 `MainScreen`
5. 移除不必要的 `Greeting` 函数
6. 更新预览函数位置

## 预期效果
- MainActivity.kt 保持简洁，仅作为应用入口
- MainScreen.kt 专注于主界面的UI实现，位于正确的目录结构中
- 代码结构更清晰，符合项目现有组织方式
- 便于后续添加更多屏幕组件

## 文件变更
- 创建：`c:\code\git\myTest\app\src\main\java\cn\debubu\mytest\ui\screen\MainScreen.kt`
- 修改：`c:\code\git\myTest\app\src\main\java\cn\debubu\mytest\MainActivity.kt`