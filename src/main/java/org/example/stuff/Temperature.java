package org.example.stuff;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;

import java.util.Arrays;
import java.util.List;

public class Temperature extends BaseInstanceEnabler {
    private static final List<Integer> supportedResources = Arrays.asList(5700, 5601, 5602, 5603, 5604, 5701, 5605);

    private Float sensorValue;  //5700
    private Float minMeasuredValue;  //5601
    private Float maxMeasuredValue;  //5602
    private final Float minRangeValue = -100f;  //5603
    private final Float maxRangeValue = 100f;  //5604
    private final String sensorUnits = "°C";  //5701

    public Temperature(){}


    @Override
    public ReadResponse read(ServerIdentity identity, int resourceid) {
        switch (resourceid) {
            case 5700 -> { // sensor value
                if (sensorValue != null)
                    return ReadResponse.success(resourceid, sensorValue);
                else return ReadResponse.notFound();
            }
            case 5601 -> { // min measured value
                if (minMeasuredValue != null)
                    ReadResponse.success(resourceid, minMeasuredValue);
                else return ReadResponse.notFound();
            }
            case 5602 -> { // max measured value
                if (maxMeasuredValue != null)
                    ReadResponse.success(resourceid, maxMeasuredValue);
                else return ReadResponse.notFound();
            }
            case 5603 ->  // min range value
                ReadResponse.success(resourceid, minRangeValue);

            case 5604 ->  // max range value
                ReadResponse.success(resourceid, maxRangeValue);

            case 5701 ->  // sensor units
                ReadResponse.success(resourceid, sensorUnits);

            default -> {
                return super.read(identity, resourceid);
            }
        }
        return ReadResponse.notAcceptable();
    }

    @Override
    public ExecuteResponse execute(ServerIdentity identity, int resourceid, String params) {
        if (resourceid == 5605) { // reset min and max measured values
            minMeasuredValue = null;
            maxMeasuredValue = null;
            return ExecuteResponse.success();
        } else {
            return super.execute(identity, resourceid, params);
        }
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }
}
