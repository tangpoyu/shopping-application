package com.example.client.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class ImageValidator implements ConstraintValidator<ImageValidation, MultipartFile> {
    @Override
    public boolean isValid(MultipartFile image, ConstraintValidatorContext context) {
        String extension = FilenameUtils.getExtension(
                image.getOriginalFilename());
        if (!isSupportedExtension(extension))
            return false;
        Tika tika = new Tika();
        String mimeType = null;
        try {
            mimeType = tika.detect(image.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!isSupportedContentType(mimeType))
            return false;

        return true;
    }

    private boolean isSupportedExtension(String extension) {
        return extension != null && (
                extension.equals("png")
                        || extension.equals("jpg")
                        || extension.equals("jpeg"));
    }

    private boolean isSupportedContentType(String contentType) {
        return contentType.equals("image/png")
                || contentType.equals("image/jpg")
                || contentType.equals("image/jpeg");
    }
}
