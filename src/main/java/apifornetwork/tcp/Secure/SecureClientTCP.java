package apifornetwork.tcp.Secure;

import apifornetwork.data.packets.Packet;
import apifornetwork.data.packets.SendSecurePacket;
import apifornetwork.tcp.SocketMake;
import apifornetwork.udp.Auth;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class SecureClientTCP extends SocketMake {


    public SecureClientTCP(final String ip, final int port, InputStream pathToCert, String passwordFromCert) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        super(UtilitaireClient.getSocketWithCert(InetAddress.getByName(ip), port, pathToCert, passwordFromCert));
        this.identity = new Auth(this.s.getPort(), this.s.getInetAddress());
    }

    @Override
    public synchronized void send(Packet packet) throws IOException {
        if (packet instanceof SendSecurePacket)
            super.send(packet);
        else
            throw new IOException("Wrong packet to send");
    }

}