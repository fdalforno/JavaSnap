package it.codedojo.json;

public class JsonResponse<T> {
	public enum ResponseType {OK,ERROR,EXCEPTION};
	
	private ResponseType type;
	private String message;
	private T data;
	
	public ResponseType getType() {
		return type;
	}
	public void setType(ResponseType type) {
		this.type = type;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
}
