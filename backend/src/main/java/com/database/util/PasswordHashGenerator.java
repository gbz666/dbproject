package com.database.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 生成 BCrypt 密码哈希，用于初始化或重置 staffs 表密码。
 * 运行 main 后，将控制台输出的哈希填入 SQL：UPDATE staffs SET password = '...' WHERE staff_name = '高柏舟';
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String hash = encoder.encode(rawPassword);
        System.out.println("明文密码: " + rawPassword);
        System.out.println("BCrypt 哈希（复制到 SQL）:");
        System.out.println(hash);
        System.out.println();
        System.out.println("-- 示例 SQL：");
        System.out.println("UPDATE staffs SET password = '" + hash + "', status = 1 WHERE staff_name = '高柏舟';");
    }
}
