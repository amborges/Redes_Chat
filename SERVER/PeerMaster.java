package SERVER;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;



public class PeerMaster{
	
    static private PeerData listOfPeers;

    public static void main(String[] args) {
        ServerSocket server;
        Socket client;

        try{
            server = new ServerSocket(6991);
            listOfPeers = new PeerData();
            while(true){
                System.out.println("Antes da conexao!");
                client = server.accept();
                System.out.println("Conexao aceita!");
                ObjectInputStream entrada = new ObjectInputStream(client.getInputStream());
                String msg = entrada.readUTF();
                entrada.close();

                String msgSplit[] = msg.split(" ");
                System.out.println(msg);
                if(msgSplit[0].equals("MASTER_PEER") && msgSplit[1].equals("CONNECT")){
                    //Cliente conectou e requisita a lista de peers
                    String clientIP = client.getInetAddress().toString().replaceAll("/","");
                    msgSplit[2] = msgSplit[2].substring(0, msgSplit[2].length()); //removendo os \n\n

                    System.out.println("CONECTANDO NOVO CLIENTE!");
                    String dataPeer = msgSplit[2].hashCode() + "," + 
                        msgSplit[2].replaceAll("\n","") + "," + clientIP + 
                        ",true," + msgSplit[3].replaceAll("\n", "");
                    listOfPeers.add(dataPeer);

                    returnPeers(); //manda pra todo mundo
                }
                else if(msgSplit[0].equals("MASTER_PEER") && msgSplit[1].equals("DISCONNECT")){
                    //cliente desconectou
                    System.out.println("CLIENTE DESCONECTANDO");

                    listOfPeers.remove(msgSplit[2].substring(0, msgSplit[2].length()-2));
                }
                else if(msgSplit[0].equals("MASTER_PEER") && msgSplit[1].equals("UPDATE\n\n")){
                    //atualizando peers online
                    System.out.println("RESPONDENDO OS PEERS ONLINE");
                    returnPeers(client.getInetAddress().toString().replaceAll("/",""));
                }

                client.close();
                System.out.println("Fim da conexao!");
            }	
        }catch(Exception e){
                System.out.println("Falha na execução do servidor: " + e);
        }
    }

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
            
            for(int i = 0; i < listOfPeers.size(); i++){
                System.out.println("Sending peers to " + listOfPeers.get(i).name);
                answer(listOfPeers.get(i).ip, msg);
            }
        }
    }
    
    public static void returnPeers(String ip){
        if(!listOfPeers.isEmpty()){
            listOfPeers.printAllPeers();
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
            Socket client = new Socket(ip, 6991);
            ObjectOutputStream saida = new ObjectOutputStream(client.getOutputStream());
            saida.flush();
            saida.writeUTF(msg);
            saida.close();
        }catch(Exception e){
            System.out.println("Falha em responder ao aplicativo cliente: " + e);
        }
    }
}
