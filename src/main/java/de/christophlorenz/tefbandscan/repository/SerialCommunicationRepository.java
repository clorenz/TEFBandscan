package de.christophlorenz.tefbandscan.repository;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortIOException;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import com.fazecast.jSerialComm.SerialPortTimeoutException;
import de.christophlorenz.tefbandscan.config.Tef6686Config;
import java.net.SocketTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

@Repository
public class SerialCommunicationRepository implements CommunicationRepository{

    private static final Logger LOGGER = LoggerFactory.getLogger(SerialCommunicationRepository.class);
    private static final int BAUDRATE = 115200;
    private static final int BYTESIZE = 8;
    private static final int STOPBITS = 0;

    private final Tef6686Config config;
    private SerialPort serialPort;
    private InputStream inputStream;
    private BufferedReader reader;
    private BufferedWriter writer;

    //private Scanner scanner;

    public SerialCommunicationRepository(Tef6686Config config) {
        this.config = config;
    }

    @Override
    public void initialize() throws RepositoryException {
        LOGGER.info("Available serial ports=" + Arrays.stream(SerialPort.getCommPorts()).map(
            SerialPort::getSystemPortName).collect(Collectors.toSet()));
        try {
            serialPort = SerialPort.getCommPort(config.serial());
        } catch (SerialPortInvalidPortException e) {
            throw new RepositoryException("Cannot establish serial connection to " + config.serial() + ": " + e);
        }
        serialPort.setBaudRate(BAUDRATE);
        serialPort.setNumDataBits(BYTESIZE);
        serialPort.setNumStopBits(STOPBITS);
        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.openPort();
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 5000, 5000);
        InputStream inputStream = serialPort.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        OutputStream outputStream = serialPort.getOutputStream();
        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        write("x");
        LOGGER.info("Connected via " + serialPort);
    }

    @Override
    public void reconnect() throws RepositoryException {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                LOGGER.warn("Cannot close serial reader: " + e);
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                LOGGER.warn("Cannot close serial input stream: " + e);
            }
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                LOGGER.warn("Cannot close serial output stream: " + e);
            }
        }
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }
        initialize();
    }

    @Override
    public String read() throws RepositoryException {
        try {
            return reader.readLine();
        } catch (SerialPortTimeoutException | SerialPortIOException e) {
            LOGGER.warn("Lost connection");
            throw new ConnectionLostException("Lost connection");
        } catch (IOException e) {
            throw new RepositoryException("Cannot read line: " + e, e);
        }
    }

    @Override
    public void write(String data) throws RepositoryException {
        try {
            writer.write(data + "\n");
            writer.flush();
        } catch (IOException e) {
            throw new RepositoryException("Cannot write line: " + e, e);
        }
    }
}
