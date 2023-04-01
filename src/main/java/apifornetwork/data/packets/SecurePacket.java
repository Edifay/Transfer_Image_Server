package apifornetwork.data.packets;

public abstract class SecurePacket extends Packet {

    public byte[] data;

    public SecurePacket(final byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] getBytesData() {
        return this.data;
    }
}
