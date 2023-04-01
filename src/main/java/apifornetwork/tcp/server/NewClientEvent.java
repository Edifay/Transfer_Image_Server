package apifornetwork.tcp.server;


import apifornetwork.tcp.SocketMake;

public final class NewClientEvent extends ServerTCPEvent {

    protected SocketMake socket;
    protected RunnableParamSocket event;

    public NewClientEvent(SocketMake socket, RunnableParamSocket event) {
        this.socket = socket;
        this.event = event;
    }

    /*
     * a runnable with a parameter
     */
    @Override
    public void run() {
        this.event.run(this.socket);
    }

}