/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package singlechat;



import javafx.application.Application;
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
 * @author lucas
 */
public class ExibeAmigos extends Application{

    private ListaAmigos parent;
    public PeerData friends;
    public ExibeAmigos(PeerData setFriends, ListaAmigos setParent){
        friends = setFriends;
        parent = setParent;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
       
        Button b[] = new Button[friends.size()+1];
        int j = 0;
        int k = -1;
        
        for(int i = 0; i < friends.size(); i++){
            if(friends.get(i).id == ListaAmigos.USERID){
                //continue;
            }
            b[i] = new Button();
            b[i].setText(friends.get(i).name);
            PeerData.Peer auxPeers = friends.get(i);
            b[i].setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event){
                    parent.iniciaConversa(auxPeers);
                    primaryStage.close();
                }
            });
            k++;
            if(k % 4 == 0){ j++; k = 0;}
            grid.add(b[i], k, j);
        }
        
        
        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setTitle("Exibindo meus amigos");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
