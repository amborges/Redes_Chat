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
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private String userName; //meu nome
    PeerData onlineFriends; //lista de amigos online
    ArrayList<String> listaAmigos; //nomes da galera que estão onlines
    
    private ArrayList<String> IPs; //lista de IPS, é string pois uso o InetAddress
                                   //pra gerar o ip, formato esperado "x.x.x.x"
    
    
    
    private final ToggleGroup group; //armazena os radio button
    private ServerLister serverLister; //servidor para iniciar um chat novo
    
    
    
    private ArrayList<String> openChat; //lista de chats abertos
    private int porta; //porta disponivel para conversa
    
    private LinkedList<SingleChat.Peers> listaPeers;
    
    
    
    
    //esse método é para iniciar conversa com alguém, na verdade
    ListaAmigos(String setName, String setPass, LinkedList<SingleChat.Peers> listaPeers){
        userName = setName;
        onlineFriends = new PeerData();
        listaAmigos = new ArrayList<String>();
        
        IPs = new ArrayList<String>();
        //IPs.add("169.254.241.240"); //remover essa linha, é usada para testes
        group = new ToggleGroup();
        //serverLister = new ServerLister(userName, this);
        //serverLister.start();
        
        openChat = new ArrayList<String>();
        porta = 6991; //porta inicial
        
        this.listaPeers = listaPeers;
        
        theMatrix = new Servidor(userName, setPass, this);
        theMatrix.start();
    }
    
    ListaAmigos(String ip){
        System.out.println("IP:" +ip);
        IPs = new ArrayList<String>();
        IPs.add(""+ip+""); //remover essa linha, é usada para testes
        group = new ToggleGroup();
        //serverLister = new ServerLister(userName, this);
        //serverLister.start();
        
        openChat = new ArrayList<String>();
        porta = 20000; //porta inicial
    }

    public ListaAmigos() {
        this.group = null;
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
        //serverLister = old.serverLister;
        //serverLister.updateListaAmigos(this);
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
                /*ExibeAmigos exibeAmigos = new ExibeAmigos(listaPeers);
                Stage sndStage = new Stage();
                try {
                    //listaAmigos.start(sndStage);
                    exibeAmigos.start(sndStage);
                } catch (Exception ex) {
                    Logger.getLogger(ListaAmigos.class.getName()).log(Level.SEVERE, null, ex);
                }
                */
                try{
                    String msgT = "MASTER_PEER CONNECT " + userName + "\n\n";
                    Socket client = new Socket(SingleChat.IPSERVIDOR, SingleChat.DOORSERVIDOR);
                    ObjectOutputStream sender = new ObjectOutputStream(client.getOutputStream());
                    sender.flush();
                    sender.writeUTF(msgT);
                    sender.close();
                }catch(Exception ex){
                    System.out.println("Erro ao requisitar peers : " + ex);
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
                
                if(!onlineFriends.get(who).inChat()){
                    try{
                        Stage sndStage = new Stage();
                        onlineFriends.get(who).startChat(sndStage, userName);
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
                theMatrix.finalize();
                System.exit(1);
            }
        });
        grid.add(closeChat, 2, 0);
        
        //ArrayList<RadioButton> radios = new ArrayList<RadioButton>();
        
        //Carrega a lista toda de IPs
        /*for(int i = 0; i < IPs.size(); i++){
            RadioButton radioButton = new RadioButton(IPs.get(i));
            radios.add(radioButton);
            radios.get(i).setToggleGroup(group);
            grid.add(radios.get(i), 1, i + 1);
        }*/
        
        //private void inicializarComboBoxGrandeArea() throws JAXBException {
        ObservableList<String> onlineFriends = FXCollections.observableArrayList();
        onlineFriends.addAll(listaAmigos);
        
        ComboBox cbList = new ComboBox();
        cbList.setItems(onlineFriends);
        cbList.valueProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object grandeAreaAnterior, Object grandeAreaNova) {
                //if (grandeAreaNova != null) {
                    cbList.setItems(onlineFriends);
                //}
            }
            
        });
        
        grid.add(cbList, 1, 2);
    
        
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
    
    public void iniciaConversa(SingleChat.Peers peer){
        IPs = new ArrayList<String>();
        IPs.add(peer.ip);
        
        //serverLister = new ServerLister(peer.nome, this);
        //serverLister.start();
        
        openChat = new ArrayList<String>();
        porta = peer.porta; //porta inicial
        
        
        
        /*
        if(!programa.has(who)){
            int friendDoor = 20000; //porta aleatória, definir corretamente depois
            JanelaChat newWindow = new JanelaChat(userName, who, programa.getPorta(), friendDoor, who, programa);
            Stage sndStage = new Stage();
            newWindow.start(sndStage);
            programa.include(who); 
        }*/
        
    }
    
    
    
    public void talkto(String friendIP, String msg){
        //manda a msg pra janela certa
    }
    public void reload(String peer[]){
        //atualiza a lista de amigos onlines
        //cada array é no formato
        //<PEER_ID>,<PEER_NAME>,<PEER_IP>,<PEER_STATUS>,<PEER_KEY>
        System.out.println(peer[0]);
        if(!onlineFriends.isEmpty()){
            onlineFriends.clear();
            listaAmigos.clear();
        }
        
        for(int i = 0; i < peer.length-1; i++){
            onlineFriends.add(peer[i]);
            listaAmigos.add(onlineFriends.get(i).name);
        }
    }
}
