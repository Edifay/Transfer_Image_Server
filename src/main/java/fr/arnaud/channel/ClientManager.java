package fr.arnaud.channel;

import apifornetwork.data.packets.SendSecurePacket;
import apifornetwork.tcp.SocketMake;
import fr.arnaud.channel.utils.PType;
import fr.arnaud.media.MediaManager;
import fr.arnaud.utils.SetupLoader;

import java.io.IOException;

public class ClientManager {

    private final SetupLoader.Settings settings;
    private final SocketMake client;

    private final MediaManager mediaManager;

    public ClientManager(final SetupLoader.Settings settings, final SocketMake client) {
        this.settings = settings;
        this.client = client;
        this.mediaManager = new MediaManager(settings.destination);
        System.out.println("Handler created " + client.getIdentity());
    }

    public void handle() throws IOException, InterruptedException, ClassNotFoundException {
        client.startListen();

        System.out.println("Starting HandShake " + client.getIdentity());
        final HandShake handShake = new HandShake(settings, client, this.mediaManager);
        final HandShake.HandShakeData handShakeData = handShake.getHandShakeData();
        System.out.println("Finished HandShake " + client.getIdentity());

        if (!handShakeData.isAuth) {
            System.out.println(client.getIdentity() + " connection refused ! Wrong password.");
            return;
        }

        System.out.println("Receiving images " + client.getIdentity());
        final ImagesReceiver imagesReceiver = new ImagesReceiver(client, mediaManager, handShakeData.missingImages);
        final ImagesReceiver.ImagesReceiverData imagesReceiverData = imagesReceiver.receive();
        System.out.println("Finished receiving images " + client.getIdentity());

        closeClient();
        System.gc();
    }

    public void closeClient() throws IOException {
        client.stopListen();
        client.send(new SendSecurePacket(PType.CLOSE, new byte[0]));
        client.close();
        System.out.println("Connection closed : " + client.getSocket().isClosed() + " with " + client.getIdentity());
    }
}
