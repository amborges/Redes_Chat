/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package singlechat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    
    private String userName; //meu nome
    private ArrayList<String> IPs; //lista de IPS, é string pois uso o InetAddress
                                   //pra gerar o ip, formato esperado "x.x.x.x"
    private final ToggleGroup group; //armazena os radio button
    private ServerLister serverLister; //servidor para iniciar um chat novo
    
    private ArrayList<String> openChat; //lista de chats abertos
    private int porta; //porta disponivel para conversa
    
    private LinkedList<SingleChat.Peers> listaPeers;
    
    
    
    
    //esse método é para iniciar conversa com alguém, na verdade
    ListaAmigos(String s, LinkedList<SingleChat.Peers> listaPeers){
        userName = s;
        IPs = new ArrayList<String>();
        IPs.add("169.254.241.240"); //remover essa linha, é usada para testes
        group = new ToggleGroup();
        serverLister = new ServerLister(userName, this);
        serverLister.start();
        
        openChat = new ArrayList<String>();
        porta = 20000; //porta inicial
        
        this.listaPeers = listaPeers;
    }

    public ListaAmigos() {
        this.group = null;
    }
   
    
    
    public void iniciaConversa(SingleChat.Peers peer){
        IPs = new ArrayList<String>();
        IPs.add(peer.ip);
        
        serverLister = new ServerLister(peer.nome, this);
        serverLister.start();
        
        openChat = new ArrayList<String>();
        porta = peer.porta; //porta inicial
        
    }
    
    ListaAmigos(ListaAmigos old){
        /*
        * Este construtor é obrigatório, pois não descobri como fazer um
        * campo de atualização dinâmica, logo, é preciso fazer uma cópia da janela
        * e renderizar ela novamente
        */
        userName = old.userName;
        IPs = old.IPs;
        group = new ToggleGroup();
        openChat = old.openChat;
        porta = old.porta;
        serverLister = old.serverLister;
        serverLister.updateListaAmigos(this);
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
                //ListaAmigos listaAmigos = new ListaAmigos(ListaAmigos.this);
                ExibeAmigos exibeAmigos = new ExibeAmigos(listaPeers);
                Stage sndStage = new Stage();
                try {
                    //listaAmigos.start(sndStage);
                    exibeAmigos.start(sndStage);
                } catch (Exception ex) {
                    Logger.getLogger(ListaAmigos.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        });
        grid.add(atualizaLista, 0, 0);
        
        Button btnChat = new Button();
        btnChat.setText("Iniciar Chat");
        btnChat.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String who = ((RadioButton)group.getSelectedToggle()).getText();
                
                if(!has(who)){
                    int friendDoor = 20000; //porta aleatória, é preciso capturar a porta corretamente
                    try{
                                            //String name, String numIP, int myporta, int friendporta, String friendname
                        JanelaChat newWindow = new JanelaChat(userName, who, getPorta(), friendDoor, who, ListaAmigos.this);
                        Stage sndStage = new Stage();
                        newWindow.start(sndStage);
                        include(who);
                    } catch(Exception ex){
                        System.out.println("Falha ao abrir janela de chat : " + ex);
                    }
                }
            }
        });
        grid.add(btnChat, 1, 0);
            
        Button closeChat = new Button();
        closeChat.setText("Fechar Chat");
        closeChat.setOnAction(new EventHandler<ActionEvent>() {
            /*
            *   Encerra o programa
            */
            @Override
            public void handle(ActionEvent e) {
                System.exit(1);
            }
        });
        grid.add(closeChat, 2, 0);
        
        ArrayList<RadioButton> radios = new ArrayList<RadioButton>();
        
        //Carrega a lista toda de IPs
        for(int i = 0; i < IPs.size(); i++){
            RadioButton radioButton = new RadioButton(IPs.get(i));
            radios.add(radioButton);
            radios.get(i).setToggleGroup(group);
            grid.add(radios.get(i), 1, i + 1);
        }
        
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
        if(openChat.remove(s))
            System.out.println(s + " removido com sucesso!");
        else
            System.out.println("Não foi possível remover " + s);
    }
    
    public void include(String s){
        //uma janela de chat foi aberta
        openChat.add(s);
    }
    
    public boolean has(String s){
        //retorna true se existe uma janela de chat com esse contato
        return openChat.contains(s);
    }
    
    public int getPorta(){
        //retorna a porta disponível para abrir um novo chat
        int r = porta;
        porta ++;
        return r;
    }
}
