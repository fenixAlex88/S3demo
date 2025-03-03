package by.alex.demos3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class S3Service {

    private final AmazonS3 amazonS3;
    private final String bucketName;

    @Autowired
    public S3Service(AmazonS3 amazonS3, @Value("${aws.s3.bucket}") String bucketName) {
        this.amazonS3 = amazonS3;
        this.bucketName = bucketName;
        log.debug("S3Service constructor called with bucketName: {}", bucketName);
        createBucketIfNotExists();
    }

    private void createBucketIfNotExists() {
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            amazonS3.createBucket(bucketName);
            log.info("Bucket created: {}", bucketName);
        } else {
            log.info("Bucket already exists: {}", bucketName);
        }
    }

    public void uploadFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        log.debug("Attempting to upload file: {}", fileName);
        if (fileName != null) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));
            log.info("File uploaded: {}", fileName);
        } else {
            throw new IOException("File name is null");
        }
    }


    public byte[] downloadFile(String fileName) throws IOException {
        S3Object s3Object = amazonS3.getObject(bucketName, fileName);
        try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
            log.info("File downloaded: {}", fileName);
            return IOUtils.toByteArray(inputStream);
        }
    }

    public List<String> listFiles() {
        List<String> files = amazonS3.listObjectsV2(bucketName).getObjectSummaries().stream()
                .map(s3ObjectSummary -> s3ObjectSummary.getKey())
                .collect(Collectors.toList());
        log.info("Files listed: {}", files.size());
        return files;
    }
}
