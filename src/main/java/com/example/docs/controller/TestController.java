package com.example.docs.controller;

import com.example.docs.entity.DocumentEntity;
import com.example.docs.entity.UserDetailEntity;
import com.example.docs.entity.UserEntity;
import com.example.docs.entity.enums.DocumentStatuses;
import com.example.docs.entity.enums.DocumentTypes;
import com.example.docs.entity.enums.Roles;
import com.example.docs.repository.DocumentEntityRepository;
import com.example.docs.repository.UserEntityRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@AllArgsConstructor
public class TestController {

    private final UserEntityRepository userRepository;
    private final DocumentEntityRepository documentRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private final String UPLOAD_DIR = "./uploads/";

    @GetMapping("/")
    public String showMainPage(Authentication authentication, Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        model.addAttribute("i_user", user);
        return "index";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @GetMapping("/sign-up")
    public String showSignUp() {
        return "sign-up";
    }

    @PostMapping("/sign-up")
    public String signUp(String name, String surname, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            // TODO
            return "redirect:/sign-up?message=error";
        }
        UserDetailEntity userDetail = UserDetailEntity.builder()
                .name(name)
                .surname(surname)
                .registerDate(LocalDate.now())
                .build();

        UserEntity user = UserEntity.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Roles.USER.name())
                .enabled(false)
                .locked(false)
                .enabled(true)
                .userDetail(userDetail)
                .build();

        userDetail.setUser(user);

        userRepository.save(user);

        //TODO message
        return "redirect:/login?message";
    }

    @GetMapping("/user-profile")
    public String showUserProfile(Authentication authentication, Model model,
                                  @RequestParam(required = false) Long id) {
        if (Objects.isNull(id)) {
            model.addAttribute("user", (UserEntity) authentication.getPrincipal());
        } else {
            // TODO ондай пользователь жоқ
            UserEntity user = userRepository.findById(id)
                    .orElseThrow(()->new RuntimeException("not found"));
            model.addAttribute("user", user);
        }
        model.addAttribute("i_user", (UserEntity) authentication.getPrincipal());
        return "user-profile";
    }

    @GetMapping("/edit-profile")
    public String showEditProfile(Authentication authentication, Model model) {
        model.addAttribute("i_user", (UserEntity) authentication.getPrincipal());
        return "edit-profile";
    }

    @PostMapping("/edit-profile")
    public String editProfile(Long id, String name, String surname, String middleName, String address, String bio, String phoneNumber) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow();
        user.getUserDetail().setName(name);
        user.getUserDetail().setSurname(surname);
        user.getUserDetail().setMiddleName(middleName);
        user.getUserDetail().setAddress(address);
        user.getUserDetail().setBio(bio);
        user.getUserDetail().setPhoneNumber(phoneNumber);
        userRepository.save(user);
        return "redirect:/logout";
    }

    @GetMapping("/error")
    public String showError() {
        return "error";
    }

    @GetMapping("/drop-file")
    public String showDropFile() {
        return "drop-file";
    }

    @GetMapping("/file-manager")
    public String showFileManager(
            Model model,
            @RequestParam(name = "folder", required = false) String folder
    ) {
        List<DocumentEntity> documents = new ArrayList<>();
        if (Objects.isNull(folder) || folder.equalsIgnoreCase(DocumentTypes.INCOMING.name())) {

        } else if (folder.equalsIgnoreCase(DocumentTypes.OUTGOING.name())) {
        } else if (folder.equalsIgnoreCase(DocumentTypes.INTERNAL.name())) {
        } else if (folder.equalsIgnoreCase(DocumentStatuses.ARCHIVE.name())) {
            documents = documentRepository.findAllByStatusIn(List.of(DocumentStatuses.ARCHIVE.name()));
            model.addAttribute("archive", true);
            model.addAttribute("documents", documents);
        }

        return "file-manager";
    }

    @GetMapping("/create-document")
    public String showCreateDocument() {
        return "create-document";
    }

    @PostMapping("/create-document")
    public String createDocument(Authentication authentication, MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        // TODO if filename exists
        try {
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // TODO file not created
            e.printStackTrace();
        }

        DocumentEntity document = DocumentEntity.builder()
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .filename(fileName)
                .status(DocumentStatuses.CREATED.name())
                .author((UserEntity) authentication.getPrincipal())
                .build();

        documentRepository.save(document);

        return "redirect:/file-manager";
    }
}

