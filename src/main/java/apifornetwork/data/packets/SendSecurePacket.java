package apifornetwork.data.packets;

public class SendSecurePacket extends SecurePacket {

    public SendSecurePacket(final short packet_number, final byte[] data) {
        super(new byte[headSize + data.length]);
        this.packetNumber = packet_number;
        System.arraycopy(getByteFromShort((short) 0), 0, this.data, 1, 2); // table number 1 -> 2
        System.arraycopy(getByteFromShort((short) 0), 0, this.data, 3, 2); // total table 3 -> 4
        System.arraycopy(getByteFromShort(this.packetNumber), 0, this.data, 5, 2); // packet number 5 -> 6
        System.arraycopy(data, 0, this.data, headSize, data.length);
    }

    public static SendSecurePacket emptyPacketOf(int packetNumber) {
        return new SendSecurePacket((short) packetNumber, new byte[0]);
    }

}
