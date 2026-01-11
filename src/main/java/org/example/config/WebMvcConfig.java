package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取当前工作目录
        String currentDir = System.getProperty("user.dir");
        System.out.println("当前工作目录: " + currentDir);

        // 构建上传目录路径（注意Windows和Linux的路径分隔符）
        String uploadPath;
        if (currentDir.contains("\\")) {
            // Windows 路径
            uploadPath = currentDir + "\\uploads\\";
        } else {
            // Linux/Mac 路径
            uploadPath = currentDir + "/uploads/";
        }

        System.out.println("上传目录绝对路径: " + uploadPath);

        // 验证目录是否存在
        File uploadDir = new File(uploadPath);
        System.out.println("上传目录是否存在: " + uploadDir.exists());
        System.out.println("上传目录路径: " + uploadDir.getAbsolutePath());

        if (uploadDir.exists()) {
            // 列出目录中的文件
            File[] files = uploadDir.listFiles();
            if (files != null) {
                System.out.println("上传目录中的文件:");
                for (File file : files) {
                    System.out.println("  - " + file.getName());
                }
            }
        }

        // 配置静态资源映射
        // Windows需要将反斜杠替换为正斜杠，并添加file:前缀
        String resourcePath;
        if (uploadPath.contains("\\")) {
            resourcePath = "file:///" + uploadPath.replace("\\", "/");
        } else {
            resourcePath = "file:" + uploadPath;
        }

        System.out.println("资源映射路径: " + resourcePath);

        // 添加静态资源处理器
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourcePath);

        // 还可以添加其他静态资源映射
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}