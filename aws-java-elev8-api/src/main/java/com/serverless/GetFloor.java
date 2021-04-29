package com.serverless;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Map;

public class GetFloor implements RequestHandler<Map<String, Object>, ApiGatewayResponse>

    {

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
            String requestedFloor = pathParameters.get("floor");
            System.out.println("Requested Floor: " + requestedFloor );

            Table table = dynamoDB.getTable(tableName);
            System.out.println("Table: " + table.getTableName() );
            Item item = table.getItem("pk", elevatorKey, "sk", elevatorId, "pk, sk, elevatorName, floors, currentFloor, elevatorState", null);
            System.out.println("Retrieved Elevator: " + item.toString() );

            if(item.getString("currentFloor") != requestedFloor) {
//              Ideally we would validate the floor against the building
                AttributeUpdate selectedFloor = new AttributeUpdate("currentFloor").put(requestedFloor);
                System.out.println("Updated floor: " + selectedFloor.toString() );
                UpdateItemOutcome outcome =  table.updateItem("pk", elevatorKey, "sk", elevatorId, selectedFloor );
            }

            System.out.println("Printing item after retrieving it....");
            System.out.println(item.getString("currentFloor"));

            // send the response back
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(item.toString())
                    .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
                    .build();

        } catch (Exception ex) {
            System.err.println("GetFloor failed.");
            System.err.println(ex.getMessage());

            logger.error("Error in getting Floor: " + ex);
            System.out.println("Error in getting Floor: " + ex);

            // send the error response back
            Response responseBody = new Response("Error in getting Floor:: ", input);
            return ApiGatewayResponse.builder()
                    .setStatusCode(500)
                    .setObjectBody(responseBody)
                    .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
                    .build();
        }
    }

    }
