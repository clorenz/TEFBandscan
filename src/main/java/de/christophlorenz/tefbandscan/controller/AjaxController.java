package de.christophlorenz.tefbandscan.controller;

import de.christophlorenz.tefbandscan.model.DataTableResponse;
import de.christophlorenz.tefbandscan.model.Status;
import de.christophlorenz.tefbandscan.repository.BandscanRepository;
import de.christophlorenz.tefbandscan.service.ScannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
public class AjaxController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AjaxController.class);

    private final BandscanRepository bandscanRepository;
    @Lazy
    private final ScannerService scannerService;

    public AjaxController(@Qualifier("csv") BandscanRepository bandscanRepository, @Qualifier("currentScanner") ScannerService scannerService) {
        this.bandscanRepository = bandscanRepository;
        this.scannerService = scannerService;
        LOGGER.info("Using current scanner=" + scannerService.getClass().getSimpleName());
    }

    @GetMapping("/status")
    public String getStatus(Model map) {
        map.addAttribute("time", new Date());

        Status status = scannerService.getCurrentStatus();
        Integer frequency = status != null ? status.frequency() : null;
        if (frequency != null) {
            map.addAttribute("freq", String.format("%.02f MHz", ((float) frequency / 1000f)));
            map.addAttribute("pi", status.rdsPi());
            map.addAttribute("piErrors", status.piErrors());
            map.addAttribute("ps", status.rdsPs());
            map.addAttribute("psWithErrors", status.psWithErrors());
            map.addAttribute("signal", status.signal() != null ? Math.round(status.signal()) : "");
            map.addAttribute("cci", status.cci());
            map.addAttribute("bandwidth", status.bandwidth());
            map.addAttribute("snr", status.snr());
            map.addAttribute("rdserrors", status.rdsErrors());
        } else {
            map.addAttribute("freq","---- (please select new frequency) ----");
        }

        return "bandscan :: #status";
    }

    @GetMapping("/bandscan")
    @ResponseBody
    public DataTableResponse getBandscan() {
        return new DataTableResponse(bandscanRepository.getEntries(), bandscanRepository.getEntries().size());
    }
}
