package com.gigaspaces.demo.client;


import com.gigaspaces.client.ChangeResult;
import com.gigaspaces.client.ChangeSet;
import com.gigaspaces.demo.common.Data;
import com.j_spaces.core.client.SQLQuery;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeExample extends Client {

    private static final Logger logger = LoggerFactory.getLogger(ChangeExample.class);

    public ChangeExample() {
        super();
    }


    public int doChange() {
        SQLQuery<Data> query = new SQLQuery<Data>(Data.class, "");

        ChangeResult<Data> changeResult = gigaSpace.change(query, new ChangeSet().set("message", "I am changed"));
        logger.info("changeResult.getNumberOfChangedEntries(): " + changeResult.getNumberOfChangedEntries());
        return changeResult.getNumberOfChangedEntries();
    }

    public static void main(String[] args) throws Exception{
        ChangeExample example = new ChangeExample();
        example.initialize();;
        example.writeObjects(10);

        example.doChange();

    }


}
