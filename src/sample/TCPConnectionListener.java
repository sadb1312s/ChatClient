package sample;

public interface TCPConnectionListener {

    void onConnectionReady(TCPConnection tcpConnection);
    void onRecieveReady(TCPConnection tcpConnection,String str);
    void onDisconect(TCPConnection tcpConnection);
    void onException(TCPConnection tcpConnection,Exception e);
}