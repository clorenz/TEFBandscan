package de.christophlorenz.tefbandscan.repository;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import de.christophlorenz.tefbandscan.model.Bandscan;
import de.christophlorenz.tefbandscan.model.BandscanEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Format: comma-separated entries, wrapped in parentheses
 *
 * The following columns are used:
 *
 * <ul>
 *     <li>QRG</li>
 *     <li>RDS-PI</li>
 *     <li>RDS-PS</li>
 *     <li>signal strength in dbµV</li>
 *     <li>CCI in percent</li>
 *     <li>SNR in dB</li>
 *     <li>Timestamp of log (ISO 8601 format)</li>
 * </ul>
 */

@Repository("csv")
public class CSVBandscanRepository implements BandscanRepository {

    private Bandscan bandscan;

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVBandscanRepository.class);
    private Path path;

    @Override
    public void init(String name) throws RepositoryException {
        path = Path.of(name + (name.endsWith(".csv") ? "" : ".csv"));
        LOGGER.info("Using CSV repository " + path.toAbsolutePath().toUri().toString());
        bandscan = readAll();
    }

    @Override
    public boolean addEntry(Integer frequencyKHz, String rdsPI, String rdsPS, Integer signalStrength, Integer cci, Integer snr) throws RepositoryException {
        if (frequencyKHz == null) {
            return false;
        }
        BandscanEntry bandscanEntry = new BandscanEntry(frequencyKHz, rdsPI, rdsPS, signalStrength, cci, snr);
        return addEntry(bandscanEntry);
    }


    @Override
    public boolean addEntry(BandscanEntry bandscanEntry) throws RepositoryException {
        boolean isNewEntry = bandscan.addBandscanEntry(bandscanEntry);
        writeAll(bandscan);
        return isNewEntry;
    }

    @Override
    public List<BandscanEntry> getEntries() {
        return bandscan.bandscanEntries();
    }

    @Override
    public void removeEntry(BandscanEntry bandscanEntry) throws RepositoryException {
        bandscan.removeBandscanEntry(bandscanEntry);
        writeAll(bandscan);
    }

    private Bandscan readAll() throws RepositoryException {
       try (Reader reader = Files.newBufferedReader(path)) {
            bandscan = new Bandscan();
            try (CSVReader csvReader = new CSVReader(reader)) {
                String[] header = csvReader.readNext();
                List<String[]> lines = csvReader.readAll();
                lines.stream().forEach(
                        l -> {
                            String[] columns = l;
                            int qrg = Integer.parseInt(columns[0]);
                            String rdsPi = columns[1];
                            if (rdsPi != null && rdsPi.isBlank()) {
                                rdsPi = null;
                            }
                            String rdsPs = columns[2];
                            if (rdsPs != null && rdsPs.isBlank()) {
                                rdsPs = null;
                            }
                            int signalStrength = Integer.parseInt(columns[3]);
                            int cci = Integer.parseInt(columns[4]);
                            Integer snr=null;
                            LocalDateTime timestamp;
                            if (columns.length>6) {
                                snr = (columns[5] != null && !"null".equals(columns[5]) && !columns[5].isBlank()) ? Integer.parseInt(columns[5]) : null;
                                timestamp = LocalDateTime.parse(columns[6], DateTimeFormatter.ISO_DATE_TIME);
                            } else {
                                timestamp = LocalDateTime.parse(columns[5], DateTimeFormatter.ISO_DATE_TIME);
                            }
                            BandscanEntry bandscanEntry = new BandscanEntry(qrg, rdsPi, rdsPs, signalStrength, cci, snr, timestamp);
                            bandscan.addBandscanEntry(bandscanEntry);
                        }
                );
            }
            return bandscan;
        } catch (NoSuchFileException e) {
            LOGGER.info("No existing bandscan file " + path + ". Creating new one.");
            return new Bandscan();
        } catch (IOException e) {
            throw new RepositoryException("Cannot read CSV file: " + e, e);
        }
    }

    private void writeAll(Bandscan bandscan) throws RepositoryException {
        try(CSVWriter writer = new CSVWriter(new FileWriter(path.toString()))) {
            List<String[]> data = new ArrayList<>();
            data.add(new String[]{"QRG","PI","PS","Signal","CCI","SNR","Timestamp"});
            bandscan.bandscanEntries().stream()
                    .sorted()
                            .forEach(
                                    e -> {
                                        data.add(buildCsvLineData(e));
                                    }
                            );


            writer.writeAll(data);
        } catch (IOException e) {
            throw new RepositoryException("Cannot write CSV file: " + e, e);
        }
    }

    private String[] buildCsvLineData(BandscanEntry e) {
        return new String[]{e.getFrequencyKHz().toString(),
                e.getRdsPi(),
                e.rdsPs(),
                String.valueOf(e.getSignalStrength()),
                String.valueOf(e.getCci()),
                e.getSnr() != null ? String.valueOf(e.getSnr()) : null,
                DateTimeFormatter.ISO_DATE_TIME.format(e.getTimestamp())};
    }
}
