package ru.mtuci.coursemanagement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

@Slf4j
@Component
public class PluginLoader {

    @Value("${app.plugin.url:}")
    private String pluginUrl;

    public void tryLoad() {
        if (pluginUrl == null || pluginUrl.isBlank()) return;

        try {
            URI uri = URI.create(pluginUrl);

            if (!"file".equalsIgnoreCase(uri.getScheme())) {
                log.warn("Загрузка плагинов разрешена только из локального file:// (blocked): {}", pluginUrl);
                return;
            }

            // Защита от file://host/...
            String host = uri.getHost();
            if (host != null && !host.isBlank() && !"localhost".equalsIgnoreCase(host)) {
                log.warn("Загрузка плагина с удаленного file-host запрещена (blocked): {}", pluginUrl);
                return;
            }

            File pluginFile = new File(uri);
            if (!pluginFile.exists() || !pluginFile.isFile()) {
                log.warn("Файл плагина не найден: {}", pluginFile.getAbsolutePath());
                return;
            }
            URL url = pluginFile.toURI().toURL();

            try (URLClassLoader cl = new URLClassLoader(new URL[]{url}, this.getClass().getClassLoader())) {
                Class<?> clazz = Class.forName("com.example.PluginMain", true, cl);


                Method m = clazz.getMethod("init");

                m.invoke(null);
                log.info("Плагин загружен из локального файла: {}", pluginFile.getAbsolutePath());
            }

        } catch (Exception e) {
            log.error("Ошибка загрузки плагина: {}", e.getMessage());
        }
    }
}
