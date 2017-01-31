package it.codedojo.websocket;

import java.io.IOException;
import java.security.GuardedObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import it.codedojo.json.command.JsonCommand;
import it.codedojo.json.command.JsonCommand.CommandType;

@WebSocket
public class SnapWebSocket {


	private static String guestUser = "GUEST";

	private static Queue<ChatSession> sessions = new ConcurrentLinkedQueue<>();
	private static final Logger log = LoggerFactory.getLogger(SnapWebSocket.class);
	private static final Gson gson = new Gson();



	/**
	 * Questo metodo invia a tutti i client un oggetto serializzato
	 * @param data oggetto da serializzare
	 */
	public static void sendMessage(Object data){
		for (Iterator<ChatSession> iterator = sessions.iterator(); iterator.hasNext();) {
			ChatSession chat =  iterator.next();
			Session session = chat.getSession();

			if(session.isOpen()){
				try {
					session.getRemote().sendString(gson.toJson(data));
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}
	}


	public static void sendMessage(Object data,ChatSession chatSession){
		Session session = chatSession.getSession();
		if(session.isOpen()){
			try {
				session.getRemote().sendString(gson.toJson(data));
			} catch (IOException e) {
				log.error(e.getMessage());
			}

		}
	}
	
	
	public static List<String>getChildren(){
		ArrayList<String> children = new ArrayList<String>();
		for (Iterator<ChatSession> iterator = sessions.iterator(); iterator.hasNext();) {
			ChatSession chat =  iterator.next();
			Session sess = chat.getSession();
			if(sess.isOpen() && !chat.getName().equals(guestUser)){
				children.add(chat.getName());
			}
		}
		
		return children;
	}


	private ChatSession getChatSession(Session session){
		for (Iterator<ChatSession> iterator = sessions.iterator(); iterator.hasNext();) {
			ChatSession chat =  iterator.next();
			Session sess = chat.getSession();
			if(sess.equals(session)){
				return chat;
			}
		}

		return null;
	}

	private ChatSession getChatSession(String name){
		for (Iterator<ChatSession> iterator = sessions.iterator(); iterator.hasNext();) {
			ChatSession chat =  iterator.next();
			
			if(chat.getName().equals(name)){
				return chat;
			}
		}

		return null;
	}


	@OnWebSocketConnect
	public void connected(Session session) {
		ChatSession chat = new ChatSession();
		chat.setSession(session);
		chat.setName(guestUser);
		sessions.add(chat);
	}

	@OnWebSocketClose
	public void closed(Session session, int statusCode, String reason) {
		ChatSession chat = getChatSession(session);
		
		if(! chat.getName().equals(guestUser)){
			JsonCommand response = new JsonCommand();
			response.setType(CommandType.EVENT);
			response.setTo(JsonCommand.BROADCAST);
			response.setMessage(chat.getName() + " leaved chat room");
			SnapWebSocket.sendMessage(response);
		}
		
		
		sessions.remove(chat);
	}

	@OnWebSocketError 
	public void onErrorReceived(Throwable t) {
		log.error("Error :", t);
	}

	@OnWebSocketMessage
	public void message(Session session, String message) throws IOException {
		log.debug("Got: " + message);  


		JsonCommand response = new JsonCommand();
		ChatSession chat = getChatSession(session);

		JsonCommand command = gson.fromJson(message, JsonCommand.class);
		CommandType type = command.getType();

		String from = command.getFrom();
		String to = command.getTo();
		String chatMessage = command.getMessage();

		switch (type) {
		case PRESENT:
			chat.setName(from);
			response.setType(CommandType.EVENT);
			response.setTo(JsonCommand.BROADCAST);
			response.setMessage("new child in room " + from);
			SnapWebSocket.sendMessage(response);
			break;
		case MESSAGE:
		case EVENT:
			response.setType(type);
			response.setMessage(chatMessage);
			
			response.setFrom(from);

			if(to.equals(JsonCommand.BROADCAST)){
				response.setTo(JsonCommand.BROADCAST);
				SnapWebSocket.sendMessage(response);
			}else{
				ChatSession toChat = getChatSession(to);
				if(toChat != null){
					response.setTo(to);
					SnapWebSocket.sendMessage(response,toChat);
				}
			}


		default:
			break;
		}

	}

}
