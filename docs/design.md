# AI 生成 Android 界面设计规范

本文件是本项目（记账软件）的 UI 设计纪律。任何时候生成、修改界面代码，都必须遵守以下规范，目标是让界面获得一致、美观、有层次的设计品质。

## 1. 颜色：一律走 Material 3 Token，禁止硬编码

- 禁止直接写十六进制颜色：`Color(0xFF....)`、`Color.Red`、`Color.Gray` 等一律不得出现在业务界面代码里。
- 所有颜色都必须从 `MaterialTheme.colorScheme.xxx` 取，这是唯一颜色来源。
- 常见取法对照：
  - 页面背景 → `colorScheme.surface` / `colorScheme.background`
  - 主文字 → `colorScheme.onSurface`
  - 次要文字 → `colorScheme.onSurfaceVariant`
  - 主要按钮/强调 → `colorScheme.primary`（文字/图标用 `onPrimary`）
  - 次要按钮 → `colorScheme.secondaryContainer`（文字用 `onSecondaryContainer`）
  - 警示/删除 → `colorScheme.error`（文字用 `onError`）
  - 分隔线 → `colorScheme.outlineVariant`
- 纯黑白灰也走 token：浅色模式文字用 `onSurface`，不要用黑色；分隔/弱化使用 `onSurfaceVariant` 与 `surfaceVariant`，不要用灰色。

## 2. 主题：使用项目现有主题与动态取色

- 统一使用 `MoneyBookTheme`，不要新建 MaterialTheme。
- 颜色应从 `colorScheme` 获取，主题色切换、账本变色、动态取色都由 Theme 层负责，界面层不感知。

## 3. 排版：建立清晰的字号层级

- 用 `MaterialTheme.typography.xxx` 取字号，禁止写死 `sp`（除极少数字体需精确控制外）。
- 层级建议：
  - 页面大标题 → `headlineMedium` / `titleLarge`
  - 区块标题 → `titleMedium` / `titleLarge`
  - 列表项标题 → `titleMedium`
  - 次要说明 → `bodyMedium`，弱化为 `onSurfaceVariant`
  - 辅助/标签 → `labelMedium` / `labelLarge`
- 同一屏内字号要有明显层级，避免全部等大、无主次。

## 4. 圆角与形状：成体系，不一律

- 用 `MaterialTheme.shapes.xxx`（`small/medium/large`），不要每处写死同一圆角值。
- 卡片/弹窗 → `large` 或 `extraLarge`；小的输入框、按钮 → `medium`；图章、标签 → `shapes.small` 或全圆角。
- 不要让所有元素都是一个圆角矩形，不同功能给不同圆角，形成语言。

## 5. 间距：用规范间距，避免 8dp 满天飞

- 用统一的 spacing，整屏间隔优先选 16dp（`Arrangement.spacedBy` / padding），内边距常用 16dp 与 12dp。
- 列表项之间的垂直间隙用统一值，不要每一处随手写不同数字。

## 6. 构图：有焦点、有层次、不做等宽卡片列

- 每个屏幕要有明确的主角（最重要的卡片/数字/按钮），放大突出它。
- 用卡片分组信息，但要有大小/色彩/位置的差异，避免一行排满一模一样的小卡片。
- 关键数字（如金额总额）用 `primary` 强调 + 大字号突出。
- 留白充足，内容不要贴边、不要挤满。

## 7. 动效与反馈（克制使用）

- 转场/动画只在有意义的地方使用，不要每个元素都动画。
- 交互反馈（点击、选中、禁用）依赖 Material3 组件默认行为，不额外写花哨效果。

## 8. 组件：优先用 Material3 内置组件

- 按钮、卡片、输入框、对话框、BottomSheet、Snackbar 一律用 `androidx.compose.material3.*` 提供的组件。
- 不要自己手搓基础控件，除非有明确定制需求。
- 图标：优先 `material-icons-extended`，已引入。

## 自检清单（生成完代码后过一遍）

- [ ] 界面代码里没有 `Color(0xff...)` 硬编码
- [ ] 颜色、字号、圆角都来自 `MaterialTheme` 的 colorScheme / typography / shapes
- [ ] 整屏字号有主次、有焦点
- [ ] 圆角、间距成体系、统一
- [ ] 覆盖了浅色与深色两种模式（用 token 自动适配）