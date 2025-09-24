package com.example.demo;

import org.springframework.data.repository.CrudRepository;

public interface  CommandeRepository extends CrudRepository<Commande, Long>{

    public Iterable<Commande> findAllByClient(Client client);

}