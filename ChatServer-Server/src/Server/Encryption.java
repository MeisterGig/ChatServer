package Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Encryption {
	
	public static PrivateKey ownPrivateKey;
	public static PublicKey ownPublicKey;
	public static Cipher cipher;
	
	public Encryption(){
		init();
	}
	
	public byte[] encrypt(byte[] data, byte[] publicKey){
		try {
			cipher.init(Cipher.ENCRYPT_MODE, decodePublicKey(publicKey));
			return cipher.doFinal(data);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public byte[] decrypt(byte[] data){
		try {
			cipher.init(Cipher.DECRYPT_MODE, ownPrivateKey);
			return cipher.doFinal(data);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return data;	
	}
	
	public byte[] encryptString(String data, byte[] publicKey){
		return encrypt(data.getBytes(), publicKey);
	}
	
	public String decryptString(byte[] data){
		return new String(decrypt(data));
	}

	private void init() {		
		try {
			File dirChat = new File("Chat");
			if(dirChat.exists()== false){
				dirChat.mkdir();
			}
			File dirHistory = new File("Chat/keys");
			if(dirHistory.exists()== false){
			    dirHistory.mkdir();
			}
			
			if(!(new File("Chat/keys/publicKey.key").exists()&&new File("Chat/keys/privateKey.key").exists())){
				KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
				kpg.initialize(2048);
				KeyPair kp = kpg.generateKeyPair();
				ownPrivateKey = kp.getPrivate();
				ownPublicKey = kp.getPublic();
				savePrivateKey(ownPrivateKey);
				savePublicKey(ownPublicKey);
			}else{
				ownPrivateKey = loadPrivateKey();
				ownPublicKey = loadPublicKey();
			}
			cipher = Cipher.getInstance("RSA");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}
	
	private void savePrivateKey(PrivateKey key){
		try {
			File f = new File("Chat/keys/privateKey.key");
			if(!f.exists())f.createNewFile();
			PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(key.getEncoded());
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(encodedKeySpec.getEncoded());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void savePublicKey(PublicKey key){
		try {
			File f = new File("Chat/keys/publicKey.key");
			if(!f.exists())f.createNewFile();
			X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(key.getEncoded());
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(encodedKeySpec.getEncoded());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private PrivateKey loadPrivateKey(){
		try {
			File file = new File("Chat/keys/privateKey.key");
			FileInputStream fis = new FileInputStream("Chat/keys/privateKey.key");
			byte[] encodedKey = new byte[(int) file.length()];
			fis.read(encodedKey);
			fis.close();
			PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(encodedKey);
			KeyFactory factory = KeyFactory.getInstance("RSA");
			return factory.generatePrivate(encodedKeySpec);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private PublicKey loadPublicKey(){
		try {
			File file = new File("Chat/keys/publicKey.key");
			FileInputStream fis = new FileInputStream("Chat/keys/publicKey.key");
			byte[] encodedKey = new byte[(int) file.length()];
			fis.read(encodedKey);
			fis.close();
			X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(encodedKey);
			KeyFactory factory = KeyFactory.getInstance("RSA");
			return factory.generatePublic(encodedKeySpec);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] encodePublicKey(PublicKey key){
		X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(key.getEncoded());
		return encodedKeySpec.getEncoded();
	}
	
	public static PublicKey decodePublicKey(byte[] key){		
		try {
			X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(key);
			KeyFactory factory = KeyFactory.getInstance("RSA");
			return factory.generatePublic(encodedKeySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] getEncodedOwnPublicKey(){
		return encodePublicKey(ownPublicKey);
	}
}
