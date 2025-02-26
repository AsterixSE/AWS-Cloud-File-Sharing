package com.example.cloudfilesharing.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.example.cloudfilesharing.service.S3Service;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private S3Service s3Service;
    @GetMapping
    public ResponseEntity<?> listFiles() {
        try {
            List<String> files = s3Service.listFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error listing files: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = s3Service.uploadFile(file);
            return ResponseEntity.ok("File uploaded successfully: " + fileName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileName:.+}")
    public void downloadFile(@PathVariable String fileName, HttpServletResponse response) {
        try {
            S3Object s3Object = s3Service.downloadFile(fileName);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();

            response.setContentType(s3Object.getObjectMetadata().getContentType());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping("/share/{fileName:.+}")
    public ResponseEntity<?> shareFile(@PathVariable String fileName,
                                       @RequestParam(defaultValue = "15") int expirationMinutes) {
        try {
            String url = s3Service.generatePreSignedUrl(fileName, expirationMinutes).toString();
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating shareable URL: " + e.getMessage());
        }
    }
}
