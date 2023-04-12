package com.example.docs.controller;

import com.example.docs.entity.UserEntity;
import com.example.docs.entity.enums.Roles;
import com.example.docs.repository.UserEntityRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@AllArgsConstructor
public class AdminController {

    private final UserEntityRepository userRepository;

    @GetMapping("/admin")
    public String admin(Authentication authentication, Model model) {
        List<UserEntity> users = userRepository.findAll()
                .stream().filter(userEntity -> userEntity.getRole().equals(Roles.USER.name()))
                .toList();
        model.addAttribute("newUsers", users.stream().filter(userEntity -> !userEntity.getEnabled()).toList());
        model.addAttribute("users", users.stream().filter(UserEntity::getEnabled).toList());
        return "admin";
    }

    @GetMapping("/enable-user")
    public String enableUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(RuntimeException::new);
        user.setEnabled(true);
        userRepository.save(user);
        return "redirect:/admin";
    }
}
