package apifornetwork.tcp;

import apifornetwork.data.packets.Packet;

import java.util.EventListener;

public interface RunnableParamPacket extends EventListener {
    void run(Packet socket);
}
