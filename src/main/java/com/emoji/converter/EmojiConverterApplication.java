package com.emoji.converter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@EnableCaching
public class EmojiConverterApplication {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║     🎭 Emoji Converter - Spring Boot 版       ║");
        System.out.println("║     智能中文文字转Emoji转换引擎                 ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        SpringApplication.run(EmojiConverterApplication.class, args);
        System.out.println("✅ Emoji Converter 已启动!");
        System.out.println("   🌐 主应用: http://localhost:7080/");
        System.out.println("   ⚙️  Admin: http://localhost:7080/aaaa/dskqrb");
    }
}
