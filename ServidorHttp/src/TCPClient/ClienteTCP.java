/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package TCPClient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author Matias
 */
public class ClienteTCP {
    
    Socket conexionCliente;
    final static int puerto = 8082;
    InetAddress IP;
    String comandos[] = {"MEET","SENDMSG","GOTMSG"}; //Arreglo con los comandos utilizados por el protocolo
    
    public ClienteTCP() throws UnknownHostException, IOException{
        this.IP = InetAddress.getByName("192.168.0.4"); 
        this.conexionCliente = new Socket("localhost", puerto);
    }
    
    public void MEET() throws IOException{
        DataOutputStream outServer;
        outServer = new DataOutputStream(this.conexionCliente.getOutputStream());
        String mensajeTotal = comandos[0] + "##Hola";
        outServer.writeBytes(mensajeTotal + "\n");
        outServer.flush();
    }
    
    //Metodo para enviar un mensaje
    public void SENDMSG(String mensaje, String IPDestino, String IPOrigen) throws IOException{
        DataOutputStream outServer;
        outServer = new DataOutputStream(this.conexionCliente.getOutputStream());
        String mensajeTotal = comandos[1] + "##" + IPDestino + "##" + IPOrigen + "##" + mensaje; //Variable que guarda el mensaje junto a los parametros adicionales necesarios
        outServer.writeBytes(mensajeTotal + "\n"); // Envio del mensajeTotal al servidor TCP
        outServer.flush();
        
    }
    
    public void GOTMSG(String IPOrigen, String nSequencia) throws IOException{
        DataOutputStream outServer;
        outServer = new DataOutputStream(this.conexionCliente.getOutputStream());
        String mensajeTotal = comandos[2] + "##" + IPOrigen + "##" + nSequencia; //Variable que guarda el mensaje junto a los parametros adicionales necesarios
        outServer.writeBytes(mensajeTotal + "\n"); // Envio del mensajeTotal al servidor TCP
        outServer.flush();
    }
    
    public String leerServidor() throws IOException{
        BufferedReader inServidorTCP;
        inServidorTCP = new BufferedReader(new InputStreamReader(this.conexionCliente.getInputStream()));
        String linea = inServidorTCP.readLine();
        return linea;
    }
}
