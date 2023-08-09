package fr.arnaud.channel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arnaud.media.ImageDescriptor;
import fr.arnaud.channel.utils.PType;
import fr.arnaud.media.MediaManager;
import fr.arnaud.utils.SetupLoader;
import fr.jazer.session.RPacket;
import fr.jazer.session.SPacket;
import fr.jazer.session.Session;

import java.io.IOException;
import java.util.ArrayList;

import static fr.arnaud.channel.Utils.getByteForObject;

public class HandShake {

    private final Session client;
    private final SetupLoader.Settings settings;
    private final MediaManager mediaManager;

    public HandShake(final SetupLoader.Settings settings, final Session client, final MediaManager mediaManager) {
        this.client = client;
        this.settings = settings;
        this.mediaManager = mediaManager;
    }

    public HandShakeData getHandShakeData() throws IOException {
        String password = receivePassword();
        System.out.println("Password : " + password);
        if (!password.equals(settings.password)) {
            sendPasswordResult(false);
            return new HandShakeData(false, null);
        }
        sendPasswordResult(true);

        ArrayList<ImageDescriptor> imagesFromPhone = receiveImagesDescriptors();
        System.out.println("Device " + client.getStringID() + " have " + imagesFromPhone.size() + " media.");
        ArrayList<ImageDescriptor> missingImages = this.mediaManager.getMissingImagesFrom(imagesFromPhone);
        writeImagesDescriptors(missingImages);
        System.out.println("Detected " + missingImages.size() + " images missing on computer.");

        client.read(PType.COMFIRM);

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

    private String receivePassword() {
        return client.read(PType.PASSWORD_PACKET).readString();
    }

    private void sendPasswordResult(final boolean isAuth) throws IOException {
        client.send(new SPacket(PType.PASSWORD_PACKET, getByteForObject(isAuth)));
    }

    private ArrayList<ImageDescriptor> receiveImagesDescriptors() throws IOException {
        final RPacket packetDescriptorList = client.read(PType.EXCHANGING_IMAGES_DESCRIPTOR);
        final String json = packetDescriptorList.readString();

        return new ObjectMapper().readValue(json, new TypeReference<>() {
        });

    }

    private void writeImagesDescriptors(final ArrayList<ImageDescriptor> descriptors) throws IOException {
        final String json = new ObjectMapper().writeValueAsString(descriptors);
        client.send(new SPacket(PType.EXCHANGING_IMAGES_DESCRIPTOR).writeString(json));
    }
}
