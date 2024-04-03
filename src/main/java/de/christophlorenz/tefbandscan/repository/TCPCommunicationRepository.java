package de.christophlorenz.tefbandscan.repository;

import de.christophlorenz.tefbandscan.config.Tef6686Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Repository
public class TCPCommunicationRepository implements CommunicationRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(TCPCommunicationRepository.class);

    private final Tef6686Config config;
    Socket tef6686Socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public TCPCommunicationRepository(Tef6686Config config) {
        this.config = config;
    }

    @Override
    public void initialize() throws RepositoryException {
        // Setup socket connection
        try {
            tef6686Socket = new Socket();
            tef6686Socket.connect(new InetSocketAddress(config.hostname(),config.port()), 5000);
            LOGGER.info("Established socket connection to " + config.hostname() + ":" + config.port());
        } catch (IOException e) {
            throw new RepositoryException("Cannot initialize socket connection to "
                    + config.hostname() + ":" + config.port() + ": " + e, e);
        }

        // Authenticate
        try {
            authenticate(config.password());
        } catch (IOException e) {
            throw new RepositoryException("Authentication failed due to communication problems: " + e, e);
        } catch (NoSuchAlgorithmException e) {
            throw new RepositoryException("Authentication failed due to SHA1 problems: " + e, e);
        }

        // Setting up line reader and writer
        try {
            reader = new BufferedReader(new InputStreamReader(tef6686Socket.getInputStream()));
        } catch (IOException e) {
            throw new RepositoryException("Cannot setup reader: " + e, e);
        }

        try {
            writer = new PrintWriter(tef6686Socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RepositoryException("Cannot setup writer: " + e, e);
        }

        // Wait, until the connection is ready
        String line;
        while ((line = read()) == null) {
            LOGGER.info("Waiting...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        // Read initially and instruct to send the currently dialled frequency
        line = read();
        write("T");

        LOGGER.info("Connection established: '" + line + "'");
    }

    @Override
    public String read() throws RepositoryException {
        try {
            String line = reader.readLine();
            if (line != null && !line.isBlank()) {
                LOGGER.debug("<" + line);
            }
            return line;
        } catch (IOException e) {
            throw new RepositoryException("Cannot read: " + e, e);
        }
    }

    @Override
    public void write(String data) throws RepositoryException {
        writer.println(data);
        LOGGER.debug(">" + data);
    }

    private void authenticate(String password) throws IOException, NoSuchAlgorithmException {
        InputStream input = tef6686Socket.getInputStream();

        // Retrieve the salt (16 bytes) from socket
        byte[] data = input.readNBytes(16);
        String toHash = new String(data);

        // Append the password to the sale
        toHash += password;

        // Build the SHA1 digest and create readable hex values
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] digest = sha1.digest(toHash.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            hexString.append(String.format("%02x", b));
        }

        // Send the hash
        OutputStream output = tef6686Socket.getOutputStream();
        output.write(hexString.toString().getBytes());
        output.flush();
    }
}
