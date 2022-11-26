
import java.io.*;
import java.nio.*;
import java.net.*;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ThreadHandle extends Thread {

    private SocketChannel clientChSock;
    private Socket client;
    private final File[] file = {new File("Wish.mp3"), new File("Itachi.jpeg")}; //ทำหน้าที่เก็บลิสต์ไฟล์
    private DataInputStream datainput; //รับ
    private DataOutputStream dataoutput; //ส่ง

    public ThreadHandle(SocketChannel clientChSock, Socket client) {
        this.clientChSock = clientChSock;
        this.client = client;
    }

    @Override
    public void run() {

        try {
            datainput = new DataInputStream(client.getInputStream()); //รับข้อมูล
            dataoutput = new DataOutputStream(client.getOutputStream()); //ส่งข้อมูล

            dataoutput.writeInt(file.length); //ส่งขนาดไฟล์ทั้งหมดไปหา client
            for (int i = 0; i < file.length; i++) {
                dataoutput.writeUTF(file[i].getName());//ส่งชื่อไฟล์ไปหาclientทั้งหมด
            }

            int choosefile = datainput.readInt(); //อ่านไฟล์ที่ต้องการจาก client
            System.out.println("Select File : " + file[choosefile - 1].getName());
            int choice = datainput.readInt(); //อ่านวิธีที่ต้องการโหลด
            dataoutput.writeLong(file[choosefile - 1].length()); //ส่งขนาดไฟล์ทั้งหมดไป

            if (choice == 1) {
                System.out.println("Normal Copy");
                normalcopy(file[choosefile - 1].getAbsolutePath(), file[choosefile - 1].length());
                System.out.println("Finished");
                System.out.println("--------------------------------------------");
            } else {
                System.out.println("Zero Copy");
                zeroCopy(file[choosefile - 1].getAbsolutePath(), file[choosefile - 1].length());
                System.out.println("Finished");
                System.out.println("--------------------------------------------");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void normalcopy(String pathFile, long sizeFile) {
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(pathFile));  //เก็บลงที่ buffer
            byte[] buffer = new byte[1024]; //เปลี่ยนเป็น 100000
            long currentRead = 0;
            while (currentRead < sizeFile) {
                int read = bis.read(buffer);//อ่านที่ละ 100000
                currentRead += read; //ทำการบวกค่าข้อมูลที่อ่านแล้ว
                dataoutput.write(buffer, 0, read); //เขียนข้อมูลตั้งแต่เริ่มต้นจนถึง read   
            }
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void zeroCopy(String pathFile, long sizeFile) {
        try {

            FileChannel inChannel = new FileInputStream(pathFile).getChannel();

            long sendByte = 0;

            while (sendByte < sizeFile) {
                long send = inChannel.transferTo(sendByte, sizeFile - sendByte, clientChSock);
                sendByte += send;
            }

            clientChSock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
