/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.*;
import java.util.*;
import java.net.*;

// Server class 
public class Server {
    public static String fileReveiver="";
    public static long fl;
    public static String fileSender="";
    public static String messageSender="";
    public static String holdMsg;
    public static ServerSocket serSoc;
    // ArrayList to store active clients 
    static Vector<ClientController> clientList = new Vector<>();
    
    // counter for clients 
    static int i = 1;
    static String filen="";
    public static void main(String[] args) throws IOException {
        serSoc = new ServerSocket(34343);
        ServerSocket ss = new ServerSocket(42069);

        Socket s;
        // running infinite loop for listening for client requests
        while (true) {
            //accept the incoming request 
            s = ss.accept();
            
            // obtain inputstreams and output streams 
            DataInputStream input = new DataInputStream(s.getInputStream());
            DataOutputStream output = new DataOutputStream(s.getOutputStream());

            // Create a new controller object for controlling the client requests. 
            ClientController client = new ClientController(s, "client"+i, input, output);
            // Create a new Thread with this object. 
            Thread t = new Thread(client);

            // add this client to active clients list 
            clientList.add(client);
            
            //sending the names of the clients that are online
            for (ClientController x : clientList) {
               for (ClientController cc : clientList) {
                   if(x.isloggedin==true && cc.isloggedin==true)
                        x.dos.writeUTF(cc.getName());
                } 
            }
                
            // start the thread
            t.start();

            // increment i for each new client
            i++;
        }
    }
   
}

// ClientHandler class 
class ClientController implements Runnable {

    private String name;
    DataInputStream dis;
    DataOutputStream dos;
    Socket s;
    boolean isloggedin;
    ArrayList<String> spokenTo=new ArrayList<>();
    ClientController receiverCC;
    

    // constructor 
    public ClientController(Socket s, String name, DataInputStream input, DataOutputStream output) {
        this.dis = input;
        this.dos = output;
        this.name = name;
        this.s = s;
        this.isloggedin = true;
        
    }
   
    public String getName()
    {
        return this.name;
    }
    
    @Override
    public void run() {
        String received;
        String recipient;
        String n;
        String fileName="";
        String msg = "";
        boolean gotFile=false;
        
        
        while (true) {
            try {
                // receive the string 
                received = dis.readUTF();
                
                //logout of server
                if (received.equals("logout")) {
                    this.isloggedin = false;
                    for (ClientController x : Server.clientList) {
                        if(x.isloggedin==true)
                        {
                            x.dos.writeUTF(this.name+"-logout");
                        }
                    }
                    System.out.println(this.name+" has logged out");
                    this.s.close();
                    break;
                }
                
                if(received.contains("sent the file")){
                    StringTokenizer st = new StringTokenizer(received, "-"); //message is split with '-'
                    st.nextToken();
                    Server.filen = st.nextToken(); //name of the file that will be saved to the server
                    readFile(Server.filen);
                }
                else if(received.contains("fileReceiver"))
                {
                    StringTokenizer st = new StringTokenizer(received, "f"); //message is split with '-'
                    Server.fileReveiver = st.nextToken(); //name of file receiver
                    for (ClientController cc : Server.clientList) {
                        if (cc.name.equals(Server.fileReveiver) && cc.isloggedin == true) {
                            Server.fileSender=this.name;
                            receiverCC = cc;
                            cc.dos.writeUTF("010Do you want to accept a file from "+this.name); 
                            break;
                        }
                    }
                    
                }
                
                else if(received.equalsIgnoreCase("yesfile"))
                {
                    for (ClientController cc : Server.clientList) {
                        if (cc.name.equals(Server.fileSender) && cc.isloggedin == true) {
                            cc.dos.writeUTF("Send File"); 
                            break;
                        }
                    }
                }
                else if(received.equalsIgnoreCase("nofile"))
                {
                    for (ClientController cc : Server.clientList) {
                        if (cc.name.equals(Server.fileSender) && cc.isloggedin == true) {
                            cc.dos.writeUTF("399File Declined"); 
                            break;
                        }
                    }
                }
                else if(received.equalsIgnoreCase("yesmessage"))
                {
                    for (ClientController cc : Server.clientList) {
                        if (cc.name.equals(Server.messageSender) && cc.isloggedin == true) {
                            this.spokenTo.add(cc.name);
                            cc.spokenTo.add(this.name);
                            cc.dos.writeUTF("#"+Server.holdMsg+"-"+this.name);
                            Server.holdMsg="";
                            break;
                        }
                    }
                }
                else if(received.equalsIgnoreCase("nomessage"))
                {
                    for (ClientController cc : Server.clientList) {
                        if (cc.name.equals(Server.messageSender) && cc.isloggedin == true) {
                            cc.dos.writeUTF("202Message Declined");
                            Server.holdMsg="";
                            break;
                        }
                    }
                }
                else {
                    // break the string into message and recipient part 
                    boolean found=false;
                    StringTokenizer st = new StringTokenizer(received, "-"); //message is split with '-'
                    msg = st.nextToken(); //the message to be sent
                    recipient = st.nextToken(); //the name of the recipient
                    System.out.println("Message from " + name + " : " + "\"" + msg + "\"" + " to : " + recipient);
                    
                    // search for the recipient in the connected clients list (clientList). 
                    // clientList is the vector storing online clients 
                    for (ClientController cc : Server.clientList) {
                        // if the recipient is found, write on its output stream 
                        if (cc.name.equals(recipient) && cc.isloggedin == true) {
                            for (String x : this.spokenTo) {
                                if(x.equalsIgnoreCase(cc.name))
                                {
                                    cc.dos.writeUTF(this.name + ": " + msg); 
                                    found=true;
                                    break;
                                } 
                            }
                            if(found==false)
                            {
                                Server.holdMsg = msg;
                                Server.messageSender=this.name;
                                cc.dos.writeUTF("566Do you want to accept a message from "+this.name);
                                break;
                            }
                        }
                    }
                }
            }
            catch (IOException e) {

                e.printStackTrace();
            }
        }
        try {
            // closing input and output streams 
            this.dis.close();
            this.dos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void readFile (String fn)
    {
        long count =0;
         byte[] contents = new byte[100000000];
         File file = new File(fn);
               
         
                try {
                    
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    
                    int bytesRead = 0;
//
                    while ((bytesRead = dis.read(contents)) != -1) {
                        bos.write(contents, 0, bytesRead);
                        count+=bytesRead;
                        if(count==39048088)
                            break;
                    }

                    bos.flush();
                    
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
         sendFile(file);
    }
    public void sendFile(File file) {
        
        try {
            receiverCC.dos.writeUTF("File name : "+file.getName());
            //receiverCC.dos.writeUTF("File size : "+file.length());
            
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis); 
            //Get socket's output stream
            
            OutputStream os = (OutputStream) receiverCC.dos;
            //Read File Contents into contents array 
            byte[] contents;
            long fileLength = file.length(); 
            long current = 0;
            long start = System.nanoTime();
            
            while(current!=fileLength){ 
                int size = 10000;
                if(fileLength - current >= size)
                    current += size;    
                else{ 
                    size = (int)(fileLength - current); 
                    current = fileLength;
                } 
                contents = new byte[size]; 
                bis.read(contents, 0, size); 
                os.write(contents);
                System.out.println((current*100)/fileLength+"% sent");
            }   

            os.flush();
            receiverCC.dos =  new DataOutputStream(s.getOutputStream());
            
            
            System.out.println("File sent to client");
                //output.writeUTF(f+"+"+recipient);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
