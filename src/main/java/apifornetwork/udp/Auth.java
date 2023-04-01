package apifornetwork.udp;

import java.net.InetAddress;
import java.util.Objects;

public class Auth {

    protected final int port;
    protected final InetAddress ip;

    public Auth(final int port, final InetAddress ip) {
        this.port = port;
        this.ip = ip;
    }

    public int getPort() {
        return this.port;
    }

    public InetAddress getIp() {
        return this.ip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Auth auth = (Auth) o;
        return port == auth.port && Objects.equals(ip, auth.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(port, ip);
    }

    @Override
    public String toString() {
        return "Auth{" +
                "port=" + port +
                ", ip=" + ip +
                '}';
    }
}
