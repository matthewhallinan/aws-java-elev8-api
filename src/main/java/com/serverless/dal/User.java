package com.serverless.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//@DynamoDBTable(tableName = "PLACEHOLDER_ELEVATOR_TABLE_NAME")
@DynamoDBTable(tableName = "java-elevator-dev")
public class User {

    // get the table name from env. var. set in serverless.yml
    private static final String ELEVATOR_TABLE_NAME = System.getenv("ELEVATOR_TABLE_NAME");

    private static DynamoDBAdapter db_adapter;
    private final AmazonDynamoDB client;
    private final DynamoDBMapper mapper;
    static AmazonDynamoDB client2 = AmazonDynamoDBClientBuilder.standard().build();
    static DynamoDB dynamoDB2 = new DynamoDB(client);

    static String tableName = "ProductCatalog";

    private Logger logger = Logger.getLogger(this.getClass());

    private String pk;
    private String sk;
    private String username;
    private ArrayList<String> buildingIds;

    @DynamoDBHashKey(attributeName = "pk")
    public String getPk() {
        return this.pk;
    }
    public void setPk(String sk) { this.sk = sk; }

    @DynamoDBRangeKey(attributeName = "sk")
    public String getSk() {
        return this.sk;
    }
    public void setSk(String sk) {
        this.sk = sk;
    }

    @DynamoDBAttribute(attributeName = "username")
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) { this.username = username; }

    @DynamoDBAttribute(attributeName = "buildingIds")
    public ArrayList<String> getBuildingIDs() {
        return this.buildingIds;
    }
    public void setBuildingIDs(ArrayList<String> buildingIds) { this.buildingIds = buildingIds; }

    public User() {
        // build the mapper config
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride(ELEVATOR_TABLE_NAME))
            .build();
        // get the db adapter
        this.db_adapter = DynamoDBAdapter.getInstance();
        this.client = this.db_adapter.getDbClient();
        // create the mapper with config
        this.mapper = this.db_adapter.createDbMapper(mapperConfig);
    }

    public String toString() {
        return String.format("User [sk=%s, username=%s, buildingIds=$%s]", this.sk, this.username, this.buildingIds);
    }

    // methods
    public Boolean ifTableExists() {
        return this.client.describeTable(ELEVATOR_TABLE_NAME).getTable().getTableStatus().equals("ACTIVE");
    }

    public User get(String id) throws IOException {
        User user = null;
        logger.info("Null User created");

        HashMap<String, AttributeValue> av = new HashMap<String, AttributeValue>();
        av.put(":v1", new AttributeValue().withS(id));
        logger.info("HashMap created");

        DynamoDBQueryExpression<User> queryExp = new DynamoDBQueryExpression<User>()
            .withKeyConditionExpression("pk = :v1")
            .withExpressionAttributeValues(av);
        logger.info("Query created");

        PaginatedQueryList<User> result = this.mapper.query(User.class, queryExp);
        logger.info("Query result retrieved");
        if (result.size() > 0) {
          user = result.get(0);
          logger.info("User - get(): user - " + user.toString());
        } else {
          logger.info("Users - get(): user - Not Found.");
        }
        return user;
    }

    public void save(User user) throws IOException {
        logger.info("User - save(): " + user.toString());
        System.out.println("User - save(): " + user.toString());

        String tableName = ELEVATOR_TABLE_NAME;
        Table table = mapper.get.getTable(tableName);

//        this.mapper.save(user);
        logger.info("Mapper Save completed");
        System.out.println("Mapper Save completed");
    }

}
