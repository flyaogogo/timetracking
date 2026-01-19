package com.timetracking;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JasyptEncryptionTest {
    
    @Autowired
    @Qualifier("jasyptStringEncryptor")
    private StringEncryptor encryptor;
    
    @Test
    void testEncryptDecrypt() {
        // 原始密码
        String originalPassword = "xxxxxxpassword";
        
        // 加密密码
        String encryptedPassword = encryptor.encrypt(originalPassword);
        System.out.println("原始密码: " + originalPassword);
        System.out.println("加密密码: " + encryptedPassword);
        System.out.println("配置文件使用: ENC(" + encryptedPassword + ")");
        
        // 解密密码
        String decryptedPassword = encryptor.decrypt(encryptedPassword);
        System.out.println("解密密码: " + decryptedPassword);
        
        // 验证解密结果
        assert originalPassword.equals(decryptedPassword);
    }
}