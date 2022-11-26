import java.io.IOException;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class Server {

    public static void main(String[] args) {
        ArrayList<ThreadHandle> clients = new ArrayList<>();
        ServerSocketChannel serverSocketChannel = null;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(5555);
            serverSocketChannel = ServerSocketChannel.open(); // เปิด port
            serverSocketChannel.bind(new InetSocketAddress(8888)); // ผูกกับport 8888
            System.out.println("Waiting for client ...");
            int n = 0;
            while (true) {
                // Client เชื่อมต่อมา
                SocketChannel clientChannel = serverSocketChannel.accept();
                Socket client = serverSocket.accept();
                n++;
                System.out.println("Client NO." + n );
                System.out.println("Address  IP : " 
                        + clientChannel.socket().getInetAddress().getHostAddress() 
                        + " Connect to Server");
                
                // create a new thread object
                clients.add(new ThreadHandle(clientChannel, client));
                clients.get(clients.size() - 1).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocketChannel != null) {
                try {
                    serverSocketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}