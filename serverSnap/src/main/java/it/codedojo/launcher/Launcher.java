/**
 * 
 */
package it.codedojo.launcher;

import static spark.Spark.get;
import static spark.Spark.init;
import static spark.Spark.port;
import static spark.Spark.staticFiles;
import static spark.Spark.webSocket;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.codedojo.json.JsonResponse;
import it.codedojo.json.JsonResponse.ResponseType;
import it.codedojo.serial.Port;
import it.codedojo.serial.Port.PortType;
import it.codedojo.serial.SerialException;
import it.codedojo.serial.SerialUtils;
import it.codedojo.transformer.JsonTransformer;
import it.codedojo.websocket.SnapWebSocket;

/**
 * @author DalFornoF
 *
 */
public class Launcher {
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {

		Logger log = LoggerFactory.getLogger(Launcher.class);
		SerialUtils utils = new SerialUtils();

		

		int port = 8000;
		String defaultBaud = "9600";
		String defaultPortName = null;

		Options options = new Options();
		options.addOption("serial", true, "serial port name");
		options.addOption("baud", true, "serial baud rate");
		options.addOption("port", true, "server port");
		options.addOption("help", "print this message");





		CommandLineParser parser = new DefaultParser();

		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			if(cmd.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("Launcher", options);
				System.exit(0);
			}

			if(cmd.hasOption("port")) {
				String value = cmd.getOptionValue("port");
				if(StringUtils.isNumeric(value)){
					port = Integer.parseInt(value);
				}
			}

			if(cmd.hasOption("baud")) {
				String value = cmd.getOptionValue("baud");
				if(StringUtils.isNumeric(value)){
					defaultBaud = value;
				}
			}

			if(cmd.hasOption("serial")) {
				String value = cmd.getOptionValue("serial");
				if(StringUtils.isAlphanumeric(value)){
					defaultPortName = value;
				}
			}


			if(StringUtils.isNotBlank(defaultPortName)){
				try {
					utils.openPort(defaultPortName, defaultBaud);
				} catch (SerialException e) {
					log.error("Error opening " + defaultPortName + " : " + e.getLocalizedMessage());
				}
			}

		} catch (ParseException e) {
			log.error(e.getLocalizedMessage());
		}


		staticFiles.location("/public");
		staticFiles.header("Access-Control-Allow-Origin", "*");


		port(port);

		/**
		 * Gestione WebSocket
		 */
		log.debug("Initialize Web Socket");
		webSocket("/snapWebSocket", SnapWebSocket.class);
		init();

		
		/**
		 * per testare le chiamate
		 */
		get("/echo/:message", (req, res) -> {
			JsonResponse<String> response = new JsonResponse<String>();
			response.setType(ResponseType.OK);
			
			
			String message = req.params(":message");
			
			if(message.equalsIgnoreCase("Error")){
				response.setType(ResponseType.ERROR);
			}
			
			if(message.equalsIgnoreCase("Exception")){
				response.setType(ResponseType.EXCEPTION);
			}
			response.setMessage(message);
			
			return response;
		}, new JsonTransformer());
		
		
		get("/listChildren", (req, res) -> {
			JsonResponse<List<String>> response = new JsonResponse<List<String>>();
			List<String> children = SnapWebSocket.getChildren();
			response.setData(children);
			response.setType(ResponseType.OK);
			return response; 
		}, new JsonTransformer());

		get("/listPort", (req, res) -> {
			
			JsonResponse<List<Port>> response = new JsonResponse<List<Port>>();
			List<Port> ports = utils.searchForPorts(PortType.SERIAL);
			
			response.setData(ports);
			response.setType(ResponseType.OK);
			
			return response; 
		}, new JsonTransformer());


		get("/connectPort/:port/:baud", (req, res) -> {
			String portName = req.params(":port");
			String baud = req.params(":baud");

			JsonResponse<String> response = new JsonResponse<String>();

			if(StringUtils.isEmpty(baud) || ! StringUtils.isNumeric(baud)){
				log.error("Parameter baud is not corrected |" + baud + "|");

				response.setType(ResponseType.EXCEPTION);
				response.setMessage("Wrong baud parameter");
				return response;
			}

			boolean find = false;

			List<Port> ports = utils.searchForPorts(PortType.SERIAL);
			for (Iterator<Port> iterator = ports.iterator(); iterator.hasNext();) {
				Port serialPort = (Port) iterator.next();
				if(serialPort.getName().equals(portName)){
					find = true;
					break;
				}
			}


			if(! find){
				log.error("Port |" + portName + "| not found");
				response.setType(ResponseType.EXCEPTION);
				response.setMessage("Wrong port parameter");
				return response;
			}

			utils.openPort(portName, baud);

			response.setType(ResponseType.OK);
			response.setMessage("Connected");

			return response;
		}, new JsonTransformer());


		get("/command", (req, res) -> {
			String command = req.queryParams("command");

			JsonResponse<String> response = new JsonResponse<String>();

			try{
				String value = utils.send(command);
				response.setType(ResponseType.OK);
				response.setData(value);
			} catch(SerialException se){
				response.setType(ResponseType.EXCEPTION);
				response.setMessage(se.getMessage());
			}

			return response;
		}, new JsonTransformer());


		get("/disconnectPort", (req, res) -> {
			JsonResponse<String> response = new JsonResponse<String>();

			utils.close();


			response.setType(ResponseType.OK);
			response.setData("Disconnected");

			return response;
		}, new JsonTransformer());


	}

}
