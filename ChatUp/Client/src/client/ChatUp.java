/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;


import java.awt.Color;
import java.awt.Cursor;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.*; 
import java.net.*; 
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


/**
 *
 * @author user
 */
public class ChatUp extends javax.swing.JFrame {
    //public static String message;
    static Client c = new Client();
    private Socket sk;
    boolean clicked =false;
    boolean suspend=false;
    String fnReceived="";
    long fileSize;
    String someFile="";
    DefaultListModel listModel = new DefaultListModel();
    /**
     * EVERY TIME A NEW CLIENT IS CREATED, A NEW GUI SCREEN IS CREATED
     */
    public ChatUp() {
        initComponents();
        
        receiver();//call method to start readMessage thread
        sender();//call method to start sendMessage thread
        //readIncomingFile();
        lblSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblAttachment.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        //String user = JOptionPane.showInputDialog(null,"Enter in your preferred name");
        //c.sendMessage("username ="+user);
        
        //mouselistener to listen for clicks on the send message button
        lblSend.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                clicked=true;
            }
        });
        
        //mouselistener to listen for clicks on the attachment button
        lblAttachment.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
               //pgbFileLoad.setVisible(true);
               String r = JOptionPane.showInputDialog(rootPane,"Enter who you want to send a file to");
               c.sendMessage(r+"fileReceiver");
            }
        });
    }
        
        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable()  
        { 
            @Override
            public void run() { 
                while (true) 
                { 
                    //read the message to deliver. 
                    String sendMsg = txfSendMessage.getText();
                    /*
                    Once the send button is clicked the message will be taken from the text field
                    and first split up so that we can print onto the textarea what we have sent.
                    Then it will send the message onto the stream to the server where it will be 
                    directed to the specified client.
                    */
                    if(clicked==true){
                        sendMsg = txfSendMessage.getText();
                        StringTokenizer st = new StringTokenizer(sendMsg, "-"); //message is split with '-'
                        String msg = st.nextToken(); //the message to be sent
                        txaMessages.append("You: "+msg+"\n\n");
                        if(!(sendMsg.equalsIgnoreCase(""))){
                            //sends the message to the server on the write stream
                            c.sendMessage(sendMsg);
                            clicked=false; //reseting the clicked variable back to fasle so that it is ready to listen for clicks
                            txfSendMessage.setText("");
                            if(msg.equalsIgnoreCase("logout"))
                            {
                                System.exit(0); //close the screen
                            }
                        }
                        
                    }
                } 
            } 
        }); 
    //read message thread    
    Thread readMessage = new Thread(new Runnable()  
        { 
            @Override
            public void run() { 
                while (true) { 
                   
                    String message = c.readMessage();
                    
                    if(message.contains(":"))
                    {
                        //print out the message received to the message text area
                        txaMessages.append(message+"\n\n"); 
                    }
                    else if(message.contains("#"))
                    {
                        c.sendMessage(message.substring(1));
                    }
                    else if(message.equalsIgnoreCase("send file"))
                    {
                        System.out.println("Server ready to receive file");
                        String f = JOptionPane.showInputDialog(rootPane, "Enter the name of the file you wish to send");
                        
                        c.sendMessage("sent the file-"+f);
                        c.sendFile(f);
                    }
                    else if (message.contains("566"))//want to accept message
                    {
                        int ans = JOptionPane.showConfirmDialog(rootPane, message.substring(3),null, JOptionPane.YES_NO_OPTION);
                        if(ans==0)
                        {
                            c.sendMessage("yesmessage");
                        }else
                        {
                            c.sendMessage("nomessage");
                        }
                    }
                    else if(message.contains("010"))//want to accept file
                    {
                        int ans = JOptionPane.showConfirmDialog(rootPane, message.substring(3),null, JOptionPane.YES_NO_OPTION);
                        if(ans==0)
                        {
                            c.sendMessage("yesfile");
                            try {
                                sk = new Socket(InetAddress.getLocalHost(), 34343);
                            }
                            catch (Exception ex) {
                                Logger.getLogger(ChatUp.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }else
                        {
                            c.sendMessage("nofile");
                        }
                    }
                    else if(message.contains("File name : "))
                    {
                        fnReceived="new"+message.substring(message.indexOf(":")+2);
                        c.readFile(fnReceived);
                    }
//                    else if(message.contains("File size : "))
//                    {
//                        fileSize=Long.parseLong(message.substring(message.indexOf(":")+2));
//                        
//                    }
                    else if(message.contains("399"))
                    {
                        txaMessages.append("File Declined!\n\n");
                    }
                    else if(message.contains("202"))
                    {
                        txaMessages.append("Message Declined!\n\n");
                    }
//                    else if(message.equalsIgnoreCase("file sent!"))
//                    {
//                        c.readFile("newMovie.mp4", fileSize);
//                    }
                    else if(!(listModel.contains(message)) && message.contains("client")){
                        if(message.contains("logout")){
                            StringTokenizer st = new StringTokenizer(message, "-"); //message is split with '-'
                            String n = st.nextToken(); 
                            listModel.removeElement(n);
                        }else{
                            listModel.addElement(message);//add the client to the list model
                            ClientList.setModel(listModel);//add the list of clients to the jlist (online clients
                        }
                    }
                    
                } 
            } 
        });
    public void receiver()
    {
        //start the readMessage thread
        readMessage.start();
    }
    public void sender()
    {
        //start the sendMessage thread
         sendMessage.start(); 
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txaMessages = new javax.swing.JTextArea();
        pnlHeader = new javax.swing.JPanel();
        lblChatUp = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        ClientList = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        txfSearch = new javax.swing.JTextField();
        txfSendMessage = new javax.swing.JTextField();
        lblSend = new javax.swing.JLabel();
        lblAttachment = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ChatUp");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPane1.setOpaque(false);

        txaMessages.setEditable(false);
        txaMessages.setColumns(20);
        txaMessages.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        txaMessages.setRows(5);
        txaMessages.setBorder(null);
        txaMessages.setFocusable(false);
        jScrollPane1.setViewportView(txaMessages);

        pnlHeader.setBackground(new java.awt.Color(255, 255, 255));

        lblChatUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Chat-Up-logo-plain.png"))); // NOI18N

        javax.swing.GroupLayout pnlHeaderLayout = new javax.swing.GroupLayout(pnlHeader);
        pnlHeader.setLayout(pnlHeaderLayout);
        pnlHeaderLayout.setHorizontalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblChatUp, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(464, Short.MAX_VALUE))
        );
        pnlHeaderLayout.setVerticalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addComponent(lblChatUp, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                .addContainerGap())
        );

        ClientList.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        ClientList.setForeground(new java.awt.Color(239, 123, 39));
        ClientList.setFocusable(false);
        ClientList.setSelectionBackground(new java.awt.Color(26, 115, 233));
        jScrollPane2.setViewportView(ClientList);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Very-Basic-Search-icon.png"))); // NOI18N

        txfSearch.setForeground(new java.awt.Color(102, 102, 102));
        txfSearch.setText("Search");
        txfSearch.setFocusable(false);
        txfSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                txfSearchMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txfSearchMouseClicked(evt);
            }
        });

        lblSend.setIcon(new javax.swing.ImageIcon(getClass().getResource("/send.png"))); // NOI18N
        lblSend.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSendMouseClicked(evt);
            }
        });
        lblSend.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lblSendKeyPressed(evt);
            }
        });

        lblAttachment.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Very-Basic-Paper-Clip-icon.png"))); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(1, 1, 1)
                        .addComponent(txfSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txfSendMessage)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblAttachment)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblSend))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 542, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(pnlHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(559, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 309, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txfSearch, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txfSendMessage)
                    .addComponent(lblSend, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                    .addComponent(lblAttachment, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap(176, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txfSearchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txfSearchMouseClicked
        txfSearch.setText("");
        txfSearch.setForeground(Color.BLACK);
    }//GEN-LAST:event_txfSearchMouseClicked

    private void txfSearchMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txfSearchMouseReleased

    }//GEN-LAST:event_txfSearchMouseReleased

    private void lblSendMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSendMouseClicked
       
    }//GEN-LAST:event_lblSendMouseClicked

    private void lblSendKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lblSendKeyPressed
       
    }//GEN-LAST:event_lblSendKeyPressed
    
    
    
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
        }
        catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ChatUp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ChatUp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ChatUp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ChatUp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        /* Create and display the form */
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ChatUp().setVisible(true);
            }
        });
        
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> ClientList;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblAttachment;
    private javax.swing.JLabel lblChatUp;
    private javax.swing.JLabel lblSend;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JTextArea txaMessages;
    private javax.swing.JTextField txfSearch;
    private javax.swing.JTextField txfSendMessage;
    // End of variables declaration//GEN-END:variables
}
