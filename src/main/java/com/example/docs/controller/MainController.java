package com.example.docs.controller;

import com.example.docs.entity.*;
import com.example.docs.entity.enums.Positions;
import com.example.docs.entity.enums.Roles;
import com.example.docs.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@AllArgsConstructor
public class MainController {

    private final UserEntityRepository userRepository;
    private final LaboratoryEntityRepository laboratoryRepository;
    private final ProjectRepository projectRepository;
    private final DocumentEntityRepository documentRepository;
    private final TagEntityRepository tagRepository;

    @GetMapping("/")
    public String showMainPage(Authentication authentication, Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (user.getRole().equals(Roles.ADMIN.name())) {
            return "redirect:/admin";
        }

        List<UserEntity> users = userRepository.findAll()
                .stream()
                .filter(userEntity -> userEntity.getRole().equals(Roles.USER.name()))
                .toList();

        List<LaboratoryEntity> laboratories = laboratoryRepository.findAll();
        List<ProjectEntity> projects = projectRepository.findAll();
        List<DocumentEntity> documents = documentRepository.findAll();

        model.addAttribute("documents", documents);
        model.addAttribute("projects", projects);
        model.addAttribute("laboratories", laboratories);
        model.addAttribute("users", users);
        model.addAttribute("i_user", user);
        return "index";
    }

    @GetMapping("/bookkeeper")
    public String bookkeeper(Authentication authentication, Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        List<UserEntity> users = userRepository.findAll()
                .stream()
                .filter(userEntity -> userEntity.getRole().equals(Roles.USER.name()))
                .filter(userEntity -> !Objects.isNull(userEntity.getUserDetail().getPosition()))
                .filter(userEntity -> userEntity.getUserDetail().getPosition().equals(Positions.БАС_БУХГАЛТЕР.name()) ||
                        userEntity.getUserDetail().getPosition().equals(Positions.БУХГАЛТЕР.name()) ||
                        userEntity.getUserDetail().getPosition().equals(Positions.ЭКОНОмИСТ.name()))
                .toList();
        model.addAttribute("i_user", user);
        model.addAttribute("users", users);
        return "bookkeeper";
    }

    @GetMapping("/kadry")
    public String kadry(Authentication authentication, Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        List<UserEntity> users = userRepository.findAll()
                .stream()
                .filter(userEntity -> userEntity.getRole().equals(Roles.USER.name()))
                .filter(userEntity -> userEntity.getUserDetail().getPosition() != null)
                .filter(userEntity -> userEntity.getUserDetail().getPosition().equals(Positions.ХАТШЫ.name()) ||
                        userEntity.getUserDetail().getPosition().equals(Positions.ЗАҢГЕР.name()))
                .toList();
        model.addAttribute("i_user", user);
        model.addAttribute("users", users);
        return "kadry";
    }

    @GetMapping("/akimshilik")
    public String akimshilik(Authentication authentication, Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        List<UserEntity> users = userRepository.findAll()
                .stream()
                .filter(userEntity -> userEntity.getRole().equals(Roles.USER.name()))
                .filter(userEntity -> userEntity.getUserDetail().getPosition() != null)
                .filter(userEntity -> userEntity.getUserDetail().getPosition().equals(Positions.БАС_ДИРЕКТОР.name()) ||
                        userEntity.getUserDetail().getPosition().equals(Positions.ДИРЕКТОР_ОРЫНБАСАРЫ.name()))
                .toList();

        model.addAttribute("i_user", user);
        model.addAttribute("users", users);
        return "akimshilik";
    }

    @GetMapping("/search-document")
    public String searchingDocument(Authentication authentication, Model model,
                                    @RequestParam(name = "request") String request
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        TagEntity tag = tagRepository.findByName(request);
        List<DocumentEntity> documents = new ArrayList<>();
        if (tag != null) {
            documents = tag.getDocuments();
        }

        model.addAttribute("i_user", user);
        model.addAttribute("documents", documents);
        return "search-document";
    }
}
