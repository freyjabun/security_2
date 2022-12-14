import java.net.*;
import java.nio.file.Files;
import java.io.*;
import java.security.*;
import java.security.spec.*;

import javax.crypto.Cipher;
import javax.net.ssl.SSLSocket;

public class Alice {
    private int k = 100;

    private Socket aliceSocket;
    private OutputStream out;
    private InputStream in;

    static PublicKey alicePublic;
    static PrivateKey alicePrivate;
    static PublicKey bobKey;

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

    public void sendMessage(String msg) {
        try {
            out.write(RSAEncrypt(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            encryptCipher.init(Cipher.ENCRYPT_MODE, bobKey);
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
            System.out.println("Bob tells you: Hello Alice");

            int m = roll();
            String r = sampleKBitString();
            String commit = r + Integer.toBinaryString(m);
            
            //Sending commit
            sendMessage(Integer.toString(commit.hashCode()));
            System.out.println("Sending commit to Bob");

            //Receiving Bob's roll
            System.out.println("Recieved Bob's roll");
            var bobRoll = RSADecrypt(in.readNBytes(messageLength));
            //Doesn't need to be saved, but if I wanted to print it in Alice's terminal that is useful maybe

            sendMessage(r);
            sendMessage(Integer.toBinaryString(m));
            System.out.println("Sending r|m to Bob");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int roll(){
        return (int)(Math.random()*6)+1;
    }

    public String sampleKBitString(){
        String s = "";
        int x;
        for(int i = 0; i<k; i++){
            x = (int)Math.round(Math.random());
            s += Integer.toString(x);
        }
        System.out.println(s);
    return s;
    }

    public static void main(String[] args) {
        Alice client = new Alice();
        client.startConnection("127.0.0.1", 6666);
        client.run();
    }
}