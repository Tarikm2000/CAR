package com.example.demo;

import org.springframework.stereotype.Service;


@Service
public class StockService {
    private Stock stock;
    public StockService() {
        stock = new Stock();
    }

    public void approvisionner(){stock.addStock("Chaise", 10); stock.addStock("Table", 10);
    }

    public Stock getStock() {
        return stock;
    }

}