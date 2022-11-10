package controller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    public Socket client;
    public static ArrayList<ClientHandler> list_clients = new ArrayList<>();
    public ObjectInputStream oin;
    public ObjectOutputStream oout;
    public String nickname;

    public ClientHandler() {
    }

    public ClientHandler(Socket client) throws Exception {
        this.client = client;
        this.oin = new ObjectInputStream(client.getInputStream());
        this.oout = new ObjectOutputStream(client.getOutputStream());
    }

    @Override
    public void run() {
        String messageClient = "";
        String messageServer = "";
        while (true) {
            messageClient = (String) this.receiveClientMessage(); // Recieve client's nickname
            if (messageClient.equals("Client joined server!")) { // Don't check password
                break;
            }
            messageServer = this.checkNicknameExist(messageClient);
            this.sendServerMessage(messageServer);
        }

        this.nickname = (String) this.receiveClientMessage();
        ServerController.setMsg_area("Client [" + this.nickname + "] is connected to server");
        System.out.println(this.nickname + ": " + this.client);

        while (!this.client.isClosed()) {
            messageClient = (String) this.receiveClientMessage();
            if (this.client.isClosed()) {
                list_clients.remove(this);
                ServerController.setMsg_area("Client [" + this.nickname + "] was disconnected server");
                break;
            }
            if (messageClient.equals("Client want to create room!")) {
                int roomId = 0;
                String roomPass = "";
                while (true) {
                    messageClient = (String) this.receiveClientMessage();

                    if (messageClient.equals("Member created room!")) {
                        break;
                    }

                    roomId = Integer.parseInt((String) this.receiveClientMessage());
                    messageServer = this.checkRoomIdExist(roomId);
                    if (!messageServer.equals("Room is existed!") &&
                            !messageServer.equals("Room is not in range!")) {
                        roomPass = (String) this.receiveClientMessage();

                    }
                }

                RoomController room = new RoomController(roomId, roomPass);
                RoomController.list_room.add(room);
                room.start();

                this.sendServerMessage("Room is created");
            }

            if (messageClient.equals("Client want to join room!")) {
                int roomId = 0;
                String roomPass = "";

                while (true) {
                    messageClient = (String) this.receiveClientMessage();
                    if (messageClient.equals("Member joined room!")) {
                        break;
                    }
                    roomId = Integer.parseInt((String) this.receiveClientMessage());
                    roomPass = (String) this.receiveClientMessage();

                    messageServer = this.checkJoinRoom(roomId, roomPass);
                    this.sendServerMessage(messageServer);
                }
            }
        }

    }

    public Object   receiveClientMessage() {
        Object object = "No message from client";
        try{
            object = this.oin.readObject();
        } catch (Exception e) {
            try {
                this.client.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
        return object;
    }

    public void sendServerMessage(Object object) {
        try {
            this.oout.writeObject(object);
        } catch (Exception e) {
            try {
                this.client.close();;
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    // Check nickname existence
    public String checkNicknameExist(String nickname) {
        for (ClientHandler ch : list_clients) {
            if (ch.nickname.toLowerCase().equals(nickname.toLowerCase())) {
                return "Nickname existed!";
            }
        }
        return "Nickname unexisted!";
    }

    public String checkRoomIdExist(int roomId) {
        if (roomId < 40000 || roomId > 50000) {
            return "Room id is not in range";
        }
        if (RoomController.list_room.size() > 0) {
            for (RoomController rc : RoomController.list_room) {
                if (rc.roomId == roomId) {
                    return "Room id existed!";
                }
            }
        }
        return "Room id unexisted!";
    }

    // Check room id and password to join room
    public String checkJoinRoom(int roomId, String roomPassword) {
        if (RoomController.list_room.size() > 0) {
            for (RoomController rc : RoomController.list_room) {
                if (rc.roomId == roomId) {
                    if (rc.roomPassword.equals(roomPassword)) {
                        return "Accepted to join!";
                    }
                    else {
                        return "Wrong password!";
                    }
                }
            }
        }
        return "Room id unexisted!";
    }
}
