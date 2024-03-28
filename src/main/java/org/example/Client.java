package org.example;

import org.eclipse.leshan.client.LwM2mClient;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Device;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.core.LwM2mId;
import org.eclipse.leshan.core.request.BindingMode;

import javax.management.ObjectInstance;
import java.util.Objects;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        LeshanClientBuilder builder = new LeshanClientBuilder(args[0]);
        ObjectsInitializer initializer = new ObjectsInitializer();

        initializer.setInstancesForObject(LwM2mId.SECURITY, Security.noSec("coap://localhost:5683", 2137));
        initializer.setInstancesForObject(LwM2mId.SERVER, new Server(2137, 5 * 60, BindingMode.U, false));
        initializer.setInstancesForObject(LwM2mId.DEVICE, new Device("Tester", "t2000", args[1], "U"));

        builder.setObjects(initializer.createAll());

        LeshanClient client = builder.build();
        client.start();

        while(true){
            Scanner sc = new Scanner(System.in);
            if (Objects.equals(sc.next(), "u"))
                client.triggerRegistrationUpdate();
        }
    }
}
