# -*- coding: utf-8 -*-
"""
自定义优先级配置
用于调整特定词汇匹配到特定Emoji时的优先级

使用场景：
- 让"开心"优先显示😀而不是🙋
- 让"聪明"优先显示🧠而不是🤓
- 调整某些不合适的默认排序
"""

CUSTOM_PRIORITIES = {
    # 格式: (emoji, source_word): priority_value
    # priority值越大，排名越靠前（默认范围：0-100）

    # ===== 示例：调整"开心"的优先级 =====
    # 默认可能🙋排第一，我们想让😀排第一
    ("😀", "开心"): 95,      # 😀 开心 → 优先级95（很高）
    ("😄", "开心"): 90,      # 😄 开心 → 优先级90
    ("🙋", "开心"): 70,      # 🙋 开心 → 降低到70

    # ===== 示例：调整"好"的优先级 =====
    ("👍", "好"): 100,       # 👍 好 → 最高优先级（竖大拇指最直观）
    ("👌", "好"): 85,        # 👌 好 → OK手势次之
    ("✅", "好"): 80,        # ✅ 好 → 对勾第三

    # ===== 示例：调整"笑"的优先级 =====
    ("😂", "笑"): 100,       # 😂 笑 → 笑哭表情最常用
    ("😆", "笑"): 95,        # 😆 笑 → 大笑第二
    ("😊", "笑"): 90,        # 😊 笑 → 微笑第三

    # ===== 示例：专业领域优化 =====
    ("💻", "电脑"): 100,     # 💻 电脑 → 电脑图标最准确
    ("🖥️", "电脑"): 90,      # 🖥️ 电脑 → 桌面电脑也可以

    # ===== 更多示例（根据需要取消注释）=====
    # ("🚗", "车"): 100,     # 🚗 车 → 小汽车优先
    # ("🚌", "车"): 60,      # 🚌 车 → 公交车降低
    # ("🏎️", "车"): 50,      # 🏎️ 车 -> 赛车更靠后

    # ("❤️", "爱"): 100,     # ❤️ 爱 → 红心最经典
    # ("💕", "爱"): 85,      # 💕 爱 → 双心次之
    # ("💖", "爱"): 80,      # 💖 爱 → 闪亮红心第三
}


def get_custom_priority(emoji: str, source_word: str) -> int:
    """
    获取自定义优先级

    参数:
        emoji: Emoji字符，如"😀"
        source_word: 来源词语，如"开心"

    返回:
        int: 自定义优先级值，如果没有设置则返回None
    """
    key = (emoji, source_word)
    return CUSTOM_PRIORITIES.get(key)


def apply_custom_priorities(candidates: list) -> list:
    """
    应用自定义优先级到候选列表

    参数:
        candidates: 候选列表，每个元素是包含emoji和source_word的字典

    返回:
        list: 应用优先级后的候选列表（已重新排序）
    """
    for cand in candidates:
        emoji = cand.get("emoji", "")
        source_word = cand.get("source_word", "")
        custom_priority = get_custom_priority(emoji, source_word)

        if custom_priority is not None:
            cand["priority"] = custom_priority
            cand["_priority_source"] = "custom"

    return sorted(candidates, key=lambda x: x.get("priority", 0), reverse=True)


if __name__ == "__main__":
    print("=" * 60)
    print("📋 自定义优先级配置示例")
    print("=" * 60)

    print(f"\n已配置 {len(CUSTOM_PRIORITIES)} 条自定义优先级规则")

    print("\n前5条规则:")
    for i, (key, value) in enumerate(list(CUSTOM_PRIORITIES.items())[:5], 1):
        emoji, word = key
        print(f"{i}. '{word}' → {emoji} (优先级: {value})")

    print("\n使用方法:")
    print("1. 在此文件中修改 CUSTOM_PRIORITIES 字典")
    print("2. 运行 python -m custom.priorities.custom_priorities 验证")
    print("3. 重启服务器使配置生效")
