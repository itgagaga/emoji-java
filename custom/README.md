# 🎨 自定义配置目录（交互式版本）

> **完全可视化的配置管理，无需写代码！**

## ✨ 新特性：Web配置管理中心

现在你可以通过**浏览器界面**直接管理所有自定义配置！

### 🚀 访问方式

```
http://127.0.0.1:8000/admin
```

打开后你会看到一个**精美的可视化界面**，包含：

- 📊 **统计面板** - 实时显示各类配置数量
- 📑 **标签页切换** - 4大功能模块一目了然
- ➕ **一键添加** - 填表单即可添加新配置
- 🗑️ **便捷删除** - 悬停即可删除不需要的配置
- ✅ **即时反馈** - 操作成功/失败都有提示

---

## 📁 目录结构

```
custom/
├── README.md                          ← 你在这里（使用说明）
├── 🔧 loader.py                       ← 配置加载器（供程序调用）
│
├── 📊 priorities/
│   └── priorities.json               ← 优先级规则（JSON数据文件）
│
├── 🔤 mappings/
│   ├── emojis.json                    ← 新增Emoji（JSON数据文件）
│   └── words.json                     ← 词语映射（JSON数据文件）
│
└── 🏷️ aliases/
    └── aliases.json                   ← 别名扩展（JSON数据文件）
```

### 💡 核心改进

| 之前 | 现在 |
|------|------|
| ❌ 需要写Python代码 | ✅ **直接编辑JSON或用Web界面** |
| ❌ 要重启服务器才能生效 | ✅ Web界面操作立即保存 |
| ❌ 容易语法错误 | ✅ 表单验证 + 自动格式化 |
| ❌ 不直观 | ✅ **可视化展示所有配置** |

---

## 🖥️ 使用方法

### 方法1：Web界面（推荐⭐）

1. 启动服务器：
   ```bash
   uv run python server.py
   ```

2. 打开浏览器访问：
   ```
   http://127.0.0.1:8000/admin
   ```

3. 在界面上操作：
   - 点击对应标签页（优先级/Emoji/映射/别名）
   - 点击"添加"按钮填写表单
   - 悬停项目点击"×"删除

**就这么简单！无需任何编程知识！**

---

### 方法2：直接编辑JSON文件

如果你更喜欢手动编辑，可以直接修改JSON文件：

#### 1️⃣ 优先级配置 (`custom/priorities/priorities.json`)

```json
[
  {
    "emoji": "😀",
    "word": "开心",
    "priority": 95,
    "description": "笑脸最直观"
  },
  {
    "emoji": "👍",
    "word": "好", 
    "priority": 100,
    "description": "竖大拇指最高"
  }
]
```

**字段说明：**
- `emoji`: Emoji字符
- `word`: 中文词语
- `priority`: 优先级（0-100，越大越靠前）
- `description`: 可选的说明文字

**编辑技巧：**
```bash
# 用VS Code编辑（有JSON格式化）
code custom/priorities/priorities.json

# 或用记事本
notepad custom/priorities/priorities.json
```

---

#### 2️⃣ 新增Emoji (`custom/mappings/emojis.json`)

```json
[
  {
    "emoji": "🧋",
    "name": "bubble tea",
    "aliases": ["奶茶", "珍珠奶茶"],
    "keywords": ["tea", "drink"],
    "priority": 95,
    "category": "食物"
  }
]
```

**字段说明：**
- `emoji`: Emoji字符（必填）
- `name`: 英文名称（必填）
- `aliases`: 中文别名列表
- `keywords`: 英文关键词列表
- `priority`: 默认优先级
- `category`: 分类标签

---

#### 3️⃣ 词语映射 (`custom/mappings/words.json`)

```json
{
  "yyds": {
    "emojis": ["👑", "🏆"],
    "category": "网络流行语",
    "description": "永远的神"
  },
  "点赞": {
    "emojis": ["👍", "❤️", "👏"],
    "category": "社交场景",
    "description": "表示赞同"
  }
}
```

