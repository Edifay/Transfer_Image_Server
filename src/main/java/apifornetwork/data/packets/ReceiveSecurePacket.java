package apifornetwork.data.packets;

public class ReceiveSecurePacket extends SecurePacket {

    public ReceiveSecurePacket(final byte[] data) {
        super(getDataHeadLess(data));
        this.packetNumber = getShort(data[5], data[6]);
    }

    public static byte[] getDataHeadLess(final byte[] data) {
        final byte[] data_head_less = new byte[Math.max(0, data.length - headSize)];
        System.arraycopy(data, headSize, data_head_less, 0, data.length-headSize);
        return data_head_less;
    }

}
