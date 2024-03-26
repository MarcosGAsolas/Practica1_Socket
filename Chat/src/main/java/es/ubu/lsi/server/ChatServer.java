package es.ubu.lsi.server;

import es.ubu.lsi.common.ChatMessage;

public interface ChatServer {

	//INICIALIZA EL SERVER
	public void startup();
	//DESCONECTA EL SERVER
	public void shutdown();
	//MENSAJES DEL SERVER
	public void brodcast(ChatMessage message);
	//REMOVE
	public void remove(int id);
	
}
