package apifornetwork.tcp.server;

import apifornetwork.data.packets.SendFastPacket;
import apifornetwork.tcp.SocketMake;
import apifornetwork.udp.Auth;
import apifornetwork.udp.server.ServerUDP;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerTCP extends ServerSocket {

    private Thread listen;

    private final ArrayList<RunnableParamSocket> eventsOnNewClient;
    private final ExecutorService exe = Executors.newCachedThreadPool();

    private final short packetSize;

    public static int packetFastSize = 8192;

    private Object keyUDP;
    private ServerUDP udp;
    protected final int port;

    protected ArrayList<SocketClient> portToClient;

    public ServerTCP(final int port, short packetSize) throws IOException {
        super(port);
        this.port = port;
        this.eventsOnNewClient = new ArrayList<>();
        this.listen = new Thread();
        this.packetSize = packetSize;
        this.keyUDP = new Object();
        this.portToClient = new ArrayList<>();
    }

    public void addEventOnNewClient(RunnableParamSocket event) {
        synchronized (this.eventsOnNewClient) {
            this.eventsOnNewClient.add(event);
        }
    }

    public void removeEventOnNewClient(RunnableParamSocket event) {
        synchronized (this.eventsOnNewClient) {
            this.eventsOnNewClient.remove(event);
        }
    }

    public void startListenClient() {
        synchronized (this.listen) {
            this.listen = new Thread(() -> {
                Thread.currentThread().setName("Server for Client Waiting");
                try {
                    while (true) {
                        SocketClient socket = new SocketClient(this.accept(), this);
                        this.portToClient.add(socket);
                        new Thread(() -> {
                            synchronized (this.eventsOnNewClient) {
                                for (RunnableParamSocket atRun : this.eventsOnNewClient)
                                    exe.submit(new NewClientEvent(socket, atRun));

                            }
                        }).start();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            this.listen.start();
        }

    }

    public void stopListenClient() {
        synchronized (this.listen) {
            this.listen.interrupt();
        }
    }

    public boolean isBindedUDP() {
        return this.udp != null;
    }

    public boolean isFastPacketEnable() {
        synchronized (this.keyUDP) {
            return this.udp != null;
        }
    }

    public boolean initFastPacket() throws SocketException {
        synchronized (this.keyUDP) {
            if (this.udp == null) {
                this.udp = new ServerUDP(this.port, this);
                this.udp.startListenClient();
                return true;
            }
            return false;
        }
    }

    public void stopFastPacket() {
        synchronized (this.keyUDP) {
            this.udp.close();
            this.udp = null;
        }
    }

    public SocketMake getClient(Auth identities) {
        for (SocketMake client : this.portToClient)
            if (client.getIdentity().equals(identities))
                return client;

        return null;
    }

    public void sendFastPacket(SendFastPacket packet, Auth identities) throws IOException {
        this.udp.sendPacket(packet, identities);
    }

}