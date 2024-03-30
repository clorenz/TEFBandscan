package de.christophlorenz.tefbandscan.repository;

import de.christophlorenz.tefbandscan.model.BandscanEntry;
import de.christophlorenz.tefbandscan.model.FrequencyPiId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SQLiteRepository extends CrudRepository<BandscanEntry, FrequencyPiId> {
}
