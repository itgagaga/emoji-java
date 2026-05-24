# -*- coding: utf-8 -*-
"""
新增自定义Emoji
用于添加系统中没有的新Emoji或组合Emoji

使用场景：
- 添加新的Emoji字符（如最新发布的）
- 创建组合Emoji（如国旗、职业等）
- 添加特殊的表情符号
"""

NEW_EMOJIS = [
    # 格式: {
    #   "emoji": "Emoji字符",
    #   "name": "英文名称",
    #   "aliases": ["中文别名1", "中文别名2"],
    #   "keywords": ["关键词1", "关键词2"],
    #   "priority": 80  (可选，默认80)
    # }

    # ===== 示例1：新增网络流行Emoji =====
    {
        "emoji": "👀",
        "name": "eyes",
        "aliases": ["眼睛", "看", "注视", "观察", "盯着"],
        "keywords": ["eye", "look", "see", "watch"],
        "priority": 90
    },

    # ===== 示例2：新增手势Emoji =====
    {
        "emoji": "🤙",
        "name": "call me hand",
        "aliases": ["打电话", "联系我", "shaka", " hang loose"],
        "keywords": ["call", "phone", "hand", "gesture"],
        "priority": 85
    },

    # ===== 示例3：新增动物Emoji =====
    {
        "emoji": "🦥",
        "name": "sloth",
        "aliases": ["树懒", "懒人", "慢吞吞", "悠闲"],
        "keywords": ["sloth", "lazy", "slow", "animal"],
        "priority": 88
    },

    # ===== 示例4：新增食物Emoji =====
    {
        "emoji": "🧋",
        "name": "bubble tea",
        "aliases": ["奶茶", "珍珠奶茶", "波霸奶茶", "饮料"],
        "keywords": ["tea", "milk", "bubble", "drink", "boba"],
        "priority": 95
    },

    # ===== 示例5：新增活动Emoji =====
    {
        "emoji": "🎮",
        "name": "video game",
        "aliases": ["游戏", "打游戏", "电子游戏", "玩游戏"],
        "keywords": ["game", "gaming", "play", "controller"],
        "priority": 92
    },

    # ===== 更多示例（根据需要取消注释）=====
    # {
    #     "emoji": "🫠",
    #     "name": "melting face",
    #     "aliases": ["融化", "热化了", "受不了", "崩溃"],
    #     "keywords": ["melting", "hot", "melt"],
    #     "priority": 88
    # },
    #
    # {
    #     "emoji": "🫡",
    #     "name": "saluting face",
    #     "aliases": ["敬礼", "收到", "遵命", "好的长官"],
    #     "keywords": ["salute", "yes sir", "military"],
    #     "priority": 90
    # },
]


def get_new_emojis() -> list:
    """
    获取所有新增的自定义Emoji

    返回:
        list: 新增Emoji列表，每个元素是字典格式
    """
    return NEW_EMOJIS


def add_new_emojis_to_data(existing_data: list) -> list:
    """
    将新增Emoji合并到现有数据中

    参数:
        existing_data: 现有的Emoji数据列表

    返回:
        list: 合并后的数据列表（新增的会追加到末尾）
    """
    data = existing_data.copy()
    existing_emojis = {item.get("emoji") for item in data}

    added_count = 0
    for new_emoji in NEW_EMOJIS:
        if new_emoji["emoji"] not in existing_emojis:
            data.append(new_emoji)
            added_count += 1

    return data, added_count


if __name__ == "__main__":
    print("=" * 60)
    print("🆕 新增自定义Emoji配置")
    print("=" * 60)

    print(f"\n已配置 {len(NEW_EMOJIS)} 个新Emoji")

    print("\n详情:")
    for i, emoji_data in enumerate(NEW_EMOJIS, 1):
        emoji = emoji_data["emoji"]
        name = emoji_data["name"]
        aliases = emoji_data.get("aliases", [])[:3]
        priority = emoji_data.get("priority", 80)

        print(f"{i}. {emoji} ({name})")
        print(f"   别名: {', '.join(aliases)}")
        print(f"   优先级: {priority}")
        print()

    print("使用方法:")
    print("1. 在此文件中的 NEW_EMOJIS 列表添加新Emoji")
    print("2. 运行 python -m custom.mappings.new_emojis 验证")
    print("3. 重启服务器使配置生效")
