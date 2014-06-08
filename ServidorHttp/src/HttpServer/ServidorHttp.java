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
import java.awt.Desktop;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Matias
 */
public class ServidorHttp implements Runnable{
    
    //Variables Estaticas
    //static final int puerto = 8080;
    static final File directorio_raiz = new File(".");
    static final String inicio = "index.html";
    
    
    Socket conexion;
    
    public ServidorHttp(Socket conexion) throws IOException{
        this.conexion = conexion;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws URISyntaxException  {
        try {
            // TODO code application logic here           
            //Para ejecutar multiples clientes en un mismo PC
            Random r = new Random();
            int puerto = r.nextInt(8000);
            ServerSocket servidor = new ServerSocket(puerto);
            Desktop.getDesktop().browse(new URI("http://localhost:"+puerto+"/"));
            
            
            //Para ejecutar solo un cliente con IP Fija.
            //ServerSocket servidor = new ServerSocket(puerto);
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
        String ParametroIPD = "";
        String IPOrigen = "";
        String IPDestino = "";
        try {
            String IPO = String.valueOf(InetAddress.getLocalHost());
            StringTokenizer tok1 = new StringTokenizer(IPO,"/");
            tok1.nextToken();
            IPOrigen = tok1.nextToken();
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(ServidorHttp.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                if(archivoPedido.indexOf("?")>0){
                    String auxiliar = archivoPedido;
                    StringTokenizer tok = new StringTokenizer(auxiliar,"?");
                    archivoPedido = tok.nextToken();
                    ParametroIPD = tok.nextToken();
                    StringTokenizer elemento = new StringTokenizer(ParametroIPD,"=");
                    
                    elemento.nextToken();
                    IPDestino = elemento.nextToken();
                    System.out.println("IPDESTINO" + IPDestino);
                }
                
                System.out.println("Archivo pedido: "+archivoPedido);
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
                        String nombre = "";
                        String ip = "";
                        while(index.ready()){
                            linea = index.readLine();
                            if(linea.indexOf("<!--Contactos-->")>0){                                  
                                Iterator iterador = lista.iterator();
                                while(iterador.hasNext()){
                                    nombre = (String) iterador.next();
                                    ip = (String) iterador.next();
                                    nuevoIndex += "<a href='chat.html?ip="+ip+"'>"+nombre+"</a><br>";  //TIENE QUE SER CAMBIADO PARA VERLOS COMO BOTONES                                
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
                if(archivoPedido.indexOf("?")>0){
                    String auxiliar = archivoPedido;
                    StringTokenizer tok = new StringTokenizer(auxiliar,"?");
                    archivoPedido = tok.nextToken();
                    ParametroIPD = tok.nextToken();
                    StringTokenizer elemento = new StringTokenizer(ParametroIPD,"=");
                    elemento.nextToken();
                    IPDestino = elemento.nextToken();
                }
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
                    String mensaje = (datos2[1]);
                    ClienteTCP TCPClient;
                    
                    //Acá se crea la conexion TCP referenciando la IP del contacto.
                    TCPClient = new ClienteTCP(IPDestino);
                    TCPClient.MEET();            
                    String linea = TCPClient.leerServidor();
                        while(linea != null){
                            StringTokenizer token1 = new StringTokenizer(linea, "##");
                            String metodo1 = token1.nextToken();           
                            System.out.println("IPDESTINO:!!"+IPDestino);
                            switch(metodo1){
                                case("GREET"):
                                    TCPClient.SENDMSG(mensaje, IPDestino, IPOrigen );
                                    escribirChat(IPDestino, "Tu: "+mensaje, -1);
                                    linea = TCPClient.leerServidor();
                                    crearHtml(IPDestino);
                                    break;
                                case("SENDOK"):
                                    linea = null;
                                    break;
                                }
                        }
                    //System.out.println("Esto es ip origen :"+IPOrigen);
                    TCPClient.GOTMSG(IPOrigen, String.valueOf(obtenerNumeroSecuencia(IPDestino)));
                    linea = TCPClient.leerServidor();
                    String parrafo = "";
                    int counter = 0;
                                                            
                    while(!linea.equals("FIN")){                                                    
                        if(!linea.equals("FIN")){
                            System.out.println(linea);
                            StringTokenizer p = new StringTokenizer(linea,"##");
                            p.nextToken();
                            parrafo += p.nextToken() + "\r\n";
                            counter += 1;
                        }
                        linea = TCPClient.leerServidor();
                    }
                    //System.out.println(parrafo);
                    escribirChat(IPDestino, parrafo,counter);
                    crearHtml(IPDestino);
                    
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
                    
                    if(archivoPedido.indexOf("?")>0){
                        String auxiliar = archivoPedido;
                        StringTokenizer tok = new StringTokenizer(auxiliar,"?");
                        archivoPedido = tok.nextToken();
                    }
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
                
              /* En esta parte  se identifica si se quiere enviar el archivo (file1) o si se quiere recibir
                el archivo. Todabia esta en duda si se descarga todos los archivos del sujeto o lo hacemos con 
                verificación de usuario
                */
                
                else if(datos2[0].startsWith("file1")){
                    try {
                        ClienteTCP TCPClient;
                        //Acá se crea la conexion TCP referenciando la IP del contacto.
                        TCPClient = new ClienteTCP(IPDestino);
                        TCPClient.MEET();
                        String linea = TCPClient.leerServidor();
                        while(linea != null){
                            StringTokenizer token1 = new StringTokenizer(linea, "##");
                            String metodo1 = token1.nextToken();           
                            System.out.println("IPDESTINO:!!"+IPDestino);
                            switch(metodo1){
                                case("GREET"):
                                    /*
                                    Supuesto: Se tiene como supuesto que la IP a la cual se enviaran los archivos.
                                    es la misma donde esta contenido el servidor TCP.
                                    1- Se obtiene los datos del archivo.
                                    2- Se envia esos datos a la IP del servidor.
                                    3-Luego se envia el flujo de datos.
                                    */
                                    File obtener_datos_archivo = new File("Enviar/"+datos2[1]);
                                    int tamannoArchivo = ( int )obtener_datos_archivo.length();
                                    String nombre_archivo = obtener_datos_archivo.getName();
                                    TCPClient.SENDFILE(IPOrigen, IPDestino, nombre_archivo, tamannoArchivo);
                                    TCPClient.enviarArchivo_datos("Enviar/"+datos2[1],IPDestino);
                                    
                                case("SENDOK"):
                                    linea=null;
                                    break;
                            }
                        }
                        
                        if(archivoPedido.indexOf("?")>0) {
                            String auxiliar = archivoPedido;
                            StringTokenizer tok = new StringTokenizer(auxiliar,"?");
                            archivoPedido = tok.nextToken();
                    }
                     
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
                        
                        
                    } catch (IOException ex) {
                        Logger.getLogger(ServidorHttp.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                        
                
                else if (datos2[0].startsWith("file2")){
                     ClienteTCP TCPClient;
                     TCPClient = new ClienteTCP(IPDestino);
                     TCPClient.MEET();
                     String linea = TCPClient.leerServidor();
                     System.out.println("Linea!");
                     while(linea != null){
                            StringTokenizer token1 = new StringTokenizer(linea, "##");
                            String metodo1 = token1.nextToken();           
                            System.out.println("IPDESTINO:!!"+IPDestino);
                            switch(metodo1){
                                case("GREET"):
                                    /*
                                    1.- GOTFILE(IP) la IP en este caso es de quien quiere recibir los archivos
                                    2.- Luego se recibe el archivo y se guarda en una carpeta con nombre IP del que recibe el archivo.
                                    */
                                    //TCPClient.GOTFILE(IPOrigen);
                                    TCPClient.GOTFILE("127.0.0.1");
                                    linea=TCPClient.leerServidor();
                                    System.out.println(linea);
                                    StringTokenizer token2 = new StringTokenizer(linea, "##");
                                    String nombreArchivo = token2.nextToken();
                                    int largo = Integer.parseInt(token2.nextToken());
                                    TCPClient.clinte_recibe_archivo_servidor(nombreArchivo,largo,IPDestino,IPOrigen);
                                case("SENDOK"):
                                    linea=null;
                                    break;
                            }
                        }
                     
                     
                        if(archivoPedido.indexOf("?")>0) {
                            String auxiliar = archivoPedido;
                            StringTokenizer tok = new StringTokenizer(auxiliar,"?");
                            archivoPedido = tok.nextToken();
                    }
                     
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
                    //System.out.println(datos2[1]);    
                    //System.out.println(datos2[3]);    
                    //System.out.println(datos2[5]);    


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
                    catch(IOException e){
                    }
                    //-----------------------------------------
                    //FIN ESCRIBIR FICHERO
                    //-----------------------------------------
                    }
                }            } catch (IOException ex) {
                Logger.getLogger(ServidorHttp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void crearHtml(String IPDestino){
        File f = new File( IPDestino+".txt" );
        File html = new File("Ventana.html");
        BufferedReader entrada;
        try {
            FileWriter escrito = new FileWriter(html);
            BufferedWriter bw = new BufferedWriter(escrito);
            PrintWriter wr = new PrintWriter(bw);  
            entrada = new BufferedReader( new FileReader( f ) );
            String linea;
            wr.append("<HTML>");
            wr.append("<BODY>");
            System.out.println(entrada.ready());
            entrada.readLine();
            while(entrada.ready()){
                linea = entrada.readLine();
                wr.append("<FONT FACE = 'calibri' >" + leerNombre(linea) + "</FONT><BR>");
            }
            wr.append("</BODY></HTML>");
            wr.close();
            bw.close();
            entrada.close();
        }catch (IOException e) {
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
                //nombre.nextToken();
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
        System.out.println(nSecuencia);
        File conversacion = new File(nombreDoc+".txt");
        //Revisar el planteamiento de la creacion del archivo txt
        if(!conversacion.exists() || nSecuencia == -1){            
            FileWriter writer = new FileWriter(conversacion, true);
            BufferedWriter bw = new BufferedWriter(writer);
            PrintWriter pw = new PrintWriter(bw);
            pw.append(linea);
            pw.close();
            bw.close();
        }
        else{
            File temporal = new File(nombreDoc+"_temporal.txt");
            FileWriter writer = new FileWriter(temporal, true);
            BufferedWriter bw = new BufferedWriter(writer);
            PrintWriter pw = new PrintWriter(bw);
            int secuenciaUpDate = obtenerNumeroSecuencia(nombreDoc) + nSecuencia;
            //Paso de datos al archivo temporal
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
    }
    
    public int obtenerNumeroSecuencia(String nombreDoc) throws FileNotFoundException, IOException{
        File conversacion = new File(nombreDoc+".txt");
        if(!conversacion.exists())
            return -1;
        BufferedReader reader = new BufferedReader(new FileReader(conversacion));
        String linea = "0";
        if(reader.ready())
            linea = reader.readLine();
        //System.out.println("Esto es linea: "+linea);
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
