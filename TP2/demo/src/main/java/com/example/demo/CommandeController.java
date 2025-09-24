package com.example.demo;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;



import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/store/client")
public class CommandeController {

    @Autowired
    private CommandeItf service;

    @Autowired
    private ClientItf clientService;

    @PostMapping("/createCommand")
    public RedirectView create(@RequestParam String nom, HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null || clientService.findByEmail(email) == null) {
            return new RedirectView("/store/home");
        }
        Client client = clientService.findByEmail(email);
        service.create(nom, client);
        return new RedirectView("/store/client");
    }

    @PostMapping("/addArticle")
    public RedirectView addArticle(@RequestParam Long commandeId, @RequestParam String articleNom, @RequestParam int quantity, @RequestParam double price, HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null || clientService.findByEmail(email) == null) {
            return new RedirectView("/store/home");
        }

        service.addArticleToCommande(commandeId, articleNom, quantity, price);
        return new RedirectView("/store/client/commande/" + commandeId);
    }

    @PostMapping("/removeArticle")
    public RedirectView removeArticle(@RequestParam Long commandeId, @RequestParam Long articleId, HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null || clientService.findByEmail(email) == null) {
            return new RedirectView("/store/home");
        }
        service.removeArticleFromCommande(commandeId, articleId);
        return new RedirectView("/store/client/commande/" + commandeId);
    }

    @GetMapping("/commande/{id}")
    public Object commandePage(@PathVariable Long id, HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null || clientService.findByEmail(email) == null) {
            return new RedirectView("/store/home");
        }
        try {
            Commande commande = service.findById(id).orElseThrow();
            var model = Map.of(
                    "commande", commande,
                    "articles", commande.getLigneCommande()
            );
            return new ModelAndView("/store/commande", model);
        } catch (NoSuchElementException e) {
            return new RedirectView("/store/client");
        }

    }

    @PostMapping("/commande/print")
    public Object printPage(HttpSession session, @RequestParam Long commandeId) {
        String email = (String) session.getAttribute("email");
        try {
            Commande commande = service.findById(commandeId).orElseThrow();
            var model = Map.of(
                    "commande", commande,
                    "articles", commande.getLigneCommande(),
                    "totalCommande", service.calculTotalCommande(commande)
            );
            return new ModelAndView("/store/print", model);
        } catch (NoSuchElementException e) {
            return new RedirectView("/store/client");
        }
    }



}