**字段说明：**
- 键名: 中文词语（如"yyds"、"点赞"）
- `emojis`: 对应的Emoji列表（第一个优先级最高）
- `category**: 分类
- `description`: 说明

**示例：添加网络流行语**
```json
{
  "泰裤辣": {
    "emojis": ["🔥", "✨", "💪"],
    "category": "网络流行语",
    "description": "太酷了"
  },
  "显眼包": {
    "emojis": ["🤡", "🎭", "⭐"],
    "category": "网络流行语", 
    "description": "爱出风头的人"
  }
}
```

---

#### 4️⃣ 别名扩展 (`custom/aliases/aliases.json`)

```json
{
  "😀": {
    "aliases": ["哈哈", "嘿嘿", "嘻嘻", "乐呵"],
    "category": "笑脸系列"
  },
  "👍": {
    "aliases": ["赞", "点赞", "支持", "牛", "666"],
    "category": "手势系列"
  }
}
```

**字段说明：**
- 键名: Emoji字符
- `aliases`: 新增的中文别名列表
- `category`: 分类

**效果：**
- 添加后输入"哈哈"、"嘿嘿"、"嘻嘻"都能匹配到😀
- 输入"666"、"牛批"都能匹配到👍

---

## 📊 当前预置数据统计

| 模块 | 数量 | 说明 |
|------|------|------|
| ✅ **优先级规则** | **10条** | 调整常用词的排序 |
| ✅ **新增Emoji** | **5个** | 👀🤙🦥🧋🎮 |
| ✅ **词语映射** | **20个** | 含网络流行语、社交、食物等 |
| ✅ **扩展别名** | **94个** | 覆盖19个常用Emoji |

---

## 🔌 API接口文档（供开发者）

如果你需要通过API管理配置：

### 获取统计信息
```http
GET /api/custom/stats
```
返回：各类型配置的数量

### 优先级管理
```http
GET /api/custom/priorities          # 获取所有规则
POST /api/custom/priorities         # 添加规则
DELETE /api/custom/priorities/{id}  # 删除指定规则
```

### Emoji管理
```http
GET /api/custom/emojis              # 获取所有自定义Emoji
POST /api/custom/emojis             # 添加Emoji
DELETE /api/custom/emojis/{id}      # 删除Emoji
```

### 词语映射管理
```http
GET /api/custom/words               # 获取所有映射
POST /api/custom/words              # 添加映射
DELETE /api/custom/words/{word}     # 删除指定词的映射
```

### 别名管理
```http
GET /api/custom/aliases             # 获取所有别名
POST /api/custom/aliases            # 添加别名
DELETE /api/custom/aliases/{emoji}  # 删除某Emoji的所有别名
```

**请求示例（PowerShell）：**
```powershell
# 添加优先级规则
$body = @{
    emoji = "😀"
    word = "开心"
    priority = 100
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8000/api/custom/priorities" `
    -Method Post -Body $body -ContentType "application/json"

# 获取统计
Invoke-RestMethod -Uri "http://localhost:8000/api/custom/stats" -Method Get
```

---

## 💡 实际应用场景

### 场景1：快速添加最新网络热词

**步骤：**
1. 打开 http://127.0.0.1:8000/admin
2. 切换到"📝 词语映射"标签页
3. 点击"+ 添加映射"
4. 填写：
   - 词语：`泰裤辣`
   - Emoji：`🔥, ✨, 💪`
   - 分类：网络流行语
5. 点击提交

**结果：** 立即生效！用户输入"泰裤辣"就会显示这些Emoji

---

### 场景2：调整转换结果的顺序

**问题：** 输入"开心"时，🙋排在第一位，但你想让😀排第一

**解决方案：**
1. 切换到"📊 优先级调整"标签页
2. 点击"+ 添加规则"
3. 填写：
   - Emoji：😀
   - 词语：开心
   - 优先级：100
4. 提交

**结果：** 下拉列表中😀会排到最前面

---

### 场景3：为产品添加品牌相关Emoji

**需求：** 让用户输入你们的产品名称能匹配到特定Emoji

**步骤：**
1. 切换到"📝 词语映射"标签页
2. 添加映射：
   - 词语：你的产品名
   - Emoji：你选择的Emoji
3. 保存

**结果：** 用户输入产品名就能看到对应的Emoji

---

## ⚙️ 高级用法

### 批量导入配置

创建脚本 `import_configs.py`：

```python
import json

# 导入大量词语映射
with open('custom/mappings/words.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

# 批量添加
new_mappings = {
    "词汇1": {"emojis": ["😀"], "category": "测试"},
    "词汇2": {"emojis": ["🎉"], "category": "测试"},
}

data.update(new_mappings)

with open('custom/mappings/words.json', 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print(f"已导入 {len(new_mappings)} 个映射")
```

运行：
```bash
uv run python import_configs.py
```

---

### 备份和恢复

**备份：**
```bash
# 备份整个custom目录
Copy-Item -Recurse custom custom_backup_$(Get-Date -Format 'yyyyMMdd')

# 或备份单个文件
copy custom\mappings\words.json words_backup.json
```

**恢复：**
```bash
# 从备份恢复
Copy-Item -Recurse custom_backup_20260523 custom -Force
```

---

### 分享配置给团队

**导出配置包：**
```bash
# 打包custom目录
Compress-Archive -Path custom* -DestinationPath emoji_custom_configs.zip
```

**导入配置包：**
```bash
# 解压到项目目录
Expand-Archive -Path emoji_custom_configs.zip -DestinationPath . -Force
```

---

## ❓ 常见问题

### Q1: Web界面和JSON文件编辑可以混用吗？

**可以！** 它们操作的是同一个JSON文件。但建议：
- 日常使用：Web界面（方便快捷）
- 大批量操作：直接编辑JSON（更高效）
- ⚠️ 不要同时用两种方式编辑同一文件

### Q2: 修改后需要重启服务器吗？

**不需要！** 
- Web界面操作：**立即生效**（实时写入JSON文件）
- JSON文件编辑：需要**重启服务器**重新加载

### Q3: JSON文件格式错误怎么办？

如果JSON格式有误：
1. 服务器会报错但不影响其他功能
2. 用在线工具检查格式：https://jsonlint.com
3. 或用VS Code打开（自动检测错误）

### Q4: 可以同时添加多个相同词语的映射吗？

**不建议。** 后添加的会覆盖先前的。建议在一个映射中列出所有候选Emoji。

### Q5: 如何查看某个配置是否生效？

1. 打开主页面 http://127.0.0.1:8000
2. 输入你配置的词语
3. 查看转换结果是否包含预期的Emoji
4. 点击Emoji查看下拉列表中的排序

---

## 📈 性能优化建议

### 1. 控制配置规模

| 类型 | 建议数量上限 | 说明 |
|------|------------|------|
| 优先级规则 | < 50条 | 太多会影响加载速度 |
| 自定义Emoji | < 200个 | 合理范围 |
| 词语映射 | < 500个 | 足够覆盖大部分场景 |
| 别名总数 | < 1000个 | 单个Emoji不超过50个别名 |

### 2. 定期清理

删除不再使用的配置：
- 在Web界面悬停点击"×"删除
- 或直接编辑JSON文件移除

### 3. 分类整理

善用分类功能：
- 网络流行语、日常用语、专业术语等分开
- 方便后续维护和管理

---

## 🎯 最佳实践总结

### ✅ 推荐

1. **先用Web界面尝试** - 直观易懂，不易出错
2. **小步快跑** - 先添加几个测试效果，再批量添加
3. **分类管理** - 使用合理的分类标签
4. **定期备份** - 重要配置及时备份
5. **查看效果** - 每次修改后在主页测试

### ❌ 避免

1. **不要重复配置** - 同一个词不要在多处定义
2. **不要过度配置** - 只添加真正需要的
3. **不要忽略优先级** - 重要的高优先级配置
4. **不要忘记测试** - 每次修改都要验证效果

---

## 📚 相关资源

- **主应用**: http://127.0.0.1:8000 （文字转Emoji界面）
- **配置管理**: http://127.0.0.1:8000/admin （本页面）
- **API文档**: 所有 `/api/custom/*` 接口
- **项目文档**: [docs/](../docs/) 文件夹

---

## 💬 反馈与建议

这个交互式系统还在不断完善中，欢迎提出宝贵意见！

**常见需求：**
- ✅ 已支持：Web界面管理
- ✅ 已支持：JSON文件直接编辑
- ✅ 已支持：API接口调用
- 🔄 开发中：配置导入/导出功能
- 🔄 开发中：配置版本历史
- 📝 计划中：多人协作编辑

---

## 🎉 开始使用吧！

**立即体验：**

```bash
# 1. 启动服务器
cd d:\ChuangZuo\ChengXu\1\emoji
uv run python server.py

# 2. 打开浏览器
# 主应用：http://127.0.0.1:8000
# 配置管理：http://127.0.0.1:8000/admin

# 3. 开始定制你的Emoji转换引擎！
```

**简单三步，零代码，完全可视化！** 🚀
