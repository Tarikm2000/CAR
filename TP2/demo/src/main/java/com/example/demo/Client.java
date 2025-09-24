package com.example.demo;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Client {

    @Id
    private String email;
    private String password;
    private String nom;
    private String prenom;

    @OneToMany
    private List<Commande> listeCommande;

    public Client() {}

    public Client(String email, String password, String nom, String prenom ){
        this.email = email;
        this.password = password;
        this.nom = nom;
        this.prenom = prenom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }


    public String getNom() {
        return nom;
    }



    public void addCommande(Commande commande) {
        this.listeCommande.add(commande);
    }

}