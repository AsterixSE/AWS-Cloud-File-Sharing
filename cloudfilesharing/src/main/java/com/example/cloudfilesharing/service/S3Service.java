package com.example.cloudfilesharing.service;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@Service
public class S3Service {

    private AmazonS3 s3client;

    @Value("${aws.accessKeyId}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        this.s3client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        s3client.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.Private));
        return fileName;
    }

    public S3Object downloadFile(String fileName) {
        return s3client.getObject(bucketName, fileName);
    }

    public URL generatePreSignedUrl(String fileName, int expirationInMinutes) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * expirationInMinutes;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, fileName)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);
        return s3client.generatePresignedUrl(generatePresignedUrlRequest);
    }

    // New method: List all file names from the S3 bucket
    public List<String> listFiles() {
        List<String> fileNames = new ArrayList<>();
        ObjectListing objectListing = s3client.listObjects(bucketName);
        for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
            fileNames.add(os.getKey());
        }
        return fileNames;
    }

    private String generateFileName(String originalFilename) {
        return UUID.randomUUID().toString() + "_" + originalFilename.replace(" ", "_");
    }
}
