package de.christophlorenz.tefbandscan.controller;

import de.christophlorenz.tefbandscan.service.BandscanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebController {

    private final BandscanService bandscanService;

    public WebController(BandscanService bandscanService) {
        this.bandscanService = bandscanService;
    }

    // TODO: HTML table
    // TODO: Current data by websocket
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("bandscan", bandscanService.getEntries());
        return "bandscan";
    }
}
