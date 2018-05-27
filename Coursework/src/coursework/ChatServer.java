/*
 * @author Tommy Godfrey, Tyler Knowles
 */
package coursework;

import java.net.*;
import java.io.*;

public class ChatServer
{
    public static void main(String[] args) throws IOException
    {
        int port = 9096;
        ServerSocket server = new ServerSocket(port);
        ServerGUI gui = new ServerGUI("Music Social Network - Chat Server Output");
        gui.setVisible(true);
        while (true)
        {
            //Wait for clients to connect
            System.out.println("Waiting for chatter on port " + port + "...");
            gui.display("Waiting for chatter on port " + port + "...");
            Socket client = server.accept();
            //Client connected, inform and show address
            System.out.println("Connected to " + client.getInetAddress());
            gui.display("Connected to " + client.getInetAddress());
            //Assign each client to a thread
            gui.display("Creating new handler");
            ChatServerHandler t = new ChatServerHandler(client);
            Thread th = new Thread(t);
            th.start();
        }
    }
}