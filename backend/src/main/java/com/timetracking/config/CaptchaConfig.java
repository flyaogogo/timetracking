package com.timetracking.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class CaptchaConfig {

    @Bean
    public DefaultKaptcha captchaProducer() {
        DefaultKaptcha captchaProducer = new DefaultKaptcha();
        Properties properties = new Properties();
        // 验证码宽度
        properties.setProperty("kaptcha.image.width", "140");
        // 验证码高度
        properties.setProperty("kaptcha.image.height", "40");
        // 验证码字符集
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        // 验证码字符长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 验证码字体大小
        properties.setProperty("kaptcha.textproducer.font.size", "24");
        // 验证码字体颜色 - 加深颜色提高对比度
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0");
        // 验证码背景颜色 - 调整为更浅的背景，提高对比度
        properties.setProperty("kaptcha.background.clear.from", "250,250,250");
        properties.setProperty("kaptcha.background.clear.to", "255,255,255");
        // 验证码干扰线颜色
        properties.setProperty("kaptcha.noise.color", "100,100,100");
        // 验证码干扰线类型 - 减少干扰，提高清晰度
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");
        // 验证码文字样式 - 使用加粗字体
        properties.setProperty("kaptcha.textproducer.font.names", "Arial Bold,黑体,楷体");
        // 验证码文字渲染方式
        properties.setProperty("kaptcha.textproducer.impl", "com.google.code.kaptcha.text.impl.DefaultTextCreator");
        // 验证码图片样式 - 使用更清晰的样式
        properties.setProperty("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.ShadowGimpy");
        // 字符间距 - 增加间距，提高可读性
        properties.setProperty("kaptcha.textproducer.char.space", "8");
        
        Config config = new Config(properties);
        captchaProducer.setConfig(config);
        
        return captchaProducer;
    }
}
