package fr.arnaud.channel;

import apifornetwork.data.packets.Packet;
import apifornetwork.data.packets.ReceiveSecurePacket;
import apifornetwork.data.packets.SendSecurePacket;
import apifornetwork.tcp.SocketMake;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arnaud.media.ImageDescriptor;
import fr.arnaud.channel.utils.PType;
import fr.arnaud.media.MediaManager;
import fr.arnaud.utils.SetupLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class HandShake {

    private final SocketMake client;
    private final SetupLoader.Settings settings;
    private final MediaManager mediaManager;

    public HandShake(final SetupLoader.Settings settings, final SocketMake client, final MediaManager mediaManager) {
        this.client = client;
        this.settings = settings;
        this.mediaManager = mediaManager;
    }

    public HandShakeData getHandShakeData() throws InterruptedException, IOException, ClassNotFoundException {

        String password = receivePassword();
        if (!password.equals(settings.password)) {
            sendPasswordResult(false);
            return new HandShakeData(false, null);
        }
        sendPasswordResult(true);

        ArrayList<ImageDescriptor> imagesFromPhone = receiveImagesDescriptors();
        System.out.println("Device " + client.getIdentity() + " have " + imagesFromPhone.size() + " media.");

        ArrayList<ImageDescriptor> missingImages = this.mediaManager.getMissingImagesFrom(imagesFromPhone);
        writeImagesDescriptors(missingImages);
        System.out.println("Detected " + missingImages.size() + " images missing on computer.");

        client.waitForPacket(PType.COMFIRM);

        return new HandShakeData(true, missingImages);
    }


    public static class HandShakeData {
        public final boolean isAuth;
        public final ArrayList<ImageDescriptor> missingImages;

        public HandShakeData(final boolean isAuth, final ArrayList<ImageDescriptor> missingImages) {
            this.isAuth = isAuth;
            this.missingImages = missingImages;
        }

    }

    private String receivePassword() throws InterruptedException, IOException, ClassNotFoundException {
        return (String) new ObjectInputStream(new ByteArrayInputStream(client.waitForPacket(PType.PASSWORD_PACKET).data)).readUnshared();
    }

    private void sendPasswordResult(final boolean isAuth) throws IOException {
        client.send(new SendSecurePacket(PType.PASSWORD_PACKET, Packet.getByteForObject(isAuth)));
    }

    private ArrayList<ImageDescriptor> receiveImagesDescriptors() throws InterruptedException, IOException, ClassNotFoundException {
        final ReceiveSecurePacket packetDescriptorList = client.waitForPacket(PType.EXCHANGING_IMAGES_DESCRIPTOR);
        final String json = (String) new ObjectInputStream(new ByteArrayInputStream(packetDescriptorList.getBytesData())).readUnshared();

        return new ObjectMapper().readValue(json, new TypeReference<>() {
        });
    }

    private void writeImagesDescriptors(final ArrayList<ImageDescriptor> descriptors) throws IOException {
        final String json = new ObjectMapper().writeValueAsString(descriptors);
        client.send(new SendSecurePacket(PType.EXCHANGING_IMAGES_DESCRIPTOR, Packet.getByteForObject(json)));
    }
}
