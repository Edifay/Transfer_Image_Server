package apifornetwork.tcp;

import apifornetwork.data.packets.*;
import apifornetwork.udp.Auth;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static apifornetwork.data.packets.Packet.getByteFromShort;
import static apifornetwork.data.packets.Packet.getShort;

public abstract class SocketMake {

    protected Socket s;
    protected Thread listen;
    protected ObjectOutputStream out;
    protected ObjectInputStream in;
    final protected HashMap<Integer, ArrayList<RunnableParamPacket>> events;
    protected ArrayList<RunnableParamPacket> eventsOnAll;
    protected ExecutorService exe = Executors.newCachedThreadPool();
    protected boolean isFastPacketEnable;
    protected PacketIDManager managerId;
    protected Auth identity;
    protected HashMap<Short, NotFinalizedReceivePacket> receivingPacket;

    public SocketMake(final Socket s) throws IOException {
        System.out.println("Creation du super SocketMake");
        this.s = s;
        this.out = new ObjectOutputStream(s.getOutputStream());
        this.in = new ObjectInputStream(s.getInputStream());
        this.events = new HashMap<>();
        this.eventsOnAll = new ArrayList<>();
        this.listen = new Thread();
        this.managerId = new PacketIDManager(this);
        this.receivingPacket = new HashMap<>();
        this.identity= new Auth(s.getPort(), s.getInetAddress());
        this.addPacketEvent(-1, (packet) -> {
            byte[] data = packet.getBytesData();
            short idAtRemove = getShort(data[0], data[1]);
        });

        this.addPacketEvent(-2, (packet) -> {
            byte[] data = packet.getBytesData();
            short idAtRemove = getShort(data[0], data[1]);
            this.managerId.removeID(idAtRemove);
        });
    }

    public void startListen() {
        synchronized (this.listen) {
            this.listen = new Thread(() -> {
                Thread.currentThread().setName("Client: Listening !");
                try {
                    while (true) {
                        ReceiveSecurePacket receivePacket = new ReceiveSecurePacket((byte[]) this.in.readUnshared());
                        this.emit(receivePacket);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    try {
                        this.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
            this.listen.start();
        }
    }

    public void emit(Packet packet) {
        new Thread(() -> {
            synchronized (this.events) {
                for (RunnableParamPacket event : this.eventsOnAll)
                    this.exe.submit(new NewPacketEvent(packet, event));
                if (this.events.containsKey((int) packet.getPacketNumber()))
                    for (RunnableParamPacket event : this.events.get((int) packet.getPacketNumber()))
                        this.exe.submit(new NewPacketEvent(packet, event));
            }
        }).start();
    }

    public void stopListen() {
        synchronized (this.listen) {
            this.listen.interrupt();
        }
    }

    public synchronized void send(Packet packet) throws IOException {
        if (!(packet instanceof SendSecurePacket))
            throw new IOException("Wrong packet");
        this.out.writeUnshared(packet.getBytesData());
        this.out.flush();
        this.out.reset();
    }

    public void addPacketEvent(int packetNumber, RunnableParamPacket event) {
        synchronized (this.events) {
            if (this.events.containsKey(packetNumber))
                this.events.get(packetNumber).add(event);
            else {
                ArrayList<RunnableParamPacket> array = new ArrayList<>();
                array.add(event);
                this.events.put(packetNumber, array);
            }
        }
    }

    public void removePacketEvent(int packetNumber, RunnableParamPacket event) {
        synchronized (this.events) {
            if (this.events.containsKey(packetNumber))
                this.events.get(packetNumber).remove(event);
        }
    }

    public void addPacketEvent(RunnableParamPacket event) {
        synchronized (this.events) {
            this.eventsOnAll.add(event);
        }
    }

    public void removePacketEvent(RunnableParamPacket event) {
        synchronized (this.events) {
            this.eventsOnAll.remove(event);
        }
    }

    public ReceiveSecurePacket waitForPacket(int packetNumber) throws InterruptedException {
        final Thread atNotify = Thread.currentThread();
        final AtomicReference<ReceiveSecurePacket> receivePacket = new AtomicReference<>();

        final RunnableParamPacket event = (packet) -> {
            synchronized (atNotify) {
                receivePacket.set((ReceiveSecurePacket) packet);
                atNotify.notify();
            }
        };

        this.addPacketEvent(packetNumber, event);
        synchronized (Thread.currentThread()) {
            Thread.currentThread().wait();
            this.removePacketEvent(packetNumber, event);
        }
        return receivePacket.get();
    }

    public short getNewID() throws IOException {
        return this.managerId.getID();
    }

    private final Object keyReceive = new Object();

    public void receiveFastPacket(byte[] data) {
        synchronized (keyReceive) {
            short id = getPacketID(data);

            if (!this.receivingPacket.containsKey(id)) {
                NotFinalizedReceivePacket receivePacket = new NotFinalizedReceivePacket(data);
                this.receivingPacket.put(id, receivePacket);
                this.exe.submit(() -> {
                    try {
                        ReceiveFastPacket packet = receivePacket.waitForFinalized(10000L);
                        if (packet != null)
                            this.emit(packet);

                        this.receivingPacket.remove(id);

                        byte[] dataShort = new byte[2];
                        System.arraycopy(getByteFromShort(id), 0, dataShort, 0, 2);
                        send(new SendFastPacket((short) -2, dataShort, (short) dataShort.length));
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                });
            } else
                this.receivingPacket.get(id).addData(data);

        }
    }

    private short getPacketID(byte[] data) {
        return getShort(data[7], data[8]);
    }

    public static byte[] getByteFromInteger(int integer) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(integer);
        return bb.array();
    }

    public static int getInteger(byte[] data) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(data);
        return bb.getInt(0);
    }

    public Auth getIdentity() {
        return this.identity;
    }

    @Override
    public String toString() {
        return "SocketMake{" +
                "s=" + s +
                ", listen=" + listen +
                ", out=" + out +
                ", in=" + in +
                ", events=" + events +
                ", eventsOnAll=" + eventsOnAll +
                ", exe=" + exe +
                ", isFastPacketEnable=" + isFastPacketEnable +
                ", managerId=" + managerId +
                ", identity=" + identity +
                ", receivingPacket=" + receivingPacket +
                ", keyReceive=" + keyReceive +
                '}';
    }

    public Socket getSocket() {
        return this.s;
    }

    public void close() throws IOException {
        this.s.close();
    }

}
