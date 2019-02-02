package sample;

import javafx.concurrent.Task;

import java.util.TimerTask;

public class Timer extends Task {
    @Override
    protected Object call() throws Exception {
        timer();
        return null;
    }

    private int timer(){
        TimerTask task2 = new TimerTask() {
            public void run() {
                System.out.println("Knock-Knock!");
                Cypher.needGenNewKey=true;
                Controller.Connection.sendString("service:KEY KEY NEW KEY");
                cancel(); }};

        java.util.Timer timer = new java.util.Timer();
        timer.schedule(task2, 60000);
        return 0;
    }
}
