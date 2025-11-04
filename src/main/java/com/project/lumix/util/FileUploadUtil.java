package com.project.lumix.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

public final class FileUploadUtil {
    private static final String DATE_FORMAT = "yyyyMMddHHmmss";

    private FileUploadUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static boolean isAllowedExtension(String fileName, String pattern) {
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(fileName).matches();
    }

    public static void assertAllowed(MultipartFile file, String pattern) {
        String fileName = file.getOriginalFilename();
        if (!isAllowedExtension(fileName, pattern)) {
            throw new IllegalArgumentException(
                    String.format("Invalid file type. Allowed pattern: %s", pattern)
            );
        }
    }

    public static String getUniqueFileName(MultipartFile file) {
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        String baseName = FilenameUtils.getBaseName(originalFilename);
        String extension = FilenameUtils.getExtension(originalFilename);

        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String dateStr = dateFormat.format(System.currentTimeMillis());

        return String.format("%s_%s.%s", baseName, dateStr, extension);
    }
}

