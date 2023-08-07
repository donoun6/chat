package com.chat2;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatServer {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8888);

        //ArrayList에 동시에 접속하지 못하도록 하는 list가 만들어진다.
        List<ChatThread> list = Collections.synchronizedList(new ArrayList<>());
        while (true) {
            /**
             * 접속이 되자마자 소켓을 만들어
             * 해당 소켓을 가진 ChatClient객체를 만들어주고
             * 쓰레드를 실행 무한반복
             * 계속해서 사용자가 접속할 때 까지 대기한다.
             */
            Socket socket = serverSocket.accept(); //클라이언트가 접속
            ChatThread chatClient = new ChatThread(socket, list);
            chatClient.start(); //쓰레드 실행
        }

    }

}
