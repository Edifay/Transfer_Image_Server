package apifornetwork.tcp.server;

import apifornetwork.data.packets.Packet;
import apifornetwork.data.packets.ReceiveSecurePacket;
import apifornetwork.data.packets.SendFastPacket;
import apifornetwork.data.packets.SendSecurePacket;
import apifornetwork.tcp.SocketMake;
import apifornetwork.udp.Auth;

import java.io.IOException;
import java.net.Socket;

public class SocketClient extends SocketMake {

    protected final ServerTCP server;

    public SocketClient(final Socket s, ServerTCP server) throws IOException {
        super(s);
        this.server = server;
    }

    public synchronized void initUDP() throws IOException, InterruptedException {
        byte[] data = new byte[1];
        if (this.server.initFastPacket())
            data[0] = 0x1;
        this.send(new SendFastPacket((short) -3, data, (short) 1));
        ReceiveSecurePacket packet = this.waitForPacket(-4);
        this.identity = new Auth(getInteger(packet.getBytesData()), this.s.getInetAddress());
    }

    @Override
    public synchronized void send(Packet packet) throws IOException {
        if (packet instanceof SendFastPacket) {
            ((SendFastPacket) packet).setServerSender(this);
            this.server.sendFastPacket((SendFastPacket) packet, this.identity);
        } else if (packet instanceof SendSecurePacket)
            super.send(packet);
        else
            throw new IOException("Wrong send packet");
    }

}