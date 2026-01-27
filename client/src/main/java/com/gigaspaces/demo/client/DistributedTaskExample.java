package com.gigaspaces.demo.client;


import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.async.AsyncResult;
import com.gigaspaces.demo.common.MyDistributedTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.concurrent.ExecutionException;

// client that runs a distributed task
public class DistributedTaskExample extends Client {

    private static final Logger logger = LoggerFactory.getLogger(DistributedTaskExample.class);

    public DistributedTaskExample() {
        super();
    }

    public long runDistributedTask() {

        logger.info("Begin distributed task");
        try {
            AsyncFuture<Long> future = gigaSpace.execute(new MyDistributedTask());
            future.setListener(new AsyncFutureListener<Long>() {
                @Override
                public void onResult(AsyncResult<Long> asyncResult) {
                    if (asyncResult.getException() != null) {
                        try {
                            throw asyncResult.getException();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    logger.info("Completed.");
                }
            });


            long result = future.get(); // result will be the number of primary spaces

            logger.info("Result is: " + result);
            return result;

        } catch (ExecutionException ee) {
            ee.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 0l;
    }

    public static void main(String[] args) throws Exception {
        DistributedTaskExample example = new DistributedTaskExample();
        example.initialize();
        example.runDistributedTask();
    }

}
