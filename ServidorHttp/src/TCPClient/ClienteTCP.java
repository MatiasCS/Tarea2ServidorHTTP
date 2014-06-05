/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package TCPClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    String comandos[] = {"MEET","SENDMSG","GOTMSG","SENDFILE","GOTFILE"}; //Arreglo con los comandos utilizados por el protocolo
    
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
        System.out.println("Mensaje SENDMSG: "+mensajeTotal);
    }
    
    public void GOTMSG(String IPOrigen, String nSequencia) throws IOException{
        DataOutputStream outServer;
        outServer = new DataOutputStream(this.conexionCliente.getOutputStream());
        String mensajeTotal = comandos[2] + "##" + IPOrigen + "##" + nSequencia; //Variable que guarda el mensaje junto a los parametros adicionales necesarios
        outServer.writeBytes(mensajeTotal + "\n"); // Envio del mensajeTotal al servidor TCP
        outServer.flush();
    }
    
    public void SENDFILE(String IPOrigen, String IPDestino, String nombreArchivo, int largoArchivo) throws IOException{
        DataOutputStream outServer;
        outServer = new DataOutputStream(this.conexionCliente.getOutputStream());
        String mensajeTotal =comandos[3] + "##" + IPOrigen + "##" + IPDestino + "##" + nombreArchivo + "##" + largoArchivo;
        outServer.writeBytes(mensajeTotal +"\n");
        outServer.flush();
    }
    
    public void GOTFILE(){
        
    }
    
    
    
    
    
    public String leerServidor() throws IOException{
        BufferedReader inServidorTCP;
        inServidorTCP = new BufferedReader(new InputStreamReader(this.conexionCliente.getInputStream()));
        String linea = inServidorTCP.readLine();
        return linea;
    }    
    
    //Funcion para enviar por una conexion los datos Nombre y Tamaño del archivo que se mandará
    //Como parametro recibe un directorio que será entregado desde la página WEB semantica
   /* public void ObtenerArchivo_nombre_y_largo(String Directorio) throws IOException{
        DataOutputStream dos = new DataOutputStream( this.conexionCliente.getOutputStream() );
        String nombreArchivo=Directorio;
        File archivo = new File( nombreArchivo );
        int tamannoArchivo = ( int )archivo.length();
        dos.writeUTF( archivo.getName() );
        dos.writeInt( tamannoArchivo );
        dos.flush();
    }
    */
    
    
    
    //Funcion para enviar por una conexion el Flujo de datos del archivo.
    //Como parametros recibe el directorio del archivo
    public void enviarArchivo_datos(String directorio) throws IOException{
        Socket socket = new Socket("localhost",15123);
        File transferFile = new File (directorio);
        byte [] bytearray  = new byte [(int)transferFile.length()];
        FileInputStream fin = new FileInputStream(transferFile);
        BufferedInputStream bin = new BufferedInputStream(fin);
        bin.read(bytearray,0,bytearray.length);
        OutputStream os = socket.getOutputStream();
        System.out.println("Sending Files...");
        os.write(bytearray,0,bytearray.length);
        os.flush();
        socket.close();
        System.out.println("File transfer complete");
        
    }
    
    /*
    public void clinte_recibe_archivo_datos_servidor() throws FileNotFoundException, IOException{
        String nombreArchivo="Directorio.pdf";
        int tam=717263;
        FileOutputStream fos = new FileOutputStream( nombreArchivo );
        BufferedOutputStream out = new BufferedOutputStream( fos );
        BufferedInputStream in = new BufferedInputStream( conexionCliente.getInputStream() );
        byte[] buffer = new byte[ tam ];
        for( int i = 0; i < buffer.length; i++ ){            
              buffer[ i ] = ( byte )in.read( ); 
        }
        out.write( buffer ); 
        out.flush(); 
        in.close();
        out.close(); 
        //conexion.close();
        System.out.println( "Archivo Recibido "+nombreArchivo );
        
    }
    
    
    public void Obtener_archivo(String IP_Fuente) throws IOException{
        DataOutputStream dos;
        dos = new DataOutputStream(this.conexionCliente.getOutputStream());
        dos.writeBytes(IP_Fuente + "\n");
        dos.flush(); 
    
    }
    */
}
