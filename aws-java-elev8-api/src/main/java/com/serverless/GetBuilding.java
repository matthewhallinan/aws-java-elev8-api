package com.serverless;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Map;

public class GetBuilding implements RequestHandler<Map<String, Object>, ApiGatewayResponse>{

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

            Table table = dynamoDB.getTable(tableName);
            System.out.println("Table: " + table.getTableName() );
            Item item = table.getItem("pk", buildingKey, "sk", buildingId, "pk, sk, buildingName, buildingLocation, elevatorIds", null);

            System.out.println("Printing item after retrieving it....");
            System.out.println(item.toJSONPretty());

            // send the response back
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(item.toString())
                    .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
                    .build();

        } catch (Exception ex) {
            System.err.println("GetBuilding failed.");
            System.err.println(ex.getMessage());

            logger.error("Error in getting building: " + ex);
            System.out.println("Error in getting building: " + ex);

            // send the error response back
            Response responseBody = new Response("Error in getting building:: ", input);
            return ApiGatewayResponse.builder()
                    .setStatusCode(500)
                    .setObjectBody(responseBody)
                    .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
                    .build();
        }
    }
}
