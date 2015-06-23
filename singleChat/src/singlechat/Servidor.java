/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package singlechat;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.MessageDigest;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

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
    char key[]; //senha do usuário do chat
    String certificate; //o meu certificado em formato string
    
        //Conjunto de codificações para descriptografar mensagens recebidas
    private final String[] codificacao = {"SSL_RSA_WITH_RC4_128_MD5"};
    
    Servidor(String setName, String setKey, ListaAmigos setProgram){
        
        KeyStore ks;
        KeyManagerFactory kmf;
        SSLContext contextoSSL;
        ServerSocketFactory ssf;
        
        try{
            name = setName;
            id = name.hashCode();
            if(id < 0) id *= -1;
            ip = InetAddress.getLocalHost();//server.getInetAddress();
            key = sha1(setKey).toCharArray();
            program = setProgram;
            certificate = createCertificate(id, key);
            
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("certificados/"+id+".cert"), key);
            
                //cria um caminho de certificação baseado em X509
            kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, key);
            
                //cria um SSLContext segundo o protocolo informado
            contextoSSL = SSLContext.getInstance("SSLv3");
            contextoSSL.init(kmf.getKeyManagers(), null, null);
            
                //Iniciando o servidor...
            ssf = contextoSSL.getServerSocketFactory();
            server = (SSLServerSocket) ssf.createServerSocket(SingleChat.DOORSERVIDOR);
            
            //connectToServer();
                        
            returnToClient(SingleChat.IPSERVIDOR, 
                    "MASTER_PEER CONNECT " + certificate.length() + "\n" + certificate + "\n\n");
            
            
        }catch(Exception e){
            System.out.println("FALHA ALOCAR NO SERVIDOR PRINCIPAL: " + e);
            System.exit(0);
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
        ObjectInputStream ouvido;
        
        while(true){
            try{
                //client = server.accept();
                client = (SSLSocket)server.accept();
                client.setEnabledCipherSuites(codificacao);
                
                    //ouve a mensagem e passa adiante
                ouvido = new ObjectInputStream(client.getInputStream());
                String msg = ouvido.readUTF();//readObject().toString();
                ouvido.close();
                client.close();
                
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
            String recmsg = "RECV_MSG " + id + "\n\n";
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
    
    public static void returnToClient(String friendID, String msg){
        /*
        Realiza o retorno de uma mensagem ao cliente
        */
        String  auxcertificate;
        String  auxfriendIP;
        char  auxkey[];
        
        if(friendID.equals(SingleChat.IPSERVIDOR)){    
            auxcertificate = ListaAmigos.fromFileToString(SingleChat.CERTIFICADOSERVIDOR);
            auxfriendIP = SingleChat.IPSERVIDOR;
            auxkey = SingleChat.PASSWORDSERVIDOR;
        }
        else{
            auxcertificate = "certificados/" + friendID + ".cert";
            auxfriendIP = ListaAmigos.onlineFriends.getByID(Integer.parseInt(friendID)).friendIP;
            auxkey = ListaAmigos.onlineFriends.getByID(Integer.parseInt(friendID)).key;
        }
             
        try{
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(auxcertificate), auxkey);
            
                //cria um caminho de certificação baseado em X509
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, auxkey);
            
                //cria um SSLContext segundo o protocolo informado
            SSLContext contextoSSL = SSLContext.getInstance("SSLv3");
            contextoSSL.init(kmf.getKeyManagers(), null, null);
            
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
                        
            tmf.init(ks);
            TrustManager tms[] = tmf.getTrustManagers();
            
            KeyManagerFactory keymf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
            
            keymf.init(ks, auxkey);
            KeyManager kms[] = keymf.getKeyManagers();
            
            contextoSSL = SSLContext.getInstance("SSL");
            contextoSSL.init(kms, tms, null);
            
            SSLSocketFactory ssf = contextoSSL.getSocketFactory();
            
            SSLSocket falante = (SSLSocket) ssf.createSocket(auxfriendIP, SingleChat.DOORSERVIDOR);
            
            ObjectOutputStream sender = new ObjectOutputStream(falante.getOutputStream());
            sender.flush();
            sender.writeUTF(msg);
            sender.close();
            
            falante.close();
            
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
    
    private String createCertificate(int id, char key[]){
        String pass = new String(key);
        String fileName = "certificados/" + id + ".cert";
        try{ //CN=alex, OU=ufpel, O=ufpel, L=pelotas, ST=rs, C=br
            String toExec = "keytool -genkey -noprompt " +
                        " -alias " + name + " " +
                            //CN=alex, OU=CDTec, O=UFPel, L=Pelotas, ST=RS, C=BR
                        " -dname \"CN="+ name +", OU=CDTec, O=UFPel, L=Pelotas, S=RS, C=BR\" " +
                        " -keyalg RSA" +
                        " -keystore "+ fileName +
                        " -storepass "+ pass +" " +
                        " -keypass "+ pass ;
            //toExec = "gedit";
            String commands[];
            if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0){
                String comWin[] = { "cmd", "/c", toExec }; //windows
                commands = comWin;
            }
            else{
                String comLin[] = { "bash", "-c", toExec }; //linux
                commands = comLin;
            }
            
            Runtime.getRuntime().exec(commands);
            Thread.sleep(2000); //pra dar tempo do arquivo ser criado localmente
            FileInputStream fis = new FileInputStream(fileName);
            //se nao carregar, vai dar falha
            fis.close();
            
            return ListaAmigos.fromFileToString(fileName);
            
        }catch(Exception e){
            System.out.println("Falha ao criar o certificado: " + e);
            System.exit(0); //sem certificado nao pode continuar
        }
        
        return "";
    }
}
