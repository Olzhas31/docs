package com.example.docs.controller;

import com.example.docs.entity.UserEntity;
import com.example.docs.repository.LaboratoryEntityRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class LaboratoryController {

    private final LaboratoryEntityRepository laboratoryRepository;

    @GetMapping("/zerthanalar")
    public String zerthanalar(Authentication authentication, Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        model.addAttribute("i_user", user);
        model.addAttribute("zerthanalar", laboratoryRepository.findAll());
        return "zerthanalar";
    }
}
