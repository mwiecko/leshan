package org.example;

import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Device;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.core.LwM2mId;
import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.StaticModel;
import org.eclipse.leshan.core.request.BindingMode;
import org.example.stuff.TestObject;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String[] finalArgs = {"clientDef-test", "00"};
        System.arraycopy(args, 0, finalArgs, 0, args.length);

        LeshanClientBuilder builder = new LeshanClientBuilder(finalArgs[0]);

        String[] modelPaths = new String[] { "42800.xml"};
        List<ObjectModel> models = ObjectLoader.loadAllDefault();
        models.addAll(ObjectLoader.loadDdfResources("/models/", modelPaths));
        ObjectsInitializer initializer = new ObjectsInitializer(new StaticModel(models));

        initializer.setInstancesForObject(LwM2mId.SECURITY, Security.noSec("coap://localhost:5683", 2137));
        initializer.setInstancesForObject(LwM2mId.SERVER, new Server(2137, 10 * 60, BindingMode.U, false));
        initializer.setInstancesForObject(LwM2mId.DEVICE, new Device("Tester", "t2000", finalArgs[1], BindingMode.U.name()));
        initializer.setInstancesForObject(42800, new TestObject(666));

        builder.setObjects(initializer.createAll());

        LeshanClient client = builder.build();
        client.start();

//Debugging stuff
        while(true){
            Scanner sc = new Scanner(System.in);
            if (Objects.equals(sc.next(), "u"))
                client.triggerRegistrationUpdate();
        }
    }
}