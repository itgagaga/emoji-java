# -*- coding: utf-8 -*-
"""
自定义别名/关键词扩展
为现有Emoji添加更多中文别名和关键词，提高匹配覆盖率

使用场景：
- 添加网络流行语的多种说法
- 为Emoji添加方言或口语化表达
- 补充行业术语或专业词汇
- 增加同义词和近义词
"""

CUSTOM_ALIASES = {
    # 格式: "emoji": ["新别名1", "新别名2", ...]

    # ===== 笑脸系列增强 =====
    "😀": ["哈哈", "嘿嘿", "嘻嘻", "乐呵", "开心果"],
    "😄": ["大笑", "开怀大笑", "笑开了花", "乐开花"],
    "😆": ["爆笑", "笑喷了", "笑死我了", "笑到肚子疼"],
    "😂": ["笑哭", "苦笑", "无奈的笑", "含泪的笑"],
    "🤣": ["笑趴", "笑翻", "笑倒", "笑得不行"],

    # ===== 手势系列增强 =====
    "👍": ["赞", "点赞", "支持", "棒极了", "牛", "666"],
    "👎": ["踩", "差评", "不好", "不行", "拉垮"],
    "👏": ["鼓掌", "掌声", "精彩", "太好了", "漂亮"],
    "🙏": ["拜托", "求你了", "麻烦", "感谢", "多谢"],
    "✌️": ["耶", "胜利", "赢了", "成功", "比耶"],

    # ===== 爱心系列增强 =====
    "❤️": ["爱", "喜欢", "爱慕", "心动", "迷恋", "钟爱"],
    "💕": ["甜蜜", "恩爱", "情深", "相爱", "两情相悦"],
    "💖": ["热爱", "深爱", "挚爱", "真爱", "至爱"],
    "💗": ["心动", "暗恋", "倾心", "迷恋", "着迷"],

    # ===== 动物系列增强 =====
    "🐶": ["狗子", "汪汪", "修勾", "柴犬", "二哈"],
    "🐱": ["猫猫", "喵星人", "猫咪", "主子", "铲屎官"],
    "🐼": ["熊猫", "国宝", "滚滚", "胖达"],
    "🦊": ["狐狸", "狡猾", "精明", "魅惑"],
    "🐰": ["兔子", "兔兔", "小白兔", "玉兔"],

    # ===== 食物系列增强 =====
    "🍚": ["米饭", "干饭", "吃饭饭", "用餐"],
    "🍜": ["面条", "拉面", "米粉", "河粉"],
    "🍔": ["汉堡", "薯条", "快餐", "垃圾食品"],
    "🍕": ["披萨", "pizza", "意式薄饼"],
    "🍰": ["蛋糕", "甜点", "甜品", "生日蛋糕"],

    # ===== 交通工具增强 =====
    "🚗": ["小车", "汽车", "轿车", "私家车", "开车"],
    "🚌": ["公交", "公交车", "巴士", "大巴"],
    "🚇": ["地铁", "捷运", "轨道交通"],
    "✈️": ["飞机", "航班", "航空", "飞行"],
    "🚲": ["单车", "自行车", "骑行", "骑车"],

    # ===== 自然天气增强 =====
    "☀️": ["太阳", "阳光", "晴天", "好天气", "大晴天"],
    "🌙": ["月亮", "月光", "月色", "月夜", "赏月"],
    "⭐": ["星星", "星光", "星夜", "星空"],
    "🌈": ["彩虹", "七彩", "绚丽", "多彩"],
    "❄️": ["雪花", "下雪", "雪天", "冰天雪地"],

    # ===== 更多示例（根据需要添加）=====
    # "emoji": ["别名1", "别名2", "别名3"],
}


def get_custom_aliases() -> dict:
    """
    获取所有自定义别名

    返回:
        dict: {emoji: [别名列表]}
    """
    return CUSTOM_ALIASES


def add_aliases_for_emoji(emoji: str, new_aliases: list, overwrite: bool = False):
    """
    为特定Emoji添加别名

    参数:
        emoji: Emoji字符
        new_aliases: 新别名列表
        overwrite: 是否覆盖已有别名（默认追加）
    """
    if emoji not in CUSTOM_ALIASES:
        CUSTOM_ALIASES[emoji] = []

    existing = set(CUSTOM_ALIASES[emoji])

    for alias in new_aliases:
        if alias not in existing or overwrite:
            CUSTOM_ALIASES[emoji].append(alias)
            existing.add(alias)


def apply_custom_aliases_to_data(emoji_data: list) -> tuple:
    """
    将自定义别名应用到现有Emoji数据

    参数:
        emoji_data: 现有的Emoji数据列表

    返回:
        tuple: (更新后的数据, 添加的别名总数)
    """
    data = emoji_data.copy()
    total_added = 0

    for item in data:
        emoji = item.get("emoji")
        if emoji and emoji in CUSTOM_ALIASES:
            new_aliases = CUSTOM_ALIASES[emoji]
            existing_aliases = set(item.get("aliases", []))
            existing_keywords = set(item.get("keywords", []))

            for alias in new_aliases:
                if alias not in existing_aliases and alias not in existing_keywords:
                    if "aliases" not in item:
                        item["aliases"] = []
                    item["aliases"].append(alias)
                    total_added += 1

    return data, total_added


if __name__ == "__main__":
    print("=" * 60)
    print("🏷️ 自定义别名扩展配置")
    print("=" * 60)

    total_emojis = len(CUSTOM_ALIASES)
    total_aliases = sum(len(aliases) for aliases in CUSTOM_ALIASES.values())

    print(f"\n已配置 {total_emojis} 个Emoji的自定义别名")
    print(f"总共添加 {total_aliases} 个新别名")

    print("\n前10个Emoji及其新增别名:")
    for i, (emoji, aliases) in enumerate(list(CUSTOM_ALIASES.items())[:10], 1):
        display_aliases = aliases[:5]
        more = f" (+{len(aliases)-5})" if len(aliases) > 5 else ""
        print(f"{i}. {emoji}: {', '.join(display_aliases)}{more}")

    if total_emojis > 10:
        print(f"\n... 还有 {total_emojis - 10} 个Emoji")

    print("\n使用方法:")
    print("1. 在此文件中的 CUSTOM_ALIASES 字典添加新别名")
    print("2. 运行 python -m custom.aliases.custom_aliases 验证")
    print("3. 重启服务器使配置生效")
