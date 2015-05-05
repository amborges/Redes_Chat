/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package singlechat;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author alex
 */
public class ServerSide extends Thread{
    
    /*
    *   Este é o lado servidor da janela de chat
    *   Fica ouvindo o contato, e reage quando há uma nova mensagem, 
    *   atualizando a janela de historico de chat
    */
    
    private ServerSocket server;
    private Socket client;
    private JanelaChat janela;
    private int porta;
    
    ServerSide(int setPorta, JanelaChat origem){
        porta = setPorta;
        janela = origem;
        try{
            server = new ServerSocket(porta);
        }catch(Exception e){
            System.out.println("FALHA ALOCAR NO SERVIDOR: " + e);
        }
    }
    
    @Override
    public void run(){
        while(true){
            try{
               System.out.println("teste"); 
                client = server.accept();
                //System.out.println("CONECTOU");
                //Recebe do cliente a mensagem, e escreve no chat
                ObjectInputStream entrada = new ObjectInputStream(client.getInputStream());
                String msgFromClient = entrada.readUTF();
                entrada.close();
                client.close();
                janela.listened(msgFromClient); 
                        //PROBLEMA
                        //Se eu escrevo algo ai em cima, aparece, mas quando coloco
                        //essa string, fica em branco. Descorbri pq!!!
                //System.out.println("MSG send");
            }catch(Exception e){
                System.out.println("FALHA CONECTAR NO SERVIDOR: " + e);
            }
        }
    }
}
