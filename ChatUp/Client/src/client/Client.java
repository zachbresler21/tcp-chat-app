/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    final static int serverPort = 42069;
    static String sendMsg;
    static String receiveMsg;
    static String fileName;
    public static DataInputStream input;
    public static DataOutputStream output;
    public static Socket s;
    public static String name;
    public Vector<String> clientList = new Vector<>();

    public static void main(String args[]) throws UnknownHostException, IOException {
        
        // getting localhost ip 
        InetAddress ip = InetAddress.getLocalHost();

        // establish the connection 
        s = new Socket(ip, serverPort);

        // obtaining inputstreams and outputstreams 
        input = new DataInputStream(s.getInputStream());
        output = new DataOutputStream(s.getOutputStream());
        
        //open gui
        new ChatUp().setVisible(true);
    }

    public Socket getSocket() {
        return this.s;
    }

    public void sendMessage(String message) {
        // write on the output stream
        try {
            output.writeUTF(message);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public String readMessage() {
        //read the message sent to this client
        try {
            return input.readUTF();
        }
        catch (Exception e) {
            
        }
        return "";
    } 
    public void sendFile(String fileName){
        try {
            File fn = new File(fileName);
            FileInputStream fis = new FileInputStream(fn);
            BufferedInputStream bis = new BufferedInputStream(fis); 
            //Get socket's output stream
            OutputStream os = (OutputStream) output;
            //Read File Contents into contents array 
            byte[] contents;
            long fileLength = fn.length(); 
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
                System.out.print((current*100)/fileLength+"% sent");
               
            }   
            
            os.flush();
            output =  new DataOutputStream(s.getOutputStream());
            
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public String readFile(String fn) 
    {
        long count=0;
        try{
            byte[] contents = new byte[100000000];
        
            File f = new File(fn);
            FileOutputStream fos = new FileOutputStream(f);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            
            int bytesRead = 0;

            while ((bytesRead = input.read(contents)) != -1) {
                bos.write(contents, 0, bytesRead);
                count+=bytesRead;
                        if(count==39048088)
                            break;
            }
            
            bos.flush();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return "File saved successfully!";
    }
}
