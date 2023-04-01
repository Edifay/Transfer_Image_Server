package apifornetwork.tcp.Secure;

import apifornetwork.data.packets.Packet;
import apifornetwork.data.packets.SendSecurePacket;
import apifornetwork.tcp.SocketMake;

import java.io.IOException;
import java.net.Socket;

public class SecureSocketClient extends SocketMake {


    protected final SecureServerTCP server;

    public SecureSocketClient(final Socket s, SecureServerTCP server) throws IOException {
        super(s);
        this.server = server;
    }
/*

    public synchronized void initUDP() throws IOException, InterruptedException {
        byte[] data = new byte[1];
        if (this.server.initFastPacket())
            data[0] = 0x1;
        this.send(new SendPacket((short) -3, data, (short) 1));
        ReceivePacket packet = this.waitForPacket(-4);
        this.identity = new Auth(getInteger(packet.getBytesData()), this.s.getInetAddress());
    }*/

    @Override
    public synchronized void send(Packet packet) throws IOException {
        if (packet instanceof SendSecurePacket)
            super.send(packet);
        else
            throw new IOException("Wrong packet to send");
    }

}
