package coursework;

import java.io.*;
import java.util.*;
import java.net.*;

public class UserData implements Serializable
{
    public InetAddress ip;
    public String username;
    public String password;
    public String placeOfBirth;
    public String dateOfBirth;
    public ArrayList<String> listOfTastes = new ArrayList<>();
    public ArrayList<String> listOfFriends = new ArrayList<>();
}
