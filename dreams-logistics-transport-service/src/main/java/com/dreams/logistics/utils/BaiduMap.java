package com.dreams.logistics.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class BaiduMap {

    @Value("${baidu.ak}")
    private String AK; // 从配置文件中读取 AK

    private static final String URL = "https://api.map.baidu.com/";
    private static final String DIRECTION_LITE_URL = "directionlite/v1/driving?";
    private static final String GEOCODING_URL = "geocoding/v3/?";

    public String geocoding(String address) throws IOException {
        if (StringUtils.isEmpty(address) || AK == null || AK.isEmpty()) {
            log.debug("Invalid parameters or AK.");
            return null;
        }
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("address", address);
        params.put("output", "json");
        params.put("ak", AK);
        params.put("callback", "showLocation");

        // 构造请求 URL
        StringBuffer urlString = new StringBuffer();
        urlString.append(URL + GEOCODING_URL);

        return execute(urlString,params);
    }


    public String directionLiteByDriving(String origin, String destination) throws IOException {
        if (origin == null || destination == null || AK == null || AK.isEmpty()) {
            log.debug("Invalid parameters or AK.");
            return null;
        }

        // 构建请求参数
        Map<String, String> params = new LinkedHashMap<>();
        params.put("origin", origin);
        params.put("destination", destination);
        params.put("ak", AK);

        // 构造请求 URL
        StringBuffer urlString = new StringBuffer();
        urlString.append(URL + DIRECTION_LITE_URL);
        
        return execute(urlString,params);
    }

    private String execute(StringBuffer urlString, Map<String, String> params) throws IOException {

        for (Map.Entry<String, String> pair : params.entrySet()) {
            urlString.append(pair.getKey()).append("=");
            urlString.append(UriUtils.encode(pair.getValue(), "UTF-8")).append("&");
        }

        // 删除最后一个 '&'
        if (urlString.length() > 0) {
            urlString.deleteCharAt(urlString.length() - 1);
        }

        // 打印请求 URL
        System.out.println("Request URL: " + urlString.toString());

        // 发送请求并处理响应
        URL url = new URL(urlString.toString());
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
