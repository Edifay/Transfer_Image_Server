package fr.arnaud;

import fr.arnaud.channel.ServerHandler;
import fr.arnaud.utils.SetupLoader;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class TransferImageServer {

    private SetupLoader.Settings settings;

    private ServerHandler serverHandler;

    public void init() {
        this.settings = SetupLoader.loadSettings();
        try {
            this.serverHandler = new ServerHandler(settings);
        } catch (UnrecoverableKeyException | CertificateException | IOException | NoSuchAlgorithmException |
                 KeyStoreException | KeyManagementException e) {
            System.err.println("Couldn't open the server !\n" + e.getMessage());
        }
    }

}
