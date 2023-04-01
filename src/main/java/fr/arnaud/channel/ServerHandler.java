package fr.arnaud.channel;

import apifornetwork.tcp.Secure.SecureServerTCP;
import apifornetwork.tcp.SocketMake;
import fr.arnaud.channel.utils.CertLoader;
import fr.arnaud.utils.SetupLoader;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class ServerHandler {

    private final SetupLoader.Settings settings;

    private final SecureServerTCP server;

    public ServerHandler(final SetupLoader.Settings settings) throws UnrecoverableKeyException, CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        this.settings = settings;
        final CertLoader.CertConfiguration certConfiguration = CertLoader.loadCert();
        this.server = new SecureServerTCP(settings.port, (short) 8192, certConfiguration.stream, certConfiguration.password);

        this.server.addEventOnNewClient(this::onNewClient);
        this.server.startListenClient();

        System.out.println("Server online");
        System.out.println("Waiting for connection");
    }

    public SecureServerTCP getServer() {
        return this.server;
    }


    public void onNewClient(final SocketMake socketMake) {
        try {
            ClientManager manager = new ClientManager(settings, socketMake);
            manager.handle();
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            System.err.println("Error during handling : " + socketMake.getIdentity() + "\n" + e.getMessage());
        }
    }




/*
    public void onNewClient(final SocketMake socketMake) {
        ClientManager manager = null;
        try {
            manager = new ClientManager(settings, socketMake);
            manager.handle();
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            System.err.println("Error during handling : " + socketMake.getIdentity() + "\n" + e.getMessage());
        }

        if (manager == null)
            return;

        try {
            manager.closeClient();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }*/

}
