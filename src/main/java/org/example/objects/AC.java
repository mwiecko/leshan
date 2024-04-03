package org.example.objects;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel.Type;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AC extends BaseInstanceEnabler {
    private static final List<Integer> supportedResources = Arrays.asList(5850, 5851, 5852, 5750);

    private Boolean onOff;  //5850
    private Integer powerLevel;  //5851
    private Integer onTime;  //5851
    private String mode; //5750

    private final List<String> modes = List.of("cooling", "heating");

    public AC(){
        onOff = false;
        powerLevel = 100;
        mode = "cooling";
        armClock();
    }

    @Override
    public ReadResponse read(ServerIdentity identity, int resourceid) {
        System.out.println("read request received");
        return switch (resourceid){
            case 5850 ->
                ReadResponse.success(resourceid, onOff);

            case 5851 ->
                ReadResponse.success(resourceid, powerLevel);

            case 5852 ->
                ReadResponse.success(resourceid, onTime);

            case 5750 ->
                ReadResponse.success(resourceid, mode);

            default -> super.read(identity, resourceid);
        };
    }

    @Override
    public WriteResponse write(ServerIdentity identity, int resourceid, LwM2mResource value) {
        System.out.println("write request received");
        switch(resourceid) {
            case 5850 ->{
                if (value.getType() != Type.BOOLEAN)
                    return WriteResponse.badRequest("invalid type: "+ value.getType().toString());
                onOff = (boolean) value.getValue();
                return WriteResponse.success();
            }
            case 5851 ->{
                if (value.getType() != Type.INTEGER)
                    return WriteResponse.badRequest("invalid type: "+ value.getType().toString());
                int intValue = ((Long) value.getValue()).intValue();
                if (intValue < 0 || intValue > 100)
                    return WriteResponse.badRequest("value out of range");
                powerLevel = intValue;
                return WriteResponse.success();
            }
            case 5852 -> {
                if (value.getType() != Type.INTEGER)
                    return WriteResponse.badRequest("invalid type: "+ value.getType().toString());
                int intValue = ((Long) value.getValue()).intValue();
                if (intValue != 0)
                    return WriteResponse.badRequest("invalid value");
                onTime = 0;
                return WriteResponse.success();
            }
            case 5750 -> {
                if (value.getType() != Type.STRING)
                    return WriteResponse.badRequest("invalid type: "+ value.getType().toString());
                String strValue = (String) value.getValue();
                if (!modes.contains(strValue))
                    return WriteResponse.badRequest("invalid value");
                mode = strValue;
                return WriteResponse.success();
            }
            default -> super.read(identity, resourceid);
        }
        return WriteResponse.internalServerError("");
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }

    private void armClock(){
        onTime = 0;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (onOff)
                    onTime += 1;
            }
        }, 0, 1000);
    }
}
