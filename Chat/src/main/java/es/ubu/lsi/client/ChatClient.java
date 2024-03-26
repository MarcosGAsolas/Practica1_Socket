package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

public interface ChatClient {

	//INICIALIZADOR DEL CLIENTE
	public boolean start();
	//ENVIAR MENSAJE DEL CLIENTE
	public void sendMessage(ChatMessage msg);
	//DESCONECTAR EL CLIENTE
	public void disconnect();
}
