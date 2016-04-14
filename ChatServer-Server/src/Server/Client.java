package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Common.Packet;
import Common.PacketType;

public class Client {
	private Server server;
	private String name;
	private ObjectInputStream is;
	private ObjectOutputStream os;
	private Socket socket;
	private boolean running = true;
	
	private byte[] publicKey;
	
	public Client(Server server, String name, byte[] publicKey, ObjectInputStream is, ObjectOutputStream os, Socket socket) {
		this.server = server;
		this.name = name;
		this.publicKey = publicKey;
		this.is = is;
		this.os = os;
		this.socket = socket;
	}
	
	public void start(){
		new Thread(new Runnable() {			
			public void run() {
				while(running){
					try {
						Packet p = (Packet) is.readObject();
						if(p.packetType==PacketType.LOGOUT_BY_SERVER){
							running = false;
							server.remove(name);
							is.close();
							os.close();
							socket.close();
							server.broadcastLogout(name);
						}else if(p.packetType==PacketType.MESSAGE){
							if(p.target.equals("Global")){
								server.sendToAll(p);
							}else{
								server.sendMessage(p.target, p);
							}
						}else if(p.packetType==PacketType.FILE){
							if(p.target.equals("Global")){
								server.sendFileToAll(p);
							}else{
								server.sendMessage(p.target, p);
							}
						}
					} catch (ClassNotFoundException | IOException e) {
						running = false;
						server.remove(name);
						server.broadcastLogout(name);
					}
				}
			}
		}).start();
	}

	public void send(Packet message) {
		try {
			os.writeObject(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] getPublicKey(){
		return publicKey;
	}
	
	public String getName(){
		return name;
	}
}
