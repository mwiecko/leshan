package org.example.objects;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;

import java.util.*;

public class TestObject extends BaseInstanceEnabler {
    private static final List<Integer> supportedResources = Arrays.asList(1, 2);
    private int id;
    private String writeTest;
    public TestObject(){}

    public TestObject(int id) {
        this.id = id;
    }

    @Override
    public ReadResponse read(ServerIdentity identity, int resourceid) {

        switch (resourceid) {
            case 1 -> { // model id
                System.out.println("read attempt");
                return ReadResponse.success(resourceid, id);
            }
            case 2 -> { // write test
                System.out.println("read attempt");
                return ReadResponse.success(resourceid, writeTest);
            }
            default -> {
                return super.read(identity, resourceid);
            }
        }
    }

    @Override
    public WriteResponse write(ServerIdentity identity, int resourceid, LwM2mResource value) {
        switch (resourceid) {
            case 2 -> { // write test
                System.out.println("attempting to write value: "+ value.getValue().toString());
                if (value.getType() != ResourceModel.Type.STRING) {
                    return WriteResponse.badRequest("invalid type");
                }
                String previousUtcOffset = writeTest;
                writeTest = value.getValue().toString();
                if (!Objects.equals(previousUtcOffset, writeTest))
                    fireResourcesChange(resourceid);
                return WriteResponse.success();
            }
            default -> {
                return super.write(identity, resourceid, value);
            }
        }
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }
}
