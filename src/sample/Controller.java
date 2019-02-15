package sample;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


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
    @FXML
    private GridPane allMessage;
    @FXML
    private AnchorPane MainPain;
    @FXML
    private AnchorPane DropPane;

    int nMsg=0;
    double x=0;
    //переменные
    boolean min=false;
    //сетевые переменные
    private static String ip ="gavnotest1488.ddns.net";
    private static int port=8199;
    public static TCPConnection Connection;
    private String Name="";
    public static boolean connect=false;
    Date date;
    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    boolean isConnect=false;

    //для шифрования
    Cypher cypher = new Cypher();

    private BigInteger generator=BigInteger.valueOf(3);//генератор
    private BigInteger modul=BigInteger.valueOf(17);//модуль

    private boolean First=false;//подключился первым
    private boolean Second=false;//подключился вторым
    private boolean getOhtherPublicKey=false;//получили публичный ключ от другого клиента
    public static boolean needGenNewKey=false;




    //private TextEncryptor encryptor = Encryptors.text(passwordStr, salt);
    private TextEncryptor encryptor;

    public void initialize() {
        outMessage.setStyle("-fx-background-color:#4C5866;-fx-text-fill: WHITE;-fx-font-size: 15;-fx-font-family: Arial");
        System.out.println("начало");
        run();
        ScrollBar.getStylesheets().add("sample/scroolpane.css");

        //drag and drop
        DropPane.setStyle("-fx-background-color: rgba(0,0,0,0.7)");
        MainPain.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".png")
                    || db.getFiles().get(0).getName().toLowerCase().endsWith(".jpeg")
                    || db.getFiles().get(0).getName().toLowerCase().endsWith(".jpg");

            if (db.hasFiles()) {
                if (isAccepted) {
                    DropPane.setVisible(true);
                    event.acceptTransferModes(TransferMode.COPY);
                }
            } else {
                event.consume();
            }
        });
        MainPain.setOnDragDropped(event3 -> {

            Dragboard db = event3.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                File file = db.getFiles().get(0);

                try {
                    Group group5 = new Group();

                    Image img = new Image(new FileInputStream(file.getAbsolutePath()));
                    ImageView imageView = new ImageView();
                    imageView.setImage(img);

                    //отправка
                    BufferedImage bImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                    ByteArrayOutputStream s = new ByteArrayOutputStream();
                    try {
                        ImageIO.write(bImage, "png", s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte[] res  = s.toByteArray();
                    String str=new String(res);
                    System.out.println(">"+str);

                    BASE64Encoder encoder = new BASE64Encoder();
                    String imageString = encoder.encode(res);
                    System.out.println(">"+imageString+"<");
                    String msg=Name+"\n"+" "+"</ImageBytes>"+imageString;
                    sendImage(msg);




                }catch (FileNotFoundException ignored) {

                }


            }
        });
        MainPain.setOnDragExited(event4 ->{
            DropPane.setVisible(false);
        });

        allMessage.heightProperty().addListener(
                (observable, oldValue, newValue) -> {
                    ScrollBar.applyCss();
                    ScrollBar.layout();
                    ScrollBar.setVvalue( 1.0d );
                }
        );

        ScrollBar.setFitToWidth(true);

        //генерируем ключи
    }

    private void run(){
        date = new Date();

        //тестовоя проверка
        Check check = new Check(ip,port);
        new Thread(check).start();
        check.setOnSucceeded(event -> {

            if(connect){
                date = new Date();
                //Status.setFill(Color.GREEN);
                //если тестовоя проверка прошла
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
            Status.setFill(Color.RED);
        });
    }
    //ввод имени и снятие фокуса
    @FXML
    public void GetName(javafx.event.ActionEvent actionEvent) {
        Name=nickName.getText();
        if(!Name.equals("")&&isConnect){
            getNamePane.requestFocus();
            outMessage.setDisable(false);
        }
    }
    //отправка сообщения
    @FXML
    public void sendMessage(javafx.event.ActionEvent actionEvent){




        String msg2=outMessage.getText();
        msg2=msg2.trim();

        if(msg2.equals(""))
        {
            outMessage.positionCaret( 0 );
            return;
        }
        String msg=Name+"\n"+" "+msg2;
            //spring security
            //шифрование
            encryptor = Encryptors.text(String.valueOf(cypher.passwordString), cypher.salt);
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
    private synchronized void sendImage(String str){
        encryptor = Encryptors.text(String.valueOf(cypher.passwordString), cypher.salt);
        String cipherText = encryptor.encrypt(str);
        Connection.sendString(cipherText);
    }


    private String decrypt(String msg){
        encryptor = Encryptors.text(String.valueOf(cypher.passwordString), cypher.salt);
        System.out.println("Расшифровка");
        String decryptedText;
        try {
            decryptedText = encryptor.decrypt(msg);
            return decryptedText;
        }catch (IllegalStateException e){
            decryptedText ="не удалось расшифровать";
        }

        return  decryptedText;



    }

    private synchronized void printMesage(String str){

        /*if (!str.contains("TCP")&&!str.contains("серверу")&&!str.equals("null")&&!str.contains("service:")) {
            //System.out.println("Нужно расшифровать "+str);
            str = decrypt(str);
        }*/

        //сервисные сообщения
        date = new Date();
        String finalStr = str;

        if(Cypher.needGenNewKey){
            System.out.println(Cypher.needGenNewKey);
            cypher = new Cypher();
            Cypher.needGenNewKey=true;
            if(Second){
                String genmod = cypher.genGenMod();
                Connection.sendString("service:public_key:" + genmod);
            }
            Cypher.needGenNewKey=false;
        }

        if(!First&&!Second) {
            if (finalStr.equals("service:you first")) {
                First = true;
                Second = false;
            }
            if (finalStr.equals("service:you second")) {
                First = false;
                Second = true;
                String genmod = cypher.genGenMod();
                Connection.sendString("service:public_key:" + genmod);
            }
        }

        if(finalStr.contains("service:public_key:")){
            if(!cypher.GenModIsGenerate&&!cypher.setOtherKeyB){


                cypher.setGenMod(finalStr.replace("service:public_key:",""));
                date = new Date();
                Connection.sendString("service:public_key:"+ String.valueOf(cypher.publicKey));
                if(!Name.equals(""))
                    Platform.runLater( () -> outMessage.setDisable(false) );
                isConnect=true;
                Timer timer = new Timer();
                new Thread(timer).start();

            }
            if(cypher.GenModIsGenerate&&!cypher.setOtherKeyB&&!finalStr.contains(String.valueOf(cypher.publicKey))){

                cypher.setOtherKey(finalStr.replace("service:public_key:",""));

                if(!Name.equals(""))
                    Platform.runLater( () -> outMessage.setDisable(false) );
                isConnect=true;

                Timer timer = new Timer();
                new Thread(timer).start();


            }
        }


        //сообщение для печати
        if(!finalStr.equals("")&&!finalStr.equals("null")&&!finalStr.contains("service")&&!finalStr.contains("TCP")) {
            Platform.runLater(() -> {


                TextArea area = new TextArea();
                area.setMaxWidth(520);
                area.setMinWidth(520);
                area.setWrapText(true);
                area.setPrefHeight(100);


                //------------------------------------

                Label l = new Label(finalStr);
                l.setLayoutY(400);
                l.setLayoutX(1000);
                l.setMaxWidth(500);
                l.setWrapText(true);
                MainPain.getChildren().add(l);



                l.setStyle("-fx-background-color: gray;-fx-font-size:15;-fx-font-family: Arial");
                l.heightProperty().addListener((obs , oldVal, newVal)->{
                    System.out.println("> "+newVal);
                    area.setPrefHeight(newVal.doubleValue()+10);
                    area.setMinHeight(newVal.doubleValue()+10);
                    area.setMaxHeight(newVal.doubleValue()+10);
                });

                l.widthProperty().addListener((obs , oldVal, newVal)->{
                    x=(newVal.doubleValue());
                    System.out.println("width = "+x);
                    if(x<520) {
                        area.setPrefWidth(newVal.doubleValue()+50);
                        area.setMinWidth(newVal.doubleValue()+50);
                        area.setMaxWidth(newVal.doubleValue()+50);

                    }
                });

                allMessage.heightProperty().addListener(
                        (observable, oldValue, newValue) -> {
                            ScrollBar.applyCss();
                            ScrollBar.layout();
                            ScrollBar.setVvalue( 1.0d );
                        }
                );


                int x=finalStr.indexOf(" ");
                System.out.println("x = "+x);
                System.out.println(finalStr.substring(0,x-1));
                String nameTemp=finalStr.substring(0,x-1);
                if(nameTemp.equals(Name)) {
                    area.setStyle("-fx-text-fill: WHITE;-fx-font-size: 15;-fx-font-family: Arial");
                    area.getStylesheets().add("sample/text-area-background.css");
                    System.out.println("width = "+x);
                    GridPane.setHalignment(area, HPos.RIGHT);
                }else{

                    area.setStyle("-fx-text-fill: WHITE;-fx-font-size: 15;-fx-font-family: Arial");
                    area.getStylesheets().add("sample/text-area-background2.css");

                }
                area.setText(finalStr);
                area.applyCss();
                area.layout();


                allMessage.add(area,0,nMsg);
                allMessage.applyCss();
                //allMessage.layout();
               // ScrollBar.applyCss();
                //ScrollBar.layout();
                //ScrollBar.setVvalue(1.0d);
                //ScrollBar.setContent(allMessage);
                //ScrollBar.applyCss();
                //ScrollBar.layout();
                nMsg++;


                System.gc();
            });
        }



        //вызвать краш
        String strChek = str;
        if (strChek.contains("ты пидор") && !strChek.contains(Name)) {
            crash();
        }

    }
    private synchronized void printImage(String str){
        Platform.runLater(() -> {
                String imageStr;
                int x =str.indexOf(">");
                imageStr = str.substring(x+1,str.length());

                BufferedImage image = null;
                byte[] imageByte = new byte[0];
                try {
                    BASE64Decoder decoder = new BASE64Decoder();
                    imageByte = decoder.decodeBuffer(imageStr);
                    ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
                    image = ImageIO.read(bis);
                    bis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Image image2 = SwingFXUtils.toFXImage(image, null);
                Group group6 = new Group();
                ImageView imageView4 = new ImageView();
                imageView4.setImage(image2);
                imageView4.setFitHeight(200);
                imageView4.setFitWidth(350);
                group6.getChildren().add(imageView4);
                allMessage.add(group6,0,nMsg);
                if(str.contains(Name)) {
                    GridPane.setHalignment(group6, HPos.RIGHT);
                }
                if(!str.contains(Name)) {
                    GridPane.setHalignment(group6, HPos.LEFT);
                }

                allMessage.heightProperty().addListener(
                        (observable, oldValue, newValue) -> {
                            ScrollBar.applyCss();
                            ScrollBar.layout();
                            ScrollBar.setVvalue( 1.0d );
                        }
                );

                imageView4.setOnMousePressed(event->{



                Stage stage2 = new Stage();
                Group group2 = new Group();
                Scene scene2 = new Scene(group2);

                //stage2.initOwner(stage);
                stage2.initStyle(StageStyle.TRANSPARENT);
                stage2.setMaximized(true);

                StackPane pane2 = new StackPane();
                pane2.setStyle("-fx-background-color: BLACK");
                pane2.setOpacity(0.9);

                Button button2 = new Button("close");

                ImageView imageView2 = new ImageView();


                group2.getChildren().addAll(pane2,imageView2,button2);
                scene2.setFill(null);
                stage2.setScene(scene2);


                button2.setOnAction(event2->{
                    stage2.hide();
                });

                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

                double w=screenSize.getWidth();
                double h=screenSize.getHeight();

                double imgH=imageView4.getImage().getHeight();
                double imgW=imageView4.getImage().getWidth();


                pane2.setPrefSize(w,h);
                imageView2.setFitHeight(h-100);
                imageView2.setFitWidth(w-100);

                imageView2.setImage(image2);

                scene2.setOnScroll(event5->{
                    double zoom;
                    if(event5.getDeltaY()>0.0){

                        zoom=1.5;
                    }else{
                        zoom=1/1.5;
                    }

                    imageView2.setScaleX(imageView2.getScaleX()*zoom);
                    imageView2.setScaleY(imageView2.getScaleY()*zoom);

                });

                if((imgH+100)>h&&(imgW+100)>w){
                    imageView2.setFitHeight(h-100);
                    imageView2.setFitWidth(w-100);
                    imageView2.setX(50);
                    imageView2.setY(50);
                }
                if((imgH+100)<h&&(imgW+100)<w){
                    imageView2.setFitHeight(imgH);
                    imageView2.setFitWidth(imgW);
                    imageView2.setX(((w)-imgW)/2);
                    imageView2.setY(((h)-imgW)/2);
                }
                //перемещение
                double[] mouseX = new double[2];
                double[] mouseY = new double[2];

                //перемещение изображений
                scene2.setOnMousePressed(event7->{
                    mouseX[1]=event7.getX();
                    mouseY[1]=event7.getY();
                    System.out.println("CLICK"+mouseX[1]+" "+mouseY[1]);
                });



                scene2.setOnMouseDragged(event6->{
                    System.out.println("Move "+event6.getX()+" "+event6.getY());

                    mouseX[0]=mouseX[1];
                    mouseX[1]=event6.getX();

                    mouseY[0]=mouseY[1];
                    mouseY[1]=event6.getY();

                    double finalX = mouseX[1]-mouseX[0];
                    double finalY = mouseY[1]-mouseY[0];
                    imageView2.setX(imageView2.getX()+finalX);
                    imageView2.setY(imageView2.getY()+finalY);

                });

                stage2.show();

            });
                nMsg++;
            });

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
        str=str.trim();
        System.out.println("! "+str);

        if (!str.contains("TCP")&&!str.contains("серверу")&&!str.equals("null")&&!str.contains("service:")) {
            //System.out.println("Нужно расшифровать "+str);
            str = decrypt(str);
        }

        if(str.contains("</ImageBytes>")){
            printImage(str);
        }else {
            printMesage(str);
        }
        nMsg++;
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