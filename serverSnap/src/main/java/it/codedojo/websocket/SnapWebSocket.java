package it.codedojo.websocket;

import java.io.IOException;
import java.util.Iterator;
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

@WebSocket
public class SnapWebSocket {

    // Store sessions if you want to, for example, broadcast a message to all users
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    private static final Logger log = LoggerFactory.getLogger(SnapWebSocket.class);
    private static final Gson gson = new Gson();
    
    
    
    /**
     * Questo metodo invia a tutti i client un oggetto serializzato
     * @param data oggetto da serializzare
     */
    public static void sendMessage(Object data){
    	for (Iterator<Session> iterator = sessions.iterator(); iterator.hasNext();) {
			Session session =  iterator.next();
			if(session.isOpen()){
				try {
					session.getRemote().sendString(gson.toJson(data));
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}
    }

    @OnWebSocketConnect
    public void connected(Session session) {
        sessions.add(session);
        
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
    }
    
    @OnWebSocketError 
    public void onErrorReceived(Throwable t) {
    	log.error("Error :", t);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
    	log.debug("Got: " + message);   // Print message
        
    }

}
