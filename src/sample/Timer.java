package sample;

import javafx.concurrent.Task;

import java.util.TimerTask;

import static sample.Controller.myNumber;

public class Timer extends Task {

    boolean stop=false;
    java.util.Timer timer = new java.util.Timer();

    @Override
    protected Object call() throws Exception {
        timer();
        return null;
    }

    private int timer(){
        TimerTask task2 = new TimerTask() {
            public void run() {
                Thread t = Thread.currentThread();

                if(!stop) {
                    System.out.println(t.getName() + " Knock-Knock!");
                    Cypher.needGenNewKey = true;
                    if (myNumber == 1)
                        Controller.Connection.sendString("NEW KEY PLEASE");
                    cancel();
                }else{
                    //System.out.println("STOP");
                }
            }};

        timer.schedule(task2, 60000);


        return 0;
    }
}
