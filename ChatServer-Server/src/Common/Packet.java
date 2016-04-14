package Common;

import java.io.Serializable;

public class Packet implements Serializable{
	private static final long serialVersionUID = 9078917536788485396L;
	public PacketType packetType;
	public byte[] message;
	public String target;
	public String from;
	public String filename;
}
