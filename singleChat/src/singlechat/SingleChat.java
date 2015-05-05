/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package singlechat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author alex
 */
public class SingleChat extends Application {
    
    /*
    *   Esta classe é a janela inicial do aplicativo
    *   Ela só abre, pede usuário e senha, e encerra
    */
    private static final String IPSERVIDOR = "169.254.241.240";
    
    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        Text scenetitle = new Text("Sign In");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label userName = new Label("User Name:");
        grid.add(userName, 0, 1);

        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 1);

        Label pw = new Label("Password:");
        grid.add(pw, 0, 2);

        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 2);
        
        Button btn = new Button("Log in");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 4);
        
        final Text actiontarget = new Text();
        grid.add(actiontarget, 1, 6);
        
        btn.setOnAction(new EventHandler<ActionEvent>() {
            /*
            *   Função de LOGIN, Usuário e senha devem estar preenchidos
            */
            @Override
            public void handle(ActionEvent e) {
                if(userTextField.getText().isEmpty()){
                    actiontarget.setFill(Color.FIREBRICK);
                    actiontarget.setText("User is empty!");
                }
                else if(pwBox.getText().isEmpty()){
                    actiontarget.setFill(Color.FIREBRICK);
                    actiontarget.setText("Pass is empty!");
                }
                else{
                    actiontarget.setFill(Color.BLUE);
                    actiontarget.setText("LOGON");
                    autenticaUsuarioNoServidor(userTextField.getText(), IPSERVIDOR);
                    ListaAmigos listaAmigos = new ListaAmigos(userTextField.getText());
                    Stage sndStage = new Stage();
                    listaAmigos.start(sndStage);
                    primaryStage.close();
                }
            }
        });
        
        Scene scene = new Scene(grid, 300, 200);
        
        primaryStage.setTitle("Welcome to singleChat!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    
    public void autenticaUsuarioNoServidor(String nome, String ipServidor){
        Socket client;
        try {
            
            //Logando usuário no servidor
            client = new Socket(ipServidor, 6991);
            
            ObjectOutputStream request = new ObjectOutputStream(client.getOutputStream());
            request.flush();
            request.writeUTF("MASTER_PEER CONNECT "+nome);
            request.close();
            client.close();
            
            ServerSocket server = new ServerSocket(6992);
            Socket cliente2 = server.accept();
            ObjectInputStream streamRetorno = new ObjectInputStream(cliente2.getInputStream());
            String listaDeAmigos = streamRetorno.readUTF();
            System.out.println(listaDeAmigos);
            
            
        } catch (IOException ex) {
            
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
