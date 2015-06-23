/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package singlechat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
            System.out.println("entrei em automaticStartChat ");
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
    };
    
    ArrayList<Peer> peer;
    
    public void add(String peerData){
        String pd[] = peerData.split(",");
        Peer aux = new Peer();
        aux.id          = Integer.parseInt(pd[0]);
        aux.name        = pd[1];
        aux.ip          = pd[2];
        aux.status      = pd[3];
        aux.key         = pd[4].toCharArray();
        aux.certificate	= pd[5];
        peer.add(aux);
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
            if(peer1.id == id)
                return peer1;
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
        for(Peer a : peer){
            System.out.println(a.name + ": " + a.id);
        }
    }
}
