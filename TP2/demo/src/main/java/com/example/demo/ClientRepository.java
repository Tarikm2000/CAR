package com.example.demo;

import org.springframework.data.repository.CrudRepository;

public interface  ClientRepository extends CrudRepository<Client, String>  {
   public Client findByEmail(String email);
   public Client findByEmailAndPassword(String email,String password);
}