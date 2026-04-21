package com.alvarengacarlos.sakura.gatewayapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UploadController {

    private final UploadService uploadService;

    @GetMapping("/")
    public String uploadPage(
            @RequestParam(required = false) String success,
            @RequestParam(required = false) String error,
            Model model) {
        if (success != null) {
            model.addAttribute("success", true);
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "upload";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, Model model) {
        log.info("Uploading file");
        try {
            uploadService.upload(file);
            log.info("Successfully uploaded");
            return "redirect:/?success";
        } catch (UnsupportedImageTypeException ex) {
            log.warn(ex.getMessage());
            return "redirect:/?error=" + ex.getMessage();
        }
    }
}
