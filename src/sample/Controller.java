package sample;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.security.Security;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import javax.crypto.Cipher;


public class Controller implements TCPConnectionListener{

    @FXML
    private AnchorPane TopPane;
    @FXML
    private Circle Status;
    @FXML
    private TextField nickName;
    @FXML
    private StackPane getNamePane;
    @FXML
    private TextFlow allMessage;
    @FXML
    private TextField outMessage;
    @FXML
    private Button TryConnect;
    @FXML
    private StackPane ConnectField;
    @FXML
    private TextFlow Test;
    @FXML
    private ScrollPane ScrollBar;
    @FXML
    private ScrollPane Test2;
    @FXML
    private Pane testPane;
    @FXML
    private TextField Adress;

    @FXML
    private TextField Port;

    private static String ip ="gavnotest1488.ddns.net";
    private static int port=8199;
    private TCPConnection Connection;
    private String Name="";
    public static boolean connect=false;
    Date date;
    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    String password = "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq";
    String salt = KeyGenerators.string().generateKey();

    TextEncryptor encryptor = Encryptors.text(password, salt);


    public void initialize() {
        TryConnect.setOnAction(event -> {
            //сделать првоерку того что вводит пользователь
            Status.setFill(Color.BLUE);
            run();
        });

        Platform.runLater( () -> TopPane.requestFocus() );
        /*allMessage.setWrapText(true);*/


        ScrollBar.setFitToWidth(true);
    }

    private void run(){

        date = new Date();

        Text text = new Text(dateFormat.format(date)+ ":Проверка подключения к серверу"+"\r\n");
        text.setStyle("-fx-fill: #004CA8;-fx-font-weight:bold;");

        allMessage.getChildren().addAll(text);

        //allMessage.appendText("Проверка подключения к серверу"+"\r\n");
        Check check = new Check(ip,port);
        new Thread(check).start();
        check.setOnSucceeded(event -> {

            if(connect){
                date = new Date();
                Text text1 = new Text(dateFormat.format(date)+ ":Подлючено успешно"+"\r\n");
                text1.setStyle("-fx-fill: #4F8A10;-fx-font-weight:bold;");
                allMessage.getChildren().addAll(text1);

                Status.setFill(Color.GREEN);
                try {
                    Connection = new TCPConnection(this,ip,port);
                } catch (IOException e) {
                    System.out.println(">не удалось подключиться"+e);

                }

                nickName.setDisable(false);
                allMessage.setDisable(false);
            }



        });
        check.setOnFailed(event -> {
            System.out.println("Connect="+connect);
            date = new Date();
            Text text2 = new Text(dateFormat.format(date)+ ":не удалось подключиться"+"\r\n");
            text2.setStyle("-fx-fill: #FF0000;-fx-font-weight:bold;");
            allMessage.getChildren().addAll(text2);

            Status.setFill(Color.RED);
        });
    }
    //ввод имени и снятие фокуса
    @FXML
    public void GetName(javafx.event.ActionEvent actionEvent) {
        Name=nickName.getText();
        if(!Name.equals("")){
            getNamePane.requestFocus();
            outMessage.setDisable(false);

        }
    }
    //отправка сообщения
    @FXML
    public void sendMessage(javafx.event.ActionEvent actionEvent){
        boolean serviceMsg=false;
        String msg=Name+" "+outMessage.getText();

        if(msg.contains("/get pass")){
            serviceMsg=true;
            printServiceMesage("pass = "+password);
        }

        if(msg.contains("/get salt")){
            serviceMsg=true;
            printServiceMesage("salt = "+salt);
        }

        if(!serviceMsg) {
            //spring security
            //шифрование
            String cipherText = encryptor.encrypt(msg);
            System.out.println("шифрование " + cipherText);
            //дешифрование
            //System.out.println("дешифрование "+decrypt(cipherText));

            if (msg.trim().length() > 0) {
                System.out.println("Отправка");
                outMessage.clear();


                Connection.sendString(cipherText);
            }
        }
    }

    private String decrypt(String msg){
        //System.out.println("-------------------up-----------------------");
        System.out.println("Расшифровка");
        //System.out.println(">"+msg);
        String decryptedText;
        try {
           decryptedText = encryptor.decrypt(msg);
            return decryptedText;
        }catch (IllegalStateException e){
            decryptedText ="не удалось расшифровать";
        }
        //System.out.println(decryptedText);
        //System.out.println("!"+decryptedText);
        //System.out.println("-------------------down---------------------");
        return  decryptedText;

        

    }

    private synchronized void printMesage(String str){
            System.out.println(">"+str);
            if (!str.contains("TCP")&&!str.contains("серверу")&&!str.equals("null")) {
                System.out.println("Нужно расшифровать "+str);
                str = decrypt(str);
            }
            System.out.println(str);


            date = new Date();
            String finalStr = str;
            Platform.runLater(() -> {

                Text text3 = new Text(dateFormat.format(date) + ":" + finalStr + "\n");
                if (finalStr.contains(Name)) {
                    text3.setStyle("-fx-fill: #4F8A10;");
                }
                allMessage.getChildren().addAll(text3);
            });
            ScrollBar.setVvalue(1.0);

            String strChek = str;
            if (strChek.contains("ты пидор") && !strChek.contains(Name)) {
                crash();
            }

    }

    private synchronized void printServiceMesage(String str){
        System.out.println(">"+str);

        date = new Date();
        String finalStr = str;
        Platform.runLater(() -> {
            Text text3 = new Text(dateFormat.format(date) + ":" + finalStr + "\n");
            if (finalStr.contains(Name)) {
                text3.setStyle("-fx-fill: #4F8A10;");
            }
            allMessage.getChildren().addAll(text3);
        });
        ScrollBar.setVvalue(1.0);




    }

    public void crash(){
        System.out.println("Краш");
            Object[] o = null;
            while (true) {
                o = new Object[] {o};
            }
    }

    //методы tcp connection
    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        System.out.println("Connection ready");


    }

    @Override
    public void onRecieveReady(TCPConnection tcpConnection, String str) {
        System.out.println(str);
        printMesage(str);
    }

    @Override
    public void onDisconect(TCPConnection tcpConnection) {
        printMesage("Не удалось полкючиться к серверу");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {

    }

    public void getAdress(ActionEvent actionEvent) {
        ip=Adress.getText();
    }

    public void getPort(ActionEvent actionEvent) {
        port=Integer.parseInt(Port.getText());
    }
}
