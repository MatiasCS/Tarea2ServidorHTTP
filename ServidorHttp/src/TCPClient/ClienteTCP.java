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
    String comandos[] = {"MEET","SENDMSG"}; //Arreglo con los comandos utilizados por el protocolo
    
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
    
    public String leerServidor() throws IOException{
        BufferedReader inServidorTCP;
        inServidorTCP = new BufferedReader(new InputStreamReader(this.conexionCliente.getInputStream()));
        String linea = inServidorTCP.readLine();
        return linea;
    }
    
    //Funcion para enviar por una conexion los datos Nombre y Tamaño del archivo que se mandará
    //Como parametro recibe un directorio que será entregado desde la página WEB semantica
    public void enviarArchivo_nombre_y_largo(String Directorio) throws IOException{
        DataOutputStream dos = new DataOutputStream( conexionCliente.getOutputStream() );
        String nombreArchivo=Directorio;
        File archivo = new File( nombreArchivo );
        int tamañoArchivo = ( int )archivo.length();
        dos.writeUTF( archivo.getName() );
        dos.writeInt( tamañoArchivo );
    }
    //Funcion para enviar por una conexion el Flujo de datos del archivo.
    //Como parametros recibe el directorio del archivo
    public void enviarArchivo_datos(String directorio) throws IOException{
        String nombreArchivo=directorio;
        File archivo = new File( nombreArchivo );
        int largo_archivo = ( int )archivo.length();
        
        FileInputStream fis = new FileInputStream( directorio );
        BufferedInputStream bis = new BufferedInputStream( fis );
        BufferedOutputStream bos = new BufferedOutputStream( conexionCliente.getOutputStream()          );
        byte[] buffer = new byte[ largo_archivo ];
        bis.read( buffer ); 
        for( int i = 0; i < buffer.length; i++ )
        {
            bos.write( buffer[ i ] ); 
        } 
        bis.close();
        bos.close();
        conexionCliente.close(); 
    }
    
}
