package apifornetwork.tcp.Secure;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/*
Code from : https://gpotter2.github.io/tutos/sslsockets

 */

public class UtilitaireClient {

    /**
     * Ca c'est une fonction utilitaire permettant de récupérer le "vérifieur de certificat", on prend uniquement celui correspondant au certificat pour minimiser le temps de chargement à la connexion
     *
     * Pas très intéressante...
     */
    private static X509TrustManager[] tm(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory trustMgrFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustMgrFactory.init(keystore);
        //on prend tous les managers
        TrustManager trustManagers[] = trustMgrFactory.getTrustManagers();
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                X509TrustManager[] tr = new X509TrustManager[1];
                //on renvoie juste celui que l'on va utiliser
                tr[0] = (X509TrustManager) trustManager;
                return tr;
            }
        }
        return null;
    }

//Le vrai du code

    public static SSLSocket getSocketWithCert(InetAddress ip, int port, InputStream pathToCert, String passwordFromCert) throws IOException,
            KeyManagementException, NoSuchAlgorithmException, CertificateException, KeyStoreException {
        X509TrustManager[] tmm;
        //ATTENTION
        //Android, remplacez JKS par BKS :)
        //On charge le lecteur de Keystore en fonction du format
        KeyStore ks  = KeyStore.getInstance("JKS");
        //On charge le Keystore avec sont stream et son mot de passe
        ks.load(pathToCert, passwordFromCert.toCharArray());                        //On démarre le gestionnaire de validation des clés
        tmm=tm(ks);
        //On démarre le contexte, autrement dit le langage utilisé pour crypter les données
        //On peut replacer TLSv1.2 par SSL
        SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        ctx.init(null, tmm, null);
        //On créee enfin la socket en utilisant une classe de création SSLSocketFactory, vers l'adresse et le port indiqué
        SSLSocketFactory SocketFactory = ctx.getSocketFactory();
        SSLSocket socket = (SSLSocket) SocketFactory.createSocket();
        socket.connect(new InetSocketAddress(ip, port), 5000);
        return socket;
    }
}
