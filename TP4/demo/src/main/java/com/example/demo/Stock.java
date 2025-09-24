package com.example.demo;

import java.util.HashMap;
import java.util.Map;



public class Stock {
    private Map<String, Integer> stock = new HashMap<>();

    public Stock() {
    }



    public void addStock(String product, Integer quantity) {
        stock.put(product, stock.getOrDefault(product, 0) + quantity);
    }
    public Map<String, Integer> getStock() {
        return stock;
    }
}