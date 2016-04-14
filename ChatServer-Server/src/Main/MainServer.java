package Main;

import Server.Server;

public class MainServer {
	public static void main(String[] args) {
		Server s = new Server(24498);
		s.start();
	}
}
