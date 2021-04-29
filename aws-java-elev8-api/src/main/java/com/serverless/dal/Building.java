package com.serverless.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@DynamoDBTable(tableName = "PLACEHOLDER_PRODUCTS_TABLE_NAME")
public class Building {

    // get the table name from env. var. set in serverless.yml
    private static final String ELEVATOR_TABLE_NAME = System.getenv("ELEVATOR_TABLE_NAME");

    private static DynamoDBAdapter db_adapter;
    private final AmazonDynamoDB client;
    private final DynamoDBMapper mapper;

    private Logger logger = Logger.getLogger(this.getClass());

    private String pk;
    private String sk;
    private String buildingName;
    private String buildingLocation;
    private ArrayList<Integer> elevatorIDs;

    @DynamoDBHashKey(attributeName = "pk")
    public String getPk() {
        return this.pk;
    }
    public void setPk(String sk) { this.pk = pk; }

    @DynamoDBRangeKey(attributeName = "sk")
    public String getSk() {
        return this.sk;
    }
    public void setSk(String sk) {
        this.sk = sk;
    }

    @DynamoDBAttribute(attributeName = "buildingName")
    public String getBuildingName() {
        return this.buildingName;
    }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    @DynamoDBAttribute(attributeName = "buildingLocation")
    public String getBuildingLocation() {
        return this.buildingLocation;
    }
    public void setBuildingLocation(String buildingLocation) { this.buildingLocation = buildingLocation; }

    @DynamoDBAttribute(attributeName = "elevatorIDs")
    public ArrayList<Integer> getElevatorIDs() {
        return this.elevatorIDs;
    }
    public void setElevatorIDs(ArrayList<Integer> elevatorIDs) { this.elevatorIDs = elevatorIDs; }

    public Building() {
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
        return String.format("Building [sk=%s, buildingName=%s, buildingLocation=%s, elevatorIds=$%s]", this.sk, this.buildingName, this.buildingLocation, this.elevatorIDs);
    }

    // methods
    public Boolean ifTableExists() {
        return this.client.describeTable(ELEVATOR_TABLE_NAME).getTable().getTableStatus().equals("ACTIVE");
    }

    public Building get(String id) throws IOException {
        Building building = null;

        HashMap<String, AttributeValue> av = new HashMap<String, AttributeValue>();
        av.put(":v1", new AttributeValue().withS(id));

        DynamoDBQueryExpression<Building> queryExp = new DynamoDBQueryExpression<Building>()
            .withKeyConditionExpression("sk = :v1")
            .withExpressionAttributeValues(av);

        PaginatedQueryList<Building> result = this.mapper.query(Building.class, queryExp);
        if (result.size() > 0) {
          building = result.get(0);
          logger.info("Building - get(): building - " + building.toString());
        } else {
          logger.info("Building - building(): building - Not Found.");
        }
        return building;
    }

    public void save(Building building) throws IOException {
        logger.info("Building - save(): " + building.toString());
        this.mapper.save(building);
    }

}
