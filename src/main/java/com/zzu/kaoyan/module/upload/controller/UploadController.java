package com.zzu.kaoyan.module.upload.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zzu.kaoyan.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@Tag(name = "文件上传", description = "图片和视频上传接口")
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    private static final Set<String> IMAGE_EXTS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> VIDEO_EXTS = Set.of("mp4", "webm", "mov", "avi");

    @Value("${app.upload.path}")
    private String uploadPath;

    @Value("${app.upload.image.max-size}")
    private long imageMaxSize;

    @Value("${app.upload.video.max-size}")
    private long videoMaxSize;

    @Operation(summary = "上传图片")
    @PostMapping("/image")
    @SaCheckLogin
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }
        if (file.getSize() > imageMaxSize) {
            return Result.error(400, "图片大小不能超过 10MB");
        }

        String ext = getExtension(file.getOriginalFilename());
        if (ext == null || !IMAGE_EXTS.contains(ext)) {
            return Result.error(400, "图片格式不支持，允许: jpg, jpeg, png, gif, webp");
        }

        if (!isImageMagicMatch(file, ext)) {
            return Result.error(400, "文件内容与扩展名不匹配");
        }

        try {
            String url = saveFile(file, "images", ext);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success(data);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.error(500, "上传失败，请稍后重试");
        }
    }

    @Operation(summary = "上传视频")
    @PostMapping("/video")
    @SaCheckLogin
    public Result<Map<String, String>> uploadVideo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }
        if (file.getSize() > videoMaxSize) {
            return Result.error(400, "视频大小不能超过 100MB");
        }

        String ext = getExtension(file.getOriginalFilename());
        if (ext == null || !VIDEO_EXTS.contains(ext)) {
            return Result.error(400, "视频格式不支持，允许: mp4, webm, mov, avi");
        }

        if (!isVideoMagicMatch(file, ext)) {
            return Result.error(400, "文件内容与扩展名不匹配");
        }

        try {
            String url = saveFile(file, "videos", ext);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success(data);
        } catch (IOException e) {
            log.error("视频上传失败", e);
            return Result.error(500, "上传失败，请稍后重试");
        }
    }

    private String saveFile(MultipartFile file, String subdir, String ext) throws IOException {
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String relativePath = "/uploads/" + subdir + "/" + dateDir + "/" + filename;

        Path dir = Path.of(uploadPath, subdir, dateDir);
        Files.createDirectories(dir);
        file.transferTo(dir.resolve(filename));

        return relativePath;
    }

    private boolean isImageMagicMatch(MultipartFile file, String ext) {
        try (InputStream in = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = in.read(header);
            if (read < 4) return false;

            // JPEG: FF D8 FF
            if (Set.of("jpg", "jpeg").contains(ext)) {
                return (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF;
            }
            // PNG: 89 50 4E 47
            if ("png".equals(ext)) {
                return (header[0] & 0xFF) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G';
            }
            // GIF: 47 49 46 38
            if ("gif".equals(ext)) {
                return header[0] == 'G' && header[1] == 'I' && header[2] == 'F';
            }
            // WEBP: RIFF....WEBP
            if ("webp".equals(ext) && read >= 12) {
                return header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                        && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    private boolean isVideoMagicMatch(MultipartFile file, String ext) {
        try (InputStream in = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = in.read(header);
            if (read < 12) return false;

            // MP4 / MOV: offset 4 = "ftyp"
            if (Set.of("mp4", "mov").contains(ext)) {
                return header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p';
            }
            // WEBM: 1A 45 DF A3 (EBML)
            if ("webm".equals(ext)) {
                return (header[0] & 0xFF) == 0x1A && (header[1] & 0xFF) == 0x45
                        && (header[2] & 0xFF) == 0xDF && (header[3] & 0xFF) == 0xA3;
            }
            // AVI: "RIFF" + "AVI " at offset 8
            if ("avi".equals(ext)) {
                return header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                        && header[8] == 'A' && header[9] == 'V' && header[10] == 'I' && header[11] == ' ';
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    private String getExtension(String filename) {
        if (filename == null) return null;
        int i = filename.lastIndexOf('.');
        if (i < 0) return null;
        return filename.substring(i + 1).toLowerCase();
    }
}
