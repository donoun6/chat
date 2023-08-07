package com.chat2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatThread extends Thread{
    private String name;
    private BufferedReader br;//읽어들이기 위한것
    private PrintWriter pw;//쓰기 위한것
    private Socket socket;
    List<ChatThread> list;

    public ChatThread(Socket socket, List<ChatThread> list) throws Exception {
        this.socket = socket;
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.br = br;
        this.pw = pw;
        this.name = br.readLine(); //무조건 한줄을 읽어 name을 보내준다.
        this.list = list;
        //자기자신 chatClient를 넣어준다.
        this.list.add(this);
    }

    public void sendMessage(String msg) {
        pw.println(msg);
        pw.flush();
    }

    @Override
    public void run() {
        // ChatThread는 사용자가 보낸 메세지를 읽어들여서 접속된 모든 클라이언트에게 메세지를 보낸다.
        // 해당 작업을 broadcast라고 부른다.

        // 나를 제외한 모든 사용자에게 OO님이 접속하셨습니다.
        // 현재 ChatThread를 제외하고 보낸다.
        try {
        broadcast(name+" 님이 연결되었습니다.", false);

        String line = null;

        //readLine이 null이 나오면 정상작동이기 때문에 finally에 연결 끊어졌다고 표시
            while ((line = br.readLine()) != null) {
                if ("/quit".equals(line)) {
                    throw new RuntimeException("접속 종료");
                }
                broadcast(name + " : " + line, true);
                //나를 포함한 ChatThread에게 메세지를 보낸다.
            }
        } catch (Exception e) { // ChatThread가 연결이 끊어졌다.

        }finally {
            broadcast(name+" 님의 연결이 끊어졌습니다.", false);
            this.list.remove(this);
            try {
                br.close();
            } catch (Exception e) {
            }

            try {
                pw.close();
            } catch (Exception e) {
            }

            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }

    private void broadcast(String msg, boolean includeMe) {
        List<ChatThread> chatThreads = new ArrayList<>();
        for (int i = 0; i < this.list.size(); i++) {
            chatThreads.add(list.get(i));
        }

        try {
            for (int i = 0; i < chatThreads.size(); i++) {
                ChatThread ct = chatThreads.get(i);
                if (!includeMe) { //나를 포함하지 말아라
                    if (ct == this) {
                        continue;
                    }
                }
                ct.sendMessage(msg);
            }
        } catch (Exception e) {
            System.out.println("///");
        }
    }
}
