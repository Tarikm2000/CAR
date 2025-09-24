package com.example.demo;

import java.util.Optional;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommandeService implements CommandeItf {

    @Autowired
    private CommandeRepository repo;

    @Autowired
    private ArticleRepository articleRepo;

    @Autowired
    private LigneCommandeRepository ligneCommandeRepo;

    @Override
    public Iterable<Commande> findAll() {
        return repo.findAll();
    }

    @Override
    public void create(String nom, Client client) {
        var commande = new Commande(nom);
        commande.setClient(client);
        client.addCommande(commande);
        repo.save(commande);
    }

    @Override
    public Iterable<Commande> findAllByClient(Client client) {
        return repo.findAllByClient(client);
    }

    @Override
    public void addArticleToCommande(Long commandeId, String articleNom, int quantity, double prix) {
        Commande commande = findCommandeById(commandeId);
        Article article = findOrCreateArticle(articleNom, prix);
        LigneCommande ligneCommande = findOrCreateLigneCommande(commande, article, quantity);
        repo.save(commande);
    }

    @Override
    public void removeArticleFromCommande(Long commandeId, Long articleId) {
        Commande commande = findCommandeById(commandeId);
        removeArticleFromCommande(commande, articleId);
        repo.save(commande);
    }

    @Override
    public Optional<Commande> findById(Long id) {
        return repo.findById(id);
    }

    private Commande findCommandeById(Long commandeId) {
        return repo.findById(commandeId)
                .orElseThrow(() -> new NoSuchElementException("Commande non trouvée"));
    }

    private Article findOrCreateArticle(String articleNom, double prix) {
        return articleRepo.findByNomArticleAndPrixUnitaire(articleNom, prix)
                .orElseGet(() -> createArticle(articleNom, prix));
    }

    private Article createArticle(String articleNom, double prix) {
        Article newArticle = new Article(articleNom, prix);
        articleRepo.save(newArticle);
        return newArticle;
    }

    private LigneCommande findOrCreateLigneCommande(Commande commande, Article article, int quantity) {
        return commande.getLigneCommande().stream()
                .filter(lc -> lc.getArticle().equals(article))
                .findFirst()
                .map(ligneCommande -> updateLigneCommande(ligneCommande, quantity))
                .orElseGet(() -> createLigneCommande(commande, article, quantity));
    }

    private LigneCommande updateLigneCommande(LigneCommande ligneCommande, int quantity) {
        ligneCommande.setNbArticle(ligneCommande.getNbArticle() + quantity);
        ligneCommandeRepo.save(ligneCommande);
        return ligneCommande;
    }

    private LigneCommande createLigneCommande(Commande commande, Article article, int quantity) {
        LigneCommande ligneCommande = new LigneCommande();
        ligneCommande.setArticle(article);
        ligneCommande.setNbArticle(quantity);
        ligneCommande.setCommande(commande);
        ligneCommandeRepo.save(ligneCommande);
        commande.getLigneCommande().add(ligneCommande);
        return ligneCommande;
    }

    private void removeArticleFromCommande(Commande commande, Long articleId) {
        boolean removed = commande.getLigneCommande().removeIf(lc -> lc.getArticle().getId().equals(articleId));
        if (!removed) {
            throw new NoSuchElementException("Article non trouvé dans la commande");
        }
    }

    @Override
    public double calculTotalCommande(Commande commande) {
        return commande.getLigneCommande().stream()
                .mapToDouble(ligne -> ligne.getNbArticle() * ligne.getArticle().getPrixUnitaire())
                .sum();
    }}

