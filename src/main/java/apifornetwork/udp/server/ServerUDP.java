package apifornetwork.udp.server;

import apifornetwork.tcp.server.ServerTCP;
import apifornetwork.udp.Auth;
import apifornetwork.udp.UDPUsage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static apifornetwork.tcp.server.ServerTCP.packetFastSize;

public class ServerUDP extends UDPUsage {

    protected final ServerTCP serverTCP;
    protected ExecutorService exe = Executors.newCachedThreadPool();

    public ServerUDP(final int port, final ServerTCP serverTCP) throws SocketException {
        super(port);
        this.serverTCP = serverTCP;
    }

    @Override
    public void startListenClient() {
        synchronized (this.listen) {
            this.listen = new Thread(() -> {
                try {

                    while (true) {
                        byte[] data = new byte[packetFastSize];
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        this.receive(packet);
                        this.exe.submit(() -> this.serverTCP.getClient(new Auth(packet.getPort(), packet.getAddress())).receiveFastPacket(data));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            this.listen.start();
        }
    }

}
