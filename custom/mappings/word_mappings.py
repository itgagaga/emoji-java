# -*- coding: utf-8 -*-
"""
自定义词语到Emoji的映射
用于建立中文词语与Emoji的直接对应关系

使用场景：
- 添加新的词汇映射（如网络流行语）
- 调整现有映射关系
- 为特定场景定制转换规则
"""

WORD_TO_EMOJI_MAPPINGS = {
    # 格式: "中文词语": ["emoji1", "emoji2", ...]
    # 列表中的第一个emoji优先级最高

    # ===== 网络流行语 =====
    "yyds": ["👑", "🏆"],
    "绝绝子": ["✨", "💖", "🌟"],
    "躺平": ["😴", "🛌", "🧘"],
    "内卷": ["📚", "😫", "🔥"],
    "摆烂": ["🗑️", "💩", "😤"],
    "破防": ["😭", "💔", "😱"],
    "emo": ["😢", "☔", "🎭"],

    # ===== 日常用语增强 =====
    "牛逼": ["🐮", "🔥", "💪"],
    "厉害": ["👍", "🌟", "⭐"],
    "牛逼": ["🐮", "🔥", "💪"],
    "给力": ["💪", "👍", "⚡"],
    "坑爹": ["😡", "🕳️", "💣"],
    "吐槽": ["💬", "🗣️", "😒"],
    "点赞": ["👍", "❤️", "👏"],
    "关注": ["👀", "⭐", "❤️"],
    "收藏": ["⭐", "📌", "💾"],
    "分享": ["↗️", "📤", "🔗"],
    "评论": ["💬", "✍️", "📝"],

    # ===== 社交场景 =====
    "早安": ["🌅", "☀️", "🌞"],
    "晚安": ["🌙", "🌜", "😴"],
    "谢谢": ["🙏", "❤️", "💝"],
    "对不起": ["🙇", "😔", "💔"],
    "恭喜": ["🎉", "🎊", "🏆"],
    "生日快乐": ["🎂", "🎁", "🎈"],
    "新年快乐": ["🧨", "🎆", "🧧"],

    # ===== 表情/情绪 =====
    "开心": ["😀", "😄", "😆", "🥳"],
    "难过": ["😢", "😞", "😔", "💔"],
    "生气": ["😠", "😡", "🤬", "💢"],
    "害怕": ["😨", "😱", "🙀", "👻"],
    "惊讶": ["😲", "😮", "🤯", "❗"],
    "无语": ["😐", "😑", "🤐", "💀"],
    "尴尬": ["😅", "🙃", "😬", "🫣"],
    "感动": ["😭", "🥺", "💕", "🤧"],

    # ===== 食物相关 =====
    "吃饭": ["🍚", "🍜", "🍽️"],
    "饿了": ["😋", "🤤", "🍽️"],
    "奶茶": ["🧋", "🥤", "☕"],
    "火锅": ["🍲", "🌶️", "🥘"],
    "烧烤": ["🍖", "🔥", "🍢"],
    "外卖": ["🛵", "📦", "🍱"],

    # ===== 工作学习 =====
    "加班": ["🌙", "😫", "💻"],
    "摸鱼": ["🐟", "📱", "😎"],
    "开会": ["👥", "📊", "📋"],
    "写代码": ["💻", "⌨️", "🔧"],
    "考试": ["📝", "📚", "✏️"],
    "毕业": ["🎓", "📜", "🎉"],

    # ===== 更多示例（根据需要添加）=====
    # "自定义词": ["emoji1", "emoji2"],
}


def get_word_mappings() -> dict:
    """
    获取所有自定义词语映射

    返回:
        dict: {词语: [emoji列表]}
    """
    return WORD_TO_EMOJI_MAPPINGS


def add_word_mapping(word: str, emojis: list, overwrite: bool = False):
    """
    动态添加词语映射

    参数:
        word: 中文词语
        emojis: Emoji列表（第一个优先级最高）
        overwrite: 是否覆盖已有映射
    """
    if word in WORD_TO_EMOJI_MAPPINGS and not overwrite:
        existing = WORD_TO_EMOJI_MAPPINGS[word]
        for emoji in emojis:
            if emoji not in existing:
                existing.append(emoji)
    else:
        WORD_TO_EMOJI_MAPPINGS[word] = emojis


def remove_word_mapping(word: str) -> bool:
    """
    移除词语映射

    参数:
        word: 要移除的词语

    返回:
        bool: 是否成功移除
    """
    if word in WORD_TO_EMOJI_MAPPINGS:
        del WORD_TO_EMOJI_MAPPINGS[word]
        return True
    return False


if __name__ == "__main__":
    print("=" * 60)
    print("📝 自定义词语映射配置")
    print("=" * 60)

    print(f"\n已配置 {len(WORD_TO_EMOJI_MAPPINGS)} 个词语映射")

    print("\n前10个映射:")
    for i, (word, emojis) in enumerate(list(WORD_TO_EMOJI_MAPPINGS.items())[:10], 1):
        primary_emoji = emojis[0] if emojis else ""
        total = len(emojis)
        print(f"{i}. '{word}' → {primary_emoji} (共{total}个候选)")

    if len(WORD_TO_EMOJI_MAPPINGS) > 10:
        print(f"\n... 还有 {len(WORD_TO_EMOJI_MAPPINGS) - 10} 个映射")

    print("\n分类统计:")
    categories = {
        "网络流行语": ["yyds", "绝绝子", "躺平", "内卷", "摆烂", "破防", "emo"],
        "日常用语": ["牛逼", "厉害", "给力", "坑爹", "吐槽", "点赞", "关注", "收藏", "分享", "评论"],
        "社交场景": ["早安", "晚安", "谢谢", "对不起", "恭喜", "生日快乐", "新年快乐"],
        "表情情绪": ["开心", "难过", "生气", "害怕", "惊讶", "无语", "尴尬", "感动"],
        "食物相关": ["吃饭", "饿了", "奶茶", "火锅", "烧烤", "外卖"],
        "工作学习": ["加班", "摸鱼", "开会", "写代码", "考试", "毕业"]
    }

    for category, words in categories.items():
        count = sum(1 for w in words if w in WORD_TO_EMOJI_MAPPINGS)
        if count > 0:
            print(f"  - {category}: {count}个")

    print("\n使用方法:")
    print("1. 在此文件中的 WORD_TO_EMOJI_MAPPINGS 字典添加新映射")
    print("2. 运行 python -m custom.mappings.word_mappings 验证")
    print("3. 重启服务器使配置生效")
