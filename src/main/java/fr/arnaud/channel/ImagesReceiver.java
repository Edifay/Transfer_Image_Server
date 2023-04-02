package fr.arnaud.channel;

import apifornetwork.data.packets.Packet;
import apifornetwork.data.packets.ReceiveSecurePacket;
import apifornetwork.data.packets.SendSecurePacket;
import apifornetwork.tcp.NewPacketEvent;
import apifornetwork.tcp.RunnableParamPacket;
import apifornetwork.tcp.SocketMake;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arnaud.channel.utils.PType;
import fr.arnaud.media.ImageDescriptor;
import fr.arnaud.media.MediaManager;
import fr.arnaud.media.utils.ExifManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import static apifornetwork.data.packets.Packet.getByteForObject;
public class ImagesReceiver {

    private final SocketMake client;
    private final MediaManager mediaManager;
    private final ArrayList<ImageDescriptor> imagesAtReceive;

    private final RunnableParamPacket event;
    private Queue<ReceiveSecurePacket> dataPackets = new LinkedList<>();

    public ImagesReceiver(final SocketMake client, final MediaManager mediaManager, final ArrayList<ImageDescriptor> imagesAtReceive) {
        this.client = client;
        this.mediaManager = mediaManager;
        this.imagesAtReceive = imagesAtReceive;

        this.event = event -> {
            if (event.getPacketNumber() == PType.RECEIVING_DATA) {
                dataPackets.add((ReceiveSecurePacket) event);
            }
        };
    }

    public ImagesReceiverData receive() throws IOException, InterruptedException {

        this.client.addPacketEvent(event);

        for (final ImageDescriptor descriptor : imagesAtReceive) {
            System.out.println("Receiving " + descriptor.getName());
            final OutputStream out = this.mediaManager.getOutputStream(descriptor);
            receiveAndWrite(out, descriptor);
            out.flush();
            out.close();
            System.gc();
            System.out.println("Flush " + descriptor.getName() + " total of \t\t" + (int) byteToMegaByte(descriptor.size) + "MB.");
        }


        this.client.removePacketEvent(event);

        return new ImagesReceiverData();
    }


    public static class ImagesReceiverData {

    }

    public void askForAnImage(final ImageDescriptor descriptor) throws IOException {
        final String json = new ObjectMapper().writeValueAsString(descriptor);
        client.send(new SendSecurePacket(PType.ASK_IMAGE, getByteForObject(json)));
    }

    public void receiveAndWrite(final OutputStream out, final ImageDescriptor descriptor) throws InterruptedException, IOException {
        int currentData = 0;

        while (currentData < descriptor.size) {
            ReceiveSecurePacket data = waitForNextReceiveData();
            byte[] bytes = data.getBytesData();
            out.write(bytes);
            currentData += bytes.length;
        }

        if (currentData != descriptor.size) {
            System.err.println("IMAGE CORROMPU !");
            client.close();
            System.exit(-1);
        }

        ExifManager.manageExif(mediaManager, descriptor);
    }

    public ReceiveSecurePacket waitForNextReceiveData() {
        try {
            while (dataPackets.size() == 0 && !client.getSocket().isClosed()) {
                Thread.sleep(200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataPackets.poll();
    }

    public static float byteToMegaByte(int value) {
        return value / 1000000f;
    }

}
