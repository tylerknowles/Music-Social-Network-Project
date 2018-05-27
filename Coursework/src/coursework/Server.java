/*
 * @author Tommy Godfrey, Tyler Knowles
 */
package coursework;

import java.net.*;
import java.io.*;

public class Server 
{
    public static void main(String[] args) throws IOException
    {
        int port = 9090;
        ServerSocket server = new ServerSocket(port);
        ServerGUI gui = new ServerGUI("Music Social Network - Server Output");
        gui.setVisible(true);
        while (true)
        {
            //Wait for clients to connect
            System.out.println("Waiting for client on port " + port + "...");
            gui.display("Waiting for client on port " + port + "...");
            Socket client = server.accept();
            //Client connected, inform and show address
            System.out.println("Connected to " + client.getInetAddress());
            gui.display("Connected to " + client.getInetAddress());
            //Assign each client to a thread
            gui.display("Creating new handler");
            ServerHandler t = new ServerHandler(client);
            Thread th = new Thread(t);
            th.start();
        }
    }
}
