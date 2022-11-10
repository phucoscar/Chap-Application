/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;


import com.github.sarxos.webcam.Webcam;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.swing.ImageIcon;
import model.ClientModel;
import view.VideoReceiveView;
import view.VideoReceiveView;
import view.VideoSendView;

public class VideoController {
    public ClientModel videoModel;
    public Socket videoSocket;
    public ObjectOutputStream oout;
    public ObjectInputStream oin;
    public String nickname;

    public VideoController() {
    }

    public VideoController(String nickname, int room_id) {
        this.nickname = nickname;
        InetSocketAddress inetSocketAddress = null;
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            //InetAddress inet_address = InetAddress.getByName("26.135.6.182");
            int portServer = room_id + 1;
            inetSocketAddress =
                    new InetSocketAddress(inetAddress, portServer);
        }
        catch(Exception e) {
            System.out.println(e);
        }

        this.videoModel = new ClientModel();
        videoModel.connectServer(inetSocketAddress);
        this.videoSocket = videoModel.getClient();
        this.oout = this.videoModel.getOout();
        this.oin = this.videoModel.getOin();
    }

    // Recieve object from server
    public Object receiveServerMessage() {
        Object serverObject = new Object();
        try {
            serverObject = this.oin.readObject();
        }
        catch(Exception e) {
            try {
                this.videoSocket.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
        return serverObject;
    }

    // Send object from client
    public void sendClientMessage(Object o) {
        try {
            this.oout.writeObject(o);
        }
        catch(Exception e) {
            try {
                this.videoSocket.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }

    // Open camera and send frame to server
    public class VideoSendThread extends Thread {
        public boolean isOpened;
        @Override
        public void run() {
            ImageIcon ic;
            BufferedImage br;
            Webcam cam = Webcam.getDefault();
            Dimension d = new Dimension(320, 240);
            cam.setViewSize(d);
            cam.open(false);
            isOpened = true;

            while (isOpened == true) {
                if (videoSocket.isClosed()) {
                    break;
                }
                try {
                    br = cam.getImage();
                    ic = new ImageIcon(br);
                    VideoSendView.video_area.setIcon(ic);
                    oout.writeObject(ic);
                }
                catch(Exception e) {
                }
            }
            cam.close();
        }
    }

    // Recieve frame sent by server
    public class VideoRecieveThread extends Thread {
        public boolean isOpened;
        @Override
        public void run() {
            isOpened = true;
            while (isOpened == true) {
                if (videoSocket.isClosed()) {
                    break;
                }
                try {
                    Object o = oin.readObject();
                    ImageIcon ic = (ImageIcon) o;
                    VideoReceiveView.video_area.setIcon(ic);
                }
                catch(Exception e) {
                    break;
                }
            }
        }
    }
}
