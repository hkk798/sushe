package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

@Controller
public class CaptchaController {
    
    // 验证码字符集
    private static final char[] chars = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U',
        'V', 'W', 'X', 'Y', 'Z'
    };
    
    // 验证码长度
    private static final int CODE_LENGTH = 4;
    
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletRequest request, 
                          HttpServletResponse response) throws IOException {
        
        // 设置响应类型为图片
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        
        // 创建验证码图片
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // 获取图形上下文
        Graphics2D g = image.createGraphics();
        
        // 设置背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        // 设置字体
        g.setFont(new Font("Arial", Font.BOLD, 24));
        
        // 生成随机验证码
        Random random = new Random();
        StringBuilder captchaCode = new StringBuilder();
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            char c = chars[random.nextInt(chars.length)];
            captchaCode.append(c);
            
            // 随机颜色
            g.setColor(new Color(
                random.nextInt(200),
                random.nextInt(200),
                random.nextInt(200)
            ));
            
            // 绘制字符（稍微随机偏移位置）
            g.drawString(String.valueOf(c), 
                        20 + i * 20 + random.nextInt(5), 
                        28 + random.nextInt(5));
        }
        
        // 添加干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(
                random.nextInt(200),
                random.nextInt(200),
                random.nextInt(200)
            ));
            g.drawLine(
                random.nextInt(width),
                random.nextInt(height),
                random.nextInt(width),
                random.nextInt(height)
            );
        }
        
        // 添加噪点
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g.setColor(new Color(
                random.nextInt(200),
                random.nextInt(200),
                random.nextInt(200)
            ));
            g.drawLine(x, y, x, y);
        }
        
        g.dispose();
        
        // 将验证码存入session
        HttpSession session = request.getSession();
        session.setAttribute("captcha", captchaCode.toString());
        
        // 输出图片到响应流
        try (OutputStream os = response.getOutputStream()) {
            ImageIO.write(image, "JPEG", os);
        }
    }
}