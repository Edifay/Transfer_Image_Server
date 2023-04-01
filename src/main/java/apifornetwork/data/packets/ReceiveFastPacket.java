package apifornetwork.data.packets;

public class ReceiveFastPacket extends FastPacket {

    public ReceiveFastPacket(final byte[][] allData) {
        super((short) allData[0].length);
        this.data = allData;
    }

    public ReceiveFastPacket(final byte[][] allData, final short packetNumber) {
        super((short) allData[0].length);
        this.data = allData;
        this.packetNumber = packetNumber;
    }

    public int getSize() {
        return this.data[0].length * this.data.length;
    }

}
