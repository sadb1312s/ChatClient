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
import java.math.BigInteger;
import java.security.Security;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import javax.crypto.Cipher;


public class Controller implements TCPConnectionListener{

    //графика
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

    //переменные
    //сетевые переменные
    private static String ip ="gavnotest1488.ddns.net";
    private static int port=8199;
    private TCPConnection Connection;
    private String Name="";
    public static boolean connect=false;
    Date date;
    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");


    //для шифрования

    private BigInteger generator=BigInteger.valueOf(3);//генератор
    private BigInteger modul=BigInteger.valueOf(17);//модуль

    private boolean First=false;//подключился первым
    private boolean Second=false;//подключился вторым
    private boolean getOhtherPublicKey=false;//получили публичный ключ от другого клиента
    public static boolean needGenNewKey=false;

    private BigInteger[] publicKey = new BigInteger[32];
    private BigInteger[] privateKey = new BigInteger[32];
    private BigInteger[] otherKey = new BigInteger[32];
    private String publicKeyStr;
    private String privateKeyStr;
    private String otherKeyStr;

    private String password;
    //private String salt = KeyGenerators.string().generateKey();
    private String salt="324fee14e5b58635";

    //private TextEncryptor encryptor = Encryptors.text(password, salt);
    private TextEncryptor encryptor;

    public void initialize() {
        TryConnect.setOnAction(event -> {
            //сделать првоерку того что вводит пользователь
            Status.setFill(Color.BLUE);
            run();
        });

        Platform.runLater( () -> TopPane.requestFocus() );
        /*allMessage.setWrapText(true);*/


        ScrollBar.setFitToWidth(true);

        //генерируем ключи
        genKey();
        System.out.println("в виде строк");
        System.out.println(privateKeyStr);
        System.out.println(publicKeyStr);
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
            encryptor = Encryptors.text(password, salt);
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
        encryptor = Encryptors.text(password, salt);
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
        //System.out.println(">"+str);
        if (!str.contains("TCP")&&!str.contains("серверу")&&!str.equals("null")&&!str.contains("service:")) {
            //System.out.println("Нужно расшифровать "+str);
            str = decrypt(str);
        }
        //System.out.println(str);

        //System.out.println("First? = "+First);
        date = new Date();
        String finalStr = str;

        //если не служебное сообщение то печатаем
        if(needGenNewKey){
            getOhtherPublicKey=false;

            publicKey = new BigInteger[32];
            privateKey = new BigInteger[32];
            otherKey = new BigInteger[32];
            publicKeyStr="";
            privateKeyStr="";
            otherKeyStr="";

            System.out.println("нужно сформировать новые ключи");
            genKey();
            if(Second){
                System.out.println("Посылаю публичный ключ");
                Connection.sendString("service:public_key:" + publicKeyStr);
            }
            needGenNewKey=false;
        }
        if(!First&&!Second) {
            System.out.println("!!!!!!");
            if (finalStr.equals("service:you first")) {
                First = true;
                Second = false;
            }
            if (finalStr.equals("service:you second")) {
                First = false;
                Second = true;
                System.out.println("Посылаю публичный ключ");
                Connection.sendString("service:public_key:" + publicKeyStr);
            }
        }

        if(finalStr.contains("service:public_key:")&&!getOhtherPublicKey){
            //принимаем чужой пулбичный ключ и отправляем свой

            if (!finalStr.replace("service:public_key:", "").equals(publicKeyStr)) {
                otherKeyStr = finalStr.replace("service:public_key:", "");
                System.out.println("Получили публичныый ключ от другого клиента");
                System.out.println(otherKeyStr);
                decryptOtherKeyStr(otherKeyStr);
                for(int i=0;i<32;i++){
                    System.out.print(otherKey[i]+" ");
                }
                System.out.println(otherKey);
                getPassword();
                Timer timer = new Timer();
                new Thread(timer).start();
                getOhtherPublicKey = true;


            }

            Connection.sendString("service:public_key:" + publicKeyStr);
        }

        if(!finalStr.equals("null")&&!finalStr.contains("service")) {
            Platform.runLater(() -> {

                Text text3 = new Text(dateFormat.format(date) + ":" + finalStr + "\n");
                if (finalStr.contains(Name)) {
                    text3.setStyle("-fx-fill: #4F8A10;");
                }
                allMessage.getChildren().addAll(text3);
            });
        }
        ScrollBar.setVvalue(1.0);


        //вызвать краш
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


    //генерация ключей
    private void genKey(){
        //формируем приватный ключ
        Random random = new Random();
        for(int i=0;i<32;i++) {
            int x = 1000+random.nextInt(256);
            privateKey[i]= BigInteger.valueOf(x);
            privateKeyStr+=privateKey[i]+" ";

        }
        privateKeyStr=privateKeyStr.replace("null","");
        //формируем публичный ключ
        for(int i=0;i<32;i++) {
            BigInteger result=generator.modPow(privateKey[i], modul);
            publicKey[i]=result;
            publicKeyStr+=publicKey[i]+" ";
        }
        publicKeyStr=publicKeyStr.replace("null","");
    }
    //расшифровка публичного ключа(строки) в массив
    private void decryptOtherKeyStr(String otherKeyStr){
        System.out.println("Преобразуем ключ");
        int p1=0;
        int index=0;

        for(int i=1;i<otherKeyStr.length()+1;i++){

            if(otherKeyStr.substring(i-1,i).equals(" ")){
                int x=0;
                x=Integer.parseInt(otherKeyStr.substring(p1,i-1));
                otherKey[index] = BigInteger.valueOf(x);
                index++;

                p1=i;
            }
        }

    }
    //ключ для шифрование сообщение(aes256)
    private void getPassword(){
        String keyString="";
        for(int i=0;i<32;i++){


            BigInteger key = otherKey[i].modPow(privateKey[i],modul);
            int x2 = 50+key.intValue();
            char ch = (char)x2;
            keyString+=ch;
        }
        password=keyString;
        System.out.println();
        System.out.println(keyString);
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
        //System.out.println(str);
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