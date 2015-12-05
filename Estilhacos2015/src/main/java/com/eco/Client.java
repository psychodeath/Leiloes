package com.eco;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Enumeration;

/**
 * Created by joaomota on 05/11/15.
 */

public class Client {
    private int portNumber;
    private String host;
    Socket clientSocket;


    public Client(int portNumber){
        this.portNumber = portNumber;
        this.host = getServerAddress();
        //this.host = "localhost";
    }

    public Client(int portNumber,String host){
        this.portNumber = portNumber;
        this.host = host;
    }

    public static String getServerAddress() {

        // discovery of server
        // try this instead: http://michieldemey.be/blog/network-discovery-using-udp-broadcast/

        InetAddress localHost = null;
        try {
            localHost = Inet4Address.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        NetworkInterface networkInterface = null;
        try {
            networkInterface = NetworkInterface.getByInetAddress(localHost);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        short subnetMask = networkInterface.getInterfaceAddresses().get(0).getNetworkPrefixLength();

        System.out.println("Subnet:" + networkInterface);

        return localHost.toString();
    }
    public void connect() throws IOException, UnknownHostException {
        System.out.println("Connecting to port " + portNumber);
        clientSocket = new Socket(host, portNumber);
        System.out.println("Connected to server.");

        System.out.println("Sending ack request to server.");
        OutputStream output = clientSocket.getOutputStream();
        output.write(("ack").getBytes());


        InputStream input  = clientSocket.getInputStream();
        System.out.println("Read from server:");
        System.out.println(input.read());

        input.close();
        output.close();
    }

    public void sendTime(long t){
        System.out.println("reporting time to server");

    }

}
