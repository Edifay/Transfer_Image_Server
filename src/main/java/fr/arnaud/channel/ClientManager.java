package fr.arnaud.channel;

import fr.arnaud.channel.utils.PType;
import fr.arnaud.media.MediaManager;
import fr.arnaud.utils.SetupLoader;
import fr.jazer.session.SPacket;
import fr.jazer.session.Session;

import java.io.IOException;

public class ClientManager {

    private final SetupLoader.Settings settings;
    private final Session client;

    private final MediaManager mediaManager;

    public ClientManager(final SetupLoader.Settings settings, final Session client) {
        this.settings = settings;
        this.client = client;
        this.mediaManager = new MediaManager(settings.destination);
        System.out.println("Handler created " + client.getStringID());
    }

    public void handle() throws IOException, InterruptedException, ClassNotFoundException {
        System.out.println("Starting HandShake " + client.getStringID());
        final HandShake handShake = new HandShake(settings, client, this.mediaManager);
        final HandShake.HandShakeData handShakeData = handShake.getHandShakeData();
        System.out.println("Finished HandShake " + client.getStringID());

        if (!handShakeData.isAuth) {
            System.out.println(client.getStringID() + " connection refused ! Wrong password.");
            return;
        }

        System.out.println("Receiving images " + client.getStringID());
        final ImagesReceiver imagesReceiver = new ImagesReceiver(client, mediaManager, handShakeData.missingImages);
        final ImagesReceiver.ImagesReceiverData imagesReceiverData = imagesReceiver.receive();
        System.out.println("Finished receiving images " + client.getStringID());

        closeClient();
        System.gc();
    }

    public void closeClient() {
        client.send(new SPacket(PType.CLOSE));
        client.destroy();
        System.out.println("Connection closed : " + client.getSocket().isClosed() + " with " + client.getStringID());
    }
}
