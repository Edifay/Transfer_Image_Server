package apifornetwork.tcp;


import apifornetwork.data.packets.Packet;
import apifornetwork.tcp.server.ServerTCPEvent;

public final class NewPacketEvent extends ServerTCPEvent {

    protected Packet packet;
    protected RunnableParamPacket event;

    public NewPacketEvent(Packet packet, RunnableParamPacket event) {
        this.packet = packet;
        this.event = event;
    }

    /*
     * a runnable with a parameter
     */
    @Override
    public void run() {
        this.event.run(this.packet);
    }

}