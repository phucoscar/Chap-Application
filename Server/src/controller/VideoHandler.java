package controller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class VideoHandler implements Runnable{
    public Socket video;
    public static ArrayList<VideoHandler> list_videos = new ArrayList<>();
    public ObjectInputStream oin;
    public ObjectOutputStream oout;
    public String nickname = "";
    public int roomId;

    public VideoHandler() {
    }

    public VideoHandler(Socket video, int roomId) throws Exception {
        this.roomId = roomId;
        this.video = video;
        this.oin = new ObjectInputStream(video.getInputStream());
        this.oout = new ObjectOutputStream(video.getOutputStream());
    }

    @Override
    public void run() {
        String messageClient = "";
        String messageServer = "";
        Object object;

        while (!this.video.isClosed()) {
            object = this.receiveMemberMessage();
            if (this.video.isClosed()) {
                list_videos.remove(this);
                if (list_videos.isEmpty()) {
                    try {
                        WebcamController.webcamModel.serverSocket.close();
                        ServerController.setMsg_area("Video call at room " + this.roomId + " ended");
                    }
                    catch(Exception e) {
                        System.out.println(e);
                    }
                    break;
                }
                break;
            }
            broadcast_other_members(object);
        }
    }

    // Recieve msg from member in room chat
    public Object receiveMemberMessage() {
        Object object = "No msg from client";
        try {
            object = this.oin.readObject();
        }
        catch(Exception e) {
            try {
                this.video.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
        return object;
    }

    // Send msg of server to a member in room chat
    public void send_room_msg(Object object) {
        try {
            this.oout.writeObject(object);
        }
        catch(Exception e) {
            try {
                this.video.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }

    // Send msg of server to all members in room chat except for the owner
    public void broadcast_all(Object object) {
        try {
            for (VideoHandler vh : list_videos) {
                if (vh.roomId == this.roomId) {
                    vh.oout.writeObject(object);
                }
            }
        }
        catch(Exception e) {
            try {
                this.video.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }

    // Send msg of server to all members in room chat except for the owner
    public void broadcast_other_members(Object object) {
        try {
            for (VideoHandler vh : list_videos) {
                if (vh != this && vh.roomId == this.roomId) {
                    vh.oout.writeObject(object);
                }
            }
        }
        catch(Exception e) {
            try {
                this.video.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }
}
