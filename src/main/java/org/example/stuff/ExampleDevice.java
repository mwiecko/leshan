package org.example.stuff;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;

import java.text.SimpleDateFormat;
import java.util.*;

public class ExampleDevice extends BaseInstanceEnabler {
    private static final List<Integer> supportedResources = Arrays.asList(0, 1, 2, 4, 11, 12, 13, 14, 15, 16, 31);
    private String manufacturer;
    private String modelNumber;
    private String serialNumber;
    private String supportedBinding;

    private String timezone = TimeZone.getDefault().getID();
    private String utcOffset = new SimpleDateFormat("X").format(Calendar.getInstance().getTime());

    public ExampleDevice(){}

    public ExampleDevice(String manufacturer, String modelNumber, String serialNumber, String supportedBinding) {
        this.manufacturer = manufacturer;
        this.modelNumber = modelNumber;
        this.serialNumber = serialNumber;
        this.supportedBinding = supportedBinding;
    }

    @Override
    public ReadResponse read(ServerIdentity identity, int resourceid) {

        switch (resourceid) {
            case 0: // manufacturer
                return ReadResponse.success(resourceid, manufacturer);

            case 1: // model number
                return ReadResponse.success(resourceid, modelNumber);

            case 2: // serial number
                return ReadResponse.success(resourceid, serialNumber);

            case 11: // error codes
                return ReadResponse.success(resourceid, new HashMap<Integer, Integer>(), ResourceModel.Type.INTEGER);

            case 13: // current time
                return ReadResponse.success(resourceid, System.currentTimeMillis());

            case 14: // utc offset
                return ReadResponse.success(resourceid, utcOffset);

            case 15: // timezone
                return ReadResponse.success(resourceid, timezone);

            case 16: // supported binding and modes
                return ReadResponse.success(resourceid, supportedBinding);

            case 31: //custom
                System.out.println("testing");
                return ReadResponse.success(resourceid, "custom");
            default:
                return super.read(identity, resourceid);
        }
    }

    @Override
    public WriteResponse write(ServerIdentity identity, int resourceid, LwM2mResource value) {
        switch(resourceid){
            case 14: // utc offset
                if (value.getType() != ResourceModel.Type.STRING) {
                    return WriteResponse.badRequest("invalid type");
                }
                String previousUtcOffset = utcOffset;
                utcOffset = value.getValue().toString();
                if (!Objects.equals(previousUtcOffset, utcOffset))
                    fireResourcesChange(resourceid);
                return WriteResponse.success();

            case 15: //timezone
                if (value.getType() != ResourceModel.Type.STRING) {
                    return WriteResponse.badRequest("invalid type");
                }
                String previousTimezone = timezone;
                timezone = value.getValue().toString();
                if (!Objects.equals(previousTimezone, timezone))
                    fireResourcesChange(resourceid);
                return WriteResponse.success();

            default:
                return super.write(identity, resourceid, value);
        }
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }
}
