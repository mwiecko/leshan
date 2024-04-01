package org.example.objects;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.example.nLeshan.TempSensorSim;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Temperature extends BaseInstanceEnabler {
    private static final List<Integer> supportedResources = Arrays.asList(5700, 5601, 5602, 5603, 5604, 5701, 5605);

    private Float sensorValue;  //5700
    private Float minMeasuredValue;  //5601
    private Float maxMeasuredValue;  //5602
    private final Float minRangeValue;  //5603
    private final Float maxRangeValue;  //5604
    private final String sensorUnits;  //5701
    public Temperature(){
        minRangeValue = -100f;
        maxRangeValue = 100f;
        sensorUnits = "°C";
        new TempSensorSim(this);
        //init();
    }

    public boolean updateSensorValue(Float value){
        if (value >= minRangeValue && value <= maxRangeValue){
            sensorValue = value;
            if (maxMeasuredValue == null)
                maxMeasuredValue = value;
            if (minMeasuredValue == null)
                minMeasuredValue = value;

            if (value > maxMeasuredValue)
                maxMeasuredValue = value;
            else if (value < minMeasuredValue)
                minMeasuredValue = value;
            return true;
        } else
            return false;
    }

    @Override
    public ReadResponse read(ServerIdentity identity, int resourceid) {
        return switch (resourceid) {
            case 5700 ->  // sensor value
                ReadResponse.success(resourceid, sensorValue);

            case 5601 ->  // min measured value
                ReadResponse.success(resourceid, minMeasuredValue);

            case 5602 ->  // max measured value
                ReadResponse.success(resourceid, maxMeasuredValue);

            case 5603 ->  // min range value
                ReadResponse.success(resourceid, minRangeValue);

            case 5604 ->  // max range value
                ReadResponse.success(resourceid, maxRangeValue);

            case 5701 ->  // sensor units
                ReadResponse.success(resourceid, sensorUnits);

            default -> super.read(identity, resourceid);
        };
    }

    @Override
    public ExecuteResponse execute(ServerIdentity identity, int resourceid, String params) {
        if (resourceid == 5605) { // reset min and max measured values
            minMeasuredValue = null;
            maxMeasuredValue = null;
            System.out.println("min/max measured value reset");
            return ExecuteResponse.success();
        } else {
            return super.execute(identity, resourceid, params);
        }
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }

    public Float getMinRangeValue() {
        return minRangeValue;
    }

    public Float getMaxRangeValue() {
        return maxRangeValue;
    }

    //    private void init() {
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                double valToUpdate = Math.random() * (
//                        maxRangeValue - minRangeValue + 1
//                ) + minRangeValue;
//                if (!updateSensorValue((float) valToUpdate))
//                    System.out.println("somting brokn");
//                System.out.println(sensorValue);
//            }
//        }, 0, 3000);
//        System.out.println("sensor simulator running");
//    }
}
