/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package singlechat;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Calendar;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 *
 * @author alex
 */
public class JanelaChat extends Application{
    
    /*
    *   Esta classe é a janela de conversa, existe uma para cada contato aberto
    */
    
    private PeerData.Peer friend; //mey amigo
    
    private TextArea chatHistory; //área de conversa
    private TextArea msg; //area de escrever a mensagem
    private ListaAmigos programa; //origem
    
    
    JanelaChat(ListaAmigos setProgram, PeerData.Peer setFriend) throws Exception{
        programa = setProgram;
        friend = setFriend;
    }
    
    @Override
    public void start(Stage primaryStage) {
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        msg = new TextArea();
        msg.setPrefSize(12, 6);
        msg.setOnKeyPressed(new EventHandler<KeyEvent>(){
            /*
            *   Se for pressionado ENTER, ele envia mensagem
            */
            @Override
            public void handle(KeyEvent k){
                if (k.getCode().equals(KeyCode.ENTER)) {
                    updateTextArea(msg);
                }
            }
        });
        grid.add(msg, 1, 2);
        
        chatHistory = new TextArea();
        chatHistory.setMinHeight(150);
        chatHistory.setEditable(false);
        grid.add(chatHistory, 1, 1);
     
        Button send = new Button();
        send.setText("Send");
        send.setOnAction(new EventHandler<ActionEvent>() {
            /*
            *   Se o botão SEND for pressionado, envia a mensagem
            */
            @Override
            public void handle(ActionEvent e) {
                updateTextArea(msg);
            }
        });
        grid.add(send, 2, 2);
        
        Button close = new Button();
        close.setText("Close Chat");
        close.setOnAction(new EventHandler<ActionEvent>() {
            /*
            *   Encerra o chat, encerrando tb o servidor paralelo
            *   Remove tb o nome deste contato da lista de chats abertos
            */
            @Override
            public void handle(ActionEvent e) {
                programa.remove(friend.name);
                primaryStage.close();
            }
        });
        grid.add(close, 2, 3);
        
        Scene scene = new Scene(grid, 800, 600);
        primaryStage.setTitle("Welcome " + ListaAmigos.USERNAME + " to singleChat! :: " + friend.name);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void updateTextArea(TextArea texto){
        /*
        *   Este método atualiza a área de conversação;
        *   Ela é chamada tanto quando se dá um enter quando se aperta um botão
        *   Quando se dá enter ela possui uma falha, o caractere enter fica na
        *   textbox de mensagem. Não consegui remover
        */
        
        if(!texto.getText().isEmpty()){ //campo mensagem não pode estar vazio
            String hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":"
                    + Calendar.getInstance().get(Calendar.MINUTE) + ":"
                    + Calendar.getInstance().get(Calendar.SECOND);
            String msgT = texto.getText();
            
            String textFinal = ListaAmigos.USERNAME + "[" + hour + "]: " + msg.getText() + "\n";
                //temos q otimizar a linha de baixo, pensando no caso de uma
                //conversa longa, vai começar a ficar lento, deveria ter algo
                //apenas para fazer um append/concat no textarea
            chatHistory.setText(chatHistory.getText().concat(textFinal));
            msg.clear();
            try{ //esse try, é pra enviar a mensagem ao amigo
                String msgToFriend = "SEND_MSG " + msgT.length() + " " + ListaAmigos.USERID + " " + msgT + "\n\n";
                Servidor.returnToClient(friend.ip, msgToFriend);
            }catch(Exception e){
                System.out.println("FALHA AO ENVIAR MENSAGEM: " + e);
            }
        }
    }
    
    public void listened(String friendMsg){
        
        /*
        *   Esta classe é acessada pelo servidor paralelo, afim de atualizar
        *   a textarea com a mensagem recebida do contato
        */
        
        String hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":"
                + Calendar.getInstance().get(Calendar.MINUTE) + ":"
                + Calendar.getInstance().get(Calendar.SECOND);

        String textFinal = friend.name + "[" + hour + "]: " + friendMsg + "\n";
            //temos q otimizar a linha de baixo, pensando no caso de uma
            //conversa longa, vai começar a ficar lento, deveria ter algo
            //apenas para fazer um append/concat no textarea
        chatHistory.setText(chatHistory.getText().concat(textFinal));
        //msg.clear();
    }
}
