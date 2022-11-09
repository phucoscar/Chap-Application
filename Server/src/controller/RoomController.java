package controller;

import model.ServerModel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoomController extends Thread{
    private static ExecutorService pool = Executors.newFixedThreadPool(10);
    public static ArrayList<RoomController> list_room = new ArrayList<>();
    public ServerModel roomModel;
    public int roomId;
    public String roomPassword;

    public RoomController(){
    }

    public RoomController(int roomId, String roomPassword) {
        this.roomId = roomId;
        this.roomPassword = roomPassword;

        roomModel = new ServerModel();
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            int portServer = roomId;
            InetSocketAddress inetSocketAddress =
                    new InetSocketAddress(inetAddress, portServer);

            roomModel.bind_server(inetSocketAddress);
        } catch (Exception e) {
            System.out.println("Can't create server");
        }
    }

    @Override
    public void run() {
        ServerController.setMsg_area("Room " + this.roomId + "is created!" );

        while (!this.roomModel.serverSocket.isClosed()) {
            try {
                // accept new client
                Socket member = roomModel.serverSocket.accept();
                System.out.println(member);

                // create new thread controller for new client
                MemberHandler mh = new MemberHandler(member, roomId);
                MemberHandler.list_members.add(mh);
                pool.execute(mh);
            } catch (Exception e) {
                break;
            }

        }
    }
}
