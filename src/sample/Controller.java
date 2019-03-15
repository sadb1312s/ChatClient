package sample;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.*;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class Controller implements TCPConnectionListener{

    //графика
    @FXML
    private TextField nickName;
    @FXML
    private StackPane getNamePane;
    @FXML
    private TextField outMessage;
    @FXML
    private ScrollPane ScrollBar;
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
    @FXML
    private Button SettingButton;
    @FXML
    private AnchorPane SettingPane;
    @FXML
    private Circle ConnectCircle;

    //
    int nMsg=0;
    double x=0;
    //сетевые переменные
    private static String ip ="gavnotest1488.ddns.net";
    private static int port=8199;
    public static TCPConnection Connection;
    private String Name="";
    public static boolean connect=false;
    Date date;
    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    boolean isConnect=false;
    UUID uuid = UUID.randomUUID();
    int abonetnN=0;
    static int myNumber;
    boolean cyhherCreate=false;
    Timer timer;
    boolean SettingVisible=false;


    //для шифрования
    private Cypher cypher = new Cypher();

    private boolean First=false;//подключился первым
    private boolean Second=false;//подключился вторым

    public void initialize() {
        allMessage.setVgap(1); //vertical gap in pixels
        outMessage.setStyle("-fx-background-color:#4C5866;-fx-text-fill: WHITE;-fx-font-size: 15;-fx-font-family: Arial");
        run();
        ScrollBar.getStylesheets().add("sample/style/scroolpane.css");

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
            List<File> files = db.getFiles();


            for(File object:files) {
                System.out.println(object.getName());
                try {

                    Image img = new Image(new FileInputStream(object.getAbsolutePath()));
                    ImageView imageView = new ImageView();
                    imageView.setImage(img);

                    //отправка

                    Task task = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {



                            BufferedImage bImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                            ByteArrayOutputStream s = new ByteArrayOutputStream();
                            try {
                                ImageIO.write(bImage, "jpg", s);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            byte[] res = s.toByteArray();
                            System.out.println(">>>>>"+res.length);
                            String str = new String(res);


                            String imageString = Base64.getEncoder().encodeToString(res);
                            String msg = Name + "\n" + " " + "</ImageBytes>" + imageString;
                            sendImage(msg);
                            return null;
                        }

                    };
                    new Thread(task).start();

                } catch (FileNotFoundException e) {
                    System.out.println(e);
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
        SettingButton.setStyle("-fx-background-color: #8AA4C1");
        SettingButton.setOnAction(event -> {
            boolean f=false;

            if(SettingVisible&&!f) {
                SettingPane.setVisible(false);
                SettingVisible=false;
                f=true;
            }

            if(!SettingVisible&&!f) {
                SettingPane.setVisible(true);
                SettingVisible=true;
                f=true;
            }

        });
        Port.setOnAction(event -> port=Integer.parseInt(Port.getText()));
        Adress.setOnAction(event -> ip=Adress.getText());
    }

    private void run(){
        date = new Date();

        //тестовоя проверка
        /*Check check = new Check(ip,port);
        new Thread(check).start();
        check.setOnSucceeded(event -> {

            if(connect){
                date = new Date();

                //если тестовоя проверка прошла
                try {
                    Connection = new TCPConnection(this,ip,port);
                } catch (IOException e) {
                    //System.out.println(">не удалось подключиться"+e);

                }
                nickName.setDisable(false);
                allMessage.setDisable(false);
            }
        });
        check.setOnFailed(event -> {
            //System.out.println("Connect="+connect);
            date = new Date();

        });*/
        try {
            Connection = new TCPConnection(this,ip,port);
            connect=true;
            isConnect=true;
            nickName.setDisable(false);
            ConnectCircle.setFill(Color.GREEN);
            //outMessage.setDisable(false);
        } catch (IOException e) {
            ConnectCircle.setFill(Color.RED);
            e.printStackTrace();
        }
    }
    //ввод имени и снятие фокуса
    @FXML
    public void GetName(javafx.event.ActionEvent actionEvent) {
        Name=uuid.toString()+"<"+nickName.getText();
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
        String msg=Name+":"+" "+msg2;
        //spring security
        //шифрование
        String cipherText = cypher.encrypt(msg);
        //System.out.println("шифрование " + cipherText);
        //дешифрование
        //System.out.println("дешифрование "+decrypt(cipherText));

        if (msg.trim().length() > 0) {
            //System.out.println("Отправка");
            outMessage.clear();
            Connection.sendString(cipherText);
        }

    }
    private synchronized void sendImage(String str){
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Connection.sendString(cypher.encrypt(str));
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.start();

    }

    private synchronized void printMessage(String str){

        System.out.println(">>>>"+str);
        date = new Date();
        boolean myMessage=chekName(str);
        String finalStr =delUUID(str);


        //сообщение для печати
        if(!finalStr.equals("")&&!finalStr.equals("null")&&!finalStr.contains("service")&&!finalStr.contains("TCP")) {
            Platform.runLater(() -> {
                if(!finalStr.contains("http")) {
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
                    l.heightProperty().addListener((obs, oldVal, newVal) -> {
                        //System.out.println("> "+newVal);
                        area.setPrefHeight(newVal.doubleValue() + 10);
                        area.setMinHeight(newVal.doubleValue() + 10);
                        area.setMaxHeight(newVal.doubleValue() + 10);
                    });

                    l.widthProperty().addListener((obs, oldVal, newVal) -> {
                        x = (newVal.doubleValue());
                        //System.out.println("width = "+x);
                        if (x < 520) {
                            area.setPrefWidth(newVal.doubleValue() + 50);
                            area.setMinWidth(newVal.doubleValue() + 50);
                            area.setMaxWidth(newVal.doubleValue() + 50);

                        }
                    });

                    allMessage.heightProperty().addListener(
                            (observable, oldValue, newValue) -> {
                                ScrollBar.applyCss();
                                ScrollBar.layout();
                                ScrollBar.setVvalue(1.0d);
                            }
                    );



                    if (myMessage) {
                        area.setStyle("-fx-text-fill: WHITE;-fx-font-size: 15;-fx-font-family: Arial");
                        area.getStylesheets().add("sample/style/text-area-background.css");
                        //System.out.println("width = "+x);
                        GridPane.setHalignment(area, HPos.RIGHT);
                    } else {

                        area.setStyle("-fx-text-fill: WHITE;-fx-font-size: 15;-fx-font-family: Arial;");

                        area.getStylesheets().add("sample/style/text-area-background2.css");

                    }
                    area.setEditable(false);
                    area.setText(finalStr);
                    area.applyCss();
                    area.layout();

                    allMessage.add(area, 0, nMsg);
                    allMessage.applyCss();
                    //l.setText("");q

                    nMsg++;
                    System.gc();
                }else {
                    Hyperlink hyperlink = new Hyperlink();
                    hyperlink.setText(finalStr);
                    hyperlink.setMaxWidth(250);
                    hyperlink.setOnAction(event -> {
                        try {
                            System.out.println(">"+finalStr);
                            Desktop.getDesktop().browse(new URL(finalStr.substring(finalStr.indexOf(" ")+1,finalStr.length())
                            ).toURI());
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    });

                    int x = finalStr.indexOf(" ");
                    String nameTemp = finalStr.substring(0, x - 1);
                    Pane pane = new Pane();
                    pane.getChildren().add(hyperlink);

                    pane.setMaxWidth(300);
                    pane.setMinHeight(25);
                    pane.setPrefWidth(50);

                    if(myMessage){
                        hyperlink.setStyle("-fx-text-fill: WHITE;-fx-font-size: 15;-fx-font-family: Arial");
                        pane.setStyle("-fx-background-color: #2E3757;-fx-border-radius: 15 15 0 15;-fx-background-radius: 15 15 0 15");
                        GridPane.setHalignment(pane, HPos.RIGHT);
                    }else {
                        hyperlink.setStyle("-fx-text-fill: WHITE;-fx-font-size: 15;-fx-font-family: Arial;");
                        pane.setStyle("-fx-background-color: #4B6C91;-fx-border-radius: 0 15 15 15;-fx-background-radius: 0 15 15 15;");
                    }
                    hyperlink.setMaxWidth(250);

                    allMessage.add(pane, 0, nMsg);

                    pane.applyCss();
                    pane.layout();




                    nMsg++;
                    System.gc();

                }
                //System.out.println(nMsg);
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

            boolean myMessage=chekName(str);
            String imageStr;
            int x = str.indexOf(">");
            imageStr = str.substring(x + 1, str.length());

            System.out.println(">! "+imageStr);

            BufferedImage image = null;
            byte[] imageByte = new byte[0];
            try {
                //BASE64Decoder decoder = new BASE64Decoder();
                //imageByte = decoder.decodeBuffer(imageStr);

                imageByte = Base64.getDecoder().decode(imageStr);

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


            allMessage.add(group6, 0, nMsg);

            if (myMessage) {
                GridPane.setHalignment(group6, HPos.RIGHT);
            }
            else{
                GridPane.setHalignment(group6, HPos.LEFT);
            }

            allMessage.heightProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        ScrollBar.applyCss();
                        ScrollBar.layout();
                        ScrollBar.setVvalue(1.0d);
                    });

            imageView4.setOnMousePressed(event -> {
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


                group2.getChildren().addAll(pane2, imageView2, button2);
                scene2.setFill(null);
                stage2.setScene(scene2);


                button2.setOnAction(event2 -> {
                    stage2.hide();
                });

                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

                double w = screenSize.getWidth();
                double h = screenSize.getHeight();

                double imgH = imageView4.getImage().getHeight();
                double imgW = imageView4.getImage().getWidth();


                pane2.setPrefSize(w, h);
                imageView2.setFitHeight(h - 100);
                imageView2.setFitWidth(w - 100);

                imageView2.setImage(image2);

                scene2.setOnScroll(event5 -> {
                    double zoom;
                    if (event5.getDeltaY() > 0.0) {

                        zoom = 1.5;
                    } else {
                        zoom = 1 / 1.5;
                    }

                    imageView2.setScaleX(imageView2.getScaleX() * zoom);
                    imageView2.setScaleY(imageView2.getScaleY() * zoom);

                });

                if ((imgH + 100) > h && (imgW + 100) > w) {
                    imageView2.setFitHeight(h - 100);
                    imageView2.setFitWidth(w - 100);
                    imageView2.setX(50);
                    imageView2.setY(50);
                }
                if ((imgH + 100) < h && (imgW + 100) < w) {
                    imageView2.setFitHeight(imgH);
                    imageView2.setFitWidth(imgW);
                    imageView2.setX(((w) - imgW) / 2);
                    imageView2.setY(((h) - imgH) / 2);
                }
                //перемещение
                double[] mouseX = new double[2];
                double[] mouseY = new double[2];

                //перемещение изображений
                scene2.setOnMousePressed(event7 -> {
                    mouseX[1] = event7.getX();
                    mouseY[1] = event7.getY();
                    //System.out.println("CLICK"+mouseX[1]+" "+mouseY[1]);
                });

                scene2.setOnMouseDragged(event6 -> {
                    //System.out.println("Move "+event6.getX()+" "+event6.getY());

                    mouseX[0] = mouseX[1];
                    mouseX[1] = event6.getX();

                    mouseY[0] = mouseY[1];
                    mouseY[1] = event6.getY();

                    double finalX = mouseX[1] - mouseX[0];
                    double finalY = mouseY[1] - mouseY[0];
                    imageView2.setX(imageView2.getX() + finalX);
                    imageView2.setY(imageView2.getY() + finalY);

                });
                stage2.show();

            });
            nMsg++;
        });
    }

    public void crash(){
        //System.out.println("Краш");
        Object[] o = null;
        while (true) {
            o = new Object[] {o};
        }
    }
    //методы tcp connection
    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        //System.out.println("Connection ready");
    }

    @Override
    public void onRecieveReady(TCPConnection tcpConnection, String str) {

        str=str.trim();
        //System.out.println("! "+str);

        if(str.contains("client disconnected TCP Connection:")){
            System.out.println("abonent "+abonetnN);
            abonetnN--;
            //Controller.Connection.sendString("NEW KEY PLEASE");
            if(abonetnN==1) {

                First=false;
                Second=false;
            }

            System.out.println("abonent "+abonetnN);

        }

        if (!str.contains("TCP")&&!str.contains("серверу")&&!str.equals("null")&&!str.contains("service:")&&!str
                .contains("serviceMU")&&!str.equals("NEW KEY PLEASE")) {
            //System.out.println("Нужно расшифровать "+str);
            str = cypher.decrypt(str);
        }

        if(str.contains("</ImageBytes>")&&!str.contains("service:")&&!str
                .contains("serviceMU")){
            printImage(str);
        }
        if(!str.contains("</ImageBytes>")&&!str.contains("service:")&&!str
                .contains("serviceMU")&&!str.equals("NEW KEY PLEASE")){
            printMessage(str);
        }


        if(str.contains("serviceMU")) {
            keyWork(str);

        }

        if(str.contains("service:public_key:")){
            keyWorkTwoAbonent(str);
        }

        if(!First&&!Second&&abonetnN<=2) {
            //System.out.println("2");
            if (str.equals("service:you first")) {
                //System.out.println("2.1");
                cypher=new Cypher();
                First = true;
                myNumber=1;
                abonetnN=2;
                Second = false;
            }
            if (str.equals("service:you second")) {
                //System.out.println("2.2");
                cypher=new Cypher();
                First = false;
                Second = true;
                abonetnN=2;
                myNumber=2;
                String genmod = cypher.genGenMod();
                Connection.sendString("service:public_key:" + genmod);
            }
        }



        if(str.equals("NEW KEY PLEASE")&&abonetnN<=2){
            if(timer!=null)
                timer.stop=true;
            outMessage.setDisable(true);
            System.out.println("new key please");

            cypher.needGenNewKey=true;
            System.out.println("> "+cypher.needGenNewKey+" "+abonetnN);
           if(Cypher.needGenNewKey&&abonetnN<=2){
                //Cypher.needGenNewKey=false;
                cypher = new Cypher();
                //System.out.println("222");

                if(Second) {
                    String genmod = cypher.genGenMod();
                    //System.out.println("222.1");
                    Connection.sendString("service:public_key:" + genmod);
                }

                Cypher.needGenNewKey=false;
            }

        }

        if(str.equals("NEW KEY PLEASE")&&abonetnN>2) {
            if(timer!=null)
                timer.stop=true;
            //if (Cypher.needGenNewKey && abonetnN > 2) {
                System.out.println("NEW KEWEQWEWQEWQ");
                //Cypher.needGenNewKey = false;
                cyhherCreate = false;
                keyWork(str);

            //}
        }
    }

    public void keyWork(String str){
        //System.out.println("!! "+str);
        if (str.contains("serviceMU:")){
            //System.out.println("1");
            //System.out.println("connect? = " + connect);
            myNumber = Integer.parseInt(str.substring(str.indexOf(":") + 1, str.indexOf(":") + 2));
            int abonetnNt = Integer.parseInt(str.substring(str.indexOf(">") + 1, str.indexOf(">") + 2));

            if (abonetnNt != abonetnN) {
                abonetnN = abonetnNt;
                cyhherCreate = false;
                //System.out.println("Начинаем по новой");

            }
           // System.out.println("number = " + myNumber);
           // System.out.println("abonetnN = " + abonetnNt);


        }
        if(!cyhherCreate&& connect){
            //System.out.println("новый обьект");
            cypher = new Cypher();
            cyhherCreate = true;


            if (myNumber == 1) {
                //System.out.println(myNumber+" gen gen gen gen gen");
                cypher.genGenMod();
                Connection.sendString("serviceMUGenMod:>" + cypher.gen + "<" + cypher.modul);

                int sendTo;
                if (myNumber == abonetnN) {
                    sendTo = 1;
                } else {
                    sendTo = myNumber + 1;
                }
                int chain = 0;
                chain += myNumber;
                Connection.sendString("serviceMUP:>" + myNumber + "!" + sendTo + "<" + chain + "^" + cypher.genPublicKey());
            }
        }
        if (str.contains("serviceMUGenMod:") && connect) {

                if (!cypher.GenModIsGenerate) {
                    //System.out.println("приняли генератор и модуль");
                    cypher.gen = new BigInteger(str.substring(str.indexOf(">") + 1, str.indexOf("<")));
                    cypher.modul = new BigInteger(str.substring(str.indexOf("<") + 1, str.length()));
                    cypher.GenModIsGenerate = true;
                    //System.out.println(cypher.gen+" "+cypher.modul);

                    int sendTo;
                    if (myNumber == abonetnN) {
                        sendTo = 1;
                    } else {
                        sendTo = myNumber + 1;
                    }
                    int chain = 0;
                    chain += myNumber;
                    //System.out.println(">" + chain);
                    //System.out.println(">" + sendTo);
                    Connection.sendString("serviceMUP:>" + myNumber + "!" + sendTo + "<" + chain + "^" + cypher.genPublicKey());

                }
            }
        if (str.contains("serviceMUP:>")) {
                //System.out.println(str);

                int chainT = 0;
                for (int i = 1; i <= abonetnN; i++)
                    chainT += i;
                //System.out.println(chainT);

                if (str.substring(str.indexOf("!") + 1, str.indexOf("<")).equals("" + myNumber)) {
                    //System.out.println("приняли часть ключа");
                    int messageChain = Integer.parseInt(str.substring(str.indexOf("<") + 1, str.indexOf("^")));
                    if (messageChain == (chainT - myNumber)) {
                        //System.out.println("конец чепочки");
                        cypher.setOtherKey(str.substring(str.indexOf("^") + 1, str.length()));
                        outMessage.setDisable(false);
                        if(myNumber==1) {
                            timer = new Timer();
                            new Thread(timer).start();
                        }
                    } else {

                        int sendTo;
                        if (myNumber == abonetnN) {
                            sendTo = 1;
                        } else {
                            sendTo = myNumber + 1;
                        }

                        messageChain += myNumber;
                        Connection.sendString("serviceMUP:>" + myNumber + "!" + sendTo + "<" + messageChain + "^" + cypher.getPassPart
                                (str.substring(str.indexOf("^") + 1, str.length())));
                    }
                }

            }


    }

    public void keyWorkTwoAbonent(String str){
        if(str.contains("service:public_key:")&&abonetnN<=2){
            //System.out.println(str);

            if(!cypher.GenModIsGenerate&&!cypher.setOtherKeyB){
                System.out.println("3");
                System.out.println(str);

                //System.out.println("3.1");
                cypher.setGenMod(str.replace("service:public_key:",""));
                date = new Date();
                Connection.sendString("service:public_key:"+ String.valueOf(cypher.publicKey));
                if(!Name.equals(""))
                    Platform.runLater( () -> outMessage.setDisable(false) );

                if(myNumber==1) {
                    isConnect = true;
                    timer = new Timer();
                }

                new Thread(timer).start();

            }
            if(cypher.GenModIsGenerate&&!cypher.setOtherKeyB&&!str.contains(String.valueOf(cypher.publicKey))){
                System.out.println("4");
                System.out.println(str);
                cypher.setOtherKey(str.replace("service:public_key:",""));

                if(!Name.equals(""))
                    Platform.runLater( () -> outMessage.setDisable(false) );
                isConnect=true;
                outMessage.setDisable(false);
                /*if(myNumber==2) {
                    Timer timer = new Timer();
                    new Thread(timer).start();
                }*/
                //abonetnN=2;


            }
        }
    }

    @Override
    public void onDisconect(TCPConnection tcpConnection) {
        printMessage("Не удалось полкючиться к серверу");
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

    public boolean chekName(String str){
        try {
            if (str.substring(0, str.indexOf("<")).contains(uuid.toString())) {
                return true;
            } else {
                return false;
            }
        }catch (Exception e){
            return false;
        }

    }

    public String delUUID(String str){
        try {
            return str.substring(str.indexOf("<")+1, str.length());
        }catch (Exception e){
            return str;
        }
    }
}