package apifornetwork.data.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class Packet {

    protected static final short headSize = 9;

    protected short packetNumber;

    protected short ID;

    public static short getShort(byte one, byte two) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(one);
        bb.put(two);
        return bb.getShort(0);
    }

    public static byte[] getByteFromShort(short s) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort(s);
        return bb.array();
    }

    public static byte[] getByteForObject(Object obj) throws IOException {
        ByteArrayOutputStream outArray = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outArray);
        out.writeUnshared(obj);
        return outArray.toByteArray();
    }

    public short getID() {
        return this.ID;
    }

    public abstract byte[] getBytesData();

    public short getPacketNumber() {
        return this.packetNumber;
    }

}
