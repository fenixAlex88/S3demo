package by.alex.demos3.controller;

import by.alex.demos3.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/files")
public class S3Controller {
    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String originalFileName = file.getOriginalFilename();
            assert originalFileName != null;
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
            String fileName = UUID.randomUUID() + fileExtension;  // Генерация уникального имени файла с сохранением расширения

            s3Service.uploadFile(file, fileName);  // Передача нового имени файла в сервис
            log.info("File uploaded successfully: {}", fileName);
            return ResponseEntity.ok("File uploaded successfully");
        } catch (IOException e) {
            log.error("Upload failed for file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping(value = "/download/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
        try {
            byte[] data = s3Service.downloadFile(fileName);
            log.info("File downloaded successfully: {}", fileName);

            // Кодирование имени файла в UTF-8 и URL encoding, с заменой + на %20
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .body(data);
        } catch (IOException e) {
            log.error("Download failed for file: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
        s3Service.deleteFile(fileName);
        log.info("File deleted successfully: {}", fileName);
        return ResponseEntity.ok("File deleted successfully");
    }

    @GetMapping
    public ResponseEntity<List<String>> listFiles() {
        List<String> files = s3Service.listFiles();
        log.info("Files listed successfully");
        return ResponseEntity.ok(files);
    }
}
