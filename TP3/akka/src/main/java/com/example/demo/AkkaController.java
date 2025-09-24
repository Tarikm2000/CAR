package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.io.File;


@Controller
@RequestMapping("/akka")
public class AkkaController {

    private final AkkaService akkaService;

    @Autowired
    public AkkaController(AkkaService akkaService) {
        this.akkaService = akkaService;
    }

    @GetMapping("")
    public String home() {
        return "akka/home";
    }

    @GetMapping("init")
    public String init(RedirectAttributes redirectAttributes) {
        akkaService.init();

        String message = (akkaService.getActorSystem() != null) ?
                "Système Akka prêt avec " + akkaService.getMappers().length + " Mappers et " + akkaService.getReducers().length + " Reducers." :
                "Échec de l'initialisation du système Akka. Veuillez réessayer.";

        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/akka";
    }

    @PostMapping("/process-file-mapping")
    public String processFile(@RequestParam("file-input") MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("fileMessage", "Erreur : Aucun fichier sélectionné.");
            return "redirect:/akka";
        }

        File tempFile = File.createTempFile("uploaded-", ".txt");
        file.transferTo(tempFile);
        akkaService.processFile(tempFile);
        redirectAttributes.addFlashAttribute("fileMessage", "Le fichier a été traité avec succès.");
        return "redirect:/akka";
    }

    @PostMapping("/search-word")
    public String searchWord(@RequestParam("search-word-input") String searchWord,
                             RedirectAttributes redirectAttributes) {
        if (searchWord == null || searchWord.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("searchMessage", "Veuillez fournir un mot à rechercher.");
            return "redirect:/akka";
        }

        int occurrences = akkaService.countOccurrences(searchWord);
        String message = occurrences > 0 ?
                "Le mot '" + searchWord + "' apparaît " + occurrences + " fois." :
                "Aucune occurrence du mot '" + searchWord + "' trouvée.";

        redirectAttributes.addFlashAttribute("searchMessage", message);
        return "redirect:/akka";
    }
}
