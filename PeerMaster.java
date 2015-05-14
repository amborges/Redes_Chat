
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;



public class PeerMaster{
    
    static class Peers{
        private int id;
        private String name;
        private String ip;
        private boolean status;
        private String key;

        Peers(String setid, String setip){
            id = setid.hashCode();
            name = setid;
            ip = setip;
            status = true;
            key = null;
        }

        Peers(String setid, String setip, String setkey){
            id = setid.hashCode();
            name = setid;
            ip = setip;
            status = true;
            key = setkey;
        }
        public int getID(){
            return id;
        }
        public String getName(){
            return name;
        }
        public String getIP(){
            return ip;
        }
        public boolean getStatus(){
            return status;
        }
        public String getKey(){
            return key;
        }
    }
    
    static private ArrayList<Peers> listOfPeers;
    
    public static void main(String[] args) {
        ServerSocket server;
        Socket client;
        
        try{
            server = new ServerSocket(6991);
            listOfPeers = new ArrayList<Peers>();
            while(true){
				System.out.println("Antes da conexao!");
                client = server.accept();
                System.out.println("Depois da conexao!");
				ObjectInputStream entrada = new ObjectInputStream(client.getInputStream());
				String msg = entrada.readUTF();
				
                String msgSplit[] = msg.split(" ");
                System.out.println(msg);
				if(msgSplit[0].equals("MASTER_PEER") && msgSplit[1].equals("CONNECT")){
                    //Cliente conectou e requisita a lista de peers
                    System.out.println("CLIENTE CONECTOU - RESPONDENDO OS PEERS ONLINE");
                    listOfPeers.add(new Peers(msgSplit[2], client.getInetAddress().toString().replaceAll("/","")));
                    System.out.println(client.getInetAddress().toString());
                    returnPeers(client.getInetAddress().toString().replaceAll("/",""));

				}
                else if(msgSplit[0].equals("MASTER_PEER") && msgSplit[1].equals("DISCONNECT")){
                    //cliente desconectou
                    System.out.println("CLIENTE DESCONECTANDO");
                    listOfPeers.remove(msgSplit[2]);
                }
                else if(msgSplit[0].equals("MASTER_PEER") && msgSplit[1].equals("UPDATE")){
                    //atualizando peers online
                    System.out.println("RESPONDENDO OS PEERS ONLINE");
                    returnPeers(client.getInetAddress().toString().replaceAll("/",""));
                }
                entrada.close();
                client.close();
            }
			
            
        }catch(Exception e){
            System.out.println("Falha na execução do servidor: " + e);
        }
    }
    
    public static void returnPeers(String ip){
		
		if(!listOfPeers.isEmpty()){
            String msg = "";
            for(int i = 0; i < listOfPeers.size(); i++){
                msg += listOfPeers.get(i).getID() + " " +
                        listOfPeers.get(i).getName() + " " +
                        listOfPeers.get(i).getIP() + " " +
                        listOfPeers.get(i).getStatus() + " " +
                        listOfPeers.get(i).getKey() + " ";
            }
            
            
            try{
				Socket client2 = new Socket(ip, 6992);
				ObjectOutputStream saida = new ObjectOutputStream(client2.getOutputStream());
				saida.flush();
                //ObjectOutputStream saida = new ObjectOutputStream(c.getOutputStream());
                //saida.flush();
                saida.writeUTF(msg);
                saida.close();
            }catch(Exception e){
                System.out.println("Falha ao retornar mensagem ao cliente: " + e);
            }
        }
    }
}