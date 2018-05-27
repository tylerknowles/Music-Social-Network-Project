/*
 * @author Tommy Godfrey, Tyler Knowles
 */
package coursework;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;

public class ClientChatter 
{
    Socket chatServer;
    ObjectOutputStream outToChatServer;
    ObjectInputStream inFromChatServer;
    int chatServerKnowPort = 9096;
    public UserData myData;
    public UserData contactData;
    
    public ClientChatter(UserData me, UserData friend)
    {
        myData = me;
        contactData = friend;
        try
        {
            chatServer = new Socket("localhost",chatServerKnowPort);
            outToChatServer = new ObjectOutputStream(chatServer.getOutputStream());
            inFromChatServer = new ObjectInputStream(chatServer.getInputStream());
            outToChatServer.writeObject(myData);
            outToChatServer.writeObject(contactData);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
    
    
    public void sendMessage(String message)
    {
        try
        {
            outToChatServer.writeObject("SEND");
            outToChatServer.writeObject(message);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
    
    public ArrayList<String> getMessages()
    {
        ArrayList<String> messageHistory = new ArrayList<>();
        try
        {
            outToChatServer.writeObject("GET");
            Object in = inFromChatServer.readObject();
            messageHistory = (ArrayList<String>) in;
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        return messageHistory;
    }
    
    public ArrayList<String> getFileHistory()
    {
        ArrayList<String> fileHistory = new ArrayList<>();
        try
        {
            outToChatServer.writeObject("GETFILES");
            Object in = inFromChatServer.readObject();
            fileHistory = (ArrayList<String>) in;
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        return fileHistory;
    }
    
    public void downloadRecentFile()
    {
        try
        {
            Object in = null;
            byte[] data = null;
            String name = null;
            outToChatServer.writeObject("GETNEWFILE");
            in = inFromChatServer.readObject();
            name = (String) in;
            in = inFromChatServer.readObject();
            data = (byte[]) in;
            FileOutputStream fileWriter = new FileOutputStream(myData.username + name);
            fileWriter.write(data);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
    
    public void sendFile(String filename, String filepath)
    {
        try
        {
            //Send file
            File myFile = new File(filepath);
            byte[] byteArray = Files.readAllBytes(myFile.toPath());
            outToChatServer.writeObject("SENDFILE");
            outToChatServer.writeObject(filename);
            outToChatServer.writeObject(byteArray);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
    
    public void leave()
    {
        try
        {
            outToChatServer.writeObject("LEAVE");
            chatServer.close();
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
}
