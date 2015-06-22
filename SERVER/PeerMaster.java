package SERVER;


import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.Socket;
import java.security.KeyStore;
import java.util.ArrayList;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;



public class PeerMaster{
	//modificado por lucas
    static private PeerData listOfPeers;
    static private char     SERVERPASSWORD[]    = new String("masterpeerpass").toCharArray();
    static private String   SERVERKEYSTORE 	= "SERVER/server_certificate.cert";
    static private int      DOOR         	= 6991;

    public static void main(String[] args) {
    	SSLServerSocket server;
        SSLSocket client;
        KeyStore ks;
        KeyManagerFactory kmf;
        SSLContext contextoSSL;
        ServerSocketFactory ssf;
        
        try{
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(SERVERKEYSTORE), SERVERPASSWORD);
            
                //cria um caminho de certificação baseado em X509
            kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, SERVERPASSWORD);
            
                //cria um SSLContext segundo o protocolo informado
            contextoSSL = SSLContext.getInstance("SSLv3");
            contextoSSL.init(kmf.getKeyManagers(), null, null);
            
                //Iniciando o servidor...
            ssf = contextoSSL.getServerSocketFactory();
            server = (SSLServerSocket) ssf.createServerSocket(DOOR);
            
                //iniciando os peers
            listOfPeers = new PeerData();
            
                //Servidor em execução
            while(true){
                System.out.println("Antes da conexao!");
                client = (SSLSocket) server.accept();
                System.out.println("Conexao aceita!");
                ObjectInputStream entrada = new ObjectInputStream(client.getInputStream());
                String msg = entrada.readUTF();
                entrada.close();

                String msgSplit[] = msg.split(" ");
                //System.out.println(msg);
                if(msgSplit[0].equals("MASTER_PEER") && msgSplit[1].equals("CONNECT")){
                    //Cliente conectou e requisita a lista de peers
                    String clientIP = client.getInetAddress().toString().replaceAll("/","");
///////////////////////////////////////////
/////////////////////////////////////////// Trocar para receber cadeia de bytes
///////////////////////////////////////////
                    msgSplit[2] = msgSplit[2].substring(0, msgSplit[2].length()); //removendo os \n\n
///////////////////////////////////////////
///////////////////////////////////////////
///////////////////////////////////////////
                    System.out.println("CONECTANDO NOVO CLIENTE!");
                    String dataPeer = msgSplit[2].hashCode() + "," + 
                        msgSplit[2].replaceAll("\n","") + "," + clientIP + 
                        ",ONLINE," + msgSplit[3].replaceAll("\n", "");
                    listOfPeers.add(dataPeer);
                    returnPeers(); //manda pra todo mundo
                    //onlinePeersGarantee();
                }
                else if(msgSplit[0].equals("MASTER_PEER") && msgSplit[1].equals("DISCONNECT")){
                    //cliente desconectou
                    System.out.println("CLIENTE DESCONECTANDO");
                    listOfPeers.remove(msgSplit[2].substring(0, msgSplit[2].length()-2));
                    //onlinePeersGarantee();
                }
                else if(msgSplit[0].equals("MASTER_PEER") && msgSplit[1].equals("UPDATE\n\n")){
                    //atualizando peers online
                    System.out.println("RESPONDENDO OS PEERS ONLINE");
                    returnPeers(client.getInetAddress().toString().replaceAll("/",""));
                    //onlinePeersGarantee();
                }
                else if(msgSplit[0].equals("RECV_MSG")){
                    listOfPeers.getByID(Integer.parseInt(msgSplit[1])).status = "ONLINE";
                }

                client.close();
                System.out.println("Fim da conexao!");
                
            } //fim do while
            
        }catch(Exception e){
            System.out.println("Falha no masterpeer: " + e);
        }
        
    } //FIM SERVIDOR

    public static void returnPeers(){
        if(!listOfPeers.isEmpty()){
            String msg = "PEER_GROUP ";
            for(int i = 0; i < listOfPeers.size(); i++){
                msg += listOfPeers.get(i).id + "," +
                        listOfPeers.get(i).name.replaceAll("\n", "") + "," +
                        listOfPeers.get(i).ip.replaceAll("\n", "") + "," +
                        listOfPeers.get(i).status + "," +
                        listOfPeers.get(i).key + "," + "\n";
            }
            msg += "\n\n";
            
						//System.out.println(msg);
			
            for(int i = 0; i < listOfPeers.size(); i++){
                System.out.println("Sending peers to " + listOfPeers.get(i).name);
                answer(listOfPeers.get(i).ip, msg);
            }
        }
    }
    
    public static void returnPeers(String ip){
        
				if(!listOfPeers.isEmpty()){
            
            String msg = "PEER_GROUP ";
            for(int i = 0; i < listOfPeers.size(); i++){
                msg += listOfPeers.get(i).id + "," +
                        listOfPeers.get(i).name.replaceAll("\n", "") + "," +
                        listOfPeers.get(i).ip.replaceAll("\n", "") + "," +
                        listOfPeers.get(i).status + "," +
                        listOfPeers.get(i).key + "," + "\n";
            }
            msg += "\n\n";  
            answer(ip, msg);
        }
    }
    
    private static void answer(String ip, String msg){
        try{
						SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            Socket client = factory.createSocket(ip, 6991);
						
						//Socket client = new Socket(ip, 6991);
            ObjectOutputStream saida = new ObjectOutputStream(client.getOutputStream());
            saida.flush();
            saida.writeUTF(msg);
            saida.close();
        }catch(Exception e){
            System.out.println("Falha em responder ao aplicativo cliente: " + e);
        }
    }
    
    
    
    private static void onlinePeersGarantee(){
    	/*
        try{
            String msg = "MASTER_PEER UPDATE ";
            String listip[] = new String[listOfPeers.size()];
            ArrayList<String> toRemove = new ArrayList<>();
            
            for(int i = 0; i < listOfPeers.size(); i++){
                if(listOfPeers.get(i).status.equals("OFFLINE")){
                    //se já foi enviado uma msg e não retornou, remove da lista
                    toRemove.add(listOfPeers.get(i).name);
                }
                else{
                    listip[i] = listOfPeers.get(i).ip;
                    msg += "(" + listOfPeers.get(i).id + ","
                            + listOfPeers.get(i).name + ","
                            + listOfPeers.get(i).ip + ","
                            + listOfPeers.get(i).status + ","
                            + listOfPeers.get(i).key + ")\n";
                    listOfPeers.get(i).status = "OFFLINE"; //envia uma vez, offline, uma segunda remove
                }
            }
            msg += "\n\n";
            
            for(String rem : toRemove){
                listOfPeers.remove(rem);
            }
            
            for(String ip : listip){
                Socket client = new Socket(ip, 6991);
                ObjectOutputStream saida = new ObjectOutputStream(client.getOutputStream());
                saida.flush();
                saida.writeUTF(msg);
                saida.close();
            }
        }catch(Exception e){
            System.out.println("Falha em verificar consistencia de cliente conectado: " + e);
        }*/
    }
    
    
}
