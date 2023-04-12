package com.example.docs.controller;

import com.example.docs.entity.DocumentEntity;
import com.example.docs.entity.ProjectEntity;
import com.example.docs.entity.UserEntity;
import com.example.docs.entity.enums.Roles;
import com.example.docs.repository.ProjectRepository;
import com.example.docs.repository.UserEntityRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Arrays;
import java.util.List;

@Controller
@AllArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserEntityRepository userRepository;

    @GetMapping("/projects")
    public String projects(Authentication authentication, Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        List<ProjectEntity> projects = projectRepository.findAll();
        model.addAttribute("projects", projects);
        model.addAttribute("i_user", user);
        return "projects";
    }

    @GetMapping("/create-project")
    public String createProjectShow(Authentication authentication, Model model) {
        List<UserEntity> users = userRepository.findAll()
                .stream()
                .filter(userEntity -> userEntity.getRole().equals(Roles.USER.name()))
                .toList();
        model.addAttribute("i_user", authentication.getPrincipal());
        model.addAttribute("users", users);
        return "create-project";
    }

    @PostMapping("/create-project")
    public String createProject(Authentication authentication, String name, String description, Long[] usersId){
        UserEntity user = (UserEntity) authentication.getPrincipal();
        ProjectEntity project = ProjectEntity.builder()
                .name(name)
                .description(description)
                .author(user)
                .users(userRepository.findAllById(List.of(usersId)))
                .size(0)
                .build();

        projectRepository.save(project);
        return "redirect:/projects";
    }

    @GetMapping("/update-project")
    public String updateProjectShow(Authentication authentication, Model model, Long id) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(RuntimeException::new);
        List<UserEntity> users = userRepository.findAll()
                .stream()
                .filter(userEntity -> userEntity.getRole().equals(Roles.USER.name()))
                .toList();

        model.addAttribute("users", users);
        model.addAttribute("project", project);
        model.addAttribute("i_user", user);
        return "update-project";
    }

    @PostMapping("/update-project")
    public String updateProject(Long id, String name, String description, Long[] usersId) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(RuntimeException::new);
        project.setName(name);
        project.setDescription(description);
        project.setUsers(userRepository.findAllById(List.of(usersId)));

        projectRepository.save(project);
        return "redirect:/projects";
    }


}
