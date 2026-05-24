# -*- coding: utf-8 -*-
"""
自定义配置加载器
统一加载和管理所有自定义配置

功能：
- 加载优先级配置
- 加载新增Emoji
- 加载词语映射
- 加载别名扩展
- 提供一键应用所有配置的方法
"""

import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from custom.priorities.custom_priorities import (
    CUSTOM_PRIORITIES,
    get_custom_priority,
    apply_custom_priorities
)
from custom.mappings.new_emojis import (
    NEW_EMOJIS,
    get_new_emojis,
    add_new_emojis_to_data
)
from custom.mappings.word_mappings import (
    WORD_TO_EMOJI_MAPPINGS,
    get_word_mappings,
    add_word_mapping,
    remove_word_mapping
)
from custom.aliases.custom_aliases import (
    CUSTOM_ALIASES,
    get_custom_aliases,
    apply_custom_aliases_to_data
)


class CustomConfigLoader:
    """自定义配置统一管理器"""

    def __init__(self):
        self.priorities = CUSTOM_PRIORITIES
        self.new_emojis = NEW_EMOJIS
        self.word_mappings = WORD_TO_EMOJI_MAPPINGS
        self.aliases = CUSTOM_ALIASES

        self.stats = {
            "priorities": len(self.priorities),
            "new_emojis": len(self.new_emojis),
            "word_mappings": len(self.word_mappings),
            "alias_emojis": len(self.aliases),
            "total_aliases": sum(len(a) for a in self.aliases.values())
        }

    def get_summary(self) -> dict:
        """
        获取配置摘要信息

        返回:
            dict: 各类配置的统计信息
        """
        return {
            "自定义优先级规则": f"{self.stats['priorities']} 条",
            "新增Emoji": f"{self.stats['new_emojis']} 个",
            "词语映射": f"{self.stats['word_mappings']} 个",
            "扩展别名的Emoji": f"{self.stats['alias_emojis']} 个",
            "新增别名总数": f"{self.stats['total_aliases']} 个"
        }

    def apply_to_converter(self, converter):
        """
        将所有自定义配置应用到转换器实例

        参数:
            converter: EmojiConverter 实例
        """
        print("📦 正在应用自定义配置...")

        # 1. 应用优先级（需要在lookup时动态应用）
        converter.custom_priorities = self.priorities
        print(f"  ✅ 已加载 {self.stats['priorities']} 条优先级规则")

        # 2. 添加新Emoji到数据
        if hasattr(converter, 'emoji_data'):
            original_count = len(converter.emoji_data)
            converter.emoji_data, added = add_new_emojis_to_data(converter.emoji_data)
            print(f"  ✅ 已添加 {added} 个新Emoji")

        # 3. 添加词语映射到word_index
        for word, emojis in self.word_mappings.items():
            if word not in converter.word_index:
                converter.word_index[word] = []
            for idx, emoji in enumerate(emojis):
                priority = 100 - idx * 5
                entry = {
                    "emoji": emoji,
                    "source_word": word,
                    "source_pinyin": "",
                    "index_type": "custom_word",
                    "priority": priority
                }
                converter.word_index[word].append(entry)
        print(f"  ✅ 已添加 {self.stats['word_mappings']} 个词语映射")

        # 4. 应用别名扩展
        if hasattr(converter, 'emoji_data'):
            converter.emoji_data, alias_added = apply_custom_aliases_to_data(converter.emoji_data)
            print(f"  ✅ 已添加 {alias_added} 个新别名")

        print("\n✨ 所有自定义配置已应用！\n")


def load_all_configs() -> CustomConfigLoader:
    """
    加载所有自定义配置的便捷函数

    返回:
        CustomConfigLoader: 配置加载器实例
    """
    return CustomConfigLoader()


if __name__ == "__main__":
    print("=" * 70)
    print("🔧 自定义配置管理中心")
    print("=" * 70)

    loader = load_all_configs()
    summary = loader.get_summary()

    print("\n📊 配置统计:")
    print("-" * 50)
    for key, value in summary.items():
        print(f"  {key}: {value}")

    print("\n" + "=" * 70)
    print("📁 配置文件位置")
    print("=" * 70)
    print("""
custom/
├── 📄 README.md                          (本说明文件)
├── priorities/
│   └── custom_priorities.py              (优先级配置)
├── mappings/
│   ├── new_emojis.py                     (新增Emoji)
│   └── word_mappings.py                  (词语映射)
└── aliases/
    └── custom_aliases.py                 (别名扩展)
""")

    print("=" * 70)
    print("⚡ 快速开始")
    print("=" * 70)
    print("""
1️⃣  编辑配置文件：
   - 调整优先级：custom/priorities/custom_priorities.py
   - 新增Emoji：custom/mappings/new_emojis.py
   - 添加映射：custom/mappings/word_mappings.py
   - 扩展别名：custom/aliases/custom_aliases.py

2️⃣  验证配置：
   python custom/loader.py

3️⃣  重启服务器使配置生效：
   重启 server.py 或 uvicorn 服务
""")
