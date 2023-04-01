package apifornetwork.udp;

import apifornetwork.data.packets.SendFastPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public abstract class UDPUsage extends DatagramSocket {

    protected Thread listen;

    public UDPUsage(int port) throws SocketException {
        super(port);
        this.listen = new Thread();
        this.setReceiveBufferSize(2097152);
    }

    public UDPUsage() throws SocketException {
        super();
        this.listen = new Thread();
    }

    public void sendPacket(SendFastPacket packet, Auth identities) throws IOException {
        for (int i = 0; i < packet.getBytes().length; i++) {
            this.send(new DatagramPacket(packet.getBytes()[i], packet.getBytes()[i].length, identities.getIp(), identities.getPort()));
        }
    }

    public abstract void startListenClient();

    public void stopListenClient() {
        synchronized (this.listen) {
            this.listen.interrupt();
        }
    }

}
