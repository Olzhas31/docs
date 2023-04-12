package com.example.docs.controller;

import com.example.docs.entity.DocumentEntity;
import com.example.docs.entity.FolderEntity;
import com.example.docs.entity.UserEntity;
import com.example.docs.entity.enums.DocumentStatuses;
import com.example.docs.repository.DocumentEntityRepository;
import com.example.docs.repository.FolderEntityRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
public class FolderController {

    private final FolderEntityRepository folderRepository;
    private final DocumentEntityRepository documentRepository;

    @GetMapping("/file-manager")
    public String showFileManager(Authentication authentication, Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        List<FolderEntity> folders = folderRepository.findByIsMain(true);

        model.addAttribute("folders", folders);
        model.addAttribute("i_user", user);
        return "file-manager";
    }

    @GetMapping("/other-folders")
    public String otherFolders(Authentication authentication, Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        List<FolderEntity> folders = folderRepository.findByIsMain(false);

        model.addAttribute("folders", folders);
        model.addAttribute("i_user", user);
        return "other-folders";
    }

    @GetMapping("/folder")
    public String folder(Authentication authentication, Model model,
                         @RequestParam(name = "id") Long id) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        FolderEntity folder = folderRepository.findById(id)
                .orElseThrow(RuntimeException::new);
        List<DocumentEntity> documents = documentRepository.findAllByFolder(folder)
                .stream()
                .filter(document -> document.getStatus().equals(DocumentStatuses.ARCHIVE.name()))
                .toList();
        List<FolderEntity> otherFolder = folderRepository.findByIsMain(false);

        model.addAttribute("folder", folder);
        model.addAttribute("documents", documents);
        model.addAttribute("i_user", user);
        model.addAttribute("otherFolders", otherFolder);
        return "folder";
    }

    @GetMapping("/create-folder")
    public String createFolderPage(Authentication authentication, Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        model.addAttribute("i_user", user);
        return "create-folder";
    }

    @PostMapping("/create-folder")
    public String createFolder(String name) {
        FolderEntity folder = FolderEntity.builder()
                .name(name)
                .isMain(false)
                .build();
        folderRepository.save(folder);
        return "redirect:/other-folders";
    }

}
