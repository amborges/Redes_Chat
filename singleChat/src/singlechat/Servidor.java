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
import java.security.MessageDigest;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author alex
 */
public class Servidor extends Thread{
    
    //propriedades
    SSLServerSocket server; //atributo ouvidor do servidor
    SSLSocket client; //atributo para responder as mensagens
    
    ListaAmigos program; //aplicativo GUI principal
    
    //atributos do protocolo
    int id; //identificador unico de rede, parte do protocolo
    InetAddress ip; //endereço IP do cliente
    String name; //nome do usuário que tá no chat
    String key; //senha do usuário do chat
    
    Servidor(String setName, String setKey, ListaAmigos setProgram){
        //System.out.println("SERVIDOR ATIVADO");
        try{
            //server = new ServerSocket(SingleChat.DOORSERVIDOR); //porta definida no protocolo
            SSLServerSocketFactory factory=(SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket server =(SSLServerSocket) factory.createServerSocket(SingleChat.DOORSERVIDOR);
            
            name = setName;
            id = name.hashCode();
            ip = InetAddress.getLocalHost();//server.getInetAddress();
            key = sha1(setKey);
            program = setProgram;
            
            
            connectToServer();
            
            //returnToClient(SingleChat.IPSERVIDOR, );
            //System.out.println("meu ip eh " + ip.toString() + " senha = " + key);
        }catch(Exception e){
            System.out.println("FALHA ALOCAR NO SERVIDOR PRINCIPAL: " + e);
        }
    }
    
    @Override
    public void finalize(){
        //System.out.println("SERVIDOR DESATIVADO");
        //quando vai ser excluido, manda uma mensagem ao servidor
        String msg = "MASTER_PEER DISCONNECT " + name + "\n\n";
        returnToClient(SingleChat.IPSERVIDOR, msg);
    }
    
    @Override
    public void run(){
        while(true){
            try{
                //client = server.accept();
                client = (SSLSocket)server.accept();
                //System.out.println("CHAT ESTA OUVINDO");
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
        //System.out.println("["+s+"]");
        //SEND_MSG <SIZE> <PEER_ID> <MSG>\n\n
        if(m[0].equals("SEND_MSG")){
            String msg = "";
                //Removo a <MSG> do resto
            for(int i = 3; i < m.length; i++)
                msg += m[i]+ " ";
            msg = msg.replace("\n", "");
            program.talkto(m[2], msg);
            //responde RECV_MSG <MY_PEER_ID>\n\n
            String recmsg = "RECV_MSG " + ip.toString() + "\n\n";
            returnToClient(m[2], recmsg);
        }
        //TALK_TO <PEER_ID>\n\n
        else if(m[0].equals("TALK_TO")){
            String friendid = m[1].replace("\n", "");
            //responde ACCEPT_TALKING <MY_PEER_ID>\n\n
            program.openChat(friendid);
        }
        //PEER_GROUP (<PEER_ID>,<PEER_NAME>,<PEER_IP>,<PEER_STATUS>,<PEER_KEY>\n)+\n\n
        else if(m[0].equals("PEER_GROUP")){
            String peer[] = m[1].split("\n");
            program.reload(peer);
        }
        else if(m[0].equals("MASTER_PEER")){
            String peer[] = m[1].split("\n");
            program.confirmOnline(peer);
        }
        else{
            System.out.println("MENSAGEM NAO RECONHECIDA");
        }       
    }
    
    public static void returnToClient(String friendIP, String msg){
        /*
        Realiza o retorno de uma mensagem ao cliente
        */
        try{
            //InetAddress friend = InetAddress.getByName(friendIP);
            //System.out.println(friendIP);
            
                
            SSLSocketFactory factory=(SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket retToClient=(SSLSocket) factory.createSocket(friendIP, SingleChat.DOORSERVIDOR);
            
                ObjectOutputStream sender = new ObjectOutputStream(retToClient.getOutputStream());
                sender.flush();
                sender.writeUTF(msg);
                sender.close();
                retToClient.close();
            
                
            
            
                   
            //Socket retToClient = new Socket(friendIP, SingleChat.DOORSERVIDOR);
            //Socket retToClient = new Socket("192.168.161.248", 6991);
            
        }catch(Exception e){
            System.out.println("Erro ao retornar msg ao amigo : " + e);
        }
    }
    
    public void connectToServer(){
        /*
        Realiza o retorno de uma mensagem ao cliente
        */
        try{
            SSLSocketFactory factory=(SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket retToClient=(SSLSocket) factory.createSocket(SingleChat.IPSERVIDOR, SingleChat.DOORSERVIDOR);
            ObjectOutputStream sender = new ObjectOutputStream(retToClient.getOutputStream());
            sender.flush();
            sender.writeUTF("MASTER_PEER CONNECT " + name + " " + key + "\n\n");
            sender.close();
            retToClient.close();
        }catch(Exception e){
            System.out.println("Erro ao retornar msg ao amigo : " + e);
        }
    }
    
    private String sha1(String s){
        String senhaCriptografada = "";
        try{
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            byte[] result = mDigest.digest(s.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < result.length; i++) {
                sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
            }
         
            senhaCriptografada = sb.toString();
        }catch(Exception e){
            System.out.println("Falha ao criptografar a senha : " + e);
        }
        return senhaCriptografada;
    }
}
