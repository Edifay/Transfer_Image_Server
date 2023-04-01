package apifornetwork.data.packets;

import apifornetwork.tcp.SocketMake;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static apifornetwork.data.packets.Packet.getByteFromShort;

public class PacketIDManager {

    private final SocketMake socket;
    private final ArrayList<Short> alreadyUsed;
    protected ExecutorService exe = Executors.newCachedThreadPool();

    public PacketIDManager(final SocketMake socket) {
        this.socket = socket;
        this.alreadyUsed = new ArrayList<>();
    }

    public synchronized short getID() throws IOException {
        for (short i = 100; i < Short.MAX_VALUE; i++) {
            if (!alreadyUsed.contains(i))
                return addUsed(i);
        }
        return 0;
    }

    public void removeID(final Short ID) {
        this.alreadyUsed.remove(ID);
    }

    private synchronized short addUsed(final Short used) throws IOException {
        this.alreadyUsed.add(used);
        byte[] data = new byte[2];
        System.arraycopy(getByteFromShort(used), 0, data, 0, 2);
        this.socket.send(new SendFastPacket((short) -1, data, (short) data.length));
        this.exe.submit(() -> {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.removeID(used);
        });
        return used;
    }

}
