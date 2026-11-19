package com.sip.aurahub.service;

import com.sip.aurahub.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path root;

    public FileStorageService(@Value("${app.upload-dir}") String dir) throws IOException {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public Path getRoot() {
        return root;
    }

    /** Enregistre l'image sur disque et renvoie son URL publique (/uploads/xxx.jpg). */
    public String store(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("Seules les images sont acceptées");
        }
        String original = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), ""));
        int dot = original.lastIndexOf('.');
        String ext = dot >= 0 ? original.substring(dot).toLowerCase() : "";
        if (!ext.matches("\\.(jpg|jpeg|png|webp|gif)")) {
            throw new BusinessException("Format non supporté (jpg, jpeg, png, webp, gif)");
        }
        String name = UUID.randomUUID() + ext;
        try {
            Files.copy(file.getInputStream(), root.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("Échec de l'enregistrement du fichier");
        }
        return "/uploads/" + name;
    }

    public void delete(String url) {
        if (url == null || !url.startsWith("/uploads/")) return;
        try {
            Path target = root.resolve(url.substring("/uploads/".length())).normalize();
            if (target.startsWith(root)) Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // fichier déjà absent : sans importance
        }
    }
}
