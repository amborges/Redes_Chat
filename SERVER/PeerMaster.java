package SERVER;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;



public class PeerMaster{
	
	static private PeerData listOfPeers;

	public static void main(String[] args) {
		ServerSocket server;
		Socket client;
	
		try{
			server = new ServerSocket(6969);//6991);
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
					System.out.println("CLIENTE CONECTOU - RESPONDENDO OS PEERS ONLINE");
                                        String clientIP = client.getInetAddress().toString().replaceAll("/","");
                                        if(!listOfPeers.contains(msgSplit[2])){
                                            System.out.println("DOEST EXIST!");
                                            listOfPeers.add(msgSplit[2].hashCode() + "," + 
                                                msgSplit[2] + "," + clientIP + ",true");
                                        }
					returnPeers(clientIP);
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
				
				client.close();
				System.out.println("Fim da conexao!");
			}	
		}catch(Exception e){
			System.out.println("Falha na execução do servidor: " + e);
		}
	}

	public static void returnPeers(String ip){

		if(!listOfPeers.isEmpty()){
			String msg = "PEER_GROUP ";
			for(int i = 0; i < listOfPeers.size(); i++){
				msg += listOfPeers.get(i).id + "," +
						listOfPeers.get(i).name.replaceAll("\n", "") + "," +
						listOfPeers.get(i).ip.replaceAll("\n", "") + "," +
						listOfPeers.get(i).status + "," + "\n";
			}
			msg += "\n\n";
		
			try{
				Socket client2 = new Socket(ip, 6991);
				ObjectOutputStream saida = new ObjectOutputStream(client2.getOutputStream());
				saida.flush();
				//ObjectOutputStream saida = new ObjectOutputStream(c.getOutputStream());
				//saida.flush();
				System.out.println("   :: " + msg);
				saida.writeUTF(msg);
				saida.close();
			}catch(Exception e){
				System.out.println("Falha ao retornar mensagem ao cliente: " + e);
			}
		}
	}
}
