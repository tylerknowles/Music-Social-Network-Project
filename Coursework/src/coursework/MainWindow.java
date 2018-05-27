/*
 * @author Tommy Godfrey, Tyler Knowles
 */
package coursework;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javazoom.jl.player.Player;

public class MainWindow extends javax.swing.JFrame 
{
    public ClientTalker myTalker;
    public String myUsername;
    public UserData myData;
    Boolean running;
    
    public ArrayList<String> onlineUsers = new ArrayList<>();
    public ArrayList<String> myRequests = new ArrayList<>();
    public ArrayList<String> myFriends = new ArrayList<>();
    public ArrayList<String> visiblePosts = new ArrayList<>();
    public ArrayList<String> visibleSongs = new ArrayList<>();
    
    Runnable updater = () -> 
    {
        while(true)
        {
            if(!running)
            {
                break;
            }
            try
            {
                updateOnline();
                updateRequests();
                updateMe();
                updateFriendsList(false);
                updateInfoList();
                updatePosts();
                updateSongs();
                Thread.sleep(2000);
            }
            catch (Exception e)
            {
                System.err.println(e.getMessage());
            }
        }
    };
    
    public MainWindow() 
    {
        initComponents();
        System.err.println("NO DATA");
    }
    
    public MainWindow(String username, ClientTalker talker) 
    {
        initComponents();
        myUsername = username;
        running = true;
        this.setTitle("Music Social Network - Logged in as: " + username);
        
        myTalker = talker;
        myData = myTalker.getUserdata(username);
        setupBoxes();
        new Thread(updater).start();
        
        
    }
    
    public void setupBoxes()
    {
        //Set up online list
        onlineUsers = myTalker.clientGetUsers(1);
        DefaultListModel listModel = new DefaultListModel();
        for(int i = 0; i < onlineUsers.size(); i++)
        {
            if (myData.listOfFriends.contains(onlineUsers.get(i)))
            {
                listModel.add(i,onlineUsers.get(i) + " (friend)");
            }
            else
            {
                listModel.add(i,onlineUsers.get(i));
            }
        }
        onlineList.setModel(listModel);
        //Set up requests list
        myRequests = myTalker.getRequests();
        listModel = new DefaultListModel();
        for(int i = 0; i < myRequests.size(); i++)
        {
            listModel.add(i,myRequests.get(i));
        }
        requestList.setModel(listModel);
        //Set up friends list
        updateFriends();
        listModel = new DefaultListModel();       
        for(int i = 0; i < myData.listOfFriends.size(); i++)
        {
            if (onlineUsers.contains(myFriends.get(i)))
            {
                listModel.add(i,myFriends.get(i) + " (online)");
            }
            else
            {
                listModel.add(i,myFriends.get(i) + " (offline)");
            }
        }
        friendList.setModel(listModel);
        //Set up posts
        visiblePosts = myTalker.getPosts();
        for (int i = 0; i < visiblePosts.size(); i++)
        {
            String[] post = visiblePosts.get(i).split(",");
            postsArea.append(post[0] + ": " + post[1] + "\n");
        }
        //Set up songs
        visibleSongs = myTalker.getSongs();
        listModel = new DefaultListModel();       
        for(int i = 0; i < visibleSongs.size(); i++)
        {
            String[] song = visibleSongs.get(i).split(",");
            listModel.add(i,song[0] + ": " + song[1]);
        }
        songsArea.setModel(listModel);
    }
    
    //Update posts
    public void updatePosts()
    {
        ArrayList<String> comparer = visiblePosts;
        visiblePosts = myTalker.getPosts();
        if (!comparer.equals(visiblePosts))
        {
            for(int i = comparer.size(); i < visiblePosts.size(); i++)
            {
                String[] post = visiblePosts.get(i).split(",");
                postsArea.append(post[0] + ": " + post[1] + "\n");
            }
        }
    }
    
    public void updateSongs()
    {
        ArrayList<String> comparer = visibleSongs;
        visibleSongs = myTalker.getSongs();
        if (!comparer.equals(visibleSongs))
        {
            DefaultListModel listModel = new DefaultListModel();
            for(int i = 0; i < visibleSongs.size(); i++)
            {
                String[] song = visibleSongs.get(i).split(",");
                listModel.add(i,song[0] + ": " + song[1]);
            }
            songsArea.setModel(listModel);
        }
    }
    
