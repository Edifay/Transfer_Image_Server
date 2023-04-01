package apifornetwork.data.packets;

import apifornetwork.tcp.SocketMake;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static apifornetwork.tcp.server.ServerTCP.packetFastSize;

public class SendFastPacket extends FastPacket {

    public SendFastPacket(final short packetNumber, final byte[] data, final short packetLength) {
        super((short) (packetFastSize));
        this.packetNumber = packetNumber;
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        this.data = new byte[(int) Math.ceil((double) in.available() / (this.packetSize - headSize))][this.packetSize];
        short actualTable = 0;
        short totalTableSize = (short) this.data.length;
        while (in.available() > 0) {
            System.arraycopy(getByteFromShort(actualTable), 0, this.data[actualTable], 1, 2); // table number 1 -> 2
            System.arraycopy(getByteFromShort(totalTableSize), 0, this.data[actualTable], 3, 2); // total table 3 -> 4
            System.arraycopy(getByteFromShort(this.packetNumber), 0, this.data[actualTable], 5, 2); // packet number 5 -> 6
            in.read(this.data[actualTable], headSize, packetSize - headSize);
            actualTable++;
        }
    }

    public SendFastPacket(final short packetNumber, final byte[] data) throws IOException {
        this(packetNumber, data, (short) (packetFastSize - headSize));
    }

    public int getSize() {
        return this.data[0].length * this.data.length;
    }

    public void setServerSender(SocketMake socketMake) throws IOException {
        this.ID = socketMake.getNewID();
        for (byte[] datum : this.data) System.arraycopy(getByteFromShort(this.ID), 0, datum, 7, 2);
    }

    public static SendFastPacket emptyPacketOf(int packetNumber) {
        return new SendFastPacket((short) packetNumber, new byte[packetFastSize], (short) packetFastSize);
    }

    public byte[][] getBytes() {
        return this.data;
    }

}