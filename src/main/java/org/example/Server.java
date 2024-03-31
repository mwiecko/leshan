package org.example;


import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.request.*;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.server.model.LwM2mModelProvider;
import org.eclipse.leshan.server.model.StaticModelProvider;
import org.eclipse.leshan.server.registration.RegistrationUpdate;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class Server {
    public static void main(String[] args) {
        LeshanServerBuilder builder = new LeshanServerBuilder();

        String[] modelPaths = new String[]{"42800.xml"};
        List<ObjectModel> models = ObjectLoader.loadAllDefault();
        models.addAll(ObjectLoader.loadDdfResources("/models/", modelPaths));
        LwM2mModelProvider modelProvider = new StaticModelProvider(models);
        builder.setObjectModelProvider(modelProvider);

        LeshanServer server = builder.build();
        server.start();


        ArrayList<Registration> regList = new ArrayList<>();

        server.getRegistrationService().addListener(new RegistrationListener() {
            public void registered(Registration registration, Registration previousReg,
                                   Collection<Observation> previousObsersations) {
                regList.add(registration);
                String lt = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                System.out.println(lt+" New Device: \"" + registration.getEndpoint() + "\"");
            }

            public void updated(RegistrationUpdate update, Registration updatedReg, Registration previousReg) {
                String lt = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                System.out.println(lt+" Device \""+ updatedReg.getEndpoint()+ "\" is still here");
            }

            public void unregistered(Registration registration, Collection<Observation> observations, boolean expired, Registration newReg) {
                regList.remove(registration);
                String lt = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                System.out.println(lt+" Device \""+registration.getEndpoint()+"\" left");
            }
        });

//Debugging stuff
        int clientId;
        char action;
        int objectId;
        int objectInstanceId;
        int resource;
        String value = "";

        while (true) {
            Scanner sc = new Scanner(System.in);
            String input = sc.nextLine();
            String[] request = input.split("/");

            if (input.isEmpty()) continue;
            if (request[0].charAt(0) == '?'){
                showFullHint();
                continue;
            } else if (request.length < 5) {
                showHint();
                continue;
            } else {
                try {
                    clientId = Integer.parseInt(request[0]);
                    action = request[1].charAt(0);
                    objectId = Integer.parseInt(request[2]);
                    objectInstanceId = Integer.parseInt(request[3]);
                    resource = Integer.parseInt(request[4]);
                }catch (Exception ignored){
                    showHint();
                    continue;
                }
            }
            if (request.length == 6)
                value = request[5];

            if (action == 'w' && request.length != 6) {
                System.out.println("write action requires value argument");
                showHint();
                continue;
            }

            try {
                switch (action) {
                    case 'r' -> {
                        ReadResponse response = server.send(regList.get(clientId), new ReadRequest(
                                objectId,
                                objectInstanceId,
                                resource
                        ));
                        if (response.isSuccess())
                            System.out.println("response: " + ((LwM2mResource) response.getContent()).getValue());
                        else
                            System.out.println("Failed to read:" + response.getCode() + " " + response.getErrorMessage());
                    }
                    case 'w' -> {
                        WriteResponse response = server.send(regList.get(clientId), new WriteRequest(
                                objectId,
                                objectInstanceId,
                                resource,
                                value
                        ));
                        if (response.isSuccess())
                            System.out.println("value changed");
                        else
                            System.out.println("Failed to write:" + response.getCode() + " " + response.getErrorMessage());
                    }
                    case 'e' -> {
                        ExecuteResponse response = server.send(regList.get(clientId), new ExecuteRequest(
                                objectId,
                                objectInstanceId,
                                resource
                        ));
                        if (response.isSuccess())
                            System.out.println("Execution successful");
                        else
                            System.out.println("Failed to execute:" + response.getCode() + " " + response.getErrorMessage());
                    }
                    default -> showHint();
                }
            }catch (Exception ignored) {}
        }
    }
    private static void showHint(){
        System.out.println("format: id/r/objectId/objectInstanceId/resource\ntype '?' for more info");
    }

    private static void showFullHint(){
        System.out.println("""
                        ---------------------
                        format: clientPseudoId/action/objectId/objectInstanceId/resource/(value)
                        available actions:
                        r -> read
                        w -> write
                        e -> execute
                        ---------------------
                        """
        );
    }
}