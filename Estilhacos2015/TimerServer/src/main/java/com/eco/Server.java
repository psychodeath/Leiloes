package com.eco;

import java.io.*;
import java.net.Socket;

/**
 * Created by joaomota on 06/11/15.
 */
public class Server extends Thread{
    protected Socket clientSocket = null;
    protected String serverText   = null;

    public Server(Socket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
    }

    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            boolean running = true;

            while (running){
                String clientCmd = in.readLine();
                System.out.println("Recebido do cliente: " + clientCmd);

                if(clientCmd.equalsIgnoreCase("ack")){
                    out.println("JMOK");
                }

                if(clientCmd.equalsIgnoreCase("quit")){
                    running = false;
                    System.out.println("kill server command received");
                }
            }
        } catch (IOException e) {
            //report exception somewhere.
            System.out.println("error talking to client");
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
