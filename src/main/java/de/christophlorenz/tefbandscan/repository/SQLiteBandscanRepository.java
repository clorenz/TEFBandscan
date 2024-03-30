package de.christophlorenz.tefbandscan.repository;

import de.christophlorenz.tefbandscan.model.Bandscan;
import de.christophlorenz.tefbandscan.model.BandscanEntry;
import de.christophlorenz.tefbandscan.model.FrequencyPiId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.StreamSupport;

@Repository("sqlite")
public class SQLiteBandscanRepository implements BandscanRepository{

    private final SQLiteRepository sqLiteRepository;

    public SQLiteBandscanRepository(SQLiteRepository sqLiteRepository) {
        this.sqLiteRepository = sqLiteRepository;
    }

    @Override
    public void addEntry(Integer frequencyKHz, String rdsPI, String rdsPS, Integer quality) {
        sqLiteRepository.save(new BandscanEntry(frequencyKHz, rdsPI, rdsPS, quality));
    }

    @Override
    public List<BandscanEntry> getEntries() {
        return StreamSupport.stream(sqLiteRepository.findAll().spliterator(), false).toList();
    }
}
