package sample;

import javafx.concurrent.Task;

import java.io.IOException;

import static sample.Controller.connect;

public class Check extends Task implements TCPConnectionListener {

    String ip;
    int port;
    public TCPConnection Connection;


    public Check(String ip,int port){
        this.ip=ip;
        this.port=port;
    }

    @Override
    public Void call() {
        System.out.println("Тестовая проверка подключения");
        try {
            Connection = new TCPConnection(this,ip,port);
            connect=true;
        } catch (IOException e) {
            System.out.println("Тест не пройден");
            connect=false;
        }
        Connection.disconnect();
        return null;


    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        System.out.println("------------------------------------");
    }

    @Override
    public void onRecieveReady(TCPConnection tcpConnection, String str) {
        System.out.println("Тестовое подключение прошло успешно");
    }

    @Override
    public void onDisconect(TCPConnection tcpConnection) {

    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {

    }
}
