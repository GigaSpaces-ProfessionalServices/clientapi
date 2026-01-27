package com.gigaspaces.demo.common;

import com.gigaspaces.async.AsyncResult;
import org.openspaces.core.executor.DistributedTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class MyDistributedTask implements DistributedTask<Integer, Long> {

    private static final Logger logger = LoggerFactory.getLogger(MyDistributedTask.class);


    public MyDistributedTask() { }

    public Integer execute() throws Exception {
        return 1;
    }


    public Long reduce(List<AsyncResult<Integer>> results) throws Exception {
        long sum = 0;
        for (AsyncResult<Integer> result : results) {
            if (result.getException() != null) {
                throw result.getException();
            }
            sum += result.getResult();
        }
        return sum;
    }
}