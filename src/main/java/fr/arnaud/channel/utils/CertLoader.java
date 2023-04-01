package fr.arnaud.channel.utils;

import fr.arnaud.Main;

import java.io.InputStream;
import java.util.Scanner;

public class CertLoader {

    private static final String PATH_CLE_PRIVE = "/keyStore/CLE_PRIVE.jks";
    private static final String PATH_MDP_CLE_PRIVE = "/keyStore/mdp_prive.txt";

    public static CertConfiguration loadCert() {
        final Scanner scanner = new Scanner(Main.class.getResourceAsStream(PATH_MDP_CLE_PRIVE));

        final InputStream stream = Main.class.getResourceAsStream(PATH_CLE_PRIVE);
        final String password = scanner.nextLine();

        scanner.close();

        return new CertConfiguration(stream, password);
    }

    public static class CertConfiguration {
        public final InputStream stream;
        public final String password;

        public CertConfiguration(final InputStream stream, final String password) {
            this.stream = stream;
            this.password = password;
        }
    }


}
