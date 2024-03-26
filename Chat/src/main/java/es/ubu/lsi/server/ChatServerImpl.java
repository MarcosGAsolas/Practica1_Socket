package es.ubu.lsi.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import es.ubu.lsi.common.ChatMessage;

public class ChatServerImpl implements ChatServer{

	private static int DEFAULT_PORT = 1500;
	private int clinetId;
    private Map<Integer, ServerThreadForClient> clients = new HashMap<>();
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private int _port;
	
	
	public ChatServerImpl(int port) {
		this._port = port;
	}
	
	/*
	 * 
	 */
	@Override
	public void startup() {

		try {
			
			serverSocket = new ServerSocket(_port);
            System.out.println("Servidor iniciado en el puerto " + _port);
			
			while (true){
			
			//ESCUCHA UNA LLAMDA PARA CONECTARSE A ESTE SOCKET Y LA ACPETA
            clientSocket = serverSocket.accept(); 
            
            //IMPRIMIMOS POR PANTALLA LA INFORMACION DEL NUEVO CLIENTE 
            System.out.println("Nuevo Cliente: " + clientSocket.getInetAddress() + "/" + clientSocket.getPort());
            
            
        	
        	Random random = new Random();

            // GENERAR UN NUMERO ALEATORIO ENTRE O Y 100
            clinetId = random.nextInt(100);
            
            //CREAMOS EL HILO CORRESPONDIENTE PARA ESE CLIENTE
        	ServerThreadForClient hilonuevocliente = new ServerThreadForClient(clientSocket, clinetId);
        	
        	//AÑADIMOS EL HILO DEL CLIENTE A LA LISTA DE HILOS CON SU ID IDENTIFICATIVO 
        	clients.put(clinetId, hilonuevocliente);
        	
        	//CREAMOS EL HILO 
            Thread clientThread = new Thread(hilonuevocliente);
            
            //LANZAMOS EL HILO 
            clientThread.start();
        	
        	
        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/*
	 * CIERRA EL SOCKET DEL CLIENTE
	 */
	@Override
	public void shutdown() {
		try {
			
			//CIERRA LA CONEXION CON EL CLIENTE 
			clientSocket.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	 * 
	 */
	@Override
	public void brodcast(ChatMessage message) {
		
		 for (ServerThreadForClient client : clients.values()) {
			 client.sendMessage(message);
	        }
	}
	/*
	 * 
	 */
	@Override
	public void remove(int id) {
		
		//SE CIERRA LA CONEXION CON EL CLIENTE
		shutdown();
		
		//ELIMINA AL CLIENTE DE LA LISTA
		clients.remove(id);
		
	}
	
	public static void main(String[] args) {
		
		
		//INICIALIZAMOS EL PUERTO DE ESCUCHA POR DEFECTO E IMPRIMIMOS UN MENSAJE POR PANTALLA DE LA ESCUCHA
		int portNumber = DEFAULT_PORT;
		//SI NOS LLEGA ARGUMENTOS
		if(args.length != 0) {
			//INTENTAMOS COGER EL PUERTO DE INICIALIZACION DE LOS ARGUMENTOS
			try {
            portNumber = Integer.parseInt(args[0]);
            //SI NO SE PUEDE, LANZAMOS UNA EXCEPCION 
            } catch (NumberFormatException e) {
            	//LANZAMOS EL MENSAJE DE ERROR
                System.err.println("El argumento debe ser un número de puerto válido.");
                //FINALIZAMOS EL PROGRAMA LANZANDO UN NUMERO DIFERENTE DE 0 PARA INDICAR QUE EL PROGRAMA A FINALIZADO 
                System.exit(1);
            }
		}
		//SE ENVIA EL MENSAJE DE ESCUCHANDO EN EL PUERTO
		System.out.println("Escuchando por puerto: " + portNumber);
		//SE INICIALIZA EL SERVIDOR
		ChatServerImpl server = new ChatServerImpl(portNumber);
		//SE LANZA EL SERVIDOR
		server.startup();
		 
	}
	
	private class ServerThreadForClient extends Thread{
		
		
		private Socket clientSocket;
		private PrintWriter out;
		private ChatMessage mensaje;
		private int _clienteId;
		private ChatServerImpl servidor;
		
		public ServerThreadForClient(Socket clientSocket, int clienteId ) {
			this.clientSocket = clientSocket;
			this._clienteId = clienteId;
			
		}
		
		public void run() {
			try {

				//SE INICIALIZAN EL BUFFERREADER PARA LEER LOS MENSAJES DE LOS CLIENTES
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				//SE INICIALIA EL PRINTWRITER PARA ESCRIBIR MENSAJES POR PANTALLA
                out = new PrintWriter(clientSocket.getOutputStream(), true);

				String inputLine;
				
				//AÑADIMOS EL SOCKET CREADO A LA LISTA
	            
				while ((inputLine = in.readLine()) != null) {
					//SI EL MENSAJE QUE RECIVIMOS ES EQUIVALENTE A SHUTDOWN
                    if (inputLine.toLowerCase().trim().equals("shutdown")) {
                    	
                    	 mensaje = new ChatMessage(_clienteId, ChatMessage.MessageType.SHUTDOWN, "Cliente desconectado");
                    //SI EL MENSAJE QUE RECIVIMOS ES EQUIVALENTE A LOGOUT	 
                    } if (inputLine.toLowerCase().trim().equals("logout")) {
                    	
                    	 mensaje = new ChatMessage(_clienteId, ChatMessage.MessageType.LOGOUT, "Cliente saliendo del servidor");
                    	 
                    }else {
                    	
                    	 mensaje = new ChatMessage(_clienteId, ChatMessage.MessageType.MESSAGE, inputLine);
                    }
                    
                    if (mensaje.getType() == ChatMessage.MessageType.SHUTDOWN || mensaje.getType() == ChatMessage.MessageType.LOGOUT)
                    {
                        servidor.remove(_clienteId);
                        servidor.brodcast(mensaje);
                    }
                    else 
                    {
                    	servidor.brodcast(mensaje);
                    }
                }
	        }
	        catch (IOException e) {
	            System.out.println("Exception caught on thread");
	            System.out.println(e.getMessage());
	        }
		
		}
		public void sendMessage(ChatMessage message) {
            out.printf(message.getMessage());
        }
		
	}

}
