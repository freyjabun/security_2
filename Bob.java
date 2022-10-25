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

            String m = Integer.toBinaryString(roll());
            //Receiving Alice's commit
            var aliceCommit = RSADecrypt(in.readNBytes(messageLength));
            System.out.println("Received Alice's commit");
            //Sending sampled dice roll
            out.write(RSAEncrypt(m));
            System.out.println("Sending roll");

            var aliceR = RSADecrypt(in.readNBytes(messageLength));
            var aliceM = RSADecrypt(in.readNBytes(messageLength));

            String aliceRString = new String(aliceR);
            String aliceMString = new String(aliceM);

            String aliceRM = aliceRString + aliceMString;

            System.out.println("Received r|m from Alice");

            String aliceCommitString = new String(aliceCommit);


            if (Integer.parseInt(aliceCommitString) == aliceRM.hashCode()){
                System.out.println("Commit checks out");
                var diceRoll = (Integer.parseInt(m,2) ^ Integer.parseInt(aliceMString, 2) % 6) + 1;
                System.out.println("The dice shows an exciting and cool: " + diceRoll);
            } 
            else System.out.println("Commit and message doesn't add up, killing myself immediately");

        } catch (Exception e) {
            e.printStackTrace();
        }
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