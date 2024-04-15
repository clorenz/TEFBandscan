package de.christophlorenz.tefbandscan.repository;

import com.fazecast.jSerialComm.SerialPort;
import de.christophlorenz.tefbandscan.config.Tef6686Config;
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

    private final Tef6686Config config;
    private SerialPort serialPort;
    private InputStream inputStream;
    private BufferedReader reader;
    private BufferedWriter writer;

    private Scanner scanner;

    public SerialCommunicationRepository(Tef6686Config config) {
        this.config = config;
    }

    @Override
    public void initialize() throws RepositoryException {
        LOGGER.info("Available port=" + Arrays.stream(SerialPort.getCommPorts()).map(s -> s.getSystemPortName()).collect(Collectors.toSet()));
        serialPort = SerialPort.getCommPort(config.serial());
        LOGGER.info("serial port=" + serialPort);
        serialPort.openPort();
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        inputStream = serialPort.getInputStream();
        //reader = new BufferedReader(new InputStreamReader(inputStream));

        scanner = new Scanner(inputStream);


        OutputStream outputStream = serialPort.getOutputStream();
        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        write("x");
    }

    @Override
    public String read() throws RepositoryException {
        if (serialPort.bytesAvailable() == 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignore) {
                //
            }
            return null;
        }


        for (int j = 0; j < 1000; ++j) {
            try {
                var data = inputStream.read();
                if (data == -1) {
                    return null;
                }
                System.out.print((char)data + "(" + data + ") ");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /*

        byte[] readBuffer = new byte[serialPort.bytesAvailable()];
        int numRead = serialPort.readBytes(readBuffer, readBuffer.length);
        return new String(readBuffer, Charset.forName("LATIN1"));

        /*

        String line = scanner.nextLine();

        System.out.println("Line=" + line);
        return line;

        /*
        try {
            String line = reader.readLine();
            LOGGER.info("Line=" + line);
            return line;
        } catch (IOException e) {
            throw new RepositoryException("Cannot read line: " + e, e);
        }

         */

        return null;

    }

    @Override
    public void write(String data) throws RepositoryException {
        try {
            writer.write(data + "\n");
        } catch (IOException e) {
            throw new RepositoryException("Cannot write line: " + e, e);
        }
    }
}
