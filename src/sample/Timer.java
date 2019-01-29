package sample;

import javafx.concurrent.Task;

public class Timer extends Task {
    @Override
    protected Object call() throws Exception {
        timer();
        return null;
    }

    private int timer(){
        long startTime = System.nanoTime();
        long stopTime;
        double timeD;
        int FinalTime;
        while(true){
            stopTime=System.nanoTime();
            timeD = (stopTime-startTime)*Math.pow(10,-9);
            FinalTime=(int)timeD;
            //System.out.println(FinalTime);
            if(FinalTime==60){
                startTime=System.nanoTime();
                Cypher.needGenNewKey=true;
                System.out.println(Controller.needGenNewKey);
            }
        }
    }
}
