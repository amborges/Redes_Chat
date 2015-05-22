/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package singlechat;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author alex
 */
public class Servidor extends Thread{
    //estatico
    static String serverip = "localhost"; //servidor pré conhecido
    
    //propriedades
    ServerSocket server;
    Socket client;
    
    ListaAmigos program;
    
    int id;
    InetAddress ip;
    String name;
    String key;
    
    Servidor(String setName, String setKey, ListaAmigos setProgram){
        System.out.println("SERVIDOR ATIVADO");
        try{
            server = new ServerSocket(6991); //porta definida no protocolo
            name = setName;
            id = name.hashCode();
            ip = InetAddress.getLocalHost();//server.getInetAddress();
            key = setKey;
            program = setProgram;
            
            returnToClient(SingleChat.IPSERVIDOR, "MASTER_PEER CONNECT " + name + "\n\n");
            System.out.println("meu ip eh " + ip.toString());
        }catch(Exception e){
            System.out.println("FALHA ALOCAR NO SERVIDOR PRINCIPAL: " + e);
        }
    }
    
    @Override
    public void finalize(){
        System.out.println("SERVIDOR DESATIVADO");
        //quando vai ser excluido, manda uma mensagem ao servidor
        String myIP = (ip.toString().split("/"))[1];
        String msg = "MASTER_PEER DISCONNECT " + name + "\n\n";
        returnToClient(serverip, msg);
    }
    
    @Override
    public void run(){
        while(true){
            try{
                client = server.accept();
                System.out.println("CHAT ESTA OUVINDO");
                ObjectInputStream entrada = new ObjectInputStream(client.getInputStream());
                String msg = entrada.readUTF();
                entrada.close();
                
                trataMsg(msg);
                
            }catch(Exception e){
                System.out.println("Falha ao ouvir : " + e);
            }
        }
    }
    
    private void trataMsg(String s){
        String m[] = s.split(" ");
        System.out.println("["+s+"]");
        //SEND_MSG <SIZE> <PEER_ID> <MSG>\n\n
        if(m[0].equals("SEND_MSG")){
            String msg = "";
                //Removo a <MSG> do resto
            for(int i = 3; i < m.length - 1; i++)
                msg += m[i];
            msg = msg.replace("\n", "");
            program.talkto(m[2], msg);
            //responde RECV_MSG <MY_PEER_ID>\n\n
            String recmsg = "RECV_MSG " + ip.toString() + "\n\n";
            returnToClient(m[2], recmsg);
        }
        //TALK_TO <PEER_ID>\n\n
        else if(m[0].equals("TALK_TO")){
            String friendip = m[1].replace("\n", "");
            program.include(friendip);
            //responde ACCEPT_TALKING <MY_PEER_ID>\n\n
            String recmsg = "ACCEPT_TALKING " + ip.toString() + "\n\n";
            returnToClient(friendip, recmsg);
        }
        //PEER_GROUP (<PEER_ID>,<PEER_NAME>,<PEER_IP>,<PEER_STATUS>,<PEER_KEY>\n)+\n\n
        else if(m[0].equals("PEER_GROUP")){
            String peer[] = m[1].split("\n");
            program.reload(peer);
        }
        else{
            System.out.println("MENSAGEM NAO RECONHECIDA");
        }       
    }
    
    private void returnToClient(String friendIP, String msg){
        /*
        Realiza o retorno de uma mensagem ao cliente
        */
        try{
            InetAddress friend = InetAddress.getByName(friendIP);
            Socket retToClient;
            if(friendIP.equals("localhost"))
                retToClient = new Socket(friend, 6969);
            else
                retToClient = new Socket(friend, 6991);
            ObjectOutputStream sender = new ObjectOutputStream(retToClient.getOutputStream());
            sender.flush();
            sender.writeUTF(msg);
            sender.close();
            retToClient.close();
        }catch(Exception e){
            System.out.println("Erro ao retornar msg ao amigo : " + e);
        }
    }
}
