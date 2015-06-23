package SERVER;



import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.security.KeyStore;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;


public class PeerMaster{
	//modificado por lucas
    static private PeerData listOfPeers;
    static private char     SERVERPASSWORD[]    = "masterpeerpass".toCharArray();
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
                    //MASTER_PEER CONNECT <CERT_SIZE> \n <PEER_CERT> \n\n
                    //Cliente conectou e requisita a lista de peers
                    String clientIP = client.getInetAddress().toString().replaceAll("/","");

                    msgSplit[2] = msgSplit[2].substring(0, msgSplit[2].length()); //removendo os \n\n

                    System.out.println("CONECTANDO NOVO CLIENTE!");
                    
                        //<id>,<name>,<ip>,<status>,<certificate>
                    int idhashed = msgSplit[2].hashCode();
                    if(idhashed < 0) idhashed *= -1;
                    String dataPeer = idhashed + "," + 
                        msgSplit[2].replaceAll("\n","") + "," + clientIP + 
                        ",ONLINE," + msgSplit[3].replaceAll("\n", "") + "";
                    listOfPeers.add(dataPeer, msgSplit[4]);
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
                answer(listOfPeers.get(i), msg);
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
            answer(listOfPeers.getByIP(ip), msg);
        }
    }
    
    private static void answer(PeerData.Peer peer, String msg){
        try{
            KeyStore ks = KeyStore.getInstance("JKS");
            //ks.load(peer.getCertificate(), new String(peer.key));
            ks.load(peer.getCertificate(), peer.key);
            
                //cria um caminho de certificação baseado em X509
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, peer.key);
            
                //cria um SSLContext segundo o protocolo informado
            SSLContext contextoSSL = SSLContext.getInstance("SSLv3");
            contextoSSL.init(kmf.getKeyManagers(), null, null);
            
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
                        
            tmf.init(ks);
            TrustManager tms[] = tmf.getTrustManagers();
            
            KeyManagerFactory keymf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
            
            keymf.init(ks, peer.key);
            KeyManager kms[] = keymf.getKeyManagers();
            
            contextoSSL = SSLContext.getInstance("SSL");
            contextoSSL.init(kms, tms, null);
            
            SSLSocketFactory ssf = contextoSSL.getSocketFactory();
            
            SSLSocket saida = (SSLSocket) ssf.createSocket(peer.ip, 6991);
            
            ObjectOutputStream sender = new ObjectOutputStream(saida.getOutputStream());
            sender.flush();
            sender.writeUTF(msg);
            sender.close();
            
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
