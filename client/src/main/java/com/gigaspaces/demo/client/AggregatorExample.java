package com.gigaspaces.demo.client;

import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.demo.common.MyData;
import com.gigaspaces.query.aggregators.AggregationResult;
import com.gigaspaces.query.aggregators.AggregationSet;
import com.j_spaces.core.client.SQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static org.openspaces.extensions.QueryExtension.max;

public class AggregatorExample extends Client {

    private static final Logger logger = LoggerFactory.getLogger(AggregatorExample.class);


    public AggregatorExample()  {
        super();
    }

    // Write Data objects
    public void writeObjects(int numObjects) {
        for (int i = 0; i < numObjects; i++) {
            MyData data = new MyData();
            data.setId(i);
            data.setValue((long) i);
            data.setProcessed(Boolean.FALSE);
            gigaSpace.write(data);
        }
        logger.info("write is done...");
    }


    public Integer maxIntProperty() {
        SQLQuery<MyData> query = new SQLQuery<MyData>(MyData.class, "");


        Integer maxIdinSpace =  max(gigaSpace, query, "id" );
        logger.info("maxIdinSpace is: " + maxIdinSpace);
        return maxIdinSpace;
    }

    public Long maxLongProperty() {
        SQLQuery<MyData> query = new SQLQuery<MyData>(MyData.class, "");

        Long maxValue = max(gigaSpace, query, "value");

        logger.info("maxValue is: " + maxValue);
        return maxValue;
    }

    public Integer minValue() {
        SQLQuery<MyData> sqlQuery = new SQLQuery<MyData>(MyData.class, "");

        AggregationResult aggregationResult = gigaSpace.aggregate(sqlQuery,
                new AggregationSet().minValue("id"));
        /*
                        .maxEntry("price").minEntry("price")
                        .sum("price").average("price")
                        .minValue("price").maxValue("price"));
         */

        logger.info("aggregationResult[0]: " + aggregationResult.get(0));
        return (Integer) aggregationResult.get(0);

    }

    public static void main(String[] args) throws Exception {
        AggregatorExample example = new AggregatorExample();
        example.initialize();

        example.writeObjects(10);

        example.maxIntProperty();

        example.maxLongProperty();

        example.minValue();
    }

}
