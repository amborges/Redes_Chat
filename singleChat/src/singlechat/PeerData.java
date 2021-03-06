/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package singlechat;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import javafx.stage.Stage;

/**
 *
 * @author alex
 */
public class PeerData {

    public PeerData() {
        this.peer = new ArrayList<>();
    }
    //Vai ser a nossa struct, para armazenar os dados:
    //<PEER_ID>,<PEER_NAME>,<PEER_IP>,<PEER_STATUS>,<PEER_KEY>
    //Basicamente ele vai ter algumas operações similares ao ArrayList<>
    
    public class Peer{
            //atributos de um peer amigo
        public int id;
        public String name;
        public String ip;
        public String status;
        public char key[];
        public String friendIP;
        public String certificate;
        public boolean inChat;
        private JanelaChat chat;
        
        public Peer(){};
        
        public void startChat(Stage stage, ListaAmigos setParent){
            try{
                inChat = true;
                //chat = new JanelaChat(setParent, this);
                //chat.start(stage);
            } catch (Exception e){
                System.out.println("Falha ao abrir chat com " + name + " : " + e);
            }
        }
        public void automaticStartChat(Stage stage, ListaAmigos setParent){

            try{
                inChat = true;
                chat = new JanelaChat(setParent, this);
                chat.start(stage);
            } catch (Exception e){
                System.out.println("Falha ao abrir chat com " + name + " : " + e);
            }
        }
        public void closeChat(){
            inChat = false;
            chat = null;
        }
        public JanelaChat getChat(){
            if(inChat)
                return chat;
            else
                return null;
        }
        public boolean inChat(){
            return inChat;
        }
        public void sendText(String msg){
            chat.listened(msg);
        }
        	
        public FileInputStream certificate_str2file(){
            //Dado uma versão do certificado em string, cria e retorna um arquivo localmente
            try{
                FileOutputStream fos = new FileOutputStream("certificados/" + id + ".cert");
                char cc[] = certificate.toCharArray();
                for(char aaa : cc){
                    fos.write((byte)aaa);
                }
                fos.close();
                return new FileInputStream("certificados/" + id + ".cert");
            }catch(Exception e){
                System.out.println("Falha ao criar certificado local: " + e);
            }
            return null;
        }
    
    public void setNameAndID(){
			if(certificate != null){
				try{
                                    certificate_tmp();
                                    KeyStore ks = KeyStore.getInstance("JKS");
                                    
                                    ks.load(new FileInputStream("certificados/tmp.cert"), key);
                                    
                                    name = ks.aliases().nextElement();
                                    id = name.hashCode();
                                    if(id < 0) id *= -1;
				}catch(Exception e){
					System.out.println("Falta ao setar nome e id do peer " + name);
				}
			}
		}
    
    public void certificate_tmp(){
                    //Dado uma versão do certificado em string, cria e retorna um arquivo localmente
                    try{
                        FileOutputStream fos = new FileOutputStream("certificados/tmp.cert");
                        
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
    
    ArrayList<Peer> peer;
    
    public void add(String certificado, String senha, String ip){
        Peer aux 			= new Peer();
        aux.ip = ip;
        aux.status  =   "ONLINE";
        aux.key = senha.toCharArray();
        aux.certificate = certificado;

        aux.setNameAndID();
        aux.certificate_str2file();

        peer.add(aux);
        
        System.out.println("Amigo " + aux.name + " foi adicionado");
    }
    
    
    public void remove(String name){
        for(Peer peer1 : peer){
            if(peer1.name.equals(name)){
                File f = new File("certificate/"+peer1.certificate);
                f.delete();
                peer.remove(peer1);
            }
        }
    }
    
    public boolean contains(String name){
        for(Peer peer1 : peer){
            if(peer1.name.equals(name))
                return true;
        }
        return false;
    }
    
    public Peer get(String name){
        for(Peer peer1 : peer){
            if(peer1.name.equals(name))
                return peer1;
        }
        return null;
    }
    
    public Peer getByID(int id){
        for(Peer peer1 : peer){
            if(peer1.id == id){
                System.out.println("Achei o amigo " + peer1.name + "para contato, ip... " + peer1.ip);
                return peer1;
            }
        }
        return null;
    }
    
    public boolean hasID(int id){
        for (Peer peer1 : peer) {
            if (peer1.id == id) {
                return true;
            }
        }
        return false;
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
    
    public void printPeers(){
        System.out.println("PRINTANDO PEERS:");
        for(Peer a : peer){
            System.out.println(a.name + ": " + a.ip);
        }
    }
}
