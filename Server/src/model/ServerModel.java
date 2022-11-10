package model;

import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class ServerModel {
    public ServerSocket serverSocket;

    public ServerModel() {
    }

    public void bind_server(InetSocketAddress inetSocketAddress) {
        try {
            this.serverSocket = new ServerSocket();
            this.serverSocket.bind(inetSocketAddress);
        } catch (Exception e) {
            System.out.println("Can't bind server");
        }
    }

    public ServerSocket getServer() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
}
