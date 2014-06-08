/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package TCPClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
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
    
    /*
    Se modifico el constructor de tal manera que al interactuar con un contacto se conecte
    automaticamente con la IP de ese contacto.
    */
    public ClienteTCP(String ip_destino) throws UnknownHostException, IOException{
        this.IP = InetAddress.getByName("192.168.0.4"); 
        this.conexionCliente = new Socket(ip_destino, puerto);
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
    
    public void GOTFILE(String IP_Solicitante) throws IOException{
        DataOutputStream outServer;
        outServer = new DataOutputStream(this.conexionCliente.getOutputStream());
        String mensajeTotal=comandos[4] + "##" + IP_Solicitante;
        outServer.writeBytes(mensajeTotal +"\n");
        outServer.flush();
    }
    
    
    public String leerServidor() throws IOException{
        BufferedReader inServidorTCP;
        inServidorTCP = new BufferedReader(new InputStreamReader(this.conexionCliente.getInputStream()));
        String linea = inServidorTCP.readLine();
        return linea;
    }    
    
    
    /*
    Funcion que envia flujo de datos.
    Primero se conecta a TCP Server que tiene como 
    */
    public void enviarArchivo_datos(String directorio,String IP_Destino) throws IOException{
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;
        
        //Socket socket = new Socket("localhost",15123);
        Socket socket = new Socket(IP_Destino,15123);
        
        File myFile = new File (directorio);
        byte [] mybytearray  = new byte [(int)myFile.length()];
        fis = new FileInputStream(myFile);
        bis = new BufferedInputStream(fis);
        bis.read(mybytearray,0,mybytearray.length);
        os = socket.getOutputStream();
        os.write(mybytearray,0,mybytearray.length);
        os.flush();
        socket.close();
   }
    
    
    
    public void clinte_recibe_archivo_servidor(String nombre,int  largo,String IP_que_envia, String IP_que_recibe) throws FileNotFoundException, IOException{        
            Socket yo = null;
            PrintWriter alServidor;
            BufferedReader delTeclado;
            DataInputStream delServidor;
            String tecleado;
            
            try {
                try {
                yo = new Socket(IP_que_envia, 15123);
            } catch (UnknownHostException e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }
            
            delTeclado = new BufferedReader(new InputStreamReader(System.in));
            alServidor = new PrintWriter(yo.getOutputStream(), true);
            delServidor = new DataInputStream(yo.getInputStream());
            File folder = new File("descargas_"+IP_que_recibe);
            folder.mkdirs();
            
            FileOutputStream fos = new FileOutputStream("descargas_"+IP_que_recibe+"/"+nombre);
            BufferedOutputStream out = new BufferedOutputStream( fos );
            BufferedInputStream in = new BufferedInputStream( yo.getInputStream() );
            // Creamos el array de bytes para leer los datos del archivo
            byte[] buffer = new byte[largo];
            // Obtenemos el archivo mediante la lectura de bytes enviados
            for ( int i = 0; i < buffer.length; i++ ) {
                buffer[ i ] = ( byte )in.read( );
            }
            out.write( buffer );
            out.flush();
            in.close();
            out.close();
            yo.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        
        
    }
    
}
