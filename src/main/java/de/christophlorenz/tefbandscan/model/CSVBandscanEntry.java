package de.christophlorenz.tefbandscan.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByNames;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvIgnore;
import com.opencsv.bean.CsvToBean;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CSVBandscanEntry implements Comparable<CSVBandscanEntry>{


  @CsvBindByName(column = "QRG")
  private String qrg;

  @CsvBindByName(column = "PI")
  private String rdsPi;

  @CsvBindByName(column = "PS")
  private String rdsPs;

  @CsvBindByName(column = "PSErr")
  private Integer psErrors;

  @CsvBindByName(column = "RDSErr")
  private Integer rdsErrors;

  @CsvBindByName(column = "Signal")
  private Integer signal;

  @CsvBindByName(column = "CCI")
  private Integer cci;

  @CsvBindByName(column = "SNR")
  private Integer snr;

  @CsvBindByName(column = "Timestamp")
  @CsvDate(value = "yyyy-MM-dd'T'HH:mm:ss.n")
  private LocalDateTime timestamp;

  public CSVBandscanEntry() {
  }

  public CSVBandscanEntry(String qrg, String rdsPi, String rdsPs, Integer psErrors, Integer rdsErrors, int signal, int cci, Integer snr, LocalDateTime timestamp) {
    this.qrg = qrg;
    this.rdsPi = rdsPi;
    this.rdsPs = rdsPs;
    this.psErrors = psErrors;
    this.rdsErrors = (rdsPi != null ? rdsErrors : null);
    this.signal = signal;
    this.cci = cci;
    this.snr = snr;
    this.timestamp = timestamp;
  }

  public String getQrg() {
    return qrg;
  }

  public void setQrg(String qrg) {
    this.qrg = qrg;
  }

  public String getRdsPi() {
    return rdsPi;
  }

  public void setRdsPi(String rdsPi) {
    this.rdsPi = rdsPi;
  }

  public String getRdsPs() {
    return rdsPs;
  }

  public void setRdsPs(String rdsPs) {
    this.rdsPs = rdsPs;
  }

  public Integer getRdsErrors() {
    return rdsErrors;
  }

  public void setRdsErrors(Integer rdsErrors) {
    this.rdsErrors = rdsErrors;
  }

  public Integer getSignal() {
    return signal;
  }

  public void setSignal(Integer signal) {
    this.signal = signal;
  }

  public Integer getCci() {
    return cci;
  }

  public void setCci(Integer cci) {
    this.cci = cci;
  }

  public Integer getSnr() {
    return snr;
  }

  public void setSnr(Integer snr) {
    this.snr = snr;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public Integer getPsErrors() {
    return psErrors;
  }

  public void setPsErrors(Integer psErrors) {
    this.psErrors = psErrors;
  }

  @Override
  public String toString() {
    return "CSVBandscanEntry{" +
        "qrg='" + qrg + '\'' +
        ", rdsPi='" + rdsPi + '\'' +
        ", rdsPs='" + rdsPs + '\'' +
        ", psErrors=" + psErrors +
        ", rdsErrors=" + rdsErrors +
        ", signal=" + signal +
        ", cci=" + cci +
        ", snr=" + snr +
        ", timestamp=" + timestamp +
        '}';
  }

  @Override
  public int compareTo(CSVBandscanEntry other) {
    return Integer.compare(Integer.parseInt(qrg), Integer.parseInt(other.qrg));
  }
}
