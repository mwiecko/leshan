package org.example.nLeshan;

import org.example.objects.Temperature;

import java.util.Timer;
import java.util.TimerTask;

public class TempSensorSim {
    private final Temperature tempObject;
    public TempSensorSim(Temperature tempObject){
        this.tempObject = tempObject;
        init();
    }

    private void init() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                double valToUpdate = Math.random() * (
                        tempObject.getMaxRangeValue() - tempObject.getMinRangeValue() + 1
                ) + tempObject.getMinRangeValue();
                tempObject.updateSensorValue((float) valToUpdate);
            }
        }, 0, 5000);
        System.out.println("sensor simulator running");
    }
}
