/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package singlechat;

import java.awt.event.ActionListener;
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
import javax.swing.JButton;

/**
 *
 * @author alex
 */
public class JanelaChat extends Application{
    private String userName;
    TextArea chatHistory;
    TextArea msg;
    
    JanelaChat(String name){
        userName = name;
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
        Button closeChat = new Button();
        closeChat.setText("Start Chat");
        closeChat.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                msg.setEditable(true);
            }
        });
        grid.add(closeChat, 1, 1);
        
        Button startChat = new Button();
        startChat.setText("Close Chat");
        startChat.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                msg.setEditable(false);
            }
        });
        grid.add(startChat, 2, 1);
        
        Button closeProgram = new Button();
        closeProgram.setText("Close singleChat");
        closeProgram.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                System.exit(0);
            }
        });
        grid.add(closeProgram, 3, 1);
        
        /////////////////////////////////////////
        //AREA TEXTUAL
        /////////////////////////////////////////
        msg = new TextArea();
        msg.setPrefSize(12, 6);
        msg.setEditable(false);
        msg.setOnKeyPressed(new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent k){
                if (k.getCode().equals(KeyCode.ENTER)) {
                    updateTextArea();
                }
            }
        });
        grid.add(msg, 1, 3, 2, 1);
        
        chatHistory = new TextArea();
        chatHistory.setMinHeight(150);
        chatHistory.setEditable(false);
        grid.add(chatHistory, 1, 2, 2, 1);
     
        Button send = new Button();
        send.setText("Send");
        send.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                updateTextArea();
            }
        });
        grid.add(send, 3, 3);
        
        /////////////////////////////////////////
        //RENDERIZANDO TUDO!!
        /////////////////////////////////////////
        Scene scene = new Scene(grid, 800, 600);
        primaryStage.setTitle("Welcome " + userName + " to singleChat!");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    private void updateTextArea(){
        if(!msg.getText().isEmpty()){
            String hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":"
                    + Calendar.getInstance().get(Calendar.MINUTE) + ":"
                    + Calendar.getInstance().get(Calendar.SECOND);
            String msgT = msg.getText();
            
            String textFinal = userName + "[" + hour + "]: " + msg.getText() + "\n";
                //temos q otimizar a linha de baixo, pensando no caso de uma
                //conversa longa, vai começar a ficar lento, deveria ter algo
                //apenas para fazer um append/concat no textarea
            chatHistory.setText(chatHistory.getText().concat(textFinal));
            msg.clear();
        }
    }
}
