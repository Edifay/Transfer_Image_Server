package fr.arnaud.utils;

import java.io.*;
import java.util.Scanner;

public class SetupLoader {

    private static final String PATH_CONFIG_FILE = "configuration.txt";

    private static final String DEFAULT_CONFIG_FILE =
            """
                    D:\\Gallerie Sync\\
                    default
                    5656
                        
                    Veuillez mettre sur la première ligne ci-dessus le chemin du dossier de destination. Le chemin default est D:\\Gallerie Sync\\.
                    Veuillez mettre sur la deuxième ligne ci-dessus le mot de passe de communication.
                    Veuillez mettre sur la troisième ligne ci-dessus le port de connexion du serveur.
                    """;

    public static Settings loadSettings() {
        Settings settings;
        final File config = new File(PATH_CONFIG_FILE);

        try {

            if (!config.exists()) {
                writeDefaultConfig();
                System.out.println("Config file created, configure the server and restart. File : " + config.getAbsolutePath() + ".");
                System.exit(0);
            }

            final Scanner scanner = new Scanner(config);
            settings = new Settings(scanner.nextLine(), scanner.nextLine(), Integer.parseInt(scanner.nextLine()));
            scanner.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return settings;
    }

    public static void writeDefaultConfig() {
        try {
            Writer writer = new BufferedWriter(new FileWriter(PATH_CONFIG_FILE));
            writer.write(DEFAULT_CONFIG_FILE);
            writer.close();
        } catch (IOException e) {
            System.err.println("Couldn't create config file.\n" + e.getMessage());
        }
    }

    public static class Settings {
        public final String destination;
        public final String password;
        public final int port;

        public Settings(final String destination, final String password, final int port) {
            this.destination = destination;
            this.password = password;
            this.port = port;
        }
    }

}
