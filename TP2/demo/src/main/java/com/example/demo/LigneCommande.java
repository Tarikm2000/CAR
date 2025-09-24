package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class LigneCommande {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "article_id", referencedColumnName = "id")
    private Article article;

    private int NbArticle;

    @ManyToOne
    private Commande commande;


    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public int getNbArticle() {
        return NbArticle;
    }

    public void setNbArticle(int nbArticle) {
        NbArticle = nbArticle;
    }


    public void setCommande(Commande commande) {
        this.commande = commande;
    }


}