/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package singlechat;

import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author alex
 */
public class ServerSide extends Thread{ //implements Runnable{
    private int porta;
    ServerSocket server;
    Socket client;
    JanelaChat janela;
    
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
                client = server.accept();
                System.out.println("CONECTOU");
                ObjectOutputStream saida = new ObjectOutputStream(client.getOutputStream());
                saida.flush();
                saida.writeObject("huahuahua");
                saida.close();
                client.close();
                janela.listened("oi, eu sou goku");
            }catch(Exception e){
                System.out.println("FALHA CONECTAR NO SERVIDOR: " + e);
            }
        }
    }
}
