import java.net.*;
import java.nio.file.Files;
import java.io.*;
import java.security.*;
import java.security.spec.*;

import javax.crypto.Cipher;

public class Alice {
    private Socket aliceSocket;
    private OutputStream out;
    private InputStream in;

    static PublicKey alicePublic;
    static PrivateKey alicePrivate;
    static PublicKey bobKey;

    static int g = 666;
    static int h = 420;
    static int p = 6661;
    static int messageLength = 256;
    

    public void startConnection(String ip, int port) {
        try {
            aliceSocket = new Socket(ip, port);
            out = aliceSocket.getOutputStream();
            in = aliceSocket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] sendMessage(String msg) {
        try {
            out.write(RSAEncrypt(msg));
            System.out.println("Alice says " + msg);
            byte[] resp = in.readNBytes(messageLength); 
            System.out.println(resp);
            return resp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void stopConnection() {
        try {
            in.close();
            out.close();
            aliceSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static KeyPair generateKeyPair(){
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair pair = gen.generateKeyPair();
            return pair;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] RSAEncrypt(String s){
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, bobKey); //Change to bob key when work hehe
            return encryptCipher.doFinal(s.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] RSADecrypt(byte[] b){
        try {
            Cipher decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, alicePrivate);
            return decryptCipher.doFinal(b);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public void run(){

        KeyPair pair = generateKeyPair();
        alicePublic = pair.getPublic();
        alicePrivate = pair.getPrivate();
        
        try (FileOutputStream fos = new FileOutputStream("alice.key")) {
            fos.write(alicePublic.getEncoded());
            
            System.out.println("Alice key file generated");
            out.write(1);
            System.out.println("alice key entered output stream");
            in.read();
            System.out.println("I can read Bob");

            File publicKeyFile = new File("boblic.key");
            byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            bobKey = keyFactory.generatePublic(publicKeySpec);

            System.out.println("Bob file read");

            sendMessage("hello server");
            RSADecrypt(in.readNBytes(messageLength));
            System.out.println(RSADecrypt(in.readNBytes(messageLength)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Alice client = new Alice();
        client.startConnection("127.0.0.1", 6666);
        client.run();
    }
}