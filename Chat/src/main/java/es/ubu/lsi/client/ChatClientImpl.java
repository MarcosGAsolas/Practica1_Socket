package es.ubu.lsi.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import es.ubu.lsi.common.ChatMessage;

public class ChatClientImpl implements ChatClient {

	private String _server;
	private String _username;
	private Socket cliente;
	private int _port;
	private boolean _carryOn = true;
	private int _id;
	private PrintWriter out;
	private BufferedReader in;
            
	
	/*
	 * CONSTRUCTOR DE LA CLASE
	 */
	public ChatClientImpl(String server, String username, int port) {
		this._server = server;
		this._username = username;
		this._port = port;
	}

	/*
	 * 
	 */
	@Override
	public boolean start() {
		
		try {
			
			//INICIALIZACION DEL SOCKET DEL CLIENTE
			cliente = new Socket(_server, _port);
			//INICIALIZACION DEL FLUJO DE ENTRADA DE DATOS
			in = new BufferedReader(new InputStreamReader(System.in));
			//INICIALIZACION DEL FLUJO DE SALIDA DE DATOS
			out = new PrintWriter(cliente.getOutputStream());
			//INICICALIZACION DEL BUFFER DE LECTURA DE DATOS
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			
			//INICIALIZAMOS EL HILO 
			ChatClientListener listener = new ChatClientListener();
			//CREAMOS EL HILO PARA LANZARLO
		    Thread listenerThread = new Thread(listener);
		    //LANZAMOS EL HILO PARA QUE ESTE ESCUCHANDO LOS MENSAJES DEL SERVIDOR
		    listenerThread.start();
			
			String userInput;
			//MIENTRAS QUE EL USUARIO MANDE MENSAJE
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.println("Mensaje enviado al servidor: " + userInput);
            }
            
            //SI AL DARLE AL ENTER NO ENVIA MENSAJE, SE DESCONETA 
            disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return _carryOn;
		
	}
	/*
	 * 
	 * 
	 */
	@Override
	public void sendMessage(ChatMessage msg) {
		out.println(msg.toString());
	}
	/*
	 * 
	 */
	@Override
	public void disconnect() {
		try {
			in.close();
			out.close();
			cliente.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * 
	 */
	public void main(String[] args) {
		
		//CREAMOS LA INSTANCIA DEL CLIENTE PARA QUE EN EL CASO DE QUE NO NOS PASEN ARGUMENTOS, EJECUTEMOS EL CLIENTE EN EL SERVIDOR STANDAR EN EL PUERTO Y NOMBRE DEL CLIENTE PREDETERMINADO
		ChatClientImpl cliente = new ChatClientImpl("localhost", "marcos", 1500);;
		
		if(args.length == 3) 
		{
			try {
				//COGEMOS EL TERCER ARGUMENTO QUE CORRESPONDE CON EL PUERTO DONDE VAMOS A LANZAR EL SERVIDOR
	            int portNumber = Integer.parseInt(args[2]);
	            //INICCIALIZAMOS EL CLIENTE CON LOS ARGUMENTOS DEL SERVER, DEL NOMBRE DE USUARIO Y DEL PUERTO
	            cliente = new ChatClientImpl(args[0], args[1], portNumber);
	            //SI NO SE PUEDE, LANZAMOS UNA EXCEPCION 
	            } catch (NumberFormatException e) {
	            	//LANZAMOS EL MENSAJE DE ERROR
	                System.err.println("El argumento del número de puerto debe de ser válido.");
	                //FINALIZAMOS EL PROGRAMA LANZANDO UN NUMERO DIFERENTE DE 0 PARA INDICAR QUE EL PROGRAMA A FINALIZADO 
	                System.exit(1);
	            }
		} 
		
		cliente.start();
	}
	
	private class ChatClientListener implements Runnable{
		
		private ChatClientImpl cliente;
		private ChatMessage mensaje;
		public ChatClientListener() {
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
                while (_carryOn) {
                    mensaje = new ChatMessage(cliente._id, ChatMessage.MessageType.MESSAGE, in.readLine());
                    if (mensaje != null) {
                        cliente.sendMessage(mensaje);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
            	cliente.disconnect();
            }

		}
	}
}
