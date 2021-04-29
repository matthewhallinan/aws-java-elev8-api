package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.dal.User;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.ArrayList;

public class CreateUser implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {

      try {
//          // get the 'body' from input
//		  logger.info(""); //logger.error("Error in saving user: " + ex);
////		  ObjectMapper mapper = new ObjectMapper();
//		  logger.info("2");
////		  JsonNode body = mapper.readTree((String) input.get("body"));
		  logger.info(input.toString());
		  System.out.println("JsonNode retrieved from input");
          JsonNode body = new ObjectMapper().readTree((String) input.get("body"));
		  logger.info("JsonNode retrieved from input");
		  System.out.println("JsonNode retrieved from input");

          // create the Product object for post
          User user = new User();
		  logger.info("User object created");
		  System.out.println("User object created" + user.toString());
		  user.setPk(body.get("pk").asText());
		  logger.info("Partition Key set");
		  System.out.println("Partition Key set" + body.get("pk").asText());
		  user.setSk(body.get("sk").asText());
		  logger.info("SortKey set");
		  System.out.println("SortKey set" + body.get("sk").asText());
		  user.setUsername(body.get("username").asText());
		  logger.info("Username set");
		  System.out.println("Username set" + body.get("username").asText());
		  user.setBuildingIDs(new ObjectMapper().convertValue(body.get("buildingIds"), ArrayList.class));
//		  user.setBuildingIDs((ArrayList<String>)body.get("buildingIds").findValues());
		  logger.info("Building IDs set");
		  System.out.println("Building IDs set" + new ObjectMapper().convertValue(body.get("buildingIds"), ArrayList.class).toString());
		  user.save(user);
		  logger.info("User saved; returned to handler");
		  System.out.println("User saved; returned to handler");

          // send the response back
      		return ApiGatewayResponse.builder()
      				.setStatusCode(200)
      				.setObjectBody(user)
      				.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
      				.build();

      } catch (Exception ex) {
          logger.error("Error in saving user: " + ex);

          // send the error response back
    			Response responseBody = new Response("Error in saving product: ", input);
    			return ApiGatewayResponse.builder()
    					.setStatusCode(500)
    					.setObjectBody(responseBody)
    					.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
    					.build();
      }
	}
}