    //Update online list
    public void updateOnline()
    {
        ArrayList<String> comparer = onlineUsers;
        onlineUsers = myTalker.clientGetUsers(1);
        if (!comparer.equals(onlineUsers))
        {
            DefaultListModel listModel = new DefaultListModel();
            for(int i = 0; i < onlineUsers.size(); i++)
            {
                UserData person = myTalker.getUserdata(onlineUsers.get(i));
                String filter = (String) tasteCombo.getSelectedItem();
                if (filter.equals("All") || person.listOfTastes.contains(filter))
                {
                    if (myData.listOfFriends.contains(onlineUsers.get(i)))
                    {
                        listModel.add(i,onlineUsers.get(i) + " (friend)");
                    }
                    else
                    {
                        listModel.add(i,onlineUsers.get(i));
                    }
                }
            }
            onlineList.setModel(listModel);
            updateFriendsList(true);
        }
    }
    
    public void updateRequests()
    {
        ArrayList<String> comparer = myRequests;
        myRequests = myTalker.getRequests();
        if (!myRequests.equals(comparer))
        {
            DefaultListModel listModel = new DefaultListModel();
            for(int i = 0; i < myRequests.size(); i++)
            {
                listModel.add(i,myRequests.get(i));
            }
            requestList.setModel(listModel);
        }
    }
    
    public void updateMe()
    {
        myData = myTalker.getUserdata(myData.username);
    }
    
    public void updateFriends()
    {
        updateMe();
        myFriends = myData.listOfFriends;
    }
    
    public void updateFriendsList(Boolean force)
    {
        ArrayList<String> comparer = myFriends;
        updateFriends();
        if (!comparer.equals(myFriends) || force)
        {
            DefaultListModel listModel = new DefaultListModel();       
            for(int i = 0; i < myFriends.size(); i++)
            {
                if (onlineUsers.contains(myFriends.get(i)))
                {
                    listModel.add(i,myFriends.get(i) + " (online)");
                }
                else
                {
                    listModel.add(i,myFriends.get(i) + " (offline)");
                }
            }
            friendList.setModel(listModel);
        }
    }
    
    public void updateInfoList()
    {
        UserData displayData = null;
        if (friendList.getSelectedValue() != null)
        {
            String selected = friendList.getSelectedValue();
            String onlineTag = " (online)";
            String offlineTag = " (offline)";
            if (selected.contains(onlineTag))
            {
                selected = selected.replace(onlineTag,"");
            }
            else if (selected.contains(offlineTag))
            {
                selected = selected.replace(offlineTag,"");
            }
            DefaultListModel listModel = new DefaultListModel(); 
            displayData = myTalker.getUserdata(selected);
            listModel.add(0,"Place of birth:" + displayData.placeOfBirth);
            listModel.add(1,"Date of birth:" + displayData.dateOfBirth);
            for(int i = 0; i < displayData.listOfTastes.size(); i++)
            {
                listModel.add(2+i,"Taste " + (i+1) + ": " + displayData.listOfTastes.get(i));
            }
            
            infoList.setModel(listModel);
            
            //download it 
            try
            {
                FileOutputStream picWriter = new FileOutputStream(myData.username + selected + ".jpg");
                picWriter.write(myTalker.getPicture(selected));

                pictureLabel.setIcon(new javax.swing.ImageIcon(myData.username + selected + ".jpg"));
                panel.add(pictureLabel);
            }
            catch(Exception e)
            {
                System.err.println(e.getMessage());
            }
        }   
    }
    
