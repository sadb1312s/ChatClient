package sample;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPConnection {

    private final Socket socket;
    private final Thread rxThread;//поток который слушает поток ввода
    private final BufferedReader in;
    private final BufferedWriter out;
    private final TCPConnectionListener eventListener;


    public TCPConnection(TCPConnectionListener eventListener,String IpAddr,int Port) throws IOException{



        this(eventListener,new Socket(IpAddr,Port));


    }

    public TCPConnection(TCPConnectionListener eventListener,Socket socket) throws IOException{





        this.socket=socket;
        this.eventListener=eventListener;

        //входящие и исходящие
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),Charset.forName("UTF-8")));
        //слущающий поток
        //анонимный класс
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    eventListener.onConnectionReady(TCPConnection.this);
                    while (!rxThread.isInterrupted()){
                        eventListener.onRecieveReady(TCPConnection.this, in.readLine());
                    }
                }catch (IOException e){
                    eventListener.onException(TCPConnection.this,e);
                } finally {
                    eventListener.onDisconect(TCPConnection.this);
                }
            }
        });
        rxThread.setDaemon(true);
        rxThread.start();
    }

    public synchronized void sendString(String str){

                try {
                    out.write(str+"\r\n");
                    out.flush();
                } catch (IOException e) {
                    eventListener.onException(TCPConnection.this,e);
                    disconnect();
                }

    }
    public synchronized void disconnect(){
        //остонавливаем поток
        rxThread.interrupt();
        //закрываем сокет
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this,e);
        }
    }
    @Override
    public String toString() {
        return "TCP Connection: "+socket.getInetAddress()+": "+socket.getPort();
    }

}