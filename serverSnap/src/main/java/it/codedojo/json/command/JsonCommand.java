package it.codedojo.json.command;

public class JsonCommand {
	public enum CommandType {MESSAGE,EVENT,PRESENT};
	
	public static final String BROADCAST = "BROADCAST";
	
	private CommandType type;
	private String message;
	private String from;
	private String to;
	
	public CommandType getType() {
		return type;
	}
	public void setType(CommandType type) {
		this.type = type;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
}
