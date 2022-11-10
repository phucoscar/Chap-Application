package controller;

import model.ClientModel;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientController {
    public ClientModel clientModel;
    public Socket clientSocket;
    public ObjectInputStream oin;
    public ObjectOutputStream out;
    public String nickname;

    public ClientController() {
        InetSocketAddress inetSocketAddress = null;
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            int portServer = 30030;
            inetSocketAddress =
                    new InetSocketAddress(inetAddress, portServer);

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.clientModel = new ClientModel();
        clientModel.connectServer(inetSocketAddress);
        this.clientSocket = clientModel.getClient();
        this.out = this.clientModel.getOout();
        this.oin = this.clientModel.getOin();
    }


    // receive object from server
    public Object receiveServerMessage() {
        Object serverObject = new Object();
        try {
            serverObject = this.oin.readObject();
        } catch (Exception e) {
            try {
                this.clientSocket.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
        return serverObject;
    }


    public void sendClientMessage(Object object) {
        try {
            this.out.writeObject(object);
        } catch (Exception e) {
            try {
                this.clientSocket.close();
            } catch (Exception ee) {
                System.out.println(ee);
            }
        }
    }

}
