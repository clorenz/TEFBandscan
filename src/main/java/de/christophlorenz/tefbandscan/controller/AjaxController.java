package de.christophlorenz.tefbandscan.controller;

import de.christophlorenz.tefbandscan.model.DataTableResponse;
import de.christophlorenz.tefbandscan.model.DisplayStatus;
import de.christophlorenz.tefbandscan.model.Status;
import de.christophlorenz.tefbandscan.model.StatusHistory;
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
        StatusHistory statusHistory = scannerService.getStatusHistory();
        Integer frequency = status != null ? status.frequency() : null;

        map.addAttribute("displayStatus", new DisplayStatus(status, statusHistory));

            //map.addAttribute("bandwidth", status.bandwidth());
            map.addAttribute("logged", status.logged());


        return "bandscan :: #status";
    }

    @GetMapping("/bandscan")
    @ResponseBody
    public DataTableResponse getBandscan(Model map) {
        map.addAttribute("scanStart", scannerService.getScanStart());
        return new DataTableResponse(bandscanRepository.getEntries(), bandscanRepository.getEntries().size());
    }
}
