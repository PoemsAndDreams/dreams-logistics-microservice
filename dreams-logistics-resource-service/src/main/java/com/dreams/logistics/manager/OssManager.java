package com.dreams.logistics.manager;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.dreams.logistics.config.AliyunConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Cos 对象存储操作
 */
@Component
public class OssManager {

    @Autowired
    private AliyunConfig aliyunConfig;
    @Autowired
    private OSSClient ossClient;

//    /**
//     * 上传对象
//     *
//     * @param key 唯一键
//     * @param localFilePath 本地文件路径
//     * @return
//     */
//    public PutObjectResult putObject(String key, String localFilePath) {
//
//        return ossClient.putObject(aliyunConfig.getBucketName(), localFilePath, new ByteArrayInputStream(uploadFile.getBytes()));
//    }

    /**
     * 上传对象
     *
     * @param key 唯一键
     * @param file 文件
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        try {
            InputStream inputStream = new FileInputStream(file);
            PutObjectRequest putObjectRequest = new PutObjectRequest(aliyunConfig.getBucketName(), key, inputStream);
            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);

            return result;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + file.getAbsolutePath(), e);
        } catch (OSSException e) {
            throw new RuntimeException("OSS error occurred: " + e.getErrorMessage(), e);
        } catch (ClientException e) {
            throw new RuntimeException("Client error occurred: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }
}
