package fr.arnaud.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arnaud.channel.utils.PType;
import fr.arnaud.media.ImageDescriptor;
import fr.arnaud.media.MediaManager;
import fr.arnaud.media.utils.ExifManager;
import fr.jazer.session.RPacket;
import fr.jazer.session.SPacket;
import fr.jazer.session.Session;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class ImagesReceiver {

    private final Session client;
    private final MediaManager mediaManager;
    private final ArrayList<ImageDescriptor> imagesAtReceive;

    public ImagesReceiver(final Session client, final MediaManager mediaManager, final ArrayList<ImageDescriptor> imagesAtReceive) {
        this.client = client;
        this.mediaManager = mediaManager;
        this.imagesAtReceive = imagesAtReceive;
    }

    public ImagesReceiverData receive() throws IOException {

        for (final ImageDescriptor descriptor : imagesAtReceive) {
            System.out.println("Receiving " + descriptor.getName());
            final OutputStream out = this.mediaManager.getOutputStream(descriptor);
            receiveAndWrite(out, descriptor);
            out.flush();
            out.close();
            System.gc();
            System.out.println("Flush " + descriptor.getName() + " total of \t\t" + (int) byteToMegaByte(descriptor.size) + "MB.");
        }

        return new ImagesReceiverData();
    }


    public static class ImagesReceiverData {

    }

    public void askForAnImage(final ImageDescriptor descriptor) throws IOException {
        final String json = new ObjectMapper().writeValueAsString(descriptor);
        client.send(new SPacket(PType.ASK_IMAGE).writeString(json));
    }

    public void receiveAndWrite(final OutputStream out, final ImageDescriptor descriptor) throws IOException {
        int currentData = 0;

        while (currentData < descriptor.size) {
            RPacket data = this.client.read(PType.RECEIVING_DATA);
            byte[] bytes = data.getData();
            out.write(bytes);
            currentData += bytes.length;
        }

        if (currentData != descriptor.size) {
            System.err.println("IMAGE CORROMPU !");
            client.destroy();
            System.exit(-1);
        }

        new Thread(() -> ExifManager.manageExif(mediaManager, descriptor)).start();
    }

    public static float byteToMegaByte(int value) {
        return value / 1000000f;
    }

}
