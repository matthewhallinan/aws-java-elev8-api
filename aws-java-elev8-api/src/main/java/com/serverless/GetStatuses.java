package com.serverless;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class GetStatuses implements RequestHandler<Map<String, Object>, ApiGatewayResponse>{

    private final Logger logger = Logger.getLogger(this.getClass());

    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    static String tableName = "java-elevator-dev";

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {

        try {
            // get the 'pathParameters' from input
            Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
            System.out.println("Path Params: " + pathParameters.toString());
            String buildingKey = pathParameters.get("pk");
            System.out.println("Building ID: " + buildingKey );
            String buildingId = pathParameters.get("sk");
            System.out.println("Building ID: " + buildingId );
            //Below is just a flag - not using it in this version but would check if == true.
            // Currently just using to distinguish from a normal building get
            String statusCheck = pathParameters.get("statusCheck");
            System.out.println("StatusCheck: " + statusCheck );

            Table table = dynamoDB.getTable(tableName);
            System.out.println("Table: " + table.getTableName() );
            Item item = table.getItem("pk", buildingKey, "sk", buildingId, "pk, sk, buildingName, buildingLocation, elevatorIds", null);

            System.out.println("Printing elevator IDs after retrieving them....");
            System.out.println("..from item directly: " + item.getList("elevatorIds"));

            ArrayList<String> elevatorIds = new ArrayList<>(item.getList("elevatorIds"));
            System.out.println("..from converted list: " + item.getList("elevatorIds"));

            ArrayList<String> elevatorStates = new ArrayList<String>();

            Item elevator = new Item();
            for (String elevatorId : elevatorIds){
                System.out.println("Retrieving Elevator " + elevatorId);
                elevator = table.getItem("pk", "Elevator", "sk", elevatorId, "pk, sk, elevatorName, floors, currentFloor, elevatorState", null);

                if( elevator != null) {
                    System.out.println("Elevator:  " + elevator.getString("elevatorState"));
                    elevatorStates.add(elevator.getString("elevatorState"));
                    System.out.println("Added Elevator" + elevatorId + " successfully to list");
                }
            }

            System.out.println("Loop complete; sending response!");

            // send the response back
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(elevatorStates.toString())
                    .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
                    .build();

        } catch (Exception ex) {
            System.err.println("GetStatuses failed.");
            System.err.println(ex.getMessage());

            logger.error("Error in getting Elevator states: " + ex);
            System.out.println("Error in getting Elevator States: " + ex);

            // send the error response back
            Response responseBody = new Response("Error in getting Elevator States:: ", input);
            return ApiGatewayResponse.builder()
                    .setStatusCode(500)
                    .setObjectBody(responseBody)
                    .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
                    .build();
        }
    }

}