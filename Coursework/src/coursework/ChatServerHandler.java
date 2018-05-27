/*
 * @author Tommy Godfrey, Tyler Knowles
 */
package coursework;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class ChatServerHandler implements Runnable
{
    Socket client;
    ObjectInputStream inFromClient;
    ObjectOutputStream outToClient;
    UserData clientData;
    UserData toData;
    public ArrayList<String> messageHistory = new ArrayList<>();
    public ArrayList<String> fileHistory = new ArrayList<>();
    String fileNameChatStart= "chat/";
    String fileNameChatEnd= "chat.txt";
    String fileNameChat;
    String fileNameChatFiles;
    
    Runnable chatUpdater = () -> 
    {
        while(true)
        {
            try
            {
                updateReadChat();
                Thread.sleep(2000);
            }
            catch (Exception e)
            {
                System.err.println(e.getMessage());
            }
        }
    };
    
    Runnable fileUpdater = () -> 
    {
        while(true)
        {
            try
            {
                updateReadFiles();
                Thread.sleep(2000);
            }
            catch (Exception e)
            {
                System.err.println(e.getMessage());
            }
        }
    };
    
    //Constructor sets up who client is and reads into data from file
    public ChatServerHandler(Socket c)
    {
        client = c;
        try
        {
            Object i = null;
            inFromClient = new ObjectInputStream(client.getInputStream());
            outToClient = new ObjectOutputStream(client.getOutputStream());
            i = inFromClient.readObject();
            clientData = (UserData) i;
            i = inFromClient.readObject();
            toData = (UserData) i;
            try
            {
                String middle;
                if (clientData.username.compareTo(toData.username) < 0)
                {
                    middle = clientData.username + toData.username;
                }
                else
                {
                    middle = toData.username + clientData.username;
                }
                fileNameChat = fileNameChatStart + middle + fileNameChatEnd;
                fileNameChatFiles = fileNameChatStart + middle + "files.txt";
            }
            catch (Exception e)
            {
                System.err.println(e.getMessage());
            }
            
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        new Thread(chatUpdater).start();
        new Thread(fileUpdater).start();
    }

    public Boolean redirecor()
    {
        Object in = null;
        String command = null;
        String message = null;
        byte[] data = null;
        try
        {
            in = inFromClient.readObject();
            command = (String) in;
            System.out.println(command);
            if (command.equals("SEND"))
            {
                in = inFromClient.readObject();
                message = (String) in;
                updateWriteChat(message);
            }
            else if (command.equals("GET"))
            {
                outToClient.writeObject(messageHistory);
            }
            else if (command.equals("LEAVE"))
            {
                client.close();
                return true;
            }
            else if (command.equals("SENDFILE"))
            {
                in = inFromClient.readObject();
                message = (String) in;
                in = inFromClient.readObject();
                data = (byte[]) in;
                downloadFile(message,data);
            }
            else if (command.equals("GETNEWFILE"))
            {
                sendRecentFile();
            }
            else if (command.equals("GETFILES"))
            {
                outToClient.writeObject(fileHistory);
            }
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        return false;
    }
    
    public void downloadFile(String name, byte[] file)
    {
        updateWriteChat("Sending file: " + name);
        try
        {
            FileOutputStream dataWriter = new FileOutputStream("chatfiles/" + name);
            dataWriter.write(file);
            dataWriter.close();
            FileWriter fileWriter = new FileWriter(fileNameChatFiles, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(name);
            bufferedWriter.newLine();
            bufferedWriter.close();
        }
        catch (Exception e)
        {
            System.err.println("Error writing request in: " + e.getMessage());
        }
    }
    
    public void sendRecentFile()
    {
        byte[] toSend = null;
        try
        {
            String name = fileHistory.get(fileHistory.size()-1);
            File theFile = new File("chatfiles/"+name);
            toSend = Files.readAllBytes(theFile.toPath());
            outToClient.writeObject(name);
            outToClient.writeObject(toSend);
        }
        catch (Exception e)
        {
            System.err.println("Error writing request in: " + e.getMessage());
        }
    }
    
    public void updateReadFiles()
    {
        fileHistory.clear();
        ArrayList<String> filesIn = new ArrayList<>();
        File file = new File(fileNameChatFiles);
        if(!file.exists()) 
        { 
            try
            {
                file.createNewFile();
            }
            catch (Exception e)
            {
                System.err.println(e.getMessage());
            }
        }
        else
        {
            try
            {
                FileReader fileReader = new FileReader(fileNameChatFiles);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String parser = bufferedReader.readLine();
                while (parser != null)
                {
                    filesIn.add(parser);
                    parser = bufferedReader.readLine();
                }
                bufferedReader.close();
            }
            catch(Exception e) 
            {
                System.err.println(e.getMessage());                
            }
            fileHistory = filesIn;
        }
    }
    
    public void updateReadChat()
    {
        
        messageHistory.clear();
        ArrayList<String> messagesIn = new ArrayList<>();
        File file = new File(fileNameChat);
        if(!file.exists()) 
        { 
            try
            {
                file.createNewFile();
            }
            catch (Exception e)
            {
                System.err.println(e.getMessage());
            }
        }
        else
        {
            try
            {
                FileReader fileReader = new FileReader(fileNameChat);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String parser = bufferedReader.readLine();
                while (parser != null)
                {
                    messagesIn.add(parser);
                    parser = bufferedReader.readLine();
                }
                bufferedReader.close();
            }
            catch(Exception e) 
            {
                System.err.println(e.getMessage());                
            }
            messageHistory = messagesIn;
        }
    }
    
    public void updateWriteChat(String message)
    {
        try
        {
            FileWriter fileWriter = new FileWriter(fileNameChat, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(clientData.username + "," + message);
            bufferedWriter.newLine();
            bufferedWriter.close();
        }
        catch (Exception e)
        {
            System.err.println("Error writing chat: " + e.getMessage());
        }
    }
    
    public void run()
    {
        while (true)
        {
            if (redirecor() == true)
            {
                break;
            }
        }
    }
}