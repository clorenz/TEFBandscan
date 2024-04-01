package de.christophlorenz.tefbandscan.controller;

import de.christophlorenz.tefbandscan.repository.BandscanRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    private final BandscanRepository bandscanRepository;

    public WebController(@Qualifier("csv") BandscanRepository bandscanRepository) {
        this.bandscanRepository = bandscanRepository;
    }

    // TODO: HTML table
    // TODO: Current data by websocket
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("bandscan", bandscanRepository.getEntries());
        return "bandscan";
    }
}
