import java.net.*;
import java.nio.file.Files;
import java.io.*;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.net.ssl.*;

public class Bob {
    /* SSL Testing */
    // private SSLServerSocket serverSocket2;
    // private SSLSocket bobSocket2;

    private ServerSocket serverSocket;
    private Socket bobSocket;


    private OutputStream out;
    private InputStream in;

    private int k = 100;
    static int g = 666;
    static int h = 420;
    static int p = 6661;
    static int messageLength = 256;

    static PublicKey Boblic;
    static PrivateKey Brivate;
    static PublicKey aliceKey;

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            bobSocket = serverSocket.accept();
            out = bobSocket.getOutputStream();
            in = bobSocket.getInputStream();
        
    } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            in.close();
            out.close();
            bobSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
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
            encryptCipher.init(Cipher.ENCRYPT_MODE, aliceKey); //Change to bob key when work hehe
            return encryptCipher.doFinal(s.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] RSADecrypt(byte[] b){
        try {
            Cipher decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, Brivate);
            return decryptCipher.doFinal(b);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public void run(){

        KeyPair pair = generateKeyPair();
        Boblic = pair.getPublic();
        Brivate = pair.getPrivate();

        try (FileOutputStream fos = new FileOutputStream("boblic.key")) {
            fos.write(Boblic.getEncoded());
            System.out.println("Made boblic file");
            
            out.write(1);
            System.out.println("Boblic stuff entered output stream");
            in.read();
            System.out.println("I can read Alice");

            File publicKeyFile = new File("alice.key");
            byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            aliceKey = keyFactory.generatePublic(publicKeySpec);
            System.out.println("Alice file read");


            RSADecrypt(in.readNBytes(messageLength));
            System.out.println("Alice says: Hello Bob");
            out.write(RSAEncrypt("hello client"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] hashCommit(){
        String s = "";
        int x;
        for(int i = 0; i<k; i++){
            x = (int)Math.round(Math.random());
            s += Integer.toString(x);
        }
        System.out.println(s);
    return s.getBytes();
    }

    public int roll(){
        return (int)(Math.random()*6)+1;
    }

    
    public static void main(String[] args) {
        Bob bob = new Bob();
        bob.start(6666);
        bob.run();
    }
}