package com.example.docs.controller;

import com.example.docs.entity.DocumentEntity;
import com.example.docs.entity.FolderEntity;
import com.example.docs.entity.UserEntity;
import com.example.docs.entity.enums.DocumentStatuses;
import com.example.docs.entity.enums.DocumentTypes;
import com.example.docs.entity.enums.Positions;
import com.example.docs.entity.enums.Roles;
import com.example.docs.repository.DocumentEntityRepository;
import com.example.docs.repository.FolderEntityRepository;
import com.example.docs.repository.UserEntityRepository;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.catalina.User;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Controller
@AllArgsConstructor
public class DocumentController {

    private final DocumentEntityRepository documentRepository;
    private final UserEntityRepository userRepository;
    private final FolderEntityRepository folderRepository;

    private final String UPLOAD_DIR = "./uploads/";

    @GetMapping("/document")
    public String document(Authentication authentication, Model model, @RequestParam(name = "id") Long id) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(RuntimeException::new);
        List<UserEntity> users = userRepository.findAll().stream()
                .filter(userEntity -> !userEntity.getRole().equals(Roles.ADMIN.name()))
                .toList();
        Long executorId = document.getExecutor() == null ? -1L : document.getExecutor().getId();
        List<FolderEntity> mainFolders = folderRepository.findByIsMain(true);
        List<FolderEntity> otherFolders = folderRepository.findByIsMain(false);

        boolean canEdit = (user.getUserDetail().getPosition() != null &&
                user.getUserDetail().getPosition().equals(Positions.ХАТШЫ.name())) ||
                user.getId().equals(document.getAuthor().getId()) ||
                (document.getExecutor() != null && user.getId().equals(document.getExecutor().getId())) ||
                (document.getApproval() != null && user.getId().equals(document.getApproval().getId()));
        boolean canApprove = (document.getStatus().equals(DocumentStatuses.ON_APPROVAL.name())) &&
                (document.getApproval() != null) &&
                (document.getApproval().getId().equals(user.getId()));

        model.addAttribute("canApprove", canApprove);
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("incomingType", DocumentTypes.INCOMING.name());
        model.addAttribute("internalType", DocumentTypes.INTERNAL.name());
        model.addAttribute("outgoingType", DocumentTypes.OUTGOING.name());
        model.addAttribute("created", DocumentStatuses.CREATED.name());
        model.addAttribute("onApproval", DocumentStatuses.ON_APPROVAL.name());
        model.addAttribute("underConsideration", DocumentStatuses.UNDER_CONSIDERATION.name());
        model.addAttribute("onExecution", DocumentStatuses.ON_EXECUTION.name());
        model.addAttribute("onAgreement", DocumentStatuses.ON_AGREEMENT.name());
        model.addAttribute("secretary", Positions.ХАТШЫ.name());
        model.addAttribute("i_user", user);
        model.addAttribute("document", document);
        model.addAttribute("users", users);
        model.addAttribute("executorId", executorId);
        model.addAttribute("mainFolders", mainFolders);
        model.addAttribute("otherFolders", otherFolders);
        return "document";
    }

    @GetMapping("/edit-document")
    public String editDocumentShow(Authentication authentication, Model model, Long id) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(RuntimeException::new);
        List<UserEntity> users = userRepository.findAll()
                .stream().filter(userEntity -> userEntity.getRole().equals(Roles.USER.name()))
                .toList();

        model.addAttribute("users", users);
        model.addAttribute("types", DocumentTypes.values());
        model.addAttribute("statuses", DocumentStatuses.values());
        model.addAttribute("document", document);
        model.addAttribute("i_user", user);
        model.addAttribute("approvalId", document.getApproval() == null ? -1 : document.getApproval().getId());
        model.addAttribute("executorId", document.getExecutor() == null ? -1 : document.getExecutor().getId());
        return "edit-document";
    }

    @PostMapping("/edit-document")
    public String editDocument(Long id, LocalDateTime deadLine, String name,
                               String number, String status, String title,
                               String type, Long approvalId, Long executorId,
                               LocalDateTime incomingDate, LocalDateTime outgoingDate,
                               MultipartFile file
    ) {
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(RuntimeException::new);
        UserEntity approval = userRepository.findById(approvalId)
                        .orElse(null);
        UserEntity executor = userRepository.findById(executorId)
                        .orElse(null);

        if (file != null) {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            try {
                Path path = Paths.get(UPLOAD_DIR + fileName);
                while (Files.exists(path)) {
                    int lastIndex = fileName.lastIndexOf(".");
                    fileName = fileName.substring(0, lastIndex) + "_" + (int) (Math.random() * 100) + fileName.substring(lastIndex);
                    System.out.println(fileName);
                    path = Paths.get(UPLOAD_DIR + fileName);
                }

                Files.copy(file.getInputStream(), path);

            } catch (IOException e) {
                e.printStackTrace();
            }
            document.setFilename(fileName);
        }

        document.setDeadline(deadLine);
        document.setName(name);
        document.setNumber(number);
        document.setStatus(status);
        document.setTitle(title);
        document.setType(type);
        document.setApproval(approval);
        document.setExecutor(executor);
        document.setIncomingDate(incomingDate);
        document.setOutgoingDate(outgoingDate);
        document.setUpdatedTime(LocalDateTime.now());

        documentRepository.save(document);
        return "redirect:/document?id=" + id;
    }

    @GetMapping("/download-document")
    public ResponseEntity<byte[]> downloadFile(Long id) throws IOException {
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(RuntimeException::new);
        File file = new File(UPLOAD_DIR + "/" + document.getFilename());
        byte[] data = Files.readAllBytes(file.toPath());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(data.length);
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

}
