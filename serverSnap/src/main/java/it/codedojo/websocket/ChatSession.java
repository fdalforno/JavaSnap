package it.codedojo.websocket;

import org.eclipse.jetty.websocket.api.Session;

public class ChatSession {
	private Session session;
	private String name;
	
	public Session getSession() {
		return session;
	}
	public void setSession(Session session) {
		this.session = session;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
