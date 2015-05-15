/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package singlechat;

import java.util.ArrayList;
import javafx.stage.Stage;

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
        public String key;
        private boolean inChat = false;
        private JanelaChat chat;
        public void startChat(Stage stage, String setUserName){
            try{
                chat = new JanelaChat(setUserName, this);
                inChat = true;
                chat.start(stage);
            } catch (Exception e){
                System.out.println("Falha ao abrir chat : " + e);
            }
        }
        public void closeChat(){
            inChat = false;
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
    };
    
    ArrayList<Peer> peer = new ArrayList<Peer>();
    
    
    public void add(String peerData){
        String pd[] = peerData.split(",");
        Peer aux = new Peer();
        aux.id      = Integer.parseInt(pd[0]);
        aux.name    = pd[1];
        aux.ip      = pd[2];
        aux.status  = pd[3];
        //aux.key     = pd[4];
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
    
}
