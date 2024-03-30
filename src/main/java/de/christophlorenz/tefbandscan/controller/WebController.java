package de.christophlorenz.tefbandscan.controller;

import de.christophlorenz.tefbandscan.repository.InMemoryBandscanRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    private final InMemoryBandscanRepository inMemoryBandscanRepository;

    public WebController(InMemoryBandscanRepository inMemoryBandscanRepository) {
        this.inMemoryBandscanRepository = inMemoryBandscanRepository;
    }

    // TODO: HTML table
    // TODO: Current data by websocket
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("bandscan", inMemoryBandscanRepository.getEntries());
        return "bandscan";
    }
}
