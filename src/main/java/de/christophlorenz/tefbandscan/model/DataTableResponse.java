package de.christophlorenz.tefbandscan.model;

import java.util.List;

public class DataTableResponse {

    private List<BandscanEntry> data;
    private long recordsTotal;

    public DataTableResponse(List<BandscanEntry> data, long recordsTotal) {
        this.data = data;
        this.recordsTotal = recordsTotal;
    }

    public List<BandscanEntry> getData() {
        return data;
    }

    public long getRecordsTotal() {
        return recordsTotal;
    }
}
