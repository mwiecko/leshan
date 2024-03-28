package org.example;


import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.request.*;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.server.registration.RegistrationUpdate;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;

import java.util.*;


public class Main {
    public static void main(String[] args) {
        LeshanServerBuilder builder = new LeshanServerBuilder();
        LeshanServer server = builder.build();
        server.start();

        ArrayList<Registration> regList = new ArrayList<>();

        server.getRegistrationService().addListener(new RegistrationListener() {
            public void registered(Registration registration, Registration previousReg,
                                   Collection<Observation> previousObsersations) {
                regList.add(registration);
                System.out.println("new device: " + registration.getEndpoint());
            }

            public void updated(RegistrationUpdate update, Registration updatedReg, Registration previousReg) {
                System.out.println("device is still here: " + updatedReg.getEndpoint());
            }

            public void unregistered(Registration registration, Collection<Observation> observations, boolean expired, Registration newReg) {
                regList.remove(registration);
                System.out.println("device left: " + registration.getEndpoint());
            }
        });

//Debugging stuff
        while(true) {
            Scanner sc = new Scanner(System.in);
            String input = sc.nextLine();
            String[] request = input.split("/");
            Object formattedRequest = null;

            switch (request[1]){
                case "r" -> {
                    if (request.length == 5)
                        formattedRequest = (Object) (new ReadRequest(
                                Integer.parseInt(request[2]),
                                Integer.parseInt(request[3]),
                                Integer.parseInt(request[4])
                        ));
                    else System.out.println("format: id/r/objectId/objectInstanceId/resource\ntype '?' for more info");
                }
                case "w" -> {
                    if (request.length == 6)
                        formattedRequest = (Object) (new WriteRequest(
                                Integer.parseInt(request[2]),
                                Integer.parseInt(request[3]),
                                Integer.parseInt(request[4]),
                                Long.parseLong(request[5])
                        ));
                    else System.out.println("format: id/w/objectId/objectInstanceId/resource/value\ntype '?' for more info");
                }
                case "e" -> {
                    if (request.length == 5)
                        formattedRequest = (Object) (new ExecuteRequest(
                                Integer.parseInt(request[2]),
                                Integer.parseInt(request[3]),
                                Integer.parseInt(request[4])
                        ));
                    else System.out.println("format: id/e/objectId/objectInstanceId/resource\ntype '?' for more info");
                }
                case "?" -> System.out.println("""
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
            try {
                ReadResponse response = server.send(
                        regList.get(Integer.parseInt(request[0])),
                        (DownlinkRequest<? extends ReadResponse>) formattedRequest
                );
                if (response.isSuccess())
                    System.out.println("response: "+ ((LwM2mResource) response.getContent()).getValue());
                else
                    System.out.println("Failed to read:" + response.getCode() + " " + response.getErrorMessage());
            } catch (Exception ignored) {}
        }
    }
}