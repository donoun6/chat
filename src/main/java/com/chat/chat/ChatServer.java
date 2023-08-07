package com.chat.chat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatServer {
    public static void main(String[] args) throws Exception {
        //챗서버도 서버를 기다려야하기 때문에 서버 소켓을 만든다. 9999번 소켓에서 기다린다.
        ServerSocket serverSocket = new ServerSocket(9999);
        // 공유객체에서 쓰레드에 안전한 리스트를 만든다.
        // chatThread가 생성될때마다 리스트 객체가 들어가는것을 공유객체라 한다. 쓰레드 객체가 만들어질때마다 같은객체를 넣어준다.
        // chatThread가 10개만들어지면 10개의 쓰레드는 하나의 객체를 공유한다.
        List<PrintWriter> outList = Collections.synchronizedList(new ArrayList<>());

        while (true) {
            //사용자가 접속하면 소켓이 나와야한다. 해당 소켓은 클라이언트와 통신하기 위한것.
            Socket socket = serverSocket.accept();
            System.out.println("접속 : " + socket);


            ChatThread chatThread = new ChatThread(socket, outList);
            chatThread.start();
        }
        /**
         * 클라이언트가 접속을 하면 접속한 클라이언트 소켓이 튀어나온다.
         * 그리고 이 소켓을 쓰레드로 돌아가게 만든다.
         * 다시 try 부분으로 올라가서 반복되도록 만들어야한다.
         */
    }
}

//여러 클라이언트에게 동시에 받아야하기 때문에 쓰레드를 만든다.
class ChatThread extends Thread {

    private Socket socket;
    private List<PrintWriter> outList;
    private PrintWriter out;
    private BufferedReader in;

    //생성자
    public ChatThread(Socket socket, List<PrintWriter> outList) {
        this.socket = socket;
        this.outList = outList;

        // 1.소켓으로부터 읽어들일 수 있는 객체를 얻는다.
        // 2.소켓에게 쓰기 위한 객체를 얻는다. (현재 연결된 클라이언트에게 쓰는 객체)
        try {
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outList.add(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run(){
        // 3.클라이언트가 보낸 메세지를 읽는다.
        // 4.접속된 모든 클라이언트에게 메세지를 보낸다. (현재 접속된 모든 클라이언트에게 쓸 수 있는 객체가 필요하다.)
        String line = null;
        try {
            while ((line = in.readLine()) != null) {
                for (int i = 0; i < outList.size(); i++) { // 접속한 모든 클라이언트에게 메시지를 전송
                    PrintWriter o = outList.get(i);
                    o.println(line);
                    o.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally { //접속한 모든 사용자에게 접속이 끊어졌다는걸 보낸다. (접속이 끊어질때)
            try {
                outList.remove(out);
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (int i = 0; i < outList.size(); i++) { // 접속한 모든 클라이언트에게 메시지를 전송
                PrintWriter o = outList.get(i);
                o.println("connect lost");
                o.flush();
            }

            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

/**
 * 클라이언트가 접속을 하면 해당 클라이언트와 통신을 하기위한 소켓이 튀어나온다.
 * 서버입장에서 클라이언트가 A,B 가있으면 소켓이 2개 있다.
 * 해당 소켓에다 읽기 위한게 in이라고 하고 해당 소켓을 쓸 수있는게 out
 * 각각의 클라이언트가 각각의 소켓에 연결되어있다. 서버가 각각 따로따로 동작해주기 위해 쓰레드를 하나씩 만든다.
 * 각 소켓은 별도로 동작한다.
 * List 공유 객체를 만들어 각 소켓에 대한 out을 담는다. (쓰레드가 공유객체를 가진다)
 * 즉, in으로 읽은것을 out으로 하나씩 꺼내 공유객체에 담는다.
 */