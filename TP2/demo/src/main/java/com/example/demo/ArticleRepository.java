package com.example.demo;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;


public interface ArticleRepository extends CrudRepository<Article, Long> {

    Optional<Article> findByNomArticleAndPrixUnitaire(String articleNom, double prix);
}