/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package singlechat;

import java.util.ArrayList;
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
    private String userName;
    private int IPs[];
    private final ToggleGroup group; //armazena os radio button
    private static ArrayList<String> openChat;
    
    ListaAmigos(String s){
        userName = s;
        IPs = new int[1];
        IPs[0] = 999;
        group = new ToggleGroup();
        openChat = new ArrayList<String>();
    }
    
    ListaAmigos(String s, int list[], ArrayList<String> als){
        userName = s;
        IPs = new int[list.length+1];
        for(int i = 0; i < list.length+1; i++){
            IPs[i] = i; 
        }
        group = new ToggleGroup();
        openChat = als;
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
            @Override
            public void handle(ActionEvent e) {
                int sizeIP = IPs.length;
                ListaAmigos listaAmigos = new ListaAmigos(userName, IPs, openChat);
                Stage sndStage = new Stage();
                listaAmigos.start(sndStage);
                primaryStage.close();
            }
        });
        grid.add(atualizaLista, 0, 0);
        
        Button btnChat = new Button();
        btnChat.setText("Iniciar Chat");
        btnChat.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String who = ((RadioButton)group.getSelectedToggle()).getText();
                
                if(!openChat.contains(who)){
                    JanelaChat newWindow = new JanelaChat(userName, who);
                    Stage sndStage = new Stage();
                    newWindow.start(sndStage);
                    openChat.add(who);
                }
            }
        });
        grid.add(btnChat, 1, 0);
            
        Button closeChat = new Button();
        closeChat.setText("Fechar Chat");
        closeChat.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                System.exit(1);
            }
        });
        grid.add(closeChat, 2, 0);
        
        ArrayList<RadioButton> radios = new ArrayList<RadioButton>();
        
        for(int i = 0; i < IPs.length; i++){
            RadioButton radioButton = new RadioButton("User " + IPs[i]);
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
    
    protected int getIP(int pos){
        return IPs[pos];
    }
    
    public static void remove(String s){
        openChat.remove(s);
    }
}