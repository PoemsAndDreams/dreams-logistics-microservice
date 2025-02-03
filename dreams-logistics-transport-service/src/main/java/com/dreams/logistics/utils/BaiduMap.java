package com.dreams.logistics.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class BaiduMap {

    @Value("${baidu.ak}")
    private String AK; // 从配置文件中读取 AK

    private static final String URL = "https://api.map.baidu.com/directionlite/v1/driving?";

    public String directionliteByDriving(String origin, String destination) throws IOException {
        if (origin == null || destination == null || AK == null || AK.isEmpty()) {
            System.out.println("Invalid parameters or AK.");
            return null;
        }

        // 构建请求参数
        Map<String, String> params = new LinkedHashMap<>();
        params.put("origin", origin);
        params.put("destination", destination);
        params.put("ak", AK);

        // 构造请求 URL
        StringBuffer queryString = new StringBuffer();
        queryString.append(URL);
        for (Map.Entry<String, String> pair : params.entrySet()) {
            queryString.append(pair.getKey()).append("=");
            queryString.append(UriUtils.encode(pair.getValue(), "UTF-8")).append("&");
        }

        // 删除最后一个 '&'
        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }

        // 打印请求 URL
        System.out.println("Request URL: " + queryString.toString());

        // 发送请求并处理响应
        URL url = new URL(queryString.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        // 读取响应
        try (InputStreamReader isr = new InputStreamReader(connection.getInputStream());
             BufferedReader reader = new BufferedReader(isr)) {
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        }
    }
}