    public void playSong(String songname)
    {
        try
        {
            FileOutputStream songWriter = new FileOutputStream(myData.username + songname);
            songWriter.write(myTalker.getSong(songname));
            FileInputStream fis = new FileInputStream(myData.username + songname);
            Player playMP3 = new Player(fis);
            playMP3.play();
        }
        catch(Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
    
    public void chooseFile()
    {
        
        JFileChooser chooser = new JFileChooser();
        String path = null;
        String name = null;
        FileNameExtensionFilter filter = new FileNameExtensionFilter("MP3 FILES", "mp3");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) 
        {
            path = chooser.getSelectedFile().getAbsolutePath();
            name = chooser.getSelectedFile().getName();
            myTalker.sendSong(name,path);
        }
    }
    
    public void logOut()
    {
        running = false;
        myTalker.logOut();
        System.out.println("Logging out");
    }
    
    /**
     * This method is called from within the constructor to initialise the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        friendList = new javax.swing.JList<>();
        jScrollPane4 = new javax.swing.JScrollPane();
        postsArea = new javax.swing.JTextArea();
        fieldPost = new javax.swing.JTextField();
        jScrollPane6 = new javax.swing.JScrollPane();
        onlineList = new javax.swing.JList<>();
        jLabel7 = new javax.swing.JLabel();
        buttonPlay = new javax.swing.JButton();
        buttonPost = new javax.swing.JButton();
        buttonAddFriend = new javax.swing.JButton();
        buttonChat = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        songsArea = new javax.swing.JList<>();
        jScrollPane7 = new javax.swing.JScrollPane();
        requestList = new javax.swing.JList<>();
        buttonAccept = new javax.swing.JButton();
        buttonReject = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        buttonPlay1 = new javax.swing.JButton();
        buttonLogout = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        infoList = new javax.swing.JList<>();
        tasteCombo = new javax.swing.JComboBox<>();
        picturePanel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        panel = new javax.swing.JPanel();
        pictureLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setText("Friends");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel2.setText("Songs");

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel3.setText("Picture");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel4.setText("Friend Requests");

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        jLabel5.setText("Your Friends' Uploads");

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel6.setText("Online Users");

        friendList.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jScrollPane1.setViewportView(friendList);

        postsArea.setEditable(false);
        postsArea.setColumns(20);
        postsArea.setRows(5);
        jScrollPane4.setViewportView(postsArea);

        onlineList.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jScrollPane6.setViewportView(onlineList);

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel7.setText("Post:");
        jLabel7.setPreferredSize(new java.awt.Dimension(32, 15));

        buttonPlay.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        buttonPlay.setText("Play Song");
        buttonPlay.setPreferredSize(new java.awt.Dimension(70, 35));
        buttonPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPlayActionPerformed(evt);
            }
        });

        buttonPost.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        buttonPost.setText("Send");
        buttonPost.setPreferredSize(new java.awt.Dimension(70, 35));
        buttonPost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPostActionPerformed(evt);
            }
        });

        buttonAddFriend.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        buttonAddFriend.setText("Add Friend");
        buttonAddFriend.setPreferredSize(new java.awt.Dimension(70, 35));
        buttonAddFriend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddFriendActionPerformed(evt);
            }
        });

        buttonChat.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        buttonChat.setText("Chat");
        buttonChat.setPreferredSize(new java.awt.Dimension(70, 35));
        buttonChat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonChatActionPerformed(evt);
            }
        });

        jScrollPane3.setViewportView(songsArea);

        requestList.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jScrollPane7.setViewportView(requestList);

        buttonAccept.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        buttonAccept.setText("Accept");
        buttonAccept.setPreferredSize(new java.awt.Dimension(70, 35));
        buttonAccept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAcceptActionPerformed(evt);
            }
        });

        buttonReject.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        buttonReject.setText("Reject");
        buttonReject.setPreferredSize(new java.awt.Dimension(70, 35));
        buttonReject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRejectActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        jLabel8.setText("Online User Information");

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel9.setText("Posts");

        buttonPlay1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        buttonPlay1.setText("Upload Song");
        buttonPlay1.setPreferredSize(new java.awt.Dimension(70, 35));
        buttonPlay1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPlay1ActionPerformed(evt);
            }
        });

        buttonLogout.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        buttonLogout.setText("Logout");
        buttonLogout.setPreferredSize(new java.awt.Dimension(70, 35));
        buttonLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLogoutActionPerformed(evt);
            }
        });

        infoList.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jScrollPane5.setViewportView(infoList);

        tasteCombo.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        tasteCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Opera", "Rock", "Pop" }));
        tasteCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tasteComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout picturePanelLayout = new javax.swing.GroupLayout(picturePanel);
        picturePanel.setLayout(picturePanelLayout);
        picturePanelLayout.setHorizontalGroup(
            picturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 55, Short.MAX_VALUE)
        );
        picturePanelLayout.setVerticalGroup(
            picturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 56, Short.MAX_VALUE)
        );

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel10.setText("Information");

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pictureLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pictureLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonChat, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(buttonAccept, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonAddFriend, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(buttonReject, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                            .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(37, 37, 37)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonPlay, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(buttonPlay1, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(190, 190, 190)
                                .addComponent(buttonLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4)
                                .addComponent(fieldPost, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(buttonPost, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 504, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 504, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(picturePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tasteCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(picturePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(226, 226, 226))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(fieldPost, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(buttonPost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1))))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(buttonPlay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(buttonPlay1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonLogout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(tasteCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(11, 11, 11)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(buttonChat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonAccept, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(11, 11, 11)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(buttonAddFriend, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonReject, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonChatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonChatActionPerformed
        // TODO add your handling code here:
        Runnable chatSession = () -> 
        {
            if (onlineList.getSelectedValue() != null)
            {
                String name = onlineList.getSelectedValue();
                String tag = " (friend)";
                if(name.contains(tag))
                {
                    name = name.replace(tag,"");
                }
                UserData friend = myTalker.getUserdata(name);
                ClientChatter chatter = new ClientChatter(myData,friend);
                new ChatWindow(chatter).setVisible(true);
            }
        };
        new Thread(chatSession).start();
        
    }//GEN-LAST:event_buttonChatActionPerformed

    private void buttonRejectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRejectActionPerformed
        // TODO add your handling code here:
        if (requestList.getSelectedValue() != null)
        {
            myTalker.replyRequest(requestList.getSelectedValue(),false);
        }
    }//GEN-LAST:event_buttonRejectActionPerformed

    private void buttonAcceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAcceptActionPerformed
        // TODO add your handling code here:
        if (requestList.getSelectedValue() != null)
        {
            myTalker.replyRequest(requestList.getSelectedValue(),true);
        }
    }//GEN-LAST:event_buttonAcceptActionPerformed

    private void buttonAddFriendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddFriendActionPerformed
        // TODO add your handling code here:
        if (onlineList.getSelectedValue() != null)
        {
            myTalker.requestFriendship(onlineList.getSelectedValue());
        }
    }//GEN-LAST:event_buttonAddFriendActionPerformed

    private void buttonPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPlayActionPerformed
        Runnable songThread = () -> 
        {
            if (songsArea.getSelectedValue() != null)
            {
                String[] songLine = songsArea.getSelectedValue().split(": ");
                String songname = songLine[1];
                playSong(songname);
            }
        };
        new Thread(songThread).start();
    }//GEN-LAST:event_buttonPlayActionPerformed

    private void buttonPostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPostActionPerformed
        // TODO add your handling code here:
        if (fieldPost.getText() != null)
        {
            myTalker.makePost(fieldPost.getText());
            fieldPost.setText("");
        }
    }//GEN-LAST:event_buttonPostActionPerformed

    private void buttonPlay1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPlay1ActionPerformed
        // TODO add your handling code here:
        chooseFile();
    }//GEN-LAST:event_buttonPlay1ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        logOut();
    }//GEN-LAST:event_formWindowClosing

    private void buttonLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLogoutActionPerformed
        // TODO add your handling code here:
        logOut();
        dispose();
    }//GEN-LAST:event_buttonLogoutActionPerformed

    private void tasteComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tasteComboActionPerformed
        // TODO add your handling code here:
        DefaultListModel listModel = new DefaultListModel();
        for(int i = 0; i < onlineUsers.size(); i++)
        {
            UserData person = myTalker.getUserdata(onlineUsers.get(i));
            String filter = (String) tasteCombo.getSelectedItem();
            if (filter.equals("All") || person.listOfTastes.contains(filter))
            {
                if (myData.listOfFriends.contains(onlineUsers.get(i)))
                {
                    listModel.add(i,onlineUsers.get(i) + " (friend)");
                }
                else
                {
                    listModel.add(i,onlineUsers.get(i));
                }
            }
        }
        onlineList.setModel(listModel);
    }//GEN-LAST:event_tasteComboActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(false);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAccept;
    private javax.swing.JButton buttonAddFriend;
    private javax.swing.JButton buttonChat;
    private javax.swing.JButton buttonLogout;
    private javax.swing.JButton buttonPlay;
    private javax.swing.JButton buttonPlay1;
    private javax.swing.JButton buttonPost;
    private javax.swing.JButton buttonReject;
    private javax.swing.JTextField fieldPost;
    private javax.swing.JList<String> friendList;
    private javax.swing.JList<String> infoList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JList<String> onlineList;
    private javax.swing.JPanel panel;
    private javax.swing.JLabel pictureLabel;
    private javax.swing.JPanel picturePanel;
    private javax.swing.JTextArea postsArea;
    private javax.swing.JList<String> requestList;
    private javax.swing.JList<String> songsArea;
    private javax.swing.JComboBox<String> tasteCombo;
    // End of variables declaration//GEN-END:variables
}
