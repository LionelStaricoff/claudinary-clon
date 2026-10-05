package com.openlabmx.claudinary.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "imagehost.storage")
public class StorageProperties {
    private String directory = "./uploads";
    private String webpDirectory = "./uploads/webp";
    private String thumbnailDirectory = "./uploads/thumbnails";
    private long maxFileSize = 10L * 1024L * 1024L;
    private long maxStoragePerUser = 100L * 1024L * 1024L;
    private String baseUrl = "http://localhost:8080";
    
    public String getUploadDir() {
        return directory;
    }
    
    public String getWebpDir() {
        return webpDirectory;
    }
    
    public String getThumbnailDir() {
        return thumbnailDirectory;
    }
}
