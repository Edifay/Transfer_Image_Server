package apifornetwork.udp.client;

import apifornetwork.tcp.client.ClientTCP;
import apifornetwork.udp.UDPUsage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static apifornetwork.tcp.server.ServerTCP.packetFastSize;

public class ClientUDP extends UDPUsage {

    protected ClientTCP socket;

    protected ExecutorService exe = Executors.newCachedThreadPool();

    public ClientUDP(final ClientTCP client) throws SocketException {
        super();
        this.socket = client;
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
                        this.exe.submit(() -> {
                            this.socket.receiveFastPacket(data);
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            this.listen.start();
        }
    }
}
