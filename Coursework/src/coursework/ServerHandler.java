/*
 * @author Tommy Godfrey, Tyler Knowles
 */
package coursework;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class ServerHandler implements Runnable
{
    Socket client;
    ObjectInputStream inFromClient;
    ObjectOutputStream outToClient;
    public ArrayList<UserData> usersData = new ArrayList<>();
    public ArrayList<UserData> onlineData = new ArrayList<>();
    UserData clientsData;
    String fileNameUserData = "userdata.txt";
    String fileNameOnlineUsers = "onlineusers.txt";
    String fileNameRequests = "friendrequests.txt";
    String fileNameSongs = "music.txt";
    String fileNamePosts = "posts.txt";
    Runnable updater = () -> 
    {
        while(true)
        {
            try
            {
                updateReadData();
                updateReadOnlineUsers();
                Thread.sleep(2000);
            }
            catch (Exception e)
            {
                System.err.println(e.getMessage());
            }
        }
    };
    
    //Constructor sets up who client is and reads into data from file
    public ServerHandler(Socket c)
    {
        client = c;
        new Thread(updater).start();
        try
        {
            inFromClient = new ObjectInputStream(client.getInputStream());
            outToClient = new ObjectOutputStream(client.getOutputStream());
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        updateReadData();
    }
    
    //Takes input then does relevant function for output
    public Boolean redirector()
    {
        String command = null;
        UserData dataU = null;
        String dataS = null;
        byte[] dataB = null;
        Object in = null;
        try
        {
            in = inFromClient.readObject();
            command = (String) in;
            if ((command.equals("REGISTER")))
            {
                in = inFromClient.readObject();
                dataU = (UserData) in;
                in = inFromClient.readObject();
                dataB = (byte[]) in;
            }
            if (command.equals("LOGIN"))
            {
                in = inFromClient.readObject();
                dataU = (UserData) in;
            }
            if ((command.equals("GETDATA")) || (command.equals("REQUESTFRIEND")) || (command.equals("REPLYYES")) || (command.equals("REPLYNO")) || (command.equals("MAKEPOST")) || (command.equals("SENDSONG")) || (command.equals("GETSONG")) || (command.equals("GETPICTURE")))
            {
                in = inFromClient.readObject();
                dataS = (String) in;
                if (command.equals("SENDSONG"))
                {
                    in = inFromClient.readObject();
                    dataB = (byte[]) in;
                }
            }
            System.out.println("In from client " + client.getInetAddress() + ": " + command);
        }
        catch (Exception e)
        {
            System.err.println("Error with redirect message: " +e.getMessage());
        }
        if (command.equals("LOGOUT"))
        {
            logoutUser();
            return true;
        }
        else if (command.equals("GETPICTURE"))
        {
            sendPic(dataS);
        }
        else if (command.equals("GETALL"))
        {
            sendUsers(0);
        }
        else if (command.equals("GETONLINE"))
        {
            sendUsers(1);
        }
        else if (command.equals("GETDATA"))
        {
            sendOneUser(dataS);
        }
        else if (command.equals("LOGIN"))
        {
            loginClient(dataU);
        }
        else if (command.equals("REGISTER"))
        {
            registerClient(dataU,dataB);
        }
        else if (command.equals("REQUESTFRIEND"))
        {
            requestFriendship(dataS);
        }
        else if (command.equals("GETREQUESTS"))
        {
            sendRequests();
        }
        else if (command.equals("REPLYYES"))
        {
            replyRequest(dataS,true);
        }
        else if (command.equals("REPLYNO"))
        {
            replyRequest(dataS,false);
        }
        else if (command.equals("MAKEPOST"))
        {
            makePost(dataS);
        }
        else if (command.equals("GETPOSTS"))
        {
            sendPosts();
        }
        else if (command.equals("SENDSONG"))
        {
            downloadSong(dataS,dataB);
        }
        else if (command.equals("GETSONGS"))
        {
            sendSongs();
        }
        else if (command.equals("GETSONG"))
        {
            sendSong(dataS);
        }
        return false;
    }
    
    public void sendPic(String user)
    {
        File myFile = new File("pictures/"+user+".jpg");
        byte[] toSend = null;
        try
        {
            toSend = Files.readAllBytes(myFile.toPath());
            outToClient.writeObject(toSend);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
    
    public void downloadSong(String songname, byte[] data)
    {
        try
        {
            FileOutputStream songWriter = new FileOutputStream("music/" + songname);
            songWriter.write(data);
            songWriter.close();
            FileWriter fileWriter = new FileWriter(fileNameSongs, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(clientsData.username + "," + songname);
            bufferedWriter.newLine();
            bufferedWriter.close();
        }
        catch (Exception e)
        {
            System.err.println("Error writing request in: " + e.getMessage());
        }
    }
    
    //Function to get user data from username
    public UserData userSearch(String desiredUsername)
    {
        UserData found = null;
        for (int i = 0; i < usersData.size(); i++)
        {
            if (usersData.get(i).username.equals(desiredUsername))
            {
                found = usersData.get(i);
            }
        }
        return found;
    }
    
    //Updates databse with friend request
    public void requestFriendship(String usernameToAdd)
    {
        try
        {
            FileWriter fileWriter = new FileWriter(fileNameRequests, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(usernameToAdd + "," + clientsData.username);
            bufferedWriter.newLine();
            bufferedWriter.close();
        }
        catch (Exception e)
        {
            System.err.println("Error writing request in: " + e.getMessage());
        }
    }
    
    public void replyRequest(String usernameToDo, Boolean accepted)
    {
        if (accepted)
        {
            addFriendToData(clientsData.username, usernameToDo);
            addFriendToData(usernameToDo, clientsData.username);
            updateReadData();
        }
        try
        {
            File oldRequests = new File(fileNameRequests);
            File tempRequests = new File("temprequests.txt");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(oldRequests));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempRequests));
            String userToRemove = usernameToDo;
            String parser = bufferedReader.readLine();
            while (parser != null)
            {
                String trimmed = parser.trim();
                String[] line = trimmed.split(",");
                if (line[1].equals(userToRemove))
                {
                    parser = bufferedReader.readLine();
                    continue;
                }
                bufferedWriter.write(parser + System.getProperty("line.separator"));
                parser = bufferedReader.readLine();
            }
            bufferedWriter.close();
            bufferedReader.close();

            oldRequests.delete();
            tempRequests.renameTo(oldRequests);
        }
        catch (Exception e)
        {
            System.err.println("Error deleting requests: " +e.getMessage());
        }
    }
    
    public void addFriendToData(String username, String toAdd)
    {
        try
        {
            File oldData = new File(fileNameUserData);
            File tempData = new File("tempuserdata.txt");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(oldData));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempData));
            String userAddTo = username;
            String parser = bufferedReader.readLine();
            while (parser != null)
            {
                String trimmed = parser.trim();
                String[] line = trimmed.split(",");
                if (line[1].equals(userAddTo))
                {
                    bufferedWriter.write(parser + toAdd + "," + System.getProperty("line.separator"));
                    parser = bufferedReader.readLine();
                    continue;
                }
                bufferedWriter.write(parser + System.getProperty("line.separator"));
                parser = bufferedReader.readLine();
            }
            bufferedWriter.close();
            bufferedReader.close();

            oldData.delete();
            tempData.renameTo(oldData);
        }
        catch (Exception e)
        {
            System.err.println("Error deleting requests: " +e.getMessage());
        }
    }
    
    public void sendRequests()
    {
        ArrayList<String> requests = new ArrayList<>();;
        String parser = null;
        try
        {
            FileReader fileReader = new FileReader(fileNameRequests);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            parser = bufferedReader.readLine();
            while (parser != null)
            {
                String[] lineSplit = parser.split(",");
                if (lineSplit[0].equals(clientsData.username))
                {
                    requests.add(lineSplit[1]);
                }
                parser = bufferedReader.readLine();
            }
            outToClient.writeObject(requests);
            bufferedReader.close();
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
    
    public void sendSongs()
    {
        ArrayList<String> songs = new ArrayList<>();
        String parser = null;
        try
        {
            FileReader fileReader = new FileReader(fileNameSongs);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            parser = bufferedReader.readLine();
            while (parser != null)
            {
                String[] lineSplit = parser.split(",");
                
                if (lineSplit[0].equals(clientsData.username) || (clientsData.listOfFriends.contains(lineSplit[0])))
                {
                    songs.add(lineSplit[0] + "," + lineSplit[1]);
                }
                parser = bufferedReader.readLine();
            }
            outToClient.writeObject(songs);
            bufferedReader.close();
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
    
    public void sendSong(String songName)
    {
        File myFile = new File("music/"+songName);
        byte[] toSend = null;
        try
        {
            toSend = Files.readAllBytes(myFile.toPath());
            outToClient.writeObject(toSend);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
    
    public void makePost(String post)
    {
        try
        {
            FileWriter fileWriter = new FileWriter(fileNamePosts, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(clientsData.username + "," + post);
            bufferedWriter.newLine();
            bufferedWriter.close();
        }
        catch (Exception e)
        {
            System.err.println("Error writing post: " + e.getMessage());
        }
    }
    
    public void sendPosts()
    {
        ArrayList<String> posts = new ArrayList<>();
        String parser = null;
        try
        {
            FileReader fileReader = new FileReader(fileNamePosts);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            parser = bufferedReader.readLine();
            while (parser != null)
            {
                String[] lineSplit = parser.split(",");
                
                if (lineSplit[0].equals(clientsData.username) || (clientsData.listOfFriends.contains(lineSplit[0])))
                {
                    posts.add(lineSplit[0] + "," + lineSplit[1]);
                }
                parser = bufferedReader.readLine();
            }
            outToClient.writeObject(posts);
            bufferedReader.close();
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
    
    public void sendOneUser(String username)
    {
        UserData dataToSend = null;
        try
        {
            dataToSend = userSearch(username);
            outToClient.writeObject(dataToSend);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
    
    //Send list of users to client, 0 = all users, 1 = online
    public void sendUsers(int selection)
    {
        ArrayList<UserData> usersToSend = new ArrayList<>();
        if (selection == 0)
        {
            usersToSend = usersData;
        }
        if (selection == 1)
        {
            usersToSend = onlineData;
        }
        ArrayList<String> usernameSend = new ArrayList<>();
        for (int i = 0; i < usersToSend.size(); i++)
        {
            if (!usersToSend.get(i).username.equals(clientsData.username))
            {
                usernameSend.add(usersToSend.get(i).username);
            }   
        }
        try
        {
            outToClient.writeObject(usernameSend);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
    
    //Update all user data from database
    public void updateReadData()
    {
        String parser = null;
        usersData.clear();
        try
        {
            FileReader fileReader = new FileReader(fileNameUserData);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            parser = bufferedReader.readLine();
            while (parser != null)
            {
                String[] dataLine = parser.split(",");
                UserData thisLine = new UserData();
                thisLine.ip = InetAddress.getByName(dataLine[0]);
                thisLine.username = dataLine[1];
                thisLine.password = dataLine[2];
                thisLine.placeOfBirth = dataLine[3];
                thisLine.dateOfBirth = dataLine[4];
                
                int index = 5;
               
                while(dataLine.length > index)
                {
                    String next = dataLine[index];
                    if (next.equals("Opera") || next.equals("Rock") || next.equals("Pop"))
                    {
                        thisLine.listOfTastes.add(next);
                    }
                    else
                    {
                        thisLine.listOfFriends.add(next);
                    }
                    
                    index++;
                }
                
                usersData.add(thisLine);
                parser = bufferedReader.readLine();
            }
            bufferedReader.close();
        }
        catch(Exception e) 
        {
            System.err.println(e.getMessage());                
        }
    }
    
    //Update online users from database, depends on all user data
    public void updateReadOnlineUsers()
    {
        String parser = null;
        onlineData.clear();
        try
        {
            FileReader fileReader = new FileReader(fileNameOnlineUsers);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            parser = bufferedReader.readLine();
            while (parser != null)
            {
                String userLine = parser;
                
                UserData thisLine = new UserData();
                thisLine = userSearch(userLine);
                onlineData.add(thisLine);
                parser = bufferedReader.readLine();
            }
            bufferedReader.close();
        }
        catch(Exception e) 
        {
            System.err.println(e.getMessage());              
        }
    }
    
    //Add an online user to database, updates local data structure
    public void updateWriteOnlineUser(String onlineUser, Boolean loggingIn)
    {
        //If true: log in, if false: log out
        if (loggingIn == true)
        {
            try
            {
                FileWriter fileWriter = new FileWriter(fileNameOnlineUsers, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(onlineUser);
                bufferedWriter.newLine();
                bufferedWriter.close();
                updateReadOnlineUsers();
            }
            catch (Exception e)
            {
                System.err.println("Error writing log in: " + e.getMessage());
            }
        }
        else
        {
            try
            {
                File oldOnline = new File(fileNameOnlineUsers);
                File tempOnline = new File("temponline.txt");
                BufferedReader bufferedReader = new BufferedReader(new FileReader(oldOnline));
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempOnline));
                String userToRemove = onlineUser;
                String parser = bufferedReader.readLine();
                while (parser != null)
                {
                    String trimmed = parser.trim();
                    if (trimmed.equals(userToRemove))
                    {
                        parser = bufferedReader.readLine();
                        continue;
                    }
                    bufferedWriter.write(parser + System.getProperty("line.separator"));
                    parser = bufferedReader.readLine();
                }
                bufferedWriter.close();
                bufferedReader.close();
                
                oldOnline.delete();
                tempOnline.renameTo(oldOnline);
            }
            catch (Exception e)
            {
                System.err.println("Error writing log out: " +e.getMessage());
            }
        }
    }
    
    //Logs off current client connected
    public void logoutUser() 
    {
        try
        {
            updateWriteOnlineUser(clientsData.username,false);
            client.close();
        }
        catch (Exception e)
        {
            System.err.println("Error logging out: " + e.getMessage());
        }
    }
    
    public void loginClient(UserData dataIn)
    {
        try
        {
            String usernameIn = dataIn.username;
            String passwordIn = dataIn.password;
            Boolean valid = false;
            for (int i = 0; i < usersData.size(); i++)
            {
                if ((usersData.get(i).username.equals(usernameIn))&&(usersData.get(i).password.equals(passwordIn)&&(usersData.get(i).ip.equals(client.getInetAddress()))))
                {
                    valid = true;
                }
            }
            if (valid == true)
            {
                outToClient.writeObject("SUCCESS");
                updateWriteOnlineUser(usernameIn,true);
                clientsData = userSearch(usernameIn);
            }
            else if (valid == false)
            {
                outToClient.writeObject("FAILURE");
            }
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
    
    public void registerClient(UserData dataIn, byte[] picture)
    {
        try
        {
            FileWriter fileWriter = new FileWriter(fileNameUserData,true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            dataIn.ip = client.getInetAddress();
            bufferedWriter.write(dataIn.ip.getHostAddress() + ",");
            bufferedWriter.write(dataIn.username + ",");
            bufferedWriter.write(dataIn.password + ",");
            bufferedWriter.write(dataIn.placeOfBirth + ",");
            bufferedWriter.write(dataIn.dateOfBirth + ",");
            for(int i = 0; i < dataIn.listOfTastes.size(); i++)
            {
                bufferedWriter.write(dataIn.listOfTastes.get(i) + ",");
            }
            bufferedWriter.newLine(); 
            outToClient.writeObject("SUCCESS"); 
            bufferedWriter.close();
            
            FileOutputStream picWriter = new FileOutputStream("pictures/" + dataIn.username + ".jpg");
            picWriter.write(picture);
            picWriter.close();
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        updateReadData();
        updateWriteOnlineUser(dataIn.username,true);
        clientsData = userSearch(dataIn.username);
    }
    
    //Run function for multithreading
    public void run()
    {
        //At the moment server functionality is useless and placeholder
        while (true)
        {
            try
            {
                if (redirector() == true)
                {
                    break;
                }
            }
            catch (Exception e)
            {
                System.err.println("Error redirecting: " +e.getMessage());
                break;
            } 
        }
    }
}

