package com.timetracking.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Autowired
    private DefaultKaptcha captchaProducer;

    /**
     * 生成验证码图片
     */
    @GetMapping("/image")
    public void generateCaptchaImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 设置响应头
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");

        // 生成验证码文本
        String captchaText = captchaProducer.createText();
        
        // 将验证码文本存储到会话中
        HttpSession session = request.getSession();
        session.setAttribute("captcha", captchaText);

        // 生成验证码图片
        BufferedImage bi = captchaProducer.createImage(captchaText);
        
        // 输出图片
        OutputStream out = response.getOutputStream();
        ImageIO.write(bi, "jpg", out);
        try {
            out.flush();
        } finally {
            out.close();
        }
    }
}
