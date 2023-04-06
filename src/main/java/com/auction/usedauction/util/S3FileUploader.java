package com.auction.usedauction.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.FileErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3FileUploader {
    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public List<UploadFIleDTO> uploadFiles(List<MultipartFile> multipartFileList, String subPath) {
        if (multipartFileList == null) {
            throw new CustomException(FileErrorCode.FILE_EMPTY);
        }
        List<UploadFIleDTO> storeResult = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFileList) {
            storeResult.add(uploadFile(multipartFile, subPath));
        }

        return storeResult;
    }

    public UploadFIleDTO uploadFile(MultipartFile multipartFile, String subPath) {
        if (isEmptyFile(multipartFile)) {
            throw new CustomException(FileErrorCode.FILE_EMPTY);
        }

        String originalFileName = getOriginalFileName(multipartFile);
        String storeFileName = createStoreFileName(originalFileName);

        String storeFileFullUrl = sendAwsS3(bucket, subPath + storeFileName, multipartFile);
        log.info("S3에 파일 전송 완료 originalFileName = {},storePath = {}, storeFullUrl={}", originalFileName, subPath + storeFileName, storeFileFullUrl);

        return new UploadFIleDTO(originalFileName, storeFileName, subPath + storeFileName, storeFileFullUrl);
    }

    private String sendAwsS3(String bucketName, String filePath, MultipartFile uploadFile) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(uploadFile.getContentType());
        objectMetadata.setContentLength(uploadFile.getSize());
        try {
            amazonS3Client.putObject(new PutObjectRequest(bucketName, filePath, uploadFile.getInputStream(), objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            log.error("exception = {}", "IOException", e);
            throw new CustomException(FileErrorCode.S3_FILE_NOT_TRANSFER);
        }

        return amazonS3Client.getUrl(bucketName, filePath).toString();

    }

    public UploadFIleDTO uploadFile(File file, String subPath) {
        if (isEmptyFile(file)) {
            throw new CustomException(FileErrorCode.FILE_NOT_FOUND);
        }

        String originalFileName = getOriginalFileName(file);
        String storeFileName = createStoreFileName(originalFileName);

        String storeFileFullUrl = sendAwsS3(bucket, subPath + storeFileName, file);
        log.info("S3에 파일 전송 완료 originalFileName = {},storePath = {}, storeFullUrl={}", originalFileName, subPath + storeFileName, storeFileFullUrl);

        return new UploadFIleDTO(originalFileName, storeFileName, subPath + storeFileName, storeFileFullUrl);
    }

    private String sendAwsS3(String bucketName, String filePath, File uploadFile) {
        amazonS3Client.putObject(new PutObjectRequest(bucketName, filePath, uploadFile)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucketName, filePath).toString();

    }

    public String deleteFile(String filePath) {
        boolean isExist = amazonS3Client.doesObjectExist(bucket, filePath);
        if (!isExist) {
            throw new CustomException(FileErrorCode.S3_FILE_NOT_FOUND);
        }
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, filePath));
        log.info("S3 파일 삭제 완료 deletedFilePath = {}", filePath);
        return filePath;
    }

    //multipart file
    public String getOriginalFileName(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new CustomException(FileErrorCode.FILE_EMPTY);
        }
        return multipartFile.getOriginalFilename();
    }

    //file
    public String getOriginalFileName(File file) {
        if (file == null || !file.exists()) {
            throw new CustomException(FileErrorCode.FILE_NOT_FOUND);
        }
        return file.getName();
    }

    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename); // 확장자 추출
        return UUID.randomUUID().toString() + "." + ext; // wfese-wfe-223.png
    }

    private static String extractExt(String originalFilename) { // 확장자 추출
        int position = originalFilename.lastIndexOf(".");
        return originalFilename.substring(position + 1);
    }

    public boolean isEmptyFile(MultipartFile multipartFile) {
        return multipartFile == null || multipartFile.isEmpty();
    }

    public boolean isEmptyFile(File file) {
        return file == null || !file.exists();
    }
}