import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Scanner;

public class Client {

    private Socket client;
    private SocketChannel clientChSock;
    private DataInputStream datainput;
    private DataOutputStream dataoutput;
    private String[] file;

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.ClientToServer();

    }

    private void ClientToServer() {
        try {
            client = new Socket("192.168.56.1", 5555); // IP + port
            clientChSock = SocketChannel.open(new InetSocketAddress("192.168.56.1", 8888));
            datainput = new DataInputStream(client.getInputStream());
            dataoutput = new DataOutputStream(client.getOutputStream());
            Scanner sc = new Scanner(System.in);
            
            // รายชื่อไฟล์ใน Server 
            int filecount = datainput.readInt();
            file = new String[filecount];
            System.out.println("Server's file");
            for (int i = 0; i < filecount; i++) {
                file[i] = datainput.readUTF();
                System.out.println("No." + (i + 1) + " file name : " + file[i]);
            }
            
            System.out.print("\nEnter the file number to download : ");
            
            // เลือกว่าจะใช้การโหลดแบบไหน
            int choosefile = sc.nextInt();
            dataoutput.writeInt(choosefile);
            System.out.println("\nEnter 1 Download with Normal copy.");
            System.out.println("Enter 2 Download with Zero copy.");
    
            int choice = sc.nextInt();
            dataoutput.writeInt(choice);
            String path = file[choosefile - 1];
            long size = datainput.readLong();
            
            if (choice == 1) { // 1 = Normal
                int start = (int) System.currentTimeMillis();
                System.out.println("Waiting ...");
                normalCopy(path, size);
                int end = (int) System.currentTimeMillis();
                System.out.println("Normal Copy Finished : Times = " + (end - start) + "ms.");
                
            } else { // 2 = ZeroCopy
                int start = (int) System.currentTimeMillis();
                System.out.println("Waiting ...");
                zeroCopy(path, size);
                int end = (int) System.currentTimeMillis();
                System.out.println("Zero Copy Finished : Times = " + (end - start) + "ms.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void normalCopy(String pathFile, long sizeFile) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pathFile));
            byte[] buffer = new byte[1024];
            long currentRead = 0;
            while (currentRead < sizeFile) {
                int read = datainput.read(buffer);
                currentRead += read;
                bos.write(buffer, 0, read);
                System.out.println("Waiting... : " + read);
            }
            bos.close(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void zeroCopy(String pathFile, long sizeFile) {
        try {
            FileChannel fileChannel = new FileOutputStream(pathFile).getChannel(); // ใช้fileCH เพราะ ByteBuffer ไม่สามารถเชื่อมต่อกับแฟ้มข้อมูลโดยตรง เลยต้องใช้fileCH เป็นทางเชื่อมระหว่างByteBufferกับfileOutPutStream
            int currentRead = 0;
            while (currentRead < sizeFile) {
                long read = fileChannel.transferFrom(clientChSock, currentRead, sizeFile - currentRead); // รับไฟล์มาจากพวกนี้ไปใส่ใน read
                currentRead += read;
                System.out.println("Waiting... : " + read);
            }
            fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

  

