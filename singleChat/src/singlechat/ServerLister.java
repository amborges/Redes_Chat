/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package singlechat;

import java.net.ServerSocket;
import java.net.Socket;
import javafx.stage.Stage;

/**
 *
 * @author alex
 */
public class ServerLister extends Thread{
    
    /*
    *   Esta classe está sempre ouvindo, afim de receber o chamado de uma conexão
    *   e abrir uma janela de conversa
    *   Ela precisa se adequar ao protocolo
    */
    
    private ServerSocket server;
    private Socket client;
    private String userName;
    private ListaAmigos programa;
    
    ServerLister(String n, ListaAmigos la){
        try{
            server = new ServerSocket(20000); //porta definida no protocolo
            userName = n;
            programa = la;
        }catch(Exception e){
            System.out.println("FALHA ALOCAR NO SERVIDOR PRINCIPAL: " + e);
        }
    }
    
    public void updateListaAmigos(ListaAmigos la){
        programa = la;
    }
    
    @Override
    public void run(){
        while(true){
            try{
                client = server.accept();
                System.out.println("CONECTOU");
                
                String who = "ciclano";
                
                if(!programa.has(who)){
                    
                    int friendDoor = 20000; //porta aleatória, definir corretamente depois
                    JanelaChat newWindow = new JanelaChat(userName, who, programa.getPorta(), friendDoor, who, programa);
                    Stage sndStage = new Stage();
                    newWindow.start(sndStage);
                    programa.include(who);
                    
                }
                
                client.close();
            }catch(Exception e){
                System.out.println("FALHA CONECTAR NO SERVIDOR: " + e);
            }
        }
    }
}
