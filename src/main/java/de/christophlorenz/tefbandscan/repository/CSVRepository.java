package de.christophlorenz.tefbandscan.repository;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import de.christophlorenz.tefbandscan.model.Bandscan;
import de.christophlorenz.tefbandscan.model.BandscanEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.File;
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
import java.util.stream.Collectors;

/**
 * Format: comma-separated entries, wrapped in parentheses
 *
 * The following columns are used:
 *
 * <ul>
 *     <li>QRG</li>
 *     <li>RDS-PI</li>
 *     <li>RDS-PS</li>
 *     <li>Quality</li>
 *     <li>Timestamp of log (ISO 8601 format)</li>
 * </ul>
 */

@Repository("csv")
public class CSVRepository implements BandscanRepository {

    private Bandscan bandscan;

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVRepository.class);
    private Path path;

    @Override
    public void init(String name) throws RepositoryException {
        path = Path.of(name + (name.endsWith(".csv") ? "" : ".csv"));
        LOGGER.info("Using CSV repository " + path.toAbsolutePath().toUri().toString());
        bandscan = readAll();
    }

    @Override
    public void addEntry(Integer frequencyKHz, String rdsPI, String rdsPS, Integer quality) throws RepositoryException {
        if (frequencyKHz == null) {
            return;
        }
        BandscanEntry bandscanEntry = new BandscanEntry(frequencyKHz, rdsPI, rdsPS, quality);
        bandscan.addBandscanEntry(bandscanEntry);
        writeAll(bandscan);
    }

    @Override
    public List<BandscanEntry> getEntries() {
        return bandscan.bandscanEntries();
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
                            int quality = Integer.parseInt(columns[3]);
                            LocalDateTime timestamp = LocalDateTime.parse(columns[4], DateTimeFormatter.ISO_DATE_TIME);
                            BandscanEntry bandscanEntry = new BandscanEntry(qrg, rdsPi, rdsPs, quality, timestamp);
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
            data.add(new String[]{"QRG","PI","PS","Q","Timestamp"});
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
                String.valueOf(e.getQuality()), DateTimeFormatter.ISO_DATE_TIME.format(e.getTimestamp())};
    }
}
