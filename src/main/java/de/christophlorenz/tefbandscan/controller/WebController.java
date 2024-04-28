package de.christophlorenz.tefbandscan.controller;

import de.christophlorenz.tefbandscan.repository.BandscanRepository;
import de.christophlorenz.tefbandscan.service.ScannerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    private final BandscanRepository bandscanRepository;
    private final ScannerService scannerService;

    public WebController(@Qualifier("csv") BandscanRepository bandscanRepository,  @Qualifier("currentScanner") ScannerService scannerService) {
        this.bandscanRepository = bandscanRepository;
        this.scannerService = scannerService;
    }

    // TODO: HTML table
    // TODO: Current data by websocket
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("scanStart", scannerService.getScanStart());
        model.addAttribute("bandscan", bandscanRepository.getEntries());
        return "bandscan";
    }
}
