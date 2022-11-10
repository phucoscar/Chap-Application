package model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientModel {
    private Socket client;
    private ObjectOutputStream oout;
    private ObjectInputStream oin;

    public ClientModel() {

    }

    public void connectServer(InetSocketAddress inetSocketAddress) {
        try {
            this.client = new Socket();
            this.client.connect(inetSocketAddress);
            System.out.println("You are connecting...");

            this.oout = new ObjectOutputStream(this.client.getOutputStream());
            this.oin = new ObjectInputStream((this.client.getInputStream()));
        } catch (Exception e) {
            System.out.println("Can not create client socket!");
        }
    }

    public Socket getClient() {
        return client;
    }

    public void setClient(Socket client) {
        this.client = client;
    }

    public ObjectOutputStream getOout() {
        return oout;
    }

    public void setOout(ObjectOutputStream oout) {
        this.oout = oout;
    }

    public ObjectInputStream getOin() {
        return oin;
    }

    public void setOin(ObjectInputStream oin) {
        this.oin = oin;
    }
}
