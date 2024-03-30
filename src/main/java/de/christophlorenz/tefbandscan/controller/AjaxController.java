package de.christophlorenz.tefbandscan.controller;

import de.christophlorenz.tefbandscan.model.DataTableResponse;
import de.christophlorenz.tefbandscan.model.Status;
import de.christophlorenz.tefbandscan.repository.InMemoryBandscanRepository;
import de.christophlorenz.tefbandscan.service.ScannerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
public class AjaxController {

    private final InMemoryBandscanRepository inMemoryBandscanRepository;
    private final ScannerService scannerService;

    public AjaxController(InMemoryBandscanRepository inMemoryBandscanRepository, ScannerService scannerService) {
        this.inMemoryBandscanRepository = inMemoryBandscanRepository;
        this.scannerService = scannerService;
    }

    @GetMapping("/status")
    public String getStatus(Model map) {
        map.addAttribute("time", new Date());

        Status status = scannerService.getCurrentStatus();
        Integer frequency = status.frequency();
        if (frequency != null) {
            map.addAttribute("freq", String.format("%.02f MHz", ((float) frequency / 1000f)));
        } else {
            map.addAttribute("freq","-----");
        }
        map.addAttribute("pi", status.rdsPi());
        map.addAttribute("ps", status.rdsPs());
        map.addAttribute("signal", status.signal());
        map.addAttribute("bandwidth", status.bandwidth());
        return "bandscan :: #status";
    }

    @GetMapping("/bandscan")
    @ResponseBody
    public DataTableResponse getBandscan() {
        return new DataTableResponse(inMemoryBandscanRepository.getEntries(), inMemoryBandscanRepository.getEntries().size());
    }
}
