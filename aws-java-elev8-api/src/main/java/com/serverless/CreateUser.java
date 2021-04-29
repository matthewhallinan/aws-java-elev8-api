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
import java.util.*;

public class CreateUser implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private final Logger logger = Logger.getLogger(this.getClass());

	static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
	static DynamoDB dynamoDB = new DynamoDB(client);
	static String tableName = "java-elevator-dev";

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {

      try {

		  logger.info(input.toString());
		  System.out.println("Input " + input.toString());
		  JsonNode body = new ObjectMapper().readTree((String) input.get("body"));
		  logger.info("JsonNode retrieved from input");
		  System.out.println("JsonNode retrieved from input: " + (String) input.get("body"));

		  Table table = dynamoDB.getTable(tableName);
		  Item item = new Item().withPrimaryKey("pk", (body.get("pk").asText()))
				  .withString("sk", (body.get("sk").asText()))
				  .withString("username", (body.get("username").asText()))
				  .withList("buildingIds", new ObjectMapper().convertValue(body.get("buildingIds"), ArrayList.class));
		  table.putItem(item);

          // send the response back
      		return ApiGatewayResponse.builder()
      				.setStatusCode(200)
      				.setObjectBody(item.toString())
      				.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
      				.build();

      } catch (Exception ex) {
          logger.error("Error in saving user: " + ex);
		  System.out.println("Error in saving user: " + ex);

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
