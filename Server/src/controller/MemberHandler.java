package controller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MemberHandler implements Runnable{
    public Socket member;
    public static ArrayList<MemberHandler> list_members = new ArrayList<>();
    public ObjectInputStream oin;
    public ObjectOutputStream oout;
    public String nickname = "";
    public int roomId;

    public MemberHandler(){
    }

    public MemberHandler(Socket member, int roomId) throws Exception {
        this.roomId = roomId;
        this.member = member;
        this.oin = new ObjectInputStream(member.getInputStream());
        this.oout = new ObjectOutputStream(member.getOutputStream());
    }

    @Override
    public void run() {
        String messageClient = "";
        String messageServer = "";
        Object object;

        this.nickname = (String) this.receiveMemberMessage();
        ServerController.setMsg_area("Member [" + this.nickname + "] "
            + "is connected to room " + this.roomId);
        System.out.println(this.nickname + ": " + this.member);

        // send welcome text to client
        messageServer = "[SERVER]:&nbsp&nbsp Welcome " + this.nickname +
                "! You are connected to room " + this.roomId + "!";
        this.sendRoomMessage(messageServer);

        messageServer = "[SERVER]:&nbsp&nbsp " + this.nickname + " joined the room!";
        this.broadcast_other_clients(messageServer);

        while (!this.member.isClosed()) {
            object = this.receiveMemberMessage();
            if (this.member.isClosed()) {
                list_members.remove(this);
                ServerController.setMsg_area("Member [" + this.nickname + "] "
                        + "was disconnected room " + this.roomId);
                int count = numberMemberInRoom(roomId);
                if (count == 0) {
                    for (RoomController rc : RoomController.list_room) {
                        if (rc.roomId == this.roomId) {
                            try {
                                rc.roomModel.serverSocket.close();
                            }
                            catch (Exception e) {
                                System.out.println("Can't close room server!");
                            }
                            RoomController.list_room.remove(rc);
                            break;
                        }
                    }
                }
                break;
            }
            if (object instanceof String) {
                messageClient = (String) object;
                if (messageClient.equals("Show members!")) {
                    this.sendRoomMessage("List members!");
                    this.sendRoomMessage("" + this.roomId);
                    String str_members = this.get_all_members();
                    this.sendRoomMessage(str_members);
                }
                else if (messageClient.equals("Admin wants to kick a member!")) {
                    String kicked_nickname = (String) this.receiveMemberMessage();
                    messageServer = "You are kicked!";
                    this.send_msg_server_specified(kicked_nickname, messageServer);
                    messageServer = "[SERVER]: " + kicked_nickname + " was kicked by admin!";
                    this.broadcast_all_except(kicked_nickname, messageServer);
                }
                else if (messageClient.equals("Start camera!")) {
                    WebcamController wc = new WebcamController(this.roomId);
                    wc.start();

                    this.sendRoomMessage("Accept to start camera!");
                    this.sendRoomMessage(""+this.roomId);
                }
                else {
                    String messageSend = "[" + this.nickname + "]:&nbsp&nbsp " + messageClient;
                    this.broadcast_other_clients(messageSend);
                }
            }
            else {
                String msg_send = "[" + this.nickname + "]:&nbsp&nbsp " + "<img src='" + object.toString() + "'></img>";
                this.broadcast_other_clients(msg_send);
            }
        }
    }

    // receive message from member in room chat
    public Object receiveMemberMessage() {
        Object object = "No message from client";
        try {
            object = this.oin.readObject();

        } catch (Exception e) {
            try {
                this.member.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
        return object;
    }

    // send message of server to all members in room chat
    public void sendRoomMessage(Object object) {
        try {
            this.oout.writeObject(object);
        } catch (Exception e) {
            try {
                this.member.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    // send message of server to all members in room chat except for the owner
    public void broadcast_all(Object object) {
        try {
            for (MemberHandler mh: list_members) {
                if (mh.roomId == this.roomId) {
                    mh.oout.writeObject(object);
                }
            }
        } catch (Exception e) {
            try {
                this.member.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    // send message of server to all members in room chat except for the owner
    public void broadcast_other_clients(Object object) {
        try {
            for(MemberHandler mh: list_members) {
                if (mh != this && mh.roomId == this.roomId) {
                    mh.oout.writeObject(object);
                }
            }
        } catch (Exception e) {
            try {
                this.member.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    // Broadcast a msg to all member in a room except for specified member
    public void broadcast_all_except(String nickname, Object o) {
        try {
            for (MemberHandler mh : list_members) {
                if ((!mh.nickname.equals(nickname)) && (mh.roomId == this.roomId)) {
                    mh.oout.writeObject(o);
                }
            }
        }
        catch(Exception e) {
            try {
                this.member.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }

    // Send object to a specified member
    public void send_msg_server_specified(String nickname, Object o) {
        try {
            for (MemberHandler mh : list_members) {
                if (mh.nickname.equals(nickname)) {
                    mh.oout.writeObject(o);
                    break;
                }
            }
        }
        catch(Exception e) {
            try {
                this.member.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }

    public int numberMemberInRoom(int roomId) {
        int count = 0;
        for (MemberHandler mh : list_members) {
            if (mh.roomId == roomId) {
                count++;
            }
        }
        return count;
    }

    // Get all members in a room
    public String get_all_members() {
        String str_members = "";
        for (MemberHandler mh : list_members) {
            if (mh.roomId == this.roomId) {
                str_members += mh.nickname + "-" +
                        mh.member.getLocalAddress().toString().substring(1) + "-" +
                        mh.member.getPort() + "-" +
                        mh.member.getLocalPort() + ";";
            }
        }
        return str_members;
    }

}
