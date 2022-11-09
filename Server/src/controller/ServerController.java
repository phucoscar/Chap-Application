package controller;

import model.ServerModel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerController extends Thread{
    private static ExecutorService pool = Executors.newFixedThreadPool(10);

    public ServerModel serverModel;

    public ServerController () {
        serverModel = new ServerModel();
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            int serverPort = 30030;
            InetSocketAddress inetSocketAddress =
                    new InetSocketAddress(inetAddress, serverPort);
            serverModel.bind_server(inetSocketAddress);
        } catch (Exception e) {
            System.out.println("Can't create server!");
        }
    }

    @Override
    public void run() {
        ServerController.setMsg_area("Server is listening ...");
        while (!this.serverModel.serverSocket.isClosed()) {
            try {
                Socket s = serverModel.serverSocket.accept();

                ClientHandler ch = new ClientHandler(s);
                ClientHandler.list_clients.add(ch);
                pool.execute(ch);
            } catch (Exception e) {
                break;
            }
        }
    }

    // set text area
    public static void setMsg_area(String msg) {
        if (ServerView.msg_are.getText().trim().equals("")) {
            ServerView.msg_area.setText(msg);
        }
        else {
            ServerView.msg_are.setText(ServerView.msg_area.getText().trim() + "\n" + msg);
        }
    }
}
