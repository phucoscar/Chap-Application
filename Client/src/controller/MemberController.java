package controller;

import model.ClientModel;
import view.ChatView;
import view.MembersView;
import view.VideoSendView;

import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MemberController {
    public ClientModel memberModel;
    public Socket memberSocket;
    public ObjectOutputStream oout;
    public ObjectInputStream oin;
    public String nickname;
    public String role;
    public static MembersView mv;

    public MemberController() {
    }

    public MemberController(String nickname, int roomId, String role) {
        this.nickname = nickname;
        this.role = role;
        InetSocketAddress inetSocketAddress = null;
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            int portServer = roomId;
            inetSocketAddress =
                    new InetSocketAddress(inetAddress, portServer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.memberModel = new ClientModel();
        memberModel.connectServer(inetSocketAddress);
        this.memberSocket = memberModel.getClient();
        this.oout = this.memberModel.getOout();
        this.oin = this.memberModel.getOin();
    }

    public Object receiveServerMessage() {
        Object serverObject = new Object();
        try {
            serverObject = this.oin.readObject();
        } catch (Exception e) {
            try {
                this.memberSocket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return serverObject;
    }

    public void sendClientMessage(Object o) {
        try {
            this.oout.writeObject(o);
        } catch (Exception e) {
            try {
                this.memberSocket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void setMessageArea(String message) {
        HTMLDocument doc = (HTMLDocument) ChatView.msg_area.getDocument();
        HTMLEditorKit editorKit = (HTMLEditorKit) ChatView.msg_area.getEditorKit();
        try {
            editorKit.insertHTML(doc, doc.getLength(), "<div>"+message+"</div>", 0, 0, null);
            ChatView.msg_area.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ChatThread extends Thread {
        public MemberController mc;

        public ChatThread(MemberController mc) {
            this.mc = mc;
        }

        @Override
        public void run() {
            if (!memberSocket.isClosed()) {
                String messageClient = nickname;
                sendClientMessage(messageClient);
                String messageServer = (String) receiveServerMessage();
                MemberController.setMessageArea(messageServer);
            }

            while (!memberSocket.isClosed()) {
                Object o = receiveServerMessage();
                if (o instanceof String) {
                    String messageServer = (String) o;
                    if (messageServer.equals("List members!")) {
                        int roomId = Integer.parseInt((String)receiveServerMessage());
                        messageServer = (String) receiveServerMessage();
                        mv = new MembersView(this.mc, nickname, roomId, messageServer);
                        mv.setVisible(true);
                    }
                    else if (messageServer.equals("You are kicked!")) {
                        BeKicked bk = new BeKicked();
                        bk.start();
                    }
                    else if (messageServer.equals("Accept to start camera!")) {
                        int roomId = Integer.parseInt((String) receiveServerMessage());
                        VideoSendView vsv = new VideoSendView(nickname, roomId);
                        vsv.setVisible(true);
                    }
                    else {
                        setMessageArea(messageServer);
                    }
                }
            }

        }

        public void close() {
            WindowEvent close_window = new WindowEvent(mv, WindowEvent.WINDOW_CLOSING);
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(close_window);
        }

        class BeKicked extends Thread {
            @Override
            public void run() {
                MemberController.setMessageArea("[SERVER]: You are kicked by admin. Window will be closed.");
                try {
                    Thread.sleep(2000);
                    memberSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                close();
                System.exit(0);
            }
        }
    }
}
