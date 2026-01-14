package com.example.budgettracker.controller;

import com.example.budgettracker.entity.User;
import com.example.budgettracker.repository.UserRepository;
import com.example.budgettracker.util.FileUploadUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute("user") User fullUserForm,
            RedirectAttributes redirectAttributes) {
        String username = userDetails.getUsername();
        User existingUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setFullName(fullUserForm.getFullName());
        existingUser.setEmail(fullUserForm.getEmail());
        existingUser.setMobile(fullUserForm.getMobile());

        userRepository.save(existingUser);

        redirectAttributes.addFlashAttribute("message", "Profile updated successfully!");
        return "redirect:/profile";
    }

    @PostMapping("/upload-image")
    public String uploadImage(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("image") MultipartFile multipartFile,
            RedirectAttributes redirectAttributes) throws IOException {

        if (multipartFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
            return "redirect:/profile";
        }

        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        user.setProfilePicture(fileName);
        userRepository.save(user);

        String uploadDir = "user-photos/" + user.getId();
        FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);

        redirectAttributes.addFlashAttribute("message", "Profile picture updated successfully!");
        return "redirect:/profile";
    }
}
