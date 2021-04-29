package com.serverless;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Map;

public class GetElevator implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

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
            String elevatorKey = pathParameters.get("pk");
            System.out.println("Elevator Key: " + elevatorKey );
            String elevatorId = pathParameters.get("sk");
            System.out.println("Elevator ID: " + elevatorId );

            Table table = dynamoDB.getTable(tableName);
            System.out.println("Table: " + table.getTableName() );
            Item item = table.getItem("pk", elevatorKey, "sk", elevatorId, "pk, sk, elevatorName, floors, currentFloor, elevatorState", null);
//            Item item = table.getItem("pk", buildingKey, "sk", buildingId, "pk, sk, buildingName, buildingLocation, elevatorIds", null);

            System.out.println("Printing item after retrieving it....");
            System.out.println(item.toJSONPretty());

            if(item.getString("elevatorState") == "Stopped") {
                //Ideally we should raise a message saying Elevator is busy/out of order as appropriate
                //For now, throw a generic Exception
                throw new Exception();
            }
            else{
                //Elevators only go up in version 1...
                AttributeUpdate bookedElevator = new AttributeUpdate("elevatorState").put("Up");
                UpdateItemOutcome outcome =  table.updateItem("pk", elevatorKey, "sk", elevatorId, bookedElevator );
            }

            // send the response back
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(item.toString())
                    .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
                    .build();

        } catch (Exception ex) {
            System.err.println("GetElevator failed.");
            System.err.println(ex.getMessage());

            logger.error("Error in getting Elevator: " + ex);
            System.out.println("Error in getting Elevator: " + ex);

            // send the error response back
            Response responseBody = new Response("Error in getting Elevator:: ", input);
            return ApiGatewayResponse.builder()
                    .setStatusCode(500)
                    .setObjectBody(responseBody)
                    .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
                    .build();
        }
    }

}
