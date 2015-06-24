/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package singlechat;

import java.io.FileInputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 *
 * @author alex
 */
public class ListaAmigos extends Application{
    
    /*
    *   Esta classe é onde se encontram os contatos ativos para iniciar uma conversa
    *
    *   É preciso criar uma função que atualiza a lista de IPs do Servidor
    */
    
    
    private Servidor theMatrix; //Servidor que tudo ouve
    static public String USERNAME; //meu nome
    static public int USERID; //meu ID
    static public PeerData onlineFriends; //lista de amigos online
    static public String meuCertificado;
    
    
    //esse método é para iniciar conversa com alguém, na verdade
    ListaAmigos(String setName, String setPass){
        USERNAME = setName;
        USERID = setName.hashCode();
        onlineFriends = new PeerData();
        theMatrix = new Servidor(USERNAME, setPass, this);
        theMatrix.start();
    }
   
    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        /////////////////////////////////////////
        //BOTOES SUPERIORES
        /////////////////////////////////////////
        
        Button atualizaLista = new Button();
        atualizaLista.setText("Obter Lista de Usuários Ativos");
        atualizaLista.setOnAction(new EventHandler<ActionEvent>() {
            /*
            *   Sempre que precisamos atualizar a lista de amigos
            *   vamos recarregar esta janela, destruindo ela e criando uma nova
            */
            @Override
            public void handle(ActionEvent e) {
                try{
                    String msgT = "MASTER_PEER UPDATE\n\n";
                    System.out.println(SingleChat.IPSERVIDOR);
                    Servidor.returnToClient(SingleChat.IPSERVIDOR, msgT);
                    //Depois de obter a lista dos amigos, o atributo onlineFriends
                    //é automaticamente atualizado, e se abre a janela pra escolher o amigo
                    ExibeAmigos exibeAmigos = new ExibeAmigos(onlineFriends, ListaAmigos.this);
                    Stage sndStage = new Stage();
                    exibeAmigos.start(sndStage);
                }catch(Exception ex){
                    System.out.println("Erro ao requisitar peers : " + ex);
                }
            }
        });
        grid.add(atualizaLista, 0, 0);
        
        Button closeChat = new Button();
        closeChat.setText("Fechar Chat");
        closeChat.setOnAction(new EventHandler<ActionEvent>() {
            /*
            *   Encerra o programa
            */
            @Override
            public void handle(ActionEvent e) {
                theMatrix.finalize();
                System.exit(1);
            }
        });
        grid.add(closeChat, 2, 0);
        
        
        /////////////////////////////////////////
        //RENDERIZANDO TUDO!!
        /////////////////////////////////////////
        Scene scene = new Scene(grid, 800, 600);
        primaryStage.setTitle("Welcome " + USERNAME + " to singleChat!");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    public void remove(String s){
        //uma janela de chat foi fechada
        onlineFriends.get(s).closeChat();
    }
    
    
    public boolean has(String s){
        //retorna true se existe uma janela de chat com esse contato
        return onlineFriends.get(s).inChat();
    }
    
    public void iniciaConversa(PeerData.Peer peer){
        if(!onlineFriends.getByID(peer.id).inChat()){ //se há uma janela aberta
          // onlineFriends.getByID(id).automaticStartChat(this);
           Platform.runLater(new Runnable(){
               @Override
               public void run(){
                   //ListaAmigos.this.iniciaConversa(ListaAmigos.this.onlineFriends.getByID(id));
                   onlineFriends.getByID(peer.id).automaticStartChat(new Stage(), ListaAmigos.this);
                   System.out.println("obaa ");
               }
           });
        }
    }
    
    public void talkto(String friendID, String msg){
        //manda a msg pra janela certa
        int id = Integer.parseInt(friendID);
        
        if(!onlineFriends.getByID(id).inChat()){ //se há uma janela aberta
          // onlineFriends.getByID(id).automaticStartChat(this);
           Platform.runLater(new Runnable(){
               @Override
               public void run(){
                   //ListaAmigos.this.iniciaConversa(ListaAmigos.this.onlineFriends.getByID(id));
                   onlineFriends.getByID(id).automaticStartChat(new Stage(), ListaAmigos.this);
                   onlineFriends.getByID(id).sendText(msg);
               }
           });
        }
        else
            onlineFriends.getByID(id).sendText(msg);
    }
    
    public void reload(String msg){
        //atualiza a lista de amigos onlines
        //cada array é no formato
        //<PEER_IP>\n<PEER_CERTIFICADO>
        //usando tb o peer_key entre ambos
        //System.out.println(peer[0]);
        if(!onlineFriends.isEmpty()){
            onlineFriends.clear();
        }
        
        String peer[] = msg.split("\n");
        
        String cert, pass, ip;
        
        for(int i = 0; i < peer.length; i = i + 3){
            ip = peer[i];
            pass = peer[i+1];
            cert = URLDecoder.decode(peer[i+2]);
            onlineFriends.add(cert, pass, ip);
        }
    }
    
    public void confirmOnline(String peer[]){
        for(String p : peer){
            String a[] = p.split(",");
            if(a.length > 2){
                if(USERID == Integer.parseInt(a[0])){
                    Servidor.returnToClient(SingleChat.IPSERVIDOR, "RECV_MSG " + USERID + "\n\n");
                    break;
                }
            }
        }
    }
    
    public void openChat(String id){
        System.out.println("entrou em openchat");
        int theFriendID = Integer.parseInt(id);
        if(onlineFriends.hasID(theFriendID)){
            onlineFriends.getByID(theFriendID).startChat(new Stage(), this);
            String recmsg = "ACCEPT_TALKING " + id + "\n\n";
            Servidor.returnToClient(onlineFriends.getByID(theFriendID).friendIP, recmsg);
        }
    }
    
    static public String fromFileToString(String filename){
        //Dado um aquivo qualquer, transforma ele em uma string
        try{
            FileInputStream fis = new FileInputStream(filename);
            
            ArrayList<Byte> b = new ArrayList<>();
            
            byte a[] = new byte[1];
            char c[];
            
            while(fis.read(a) != -1){
                b.add(a[0]);
            }
            
            fis.close();
            
            c = new char[b.size()];
            int i = 0;
            for(Byte B : b){
                c[i++] = (char)B.byteValue();
            }
            
            return new String(c);
        } catch(Exception e){
            System.out.println("Falha ao gerar String apartir de um arquivo: " + e);
        }
        return null;
    }
}
