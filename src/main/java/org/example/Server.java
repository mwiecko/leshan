package org.example;


import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.request.ExecuteRequest;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.model.LwM2mModelProvider;
import org.eclipse.leshan.server.model.StaticModelProvider;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.leshan.server.registration.RegistrationUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class Server {
    public static final Logger LOG = LoggerFactory.getLogger("server----");
    public static void main(String[] args) {

        System.out.println("test 2");
        LeshanServerBuilder builder = new LeshanServerBuilder();

        String[] modelPaths = new String[]{"3303.xml", "3306.xml", "42800.xml"};
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
                LOG.info(lt+" New Device: \"" + registration.getEndpoint() + "\"");
            }

            public void updated(RegistrationUpdate update, Registration updatedReg, Registration previousReg) {
                String lt = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                LOG.info(lt+" Device \""+ updatedReg.getEndpoint()+ "\" is still here");
            }

            public void unregistered(Registration registration, Collection<Observation> observations, boolean expired, Registration newReg) {
                regList.remove(registration);
                String lt = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                LOG.info(lt+" Device \""+registration.getEndpoint()+"\" left");
            }
        });

//Debugging stuff
        int clientId;
        int objectId;
        int objectInstanceId;
        int resource;
        char action;
        String value = "";
        String memory = "";

        while (true) {
            System.out.print(memory);
            Scanner sc = new Scanner(System.in);
            String input = sc.nextLine();
            String[] request = input.split("/");

            if (input.isEmpty()){
                continue;
            }
            if (input.equals("stop")){
                server.stop();
                continue;
            }
            if (input.equals("start")) {
                server.start();
                continue;
            }
            if (Objects.equals(request[0], "m+")) {
                input = input.replaceFirst("m\\+/", "");
                memory += input;
                if (memory.charAt(memory.length()-1) != '/')
                    memory += '/';
                continue;
            } else if (Objects.equals(request[0], "m-")) {
                String[] memSplit = memory.split("/");
                memory = "";
                for (int i = 0; i < memSplit.length - 1; i++) {
                    memory += memSplit[i] +'/';
                }
                continue;
            } else if (Objects.equals(request[0], "mc")) {
                memory = "";
                System.out.println("memory cleared");
                continue;
            }

            input = memory + input;
            request = input.split("/");

            if (request[0].charAt(0) == '?'){
                showFullHint();
                continue;
            } else if (request.length < 5) {
                showHint();
                continue;
            } else {
                try {
                    clientId = Integer.parseInt(request[0]);
                    objectId = Integer.parseInt(request[1]);
                    objectInstanceId = Integer.parseInt(request[2]);
                    resource = Integer.parseInt(request[3]);
                    action = request[4].charAt(0);
                }catch (Exception ignored){
                    showHint();
                    continue;
                }
            }
            if (request.length == 6)
                value = request[5];

            if (action == 'w' && request.length != 6) {
                System.out.println("write action requires value");
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
                            LOG.info("response: " + ((LwM2mResource) response.getContent()).getValue());
                        else
                            LOG.error("Failed to read:" + response.getCode() + " " + response.getErrorMessage());
                    }
                    case 'w' -> {
                        WriteResponse response = anyDataTypeWriteRequest(
                                server,
                                regList,
                                clientId,
                                objectId,
                                objectInstanceId,
                                resource,
                                value
                        );
                        if (response.isSuccess())
                            LOG.info("value changed");
                        else
                            LOG.error("Failed to write:" + response.getCode() + " " + response.getErrorMessage());
                    }
                    case 'e' -> {
                        ExecuteResponse response = server.send(regList.get(clientId), new ExecuteRequest(
                                objectId,
                                objectInstanceId,
                                resource
                        ));
                        if (response.isSuccess())
                            LOG.info("Execution successful");
                        else
                            LOG.error("Failed to execute:" + response.getCode() + " " + response.getErrorMessage());
                    }
                    default -> showHint();
                }
                Thread.sleep(500);
            }catch (Exception ignored) {}
        }
    }

    private static WriteResponse anyDataTypeWriteRequest(LeshanServer server, ArrayList<Registration> regList, int clientId,
                                                         int objectID, int objectInstanceID, int resource, String value) throws InterruptedException {
        String refinedValue = value.replaceFirst(String.valueOf(value.charAt(0)), "");
        WriteRequest request;
        switch (value.charAt(0)){
            case 's' -> request = new WriteRequest(
                    objectID,
                    objectInstanceID,
                    resource,
                    refinedValue
            );
            case 'b' -> request = new WriteRequest(
                    objectID,
                    objectInstanceID,
                    resource,
                    Boolean.parseBoolean(refinedValue)
            );
            case 'i' -> request = new WriteRequest(
                    objectID,
                    objectInstanceID,
                    resource,
                    Integer.parseInt(refinedValue)
            );
            default -> request = null;
        }
        return server.send(regList.get(clientId), request);
    }

    private static void showHint(){
        System.out.println("format: id/objectId/objectInstanceId/resource/action/(value)\ntype '?' for more info");
    }

    private static void showFullHint(){
        System.out.println("""
                        ---------------------
                        format: clientPseudoId/objectId/objectInstanceId/resource/action/(value)
                        available actions:
                        r -> read
                        w -> write
                        e -> execute
                        ---------------------
                        """
        );
    }
}