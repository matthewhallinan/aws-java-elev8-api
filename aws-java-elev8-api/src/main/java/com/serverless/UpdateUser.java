package com.serverless;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class UpdateUser implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private final Logger logger = Logger.getLogger(this.getClass());

    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    static String tableName = "java-elevator-dev";

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {

        try {

            logger.info(input.toString());
            System.out.println("Input " + input.toString());

            Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
            System.out.println("Path Params: " + pathParameters.toString());
            String userKey = pathParameters.get("pk");
            String userId = pathParameters.get("sk");

            Table table = dynamoDB.getTable(tableName);
            Item item = table.getItem("pk", userKey, "sk", userId, "pk, sk, username, buildingIds", null);

            if( item != null){

                JsonNode body = new ObjectMapper().readTree((String) input.get("body"));
                logger.info("JsonNode retrieved from input");
                System.out.println("JsonNode retrieved from input: " + (String) input.get("body"));

//              This will do as a basic for now; ideally I would compare each value to the DB attribute.
                item = new Item().withPrimaryKey("pk", (body.get("pk").asText()))
                        .withString("sk", (body.get("sk").asText()))
                        .withString("username", (body.get("username").asText()))
                        .withList("buildingIds", new ObjectMapper().convertValue(body.get("buildingIds"), ArrayList.class));
//              If time allows, will update to use updateItem later
                table.putItem(item);
                System.out.println("Update successful: " + item.toJSONPretty());

            }
            else{
                //Make this more robust later
                throw new Exception();
            }

            // send the response back
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(item.toString())
                    .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
                    .build();

        } catch (Exception ex) {
            logger.error("Error in updating user: " + ex);
            System.out.println("Error in updating user: " + ex);

            // send the error response back
            Response responseBody = new Response("Error in saving user: ", input);
            return ApiGatewayResponse.builder()
                    .setStatusCode(500)
                    .setObjectBody(responseBody)
                    .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
                    .build();
        }
    }
}
