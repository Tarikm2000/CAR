package com.example.demo;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/stock")
public class HomeController {

    @Autowired
    private StockService stockService ;

    @GetMapping("/home")
    public ModelAndView home() {
        var model = Map.of(
                "stock", stockService.getStock()
        );
        return new ModelAndView("/stock/home", model);
    }

    @GetMapping("/approvisionner")
    public ModelAndView refill() {
        stockService.approvisionner();
        var model = Map.of(
                "stock", stockService.getStock()
        );
        return new ModelAndView("/stock/home",model);
    }


    @GetMapping("/refresh")
    public RedirectView refresh() {
        return new RedirectView("/stock/home");
    }


}