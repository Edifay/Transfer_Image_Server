package fr.arnaud.channel;

import fr.arnaud.channel.utils.CertLoader;
import fr.arnaud.utils.SetupLoader;
import fr.jazer.session.SPacket;
import fr.jazer.session.Session;
import fr.jazer.session.SessionServer;
import fr.jazer.session.utils.ConnectionStatus;
import fr.jazer.session.utils.crypted.CertFormat;
import fr.jazer.session.utils.crypted.SecureType;
import fr.jazer.session.utils.crypted.ServerCertConfig;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class ServerHandler {

    private final SetupLoader.Settings settings;

    private final SessionServer server;

    public ServerHandler(final SetupLoader.Settings settings) throws UnrecoverableKeyException, CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        this.settings = settings;

        final CertLoader.CertConfiguration certConfiguration = CertLoader.loadCert();
        ServerCertConfig config = new ServerCertConfig(certConfiguration.stream, certConfiguration.password, SecureType.TLSv1_2, CertFormat.JKS);
        this.server = new SessionServer();
        this.server.openSession(settings.port, config);

        this.server.addSessionListener(this::onNewClient);

        System.out.println("Server online");
        System.out.println("Waiting for connection");
    }

    public SessionServer getServer() {
        return this.server;
    }


    public void onNewClient(final Session session) {
        try {
            ClientManager manager = new ClientManager(settings, session);
            manager.handle();
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            System.err.println("Error during handling : " + session.getStringID() + "\n" + e.getMessage());
        }
    }

}
