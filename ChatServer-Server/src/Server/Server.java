package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import Common.Packet;
import Common.PacketType;

public class Server {
	private HashMap<String, Client> clients;
	private int port;
	private boolean listening = true;
	
	private Encryption encryption;
	
	public Server(int port){
		clients = new HashMap<String, Client>();
		this.port = port;
	}
	
	public void start(){
		try {
			encryption = new Encryption();
			ServerSocket server = new ServerSocket(port);
			while(listening){
				Socket socket = server.accept();
				ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
				Packet packet = (Packet)is.readObject();
				String name = packet.from;
				byte[] publicKey = packet.message;
				if(!clients.containsKey(name)){
					Client client = new Client(this, name, publicKey, is, os, socket);
					clients.put(name, client);
					client.start();
					broadcastLogin(name, publicKey);
					sendUsers(name);
				}else{
					Packet p = new Packet();
					p.packetType = PacketType.LOGOUT_BY_SERVER;
					os.writeObject(p);
					os.close();
					is.close();
					socket.close();
				}
			}
			server.close();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void sendMessage(String name,Packet message){
		Client c = clients.get(name);
		if(c!=null){
			c.send(message);
			System.out.println(message.from+"->"+message.target);
		}
	}
	
	public void sendToAll(Packet message){
		String from = message.from;
		String msg = encryption.decryptString(message.message);
		clients.forEach((k, v)->{
			if(!k.equals(from)){
				message.message=encryption.encryptString("<b>" + from + "</b>: " + msg, v.getPublicKey());
				message.from="Global";
				message.target=from;
				v.send(message);
			}
		});
		System.out.println(message.from+"->all");
	}
	
	public void sendFileToAll(Packet message) {
		String from = message.from;
		clients.forEach((k, v)->{
			if(!k.equals(from)){
				message.from="Global";
				v.send(message);
			}
		});
		System.out.println(message.from+"->all");
	}
	
	public void sendUsers(String name){
		Client c = clients.get(name);
		clients.forEach((k, v)->{
			if(!k.equals(name)){
				Packet p = new Packet();
				p.packetType=PacketType.LOGIN;
				p.from=v.getName();
				p.message=v.getPublicKey();
				c.send(p);
			}
		});
		Packet p = new Packet();
		p.from="Global";
		p.packetType=PacketType.LOGIN;
		p.message=encryption.getEncodedOwnPublicKey();
		c.send(p);
	}
	
	public void broadcastLogin(String name, byte[] publicKey){
		Packet p = new Packet();
		p.packetType=PacketType.LOGIN;
		p.from = name;
		p.message = publicKey;
		clients.forEach((k, v)->{if(!k.equals(name))v.send(p);});
	}
	
	public void broadcastLogout(String name){
		Packet p = new Packet();
		p.packetType=PacketType.LOGOUT;
		p.from = name;
		clients.forEach((k, v)->{if(!k.equals(name))v.send(p);});
	}
	
	public void remove(String name){
		clients.remove(name);
	}
}
