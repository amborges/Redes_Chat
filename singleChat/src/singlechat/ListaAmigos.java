/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package singlechat;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
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
    public String userName; //meu nome
    PeerData onlineFriends; //lista de amigos online
    ComboBox cbList; //combobox dos amigos onlines
    Vector vectorFriends; //a ser usado em combinação com o combobox
    
    
    //esse método é para iniciar conversa com alguém, na verdade
    ListaAmigos(String setName, String setPass){
        userName = setName;
        onlineFriends = new PeerData();
        theMatrix = new Servidor(userName, setPass, this);
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
                    Socket client = new Socket(SingleChat.IPSERVIDOR, SingleChat.DOORTEST);
                    ObjectOutputStream sender = new ObjectOutputStream(client.getOutputStream());
                    sender.flush();
                    sender.writeUTF(msgT);
                    sender.close();   
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
        primaryStage.setTitle("Welcome " + userName + " to singleChat!");
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
        try{
            if(!onlineFriends.get(peer.name).inChat()){
                onlineFriends.get(peer.name).startChat(new Stage(), this);
            }
        }catch(Exception e){
            System.out.println("Falha ao abrir chat: " + e);
        }
    }
    
    public void talkto(String friendIP, String msg){
        //manda a msg pra janela certa
        if(onlineFriends.get(friendIP).inChat())
            onlineFriends.get(friendIP).sendText(msg);
        else{
            onlineFriends.get(friendIP).startChat(new Stage(), this);
            onlineFriends.get(friendIP).sendText(msg);
        }
    }
    
    public void reload(String peer[]){
        //atualiza a lista de amigos onlines
        //cada array é no formato
        //<PEER_ID>,<PEER_NAME>,<PEER_IP>,<PEER_STATUS>,<PEER_KEY>
        System.out.println(peer[0]);
        if(!onlineFriends.isEmpty()){
            onlineFriends.clear();
        }
        
        for(int i = 0; i < peer.length-1; i++){
            onlineFriends.add(peer[i]);
        }
    }
}
