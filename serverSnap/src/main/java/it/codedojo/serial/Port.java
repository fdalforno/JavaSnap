package it.codedojo.serial;

public class Port {
	public enum PortType {SERIAL,PARALLEL,IIC,RAW,RS485,UNKNOWN,ALL}
	
	private String name;
	private PortType type;
	private boolean owned;
	
	public boolean isOwned() {
		return owned;
	}
	public void setOwned(boolean owned) {
		this.owned = owned;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public PortType getType() {
		return type;
	}
	public void setType(PortType type) {
		this.type = type;
	}
	
	
	@Override
	public String toString() {
		return "Port [name=" + name + ", type=" + type + ", owned=" + owned + "]";
	};
	
}
