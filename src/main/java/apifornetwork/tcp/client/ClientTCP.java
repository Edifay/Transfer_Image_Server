package apifornetwork.tcp.client;

import apifornetwork.data.packets.Packet;
import apifornetwork.data.packets.SendFastPacket;
import apifornetwork.data.packets.SendSecurePacket;
import apifornetwork.tcp.SocketMake;
import apifornetwork.udp.Auth;
import apifornetwork.udp.client.ClientUDP;

import java.io.IOException;
import java.net.Socket;

public class ClientTCP extends SocketMake {

    protected ClientUDP udp;

    public ClientTCP(final String ip, final int port) throws IOException {
        super(new Socket(ip, port));
        this.identity = new Auth(this.s.getPort(), this.s.getInetAddress());
        this.addPacketEvent(-3, (packet) -> {
            synchronized (this) {
                this.isFastPacketEnable = packet.getBytesData()[0] != 0;
                try {
                    this.udp = new ClientUDP(this);
                    //this.udp.sendPacket(SendPacket.emptyPacketOf(0, true), this.identity);
                    this.udp.startListenClient();
                    byte[] data = getByteFromInteger(this.udp.getLocalPort());
                    this.send(new SendFastPacket((short) -4, data, (short) data.length));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public synchronized void send(Packet packet) throws IOException {
        if (packet instanceof SendFastPacket) {
            ((SendFastPacket) packet).setServerSender(this);
            this.udp.sendPacket((SendFastPacket) packet, this.identity);
        } else if (packet instanceof SendSecurePacket)
            super.send( packet);
        else {
            throw new IOException("Wrong Packet to Send !");
        }

    }


}
