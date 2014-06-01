/*lo tuyo no
 Probando!
 */

package HttpServer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import TCPClient.ClienteTCP;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Matias
 */
public class ServidorHttp implements Runnable{
    
    //Variables Estaticas
    static final int puerto = 8080;
    static final File directorio_raiz = new File(".");
    static final String inicio = "index.html";
    
    
    Socket conexion;
    
    public ServidorHttp(Socket conexion) throws IOException{
        this.conexion = conexion;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)  {
        try {
            // TODO code application logic here           
            
            //Creacion del servidor y espera de clientes
            System.out.println(InetAddress.getLocalHost());
            ServerSocket servidor = new ServerSocket(puerto);
            while(true){
                ServidorHttp cliente = new ServidorHttp(servidor.accept());
                Thread hebra = new Thread(cliente);
                hebra.start();  
              }
            
        } catch (IOException ex) {
            Logger.getLogger(ServidorHttp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        
        // Mensaje cliente, archivo que se pide, metodo POST o GET
        BufferedReader formulario;      //Variable que guarda la entrada.
        BufferedReader entradacliente;  //Variable que lee la entrada del cliente
        String archivoPedido;           //Nombre del archivo que pide el cliente http
        String metodo;                  //Metodo usado en el form html
        BufferedOutputStream salidaArchivo; //Variable para enviar el arhvio;
        PrintWriter output = null;          //Variable para enviar el mensaje del Servidor al cliente
        try {
        
            //Lectura mensaje enviado por el cliente
            entradacliente = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            String entrada = entradacliente.readLine();
            //System.out.println("Entrada:"+ entrada + "\n");            
            StringTokenizer token = new StringTokenizer(entrada);
            metodo = token.nextToken();            
            archivoPedido = token.nextToken();
            salidaArchivo = new BufferedOutputStream(conexion.getOutputStream());
            output = new PrintWriter(conexion.getOutputStream());
            
            
            formulario =entradacliente; //Se le da el valor a la variable de lo que envia el Usuario
            
            //Se verifica s el arhivo es el index.html
            if(archivoPedido.equals("/"))
                archivoPedido += inicio;
        
            //Implementacion GET
            if(metodo.equals("GET")){
                FileInputStream stream;               
                File archivo = new File(directorio_raiz,archivoPedido);                    
                int pesoArchivo = (int) archivo.length();             
                //Se verifica si se hace una peticion GET O POST o si se quiere enviar un mensaje
                    byte[] buffer = new byte[pesoArchivo];                   
                    List <String> lista = obtenerContactos();
                    
                    /*Se interfiere el archivo index para poder mostrar la lista de contactos sin la necesidad
                    de reescribir el archivo html original*/
                    if(archivoPedido.startsWith("/index")){
                        BufferedReader index = new BufferedReader(new FileReader(archivo));
                        String linea;
                        String nuevoIndex = "";                        
                        while(index.ready()){
                            linea = index.readLine();
                            if(linea.indexOf("<!--Contactos-->")>0){                                  
                                Iterator iterador = lista.iterator();
                                while(iterador.hasNext()){                                    
                                    nuevoIndex += (String) iterador.next();  //TIENE QUE SER CAMBIADO PARA VERLOS COMO BOTONES                                
                                }
                            }
                            else
                                nuevoIndex = nuevoIndex + linea;
                        }
                        //Envio del archivo temp que equivale al nuevo index html con los contactos
                        output.println("HTTP/1.0 200 OK");
                        output.println("Server: Java HTTP Server 1.0");
                        output.println("Date: " + new Date());
                        output.println("Content-length: " + nuevoIndex.length());
                        output.println("Content-type: text/html");
                        output.println("");
                        output.println(nuevoIndex);
                        output.flush();                                               
                    }
                    else{
                        stream = new FileInputStream(archivo);
                        stream.read(buffer);                
                        String contenido = tipoDeContenido(archivoPedido);

                        output.println("HTTP/1.0 200 OK");
                        output.println("Server: Java HTTP Server 1.0");
                        output.println("Date: " + new Date());
                        output.println("Content-length: " + pesoArchivo);
                        output.println("Content-type: " + contenido);
                        output.println();
                        output.flush();

                        salidaArchivo.write(buffer,0,pesoArchivo);
                        salidaArchivo.flush();

                        entradacliente.close();
                        salidaArchivo.close();
                    }               
                conexion.close();
                output.close();
            }
            //Implementacion POST
            else if(metodo.equals("POST")){
                //--------------------------------------
                //Parte de identificar el contenido.
                //--------------------------------------
                int Largo=-1;                   //Variable que indica largo de la palabra.
                String delimitadores="[& =]";   //String que contiene los delimitadores de las palabras
                String datos1;                  //Variable auxiliar para guardar los datos que se tomaran con el metodo post
                String[] datos2;                //Varible auxiliar para guardar los datos finales.
                File archivo = new File(directorio_raiz,archivoPedido);
                int pesoArchivo = (int) archivo.length();
                while(true){
                    final String linea=formulario.readLine();
                    final String iniciador = "Content-Length: ";            //Variable para indicar que se debe leer la palabra que comienze con esas palabras
                    if(linea.startsWith(iniciador)){
                        Largo=Integer.parseInt(linea.substring(iniciador.length()));
                    }
                    if (linea.length()==0){
                        break;
                    }
                }
                final char[] contenido=new char[Largo];
                formulario.read(contenido);
                       
                datos1 = new String(contenido);
                System.out.println("Esto es datos1: "+datos1);    
                datos2=datos1.split(delimitadores);
                
                if(datos2[0].startsWith("mensaje")){
                    System.out.println(datos2[1]);
                    //Prueba
                    ClienteTCP TCPClient;
                    TCPClient = new ClienteTCP();
                    //Inicio de la convesacion con el Servidor
                    TCPClient.MEET();            
                    String linea = TCPClient.leerServidor();
                        while(linea != null){
                            StringTokenizer token1 = new StringTokenizer(linea, "##");
                            String metodo1 = token1.nextToken();           
                            System.out.println(metodo1);
                            switch(metodo1){
                                case("GREET"):
                                    TCPClient.SENDMSG("Anyone is there?", "192.168.0.4", "192.168.0.4" );
                                    linea = TCPClient.leerServidor();
                                    break;
                                case("SENDOK"):
                                    linea = null;
                                    break;
                                }
                        }
                    TCPClient.GOTMSG("192.168.0.4", "0");
                    linea = TCPClient.leerServidor();
                    String parrafo = "";
                    int counter = 0;
                                                            
                    while(!linea.equals("FIN")){                                                    
                        if(!linea.equals("FIN")){
                            parrafo += linea + "\r\n";
                            counter += 1;
                        }
                        linea = TCPClient.leerServidor();
                    }
                    System.out.println(parrafo);
                    escribirChat("192.168.0.4", parrafo,counter);
                    
                    //Prueba
                       
                        
                        /*ClienteTCP TCPClient;
                        TCPClient = new ClienteTCP();
                        //Inicio de la convesacion con el Servidor
                        TCPClient.MEET();
                        String linea = TCPClient.leerServidor();
                        StringTokenizer token1 = new StringTokenizer(linea, "##");
                        String metodo1 = token1.nextToken();
                        
                        switch(metodo1){
                            case("GREET"):
                                //TCPClient.SENDMSG(, );
                                break;
                            case("SENDOK"):
                                break;
                            //Enviar Mensaje.
                            case("SENDM"):
                                break;
                            //Enviar Archivo.
                            case("SENDF"):
                                break;
                        }*/
                      FileInputStream stream;
                        byte[] buffer = new byte[pesoArchivo];

                        stream = new FileInputStream(archivo);
                        stream.read(buffer);

                        output.println("HTTP/1.0 200 OK");
                        output.println("Server: Java HTTP Server 1.0");
                        output.println("Date: " + new Date());
                        output.println("Content-length: " + pesoArchivo);
                        output.println("Content-type: " + contenido);
                        output.println();
                        output.flush();

                        salidaArchivo.write(buffer,0,pesoArchivo);
                        salidaArchivo.flush();
                }
                else{
                    System.out.println(datos2[1]);    
                    System.out.println(datos2[3]);    
                    System.out.println(datos2[5]);    


                    //-----------------------------------------
                    //Parte de ingresar los datos a un archivo.txt
                    //-----------------------------------------
                    File fichero;
                    fichero = new File("Contactos.txt");

                    try{
                        FileWriter escritor=new FileWriter(fichero,true);
                        BufferedWriter buffescritor=new BufferedWriter(escritor);
                        PrintWriter escritor_final= new PrintWriter(buffescritor);

                        escritor_final.append(datos2[1]+" "+datos2[3]+" "+datos2[5]+"\r\n");
                        escritor_final.close();
                        buffescritor.close();

                        FileInputStream stream;
                        byte[] buffer = new byte[pesoArchivo];

                        stream = new FileInputStream(archivo);
                        stream.read(buffer);

                        output.println("HTTP/1.0 200 OK");
                        output.println("Server: Java HTTP Server 1.0");
                        output.println("Date: " + new Date());
                        output.println("Content-length: " + pesoArchivo);
                        output.println("Content-type: " + contenido);
                        output.println();
                        output.flush();

                        salidaArchivo.write(buffer,0,pesoArchivo);
                        salidaArchivo.flush();

                    }
                    catch(IOException e){}
                    //-----------------------------------------
                    //FIN ESCRIBIR FICHERO
                    //-----------------------------------------
                    }
                }
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            } catch (IOException ex) {
                Logger.getLogger(ServidorHttp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public List<String> obtenerContactos(){
        File f = new File( "Contactos.txt" );
        List<String> Contactos = new ArrayList<>();
        //File html = new File("contacto.html");
        BufferedReader entrada;
        try {
            //FileWriter escrito = new FileWriter(html);
            //BufferedWriter bw = new BufferedWriter(escrito);
            //PrintWriter wr = new PrintWriter(bw);  

            entrada = new BufferedReader( new FileReader( f ) );
            String linea;
            //wr.append("<HTML>");
            //wr.append("<BODY>");
            System.out.println(entrada.ready());
            while(entrada.ready()){
                linea = entrada.readLine();
                StringTokenizer nombre = new StringTokenizer(linea);
                Contactos.add(leerNombre(nombre.nextToken()));
                //wr.append("<FONT FACE = 'calibri' >" + leerNombre(nombre.nextToken()) + "</FONT><BR>");
            }
            //wr.append("</BODY></HTML>");
            //wr.close();
            //bw.close();
            entrada.close();
        }catch (IOException e) {
        }
        return Contactos;
    }
    
    public String leerNombre(String nombre){
        StringTokenizer token = new StringTokenizer(nombre, "+");
        String nombreCompleto = "";
        while(token.hasMoreElements()){
            nombreCompleto += token.nextToken() + " ";
        }
        return(nombreCompleto);
    }
    
    public void escribirChat(String nombreDoc, String linea, int nSecuencia) throws IOException{
        File temporal = new File(nombreDoc+"_temporal.txt");
        FileWriter writer = new FileWriter(temporal, true);
        BufferedWriter bw = new BufferedWriter(writer);
        PrintWriter pw = new PrintWriter(bw);
        int secuenciaUpDate = obtenerNumeroSecuencia(nombreDoc) + nSecuencia;
        //Paso de datos al archivo temporal
        File conversacion = new File(nombreDoc+".txt");
        BufferedReader br = new BufferedReader(new FileReader(conversacion));
        String line = "";
        pw.append(String.valueOf(secuenciaUpDate+"\r\n"));
        line = br.readLine();
        while(br.ready()){
            line = br.readLine();
            pw.append(line+"\r\n");
        }
        pw.append(linea+"\r\n");
        br.close();
        conversacion.delete();      
        bw.close();
        pw.close();
        temporal.renameTo(new File(nombreDoc+".txt"));
    }
    
    public int obtenerNumeroSecuencia(String nombreDoc) throws FileNotFoundException, IOException{
        File conversacion = new File(nombreDoc+".txt");
        BufferedReader reader = new BufferedReader(new FileReader(conversacion));
        String linea = reader.readLine();
        int NumeroSequencia = Integer.parseInt(linea);
        reader.close();
        return NumeroSequencia;
        
    }
    
    private String tipoDeContenido(String archivo){
      if (archivo.endsWith(".htm") ||
        archivo.endsWith(".html"))
      {
        return "text/html";
      }
      else if (archivo.endsWith(".png"))
      {
        return "image/png";
      }
      else if (archivo.endsWith(".jpg") ||
        archivo.endsWith(".jpeg"))
      {
        return "image/jpeg";
      }
      else
      {
        return "text/plain";
      }
    }    
}
