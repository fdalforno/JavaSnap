package it.codedojo.serial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import it.codedojo.serial.Port.PortType;

public class SerialUtils implements SerialPortEventListener{

	Logger log = LoggerFactory.getLogger(SerialUtils.class);

	private SerialPort serialPort;
	private OutputStream outStream;
	private InputStream inStream;


	private PrintWriter writer;
	private BufferedReader reader;

	@SuppressWarnings("rawtypes")
	public List<Port> searchForPorts(PortType type){
		Enumeration portIdentifiers = CommPortIdentifier.getPortIdentifiers();

		List<Port> result = new ArrayList<Port>();

		while (portIdentifiers.hasMoreElements()) {
			CommPortIdentifier pid = (CommPortIdentifier) portIdentifiers.nextElement();

			Port port = new Port();
			port.setName(pid.getName());
			port.setOwned(pid.isCurrentlyOwned());

			switch (pid.getPortType()) {

			case CommPortIdentifier.PORT_RS485:
				port.setType(PortType.RS485);
				break;

			case CommPortIdentifier.PORT_SERIAL:
				port.setType(PortType.SERIAL);
				break;

			case CommPortIdentifier.PORT_PARALLEL:
				port.setType(PortType.PARALLEL);
				break;

			case CommPortIdentifier.PORT_I2C:
				port.setType(PortType.IIC);
				break;

			case CommPortIdentifier.PORT_RAW:
				port.setType(PortType.RAW);
				break;	

			default:
				port.setType(PortType.UNKNOWN);
				break;
			}

			if(port.getType().equals(type) || type.equals(PortType.ALL)){
				result.add(port);				
			}

		}
		return result;
	}

	public boolean isOpen(){
		return serialPort != null;
	}
	
	
	public void openPort(String portName,String baudValue) throws SerialException{
		try {
			CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(portName);

			
			
			serialPort = (SerialPort) portId.open(portName, 5000);
			int baudRate = Integer.parseInt(baudValue);

			serialPort.setSerialPortParams(
					baudRate,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			
			
			
			try {
				serialPort.addEventListener(this);
			} catch (TooManyListenersException e) {
				log.error("Error",e);
				throw new SerialException(e.getLocalizedMessage());
			}


			outStream = serialPort.getOutputStream();
			inStream = serialPort.getInputStream();

			reader = new BufferedReader(new InputStreamReader(inStream));
			writer = new PrintWriter(outStream, true);

		} catch (NoSuchPortException e) {
			log.error("Error",e);
			throw new SerialException(e.getLocalizedMessage());
		} catch (PortInUseException e) {
			log.error("Error",e);
			throw new SerialException(e.getLocalizedMessage());
		} catch (UnsupportedCommOperationException e) {
			log.error("Error",e);
			throw new SerialException(e.getLocalizedMessage());
		} catch (IOException e) {
			log.error("Error",e);
			serialPort.close();
			serialPort = null;
			throw new SerialException(e.getLocalizedMessage());
		}
	}

	public String send(String value) throws SerialException{
		String response;
		
		if(writer != null){
			writer.println(value);
			serialPort.notifyOnDataAvailable(false);

			boolean valid = true;
			try{
				while(reader.ready() == false){};
				do{
					response = reader.readLine();

					if(response.length() >= 1){
						valid = response.charAt(0) != '!';
					}
				}while(valid == false);
			}catch(IOException e){
				log.error("IOException, error reading from port",e);
				throw new SerialException("IOException, error reading from port");
			}

			serialPort.notifyOnDataAvailable(true);  
			
		}else{
			throw new SerialException("Serialport not opened");
		}
		
		return response;
	}

	public String sendRPC(String name, String method, String[] args) throws SerialException{

		
		String arguments = "";
		if(args != null){
			int s = args.length;
			for(int i = 0; i < s; i++){
				arguments = arguments + " " + args[i];
			}
		}

		return send("/" + name + "/" + method + arguments);
	}




	/**
	 * Get the serial port input stream
	 * @return The serial port input stream
	 */
	public InputStream getSerialInputStream() {
		return inStream;
	}

	/**
	 * Get the serial port output stream
	 * @return The serial port output stream
	 */
	public OutputStream getSerialOutputStream() {
		return outStream;
	}

	public void close() {
		if(serialPort != null){
			serialPort.close();
		}
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		log.info("Serial Port event " + event.getEventType());
		switch (event.getEventType()) {
		 	case SerialPortEvent.DATA_AVAILABLE:
			try {
				while (! reader.ready()) {}
				String value = reader.readLine();
				
				log.info("event data " + value);
		 		 
			} catch (IOException e) {
				log.error("IOException " + e.getMessage(),e);
			}
		}
	}

}
