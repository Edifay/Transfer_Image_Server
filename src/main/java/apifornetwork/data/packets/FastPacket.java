package apifornetwork.data.packets;

public abstract class FastPacket extends Packet {

    protected byte[][] data;

    protected final short packetSize;

    protected FastPacket(final short packetSize) {
        this.packetSize = packetSize;
    }

    @Override
    public byte[] getBytesData() {
        byte[] data = new byte[this.data.length * (this.data[0].length - headSize)];
        for (int i = 0; i < this.data.length; i++)
            System.arraycopy(this.data[i], headSize, data, i * (this.data[i].length - headSize), this.data[i].length - headSize);
        return data;
    }

}
