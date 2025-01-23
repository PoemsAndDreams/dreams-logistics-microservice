package com.dreams.logistics.controller;

import cn.hutool.core.io.FileUtil;

//import com.aliyun.oss.model.PutObjectResult;
import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.constant.FileConstant;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.manager.CosManager;
import com.dreams.logistics.manager.OssManager;
import com.dreams.logistics.model.dto.file.UploadFileRequest;
import com.dreams.logistics.model.entity.DcUser;
import com.dreams.logistics.model.enums.FileUploadBizEnum;
import com.dreams.logistics.service.UserFeignClient;

import com.dreams.logistics.util.SecurityUtil;
import com.qcloud.cos.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;

/**
 * 文件接口
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private CosManager cosManager;

    @Autowired
    private OssManager ossManager;
    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validFile(multipartFile, fileUploadBizEnum);

        DcUser loginDcUser = SecurityUtil.getUser();


        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();

        // todo 腾讯云cos
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginDcUser.getId(), filename);

        // todo 阿里云oss
//        String filepath = String.format("%s/%s/%s", fileUploadBizEnum.getValue(), loginDcUser.getId(), filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            // todo cos上传
            // import com.qcloud.cos.model.PutObjectResult;
            PutObjectResult putObjectResult = cosManager.putObject(filepath, file);
            // todo oss上传
            //import com.aliyun.oss.model.PutObjectResult;
//            PutObjectResult putObjectResult = ossManager.putObject(filepath, file);


            // 返回可访问地址
            // todo oss
//            return ResultUtils.success(FileConstant.OSS_HOST + filepath);
            // todo cos
            return ResultUtils.success(FileConstant.COS_HOST + filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

//    /**
//     * 文件上传
//     *
//     * @param multipartFile
//     * @param uploadFileRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/upload")
//    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
//                                           UploadFileRequest uploadFileRequest, HttpServletRequest request) {
//        String biz = uploadFileRequest.getBiz();
//        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
//        if (fileUploadBizEnum == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        validFile(multipartFile, fileUploadBizEnum);
//        DcUser loginUser = userService.getLoginUser(request);
//        // 文件目录：根据业务、用户来划分
//        String uuid = RandomStringUtils.randomAlphanumeric(8);
//        String filename = uuid + "-" + multipartFile.getOriginalFilename();
//
//        // todo 腾讯云cos
//        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
//
//        File file = null;
//        try {
//            // 上传文件
//            file = File.createTempFile(filepath, null);
//            multipartFile.transferTo(file);
//            // cos上传
//            PutObjectResult putObjectResult = cosManager.putObject(filepath, file);
//            // 返回可访问地址
//            return ResultUtils.success(FileConstant.COS_HOST + filepath);
//        } catch (Exception e) {
//            log.error("file upload error, filepath = " + filepath, e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
//        } finally {
//            if (file != null) {
//                // 删除临时文件
//                boolean delete = file.delete();
//                if (!delete) {
//                    log.error("file delete error, filepath = {}", filepath);
//                }
//            }
//        }
//    }

//    @PostMapping("/upload")
//    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
//                                           UploadFileRequest uploadFileRequest, HttpServletRequest request) {
//        String biz = uploadFileRequest.getBiz();
//        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
//        if (fileUploadBizEnum == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        validFile(multipartFile, fileUploadBizEnum);
//        DcUser loginUser = userService.getLoginUser(request);
//
//        // 文件目录：根据业务、用户来划分
//        String uuid = RandomStringUtils.randomAlphanumeric(8);
//        String filename = uuid + "-" + multipartFile.getOriginalFilename();
//
//        // 获取项目根目录，并创建 static/uploads 目录
//        String projectRoot = System.getProperty("user.dir");  // 获取项目的根目录
//        String uploadDir = projectRoot + "/src/main/resources/static/uploads/" + fileUploadBizEnum.getValue() + "/" + loginUser.getId();
//        Path uploadPath = Paths.get(uploadDir);
//
//        // 创建目录（如果不存在）
//        try {
//            if (!Files.exists(uploadPath)) {
//                Files.createDirectories(uploadPath);
//            }
//        } catch (IOException e) {
//            log.error("Failed to create directory: " + uploadPath, e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建目录失败");
//        }
//
//        // 文件路径
//        Path filePath = uploadPath.resolve(filename);
//
//        try {
//            // 保存文件到本地
//            multipartFile.transferTo(filePath.toFile());
//
//            // 返回文件的可访问路径：通过 http://localhost:8080/uploads/{业务类型}/{用户ID}/{文件名} 来访问
//            String fileUrl = "/uploads/" + fileUploadBizEnum.getValue() + "/" + loginUser.getId() + "/" + filename;
//
//            return ResultUtils.success(fileUrl);  // 返回文件的可访问 URL
//        } catch (IOException e) {
//            log.error("File upload error, filePath = " + filePath, e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
//        }
//    }



    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
