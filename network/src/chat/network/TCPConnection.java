package chat.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPConnection {

    private final Socket socket;
    private  final Thread rxThread;
    private final TCPConnectionListener eventListener;
    private final BufferedReader in;
    private final BufferedWriter out;

    public TCPConnection(TCPConnectionListener eventListener,String ipAddr , int port) throws IOException {
        this(new Socket(ipAddr, port), eventListener);
    }

    public TCPConnection(Socket socket, TCPConnectionListener eventListener) throws IOException{
        this.socket = socket;
        this.eventListener = eventListener;
        socket.getInputStream();
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
        rxThread= new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while (!rxThread.isInterrupted()){
                        String message = in.readLine();
                        eventListener.onReceiveString(TCPConnection.this,in.readLine());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    eventListener.onException(TCPConnection.this,e);
                }finally {
                eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        rxThread.start();

    }


    public  synchronized void sendString (String value){
        try {
            out.write(value+"\r\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            eventListener.onException(TCPConnection.this,e);
            disconnect();
        }

    }



    public  synchronized void disconnect(){
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            eventListener.onException(TCPConnection.this,e);
        }

    }

    @Override
    public String toString(){
        return "TCPConnection: " + socket.getInetAddress() +": "+socket.getPort();
    }



}
