package de.christophlorenz.tefbandscan.repository;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.HeaderColumnNameMappingStrategyBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import de.christophlorenz.tefbandscan.model.Bandscan;
import de.christophlorenz.tefbandscan.model.BandscanEntry;
import de.christophlorenz.tefbandscan.model.CSVBandscanEntry;
import java.io.FileReader;
import java.io.Writer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
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
 *     <li>RDS-Errors in %</li>
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
    public boolean addEntry(Integer frequencyKHz, String rdsPI, String rdsPS, Integer rdsErrors, Integer signalStrength, Integer cci, Integer snr) throws RepositoryException {
        if (frequencyKHz == null) {
            return false;
        }
        BandscanEntry bandscanEntry = new BandscanEntry(frequencyKHz, rdsPI, rdsPS, rdsErrors, signalStrength, cci, snr);
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
       try (FileReader reader = new FileReader(path.toFile())) {
           CsvToBean<CSVBandscanEntry> cb =
               new CsvToBeanBuilder<CSVBandscanEntry>(reader)
                   .withType(CSVBandscanEntry.class)
                   .build();
         List<CSVBandscanEntry> entries = cb.parse();
         Bandscan bandscan = new Bandscan();
         entries.forEach(
                   e -> bandscan.addBandscanEntry(new BandscanEntry(
                       Integer.parseInt(e.getQrg()),
                       e.getRdsPi(),
                       e.getRdsPs(),
                       e.getRdsErrors(),
                       e.getSignal(),
                       e.getCci(),
                       e.getSnr(),
                       e.getTimestamp()
                   ))
         );
         return bandscan;
        } catch (NoSuchFileException e) {
            LOGGER.info("No existing bandscan file " + path + ". Creating new one.");
            return new Bandscan();
        } catch (IOException e) {
            throw new RepositoryException("Cannot read CSV file: " + e, e);
        }
    }

    private void writeAll(Bandscan bandscan) throws RepositoryException {
      HeaderColumnNameMappingStrategy<CSVBandscanEntry> strategy = new HeaderColumnNameMappingStrategyBuilder<CSVBandscanEntry>().build();
      strategy.setType(CSVBandscanEntry.class);
      strategy.setColumnOrderOnWrite(new CSVBandScanEntryHeaderPositionComparator());

        try( Writer writer = new FileWriter(path.toString())) {
         StatefulBeanToCsv<CSVBandscanEntry> sbc = new StatefulBeanToCsvBuilder<CSVBandscanEntry>(writer)
              .withQuotechar('\"')
              .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
             .withMappingStrategy(strategy)
              .build();
          List<CSVBandscanEntry> bandscanEntries = bandscan.bandscanEntries().stream()
                  .map(
                      e ->
                        new CSVBandscanEntry(
                            e.getFrequencyKHz()+"",
                            e.getRdsPi(),
                            e.getRdsPs(),
                            e.getRdsErrors(),
                            e.getSignalStrength(),
                            e.getCci(),
                            e.getSnr(),
                            e.getTimestamp()
                        )
                  )
              .sorted()
                      .toList();
          sbc.write(bandscanEntries);
          writer.close();
          LOGGER.info("Wrote " + bandscanEntries.size() + " bandscan entries");
/*


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

 */
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
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

  private class CSVBandScanEntryHeaderPositionComparator implements Comparator<String> {

    private Map<String, Integer> positions = new HashMap<>();

    public CSVBandScanEntryHeaderPositionComparator() {
      positions.put("QRG",0);
      positions.put("PI",1);
      positions.put("PS",2);
      positions.put("RDSERR", 3);
      positions.put("SIGNAL", 4);
      positions.put("CCI", 5);
      positions.put("SNR", 6);
      positions.put("TIMESTAMP", 7);
    }

    @Override
    public int compare(String s, String t) {
      Integer positionS = positions.get(s.toUpperCase());
      Integer positionT = positions.get(t.toUpperCase());

      if (positionS==null) {
        throw new RuntimeException("Empty position for column '" + s + "'");
      }
      if (positionT==null) {
        throw new RuntimeException("Empty position for column '" + t + "'");
      }


      return Integer.compare(positionS, positionT);
    }
  }
}
