/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SERVER;

import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *
 * @author alex
 */
public class PeerData {
	//Vai ser a nossa struct, para armazenar os dados:
	//<PEER_ID>,<PEER_NAME>,<PEER_IP>,<PEER_STATUS>,<PEER_KEY>
	//Basicamente ele vai ter algumas operações similares ao ArrayList<>
	
	public class Peer{
		public int id;
		public String name;
		public String ip;
		public String status;
		public char key[];
		public String certificate;
		
		public FileInputStream getCertificate(){
			if(certificate != null){
				try{
					return new FileInputStream("certificados/"+id+".cert");
				}catch(Exception e){
					System.out.println("Falta ao recuperar certificado de " + name);
					return null;
				}
			}
			return null;
		}
                
                public void certificate_str2file(){
                    //Dado uma versão do certificado em string, cria e retorna um arquivo localmente
                    try{
                        FileOutputStream fos = new FileOutputStream("certificados/" + id + ".cert");
                        char cc[] = certificate.toCharArray();
                        for(char aaa : cc){
                            fos.write((byte)aaa);
                        }
                        fos.close();
                        Thread.sleep(1000); //pra dar tempo do arquivo ser criado localmente
                    }catch(Exception e){
                        System.out.println("Falha ao criar certificado: "+e);
                    }
                }
	};
	
	ArrayList<Peer> peer = new ArrayList<Peer>();
	
	public void add(String peerData, String setcertificate){
		String pd[] = peerData.split(",");
		Peer aux 			= new Peer();
		aux.id	  			= Integer.parseInt(pd[0]);
		aux.name			= pd[1];
		aux.ip	  			= pd[2];
		aux.status			= pd[3];
		aux.key                         = pd[4].toCharArray();
                aux.certificate                 = setcertificate;
		aux.certificate_str2file();
		peer.add(aux);
	}
	public void remove(String name){
		int pos = -1;
		for(int i = 0; i < peer.size() - 1; i++){
			if(peer.get(i).name.equals(name)){
				pos = i;
				break;
			}
		}
		
		if(pos != -1){
			File f = new File(peer.get(pos).certificate);
			if(!f.delete())
				System.out.println("Falha ao remover certificado do usuário desconectado");
			peer.remove(pos);
			
		}
	}
	
	public boolean contains(String name){
	
		for(int i = 0; i < peer.size() - 1; i++){
			if(peer.get(i).name.equals(name)){
				return true;
			}
		}
		return false;
	}
	
	public Peer get(String name){
		int pos = -1;
		for(int i = 0; i < peer.size() - 1; i++){
			if(peer.get(i).name.equals(name)){
				pos = i;
				break;
			}
		}
		
		if(pos != -1)
			return peer.get(pos);
		else
			return null;
	}
	public Peer get(int i){
		return peer.get(i);
	}
	
	public boolean isEmpty(){
		return peer.isEmpty();
	}
	
	public void clear(){
		peer.clear();
	}
	
	public int size(){
		return peer.size();
	}
	
	public Peer getByID(int id){
    for(Peer peer1 : peer){
        if(peer1.id == id)
            return peer1;
    }
  	return null;
  }
        
        public Peer getByIP(String ip){
    for(Peer peer1 : peer){
        if(peer1.ip.equals(ip))
            return peer1;
    }
  	return null;
  }
	
	public void printAllPeers(){
      System.out.println("LISTA DOS CONECTADOS");
      for (Peer peer1 : peer) {
          System.out.println(peer1.name);
      }
  }
}
