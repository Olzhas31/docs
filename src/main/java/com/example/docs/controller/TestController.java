package com.example.docs.controller;

import com.example.docs.entity.*;
import com.example.docs.entity.enums.DocumentStatuses;
import com.example.docs.entity.enums.DocumentTypes;
import com.example.docs.entity.enums.Positions;
import com.example.docs.entity.enums.Roles;
import com.example.docs.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
public class TestController {

    private final UserEntityRepository userRepository;
    private final DocumentEntityRepository documentRepository;
    private final ArticleEntityRepository articleEntityRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TagEntityRepository tagEntityRepository;

    private final String UPLOAD_DIR = "./uploads/";
    private final FolderEntityRepository folderEntityRepository;
    private final LaboratoryEntityRepository laboratoryEntityRepository;

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
        UserEntity iUser = (UserEntity) authentication.getPrincipal();
        UserEntity user = iUser;

        if (!Objects.isNull(id)) {
            // TODO ондай пользователь жоқ
            user = userRepository.findById(id)
                    .orElseThrow(()->new RuntimeException("not found"));
        }

        List<ArticleEntity> articles = articleEntityRepository.findByAuthorsContains(user);

        model.addAttribute("i_user", iUser);
        model.addAttribute("user", user);
        model.addAttribute("articles", articles);
        return "user-profile";
    }

    @GetMapping("/edit-profile")
    public String showEditProfile(Authentication authentication, Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        Long laboratoryId = Objects.isNull(user.getUserDetail().getLaboratory()) ? null :
                user.getUserDetail().getLaboratory().getId();

        model.addAttribute("i_user", user);
        model.addAttribute("laboratories", laboratoryEntityRepository.findAll());
        model.addAttribute("laboratoryId", laboratoryId);
        model.addAttribute("positions", Positions.values());
        return "edit-profile";
    }

    @PostMapping("/edit-profile")
    public String editProfile(Long id, String name, String surname,
                              String middleName, String address, String bio,
                              String phoneNumber, Long laboratoryId, String position) {
        LaboratoryEntity laboratoryEntity = Objects.isNull(laboratoryId) ? null:
                laboratoryEntityRepository.findById(laboratoryId).orElse(null);
        UserEntity user = userRepository.findById(id)
                .orElseThrow();
        user.getUserDetail().setName(name);
        user.getUserDetail().setSurname(surname);
        user.getUserDetail().setMiddleName(middleName);
        user.getUserDetail().setAddress(address);
        user.getUserDetail().setBio(bio);
        user.getUserDetail().setPhoneNumber(phoneNumber);
        user.getUserDetail().setLaboratory(laboratoryEntity);
        user.getUserDetail().setPosition(Objects.equals(position, "-1") ? null :
                String.valueOf(Positions.valueOf(position)));
        userRepository.save(user);
        return "redirect:/logout";
    }

    @PostMapping("/edit-account")
    public String editAccount(Authentication authentication, @RequestParam String email, @RequestParam String password) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (!user.getEmail().equalsIgnoreCase(email)) {
            if (userRepository.existsByEmail(email)) {
                return "redirect:/edit-profile?emailAlreadyExists";
            }
            user.setEmail(email);
        }
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return "redirect:/logout?success_update";
    }

    @GetMapping("/error")
    public String showError() {
        return "error";
    }


    @GetMapping("/incoming-documents")
    public String incomingDocuments(Authentication authentication,
                                    Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        List<DocumentEntity> documents = documentRepository.findAllByType(DocumentTypes.INCOMING.name())
                .stream()
                .filter(document -> !document.getStatus().equalsIgnoreCase(DocumentStatuses.ARCHIVE.name()))
                .collect(Collectors.toList());

        model.addAttribute("i_user", user);
        model.addAttribute("documents", documents);
        return "incoming-documents";
    }

    @GetMapping("/outgoing-documents")
    public String outgoingDocuments(Authentication authentication,
                                    Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        List<DocumentEntity> documents = documentRepository.findAllByType(DocumentTypes.OUTGOING.name())
                .stream()
                .filter(document -> !document.getStatus().equalsIgnoreCase(DocumentStatuses.ARCHIVE.name()))
                .toList();

        model.addAttribute("i_user", user);
        model.addAttribute("documents", documents);
        return "outgoing-documents";
    }

    @GetMapping("/internal-documents")
    public String internalDocuments(Authentication authentication,
                                    Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        List<DocumentEntity> documents = documentRepository.findAllByType(DocumentTypes.INTERNAL.name())
                .stream()
                .filter(document -> !document.getStatus().equalsIgnoreCase(DocumentStatuses.ARCHIVE.name()))
                .collect(Collectors.toList());

        model.addAttribute("i_user", user);
        model.addAttribute("documents", documents);
        return "internal-documents";
    }

    @GetMapping("/selected-documents")
    public String selectedDocuments(Authentication authentication,
                                    Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        TagEntity selected = tagEntityRepository.findByName("selected");

        List<DocumentEntity> documents = documentRepository.findAll()
                .stream()
                .filter(document -> document.getTags().contains(selected))
                .collect(Collectors.toList());

        model.addAttribute("i_user", user);
        model.addAttribute("documents", documents);
        return "selected-documents";
    }

    @GetMapping("/create-document")
    public String showCreateDocument(Authentication authentication, Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        model.addAttribute("i_user", user);
        model.addAttribute("types", DocumentTypes.values());
        return "create-document";
    }

    @PostMapping("/create-document")
    public String createDocument(Authentication authentication,
                                 MultipartFile file,
                                 String name,
                                 String type,
                                 String title) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
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
            // TODO file not created
            e.printStackTrace();
        }

        DocumentEntity document = DocumentEntity.builder()
                .name(name)
                .type(type)
                .title(title)
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .filename(fileName)
                .status(DocumentStatuses.CREATED.name())
                .author(user)
                .build();

        document = documentRepository.save(document);

        return "redirect:/document?id=" + document.getId();
    }

    @GetMapping("/register-document-1")
    public String registerDocument(Authentication authentication, Model model,
                                   @RequestParam(name = "id") Long id) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(RuntimeException::new);
        List<UserEntity> users = userRepository.findAll().stream()
                .filter(userEntity -> !userEntity.getRole().equals(Roles.ADMIN.name()))
                .filter(userEntity -> userEntity.getUserDetail().getPosition() != null)
                .filter(userEntity -> userEntity.getUserDetail().getPosition().equals(Positions.ЗЕРТХАНА_ЖЕТЕКШІСІ.name()))
                .toList();
        model.addAttribute("document", document);
        model.addAttribute("i_user", user);
        model.addAttribute("users", users);
        return "register-document";
    }

    // incoming, internal documents
    @PostMapping("/register-document-1")
    public String registerDocument(Long id, LocalDateTime registerTime, String number, Long approvalId, LocalDateTime incomingDate) {
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(RuntimeException::new);
        UserEntity approval = userRepository.findById(approvalId)
                .orElse(null);
        document.setRegisterTime(registerTime);
        document.setNumber(number);
        document.setStatus(DocumentStatuses.UNDER_CONSIDERATION.name());
        document.setUpdatedTime(LocalDateTime.now());
        document.setApproval(approval);
        document.setIncomingDate(incomingDate);

        documentRepository.save(document);
        return "redirect:/document?id=" + id;
    }

    @GetMapping("/register-document-2")
    public String registerDocument2(Authentication authentication, Model model,
                                   @RequestParam(name = "id") Long id) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(RuntimeException::new);
        model.addAttribute("document", document);
        model.addAttribute("i_user", user);
        return "register-document-2";
    }

    // outgoing documents
    @PostMapping("/register-document-2")
    public String registerDocument2(Long id, LocalDateTime registerTime, String number, LocalDateTime outgoingDate) {
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(RuntimeException::new);
        document.setRegisterTime(registerTime);
        document.setNumber(number);
        document.setUpdatedTime(LocalDateTime.now());
        document.setOutgoingDate(outgoingDate);

        documentRepository.save(document);
        return "redirect:/document?id=" + id;
    }

    @PostMapping("/document-to-execute")
    public String documentToExecute(Long documentId, Long executorId, LocalDateTime deadLine) {
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(RuntimeException::new);
        UserEntity executor = userRepository.findById(executorId)
                .orElseThrow(RuntimeException::new);
        document.setExecutor(executor);
        document.setStatus(DocumentStatuses.ON_EXECUTION.name());
        document.setDeadline(deadLine);
        document.setUpdatedTime(LocalDateTime.now());

        documentRepository.save(document);
        return "redirect:/document?id=" + documentId;
    }

    @PostMapping("/document-to-agreement")
    public String documentToAgreement(Long documentId) {
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(RuntimeException::new);
        document.setStatus(DocumentStatuses.ON_AGREEMENT.name());
        document.setUpdatedTime(LocalDateTime.now());
        documentRepository.save(document);
        return "redirect:/document?id=" + documentId;
    }

    @PostMapping("/document-approve")
    public String documentApprove(Long documentId) {
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(RuntimeException::new);
        document.setStatus(DocumentStatuses.ON_APPROVAL.name());
        document.setUpdatedTime(LocalDateTime.now());
        documentRepository.save(document);
        return "redirect:/document?id=" + documentId;
    }

    @PostMapping("/add-tag-to-document")
    public String addTagToDocument(Long id, String tagName) {
        TagEntity tag;
        if (tagEntityRepository.existsByName(tagName)) {
            tag = tagEntityRepository.findByName(tagName);
        } else {
            tag = tagEntityRepository.save(TagEntity.builder().name(tagName).build());
        }
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(RuntimeException::new);
        if (!document.getTags().contains(tag)) {
            document.getTags().add(tag);
        }

        documentRepository.save(document);
        return "redirect:/document?id=" + id;
    }

    @PostMapping("/document-to-archive")
    public String documentToArchive(Long documentId, Long mainFolderId, Long otherFolderId) {
        FolderEntity folder = folderEntityRepository.findById(mainFolderId)
                .orElseThrow(RuntimeException::new);
        if (otherFolderId != -1) {
            folder = folderEntityRepository.findById(otherFolderId)
                    .orElseThrow(RuntimeException::new);
        }

        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(RuntimeException::new);
        document.setStatus(DocumentStatuses.ARCHIVE.name());
        document.setUpdatedTime(LocalDateTime.now());
        document.setFolder(folder);

        documentRepository.save(document);
        return "redirect:/document?id=" + documentId;
    }

    @GetMapping("/conferences")
    public String conferences(Authentication authentication, Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        model.addAttribute("i_user", user);
        return "conferences";
    }

    @GetMapping("/events")
    public String events(Authentication authentication, Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        model.addAttribute("i_user", user);
        return "events";
    }

    @GetMapping("/about-us")
    public String aboutUs(Authentication authentication, Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        model.addAttribute("i_user", user);
        return "about-us";
    }

    @GetMapping("/bailanis")
    public String bailanis(Authentication authentication, Model model) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        model.addAttribute("i_user", user);
        return "bailanis";
    }

}

