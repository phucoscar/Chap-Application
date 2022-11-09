/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import model.ServerModel;

public class WebcamController extends Thread {
    private static ExecutorService pool = Executors.newFixedThreadPool(10);
    public static ServerModel webcamModel;
    public int roomId;

    public WebcamController() {
    }

    public WebcamController(int roomId) {
        this.roomId = roomId;
        webcamModel = new ServerModel();
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            int portServer = roomId + 1;
            InetSocketAddress inetSocketAddress =
                    new InetSocketAddress(inetAddress, portServer);

            webcamModel.bind_server(inetSocketAddress);
        }
        catch(Exception e) {
            System.out.println("Can not create server!");
        }
    }

    @Override
    public void run() {
        ServerController.setMsg_area("Video call at room " + this.roomId + " is created");

        while (!this.webcamModel.serverSocket.isClosed()) {
            try {
                // Accept new client
                Socket video = webcamModel.serverSocket.accept();
                System.out.println(video);

                // Create new thread controller for new client
                VideoHandler vh = new VideoHandler(video, roomId);
                VideoHandler.list_videos.add(vh);
                pool.execute(vh);
            }
            catch(Exception e) {
                break;
            }
        }
    }
}
