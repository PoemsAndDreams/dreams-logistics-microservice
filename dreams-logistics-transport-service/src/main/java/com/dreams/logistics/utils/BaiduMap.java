package com.dreams.logistics.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class BaiduMap {

    @Value("${baidu.ak}")
    private String AK; // 从配置文件中读取 AK

    private static final String URL = "https://api.map.baidu.com/";
    private static final String DIRECTION_LITE_URL = "directionlite/v1/driving?";
    private static final String GEOCODING_URL = "geocoding/v3/?";


    public List<Double> geocodingReturn(String address) {
        String geocoding = "";
        try {
            //根据详细地址查询坐标
            geocoding = geocoding(address);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 去掉回调函数包裹，提取 JSON 部分
        String jsonString = geocoding.substring(geocoding.indexOf('(') + 1, geocoding.lastIndexOf(')'));

        // 解析 JSON 数据
        JSONObject jsonObject = JSONUtil.parseObj(jsonString);
        // 获取 "result" 对象
        JSONObject resultJson = jsonObject.getJSONObject("result");

        if (ObjectUtil.isEmpty(resultJson)) {
            log.error("地址无法定位");
            throw new BusinessException(ErrorCode.ADDRESS_CANNOT_BE_LOCATED);
        }

        // 获取 "location" 对象
        JSONObject locationJson = resultJson.getJSONObject("location");

        // 提取经度和纬度
        double lng = locationJson.getDouble("lng");
        double lat = locationJson.getDouble("lat");

        return Arrays.asList(lng,lat);
    }


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
