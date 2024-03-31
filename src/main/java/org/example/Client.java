package org.example;

import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Device;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.core.LwM2mId;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.request.BindingMode;
import org.example.stuff.ExampleDevice;

import java.util.Objects;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
    public static void main(String[] args) {
        String[] finalArgs = {"clientDef-test", "00"};
        System.arraycopy(args, 0, finalArgs, 0, args.length);

        LeshanClientBuilder builder = new LeshanClientBuilder(finalArgs[0]);
        ObjectsInitializer initializer = new ObjectsInitializer();

        initializer.setInstancesForObject(LwM2mId.SECURITY, Security.noSec("coap://localhost:5683", 2137));
        initializer.setInstancesForObject(LwM2mId.SERVER, new Server(2137, 5 * 60, BindingMode.U, false));
        initializer.setInstancesForObject(LwM2mId.DEVICE, new Device("MW", "t2000", finalArgs[1], BindingMode.U.name()));
        ResourceModel myrm= new ResourceModel(111, prostymodel, ResourceModel.Operations operations, 0, 1, STRING, String rangeEnumeration, String units, String description) {

            ObjectModel myom = new ObjectModel(123, "MyModel", "Sensor", "v1.0", 0, 1, Collection<ResourceModel > resources, String urn, String lwm2mVersion, String description2)
        builder.setObjects(initializer.createAll());

         //   return initializer.model.objects;

        LeshanClient client = builder.build();
        //Client client1 = this;
        //LOG.  .info("Leshan client started.");
//        System.LoggerFinder lf= System.LoggerFinder.getLoggerFinder();// .Logger.Level(SEVERE); // Level(20);//  .info("Starting Leshan client ...");
//        System.Logger logger =lf.getLogger();
        System.out.println("Client " +finalArgs[0] + " ready to start ...");
        client.start();
        System.out.println("Client " +finalArgs[0] + " started successfully and is running ...");
//Debugging stuff
        while(true){
            Scanner sc = new Scanner(System.in);
            if (Objects.equals(sc.next(), "u")) {
                System.out.println("Before trigger");
                client.triggerRegistrationUpdate();
                System.out.println("After trigger");
            }
        }
    }
}