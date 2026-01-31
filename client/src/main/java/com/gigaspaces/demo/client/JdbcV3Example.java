package com.gigaspaces.demo.client;

import com.gigaspaces.demo.common.jdbc.Customer;
import com.gigaspaces.demo.common.jdbc.Department;
import com.gigaspaces.demo.common.jdbc.Product;
import com.gigaspaces.demo.common.jdbc.Purchase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class JdbcV3Example extends JdbcV3Client {

    private static final Logger logger = LoggerFactory.getLogger(JdbcV3Example.class);

    public JdbcV3Example() {
        super();
    }


    /*
       Simple read with ordering
     */
    public List<String> orderCustomerByFirstName() throws Exception {
        logger.info("======================= orderCustomerByFirstName ===========================");

        String queryPrefix = "SELECT * FROM  \"" + Customer.class.getName() + "\"" + " WHERE ";
        String query = queryPrefix + "lastName = ? ORDER BY firstName DESC NULLS LAST";
        Object[] parameters = new Object[1];
        parameters[0] = "Cohen";
        return read(query, parameters);
    }

    /*
       Find per each product how many were sold and total paid
     */
    public List<String> productUnitSoldAndTotalPaid() throws Exception {
        //" join from Purchase & Product by PU.productId" U.productId, P.name, P.price, PU.amount
        // PU.productId, P.id
        logger.info("======================= productUnitSoldAndTotalPaid ===========================");
        String queryPrefix = "SELECT P.id AS product_id, P.name, P.price, SUM(PU.amount) AS purchase_amount, SUM(PU.amount)*P.price AS total FROM \"" + Purchase.class.getName() + "\" AS PU ";
        String condition = " LEFT JOIN \"" + Product.class.getName() + "\" AS P ON PU.productId=P.id GROUP BY P.id,P.name,P.price";
        String query = queryPrefix + condition;
        return read(query);
    }

    /*
      Find per each product how many were sold
     */
    public List<String> unitSoldPerProduct() throws Exception {
        //" join from Purchase & Product by PU.productId" U.productId, P.name, P.price, PU.amount
        logger.info("======================= unitSoldPerProduct ===========================");
        String queryPrefix = "SELECT PU.customerId AS customer_id, P.name AS product_name, PU.amount FROM \"" + Purchase.class.getName() + "\" AS PU ";
        String condition = " LEFT JOIN \"" + Product.class.getName() + "\" AS P ON P.id=PU.productId ORDER BY P.name";

        String query = queryPrefix + condition;
        return read(query);
    }

    /*
      Find per each customer how many products he bought
    */
    public List<String> productsBoughtPerCustomer() throws Exception {
        //" join from Purchase & Product by PU.productId" U.productId, P.name, P.price, PU.amount
        logger.info("======================= productsBoughtPerCustomer ===========================");
        String queryPrefix = "SELECT PU.customerId AS customer_id, C.firstName, C.lastName, SUM(PU.amount) AS amount FROM \"" + Purchase.class.getName() + "\" AS PU ";
        String condition = " LEFT JOIN \"" + Customer.class.getName() + "\" AS C ON C.id=PU.customerId GROUP BY PU.customerId, C.firstName, C.lastName";
        String query = queryPrefix + condition;
        return read(query);
    }


    /*
      Find the product that was sold the most (order by max_amount not supported yet)
    */
    public List<String> mostSoldProduct() throws Exception {
        //" join from Purchase & Product by PU.productId" U.productId, P.name, P.price, PU.amount
        logger.info("======================= mostSoldProduct ===========================");

        String queryPrefix = "SELECT PU.productId, P.name,  MAX(PU.amount) AS max_amount FROM \"" + Purchase.class.getName() + "\" AS PU ";
        String condition = " INNER JOIN \"" + Product.class.getName() + "\" AS P ON P.id=PU.productId GROUP BY PU.productId,P.name ORDER BY max_amount DESC";

        String query = queryPrefix + condition;
        return read(query);
    }

    public List<String> productsByDepartmentAbovePrice() throws Exception {
        logger.info("======================= productsByDepartmentAbovePrice ===========================");

        String queryPrefix = "SELECT  D.name AS Department, P.name,  P.price ";
        String from1 = "FROM (SELECT name,price,depId  FROM \"" + Product.class.getName() + "\"  WHERE price > ? ) AS P ";
        String join = " JOIN \"" + Department.class.getName() + "\" D ";
        String joinCondition = " ON P.depId=D.id";

        String query = queryPrefix + from1 + join + joinCondition;
        Object[] params = new Object[1];
        params[0] = 100;
        return read(query, params);
    }


    /*
      Find all products that amount of sold is more than X
    */
    public List<String> productsSoldAboveThreshold() throws Exception {
        //" join from Purchase & Product by PU.productId" U.productId, P.name, P.price, PU.amount
        logger.info("======================= productsSoldAboveThreshold ===========================");

        String queryPrefix = "SELECT  P.name, SUM(PU.amount) AS total FROM \"" + Purchase.class.getName() + "\" AS PU ";
        String condition = " LEFT JOIN \"" + Product.class.getName() + "\" AS P ON P.id=PU.productId GROUP BY P.name HAVING total > ? ";

        String query = queryPrefix + condition;
        Object[] params = new Object[1];
        params[0] = 5;
        return read(query, params);
    }

    public List<String> departmentsWithProductsAbovePrice() throws Exception {
        logger.info("======================= departmentsWithProductsAbovePrice ===========================");
        String queryPrefix = "SELECT D.id, D.name  FROM \"" + Department.class.getName() + "\" AS D ";
        String condition1 = " WHERE EXISTS (SELECT P.name  FROM \"" + Product.class.getName() + "\" AS P WHERE  P.depId=D.id AND P.price > ?)";

        String query = queryPrefix + condition1;
        Object[] params = new Object[1];
        params[0] = 100;
        return read(query, params);
    }

    public List<String> departmentsWithPremiumProducts() throws Exception {
        logger.info("======================= departmentsWithPremiumProducts ===========================");
        String queryPrefix = "SELECT D.id,D.name  FROM \"" + Department.class.getName() + "\" AS D ";
        String condition1 = " WHERE EXISTS (SELECT P.name FROM \"" + Product.class.getName() + "\" AS P WHERE P.depId=D.id AND P.price>?)";

        String query = queryPrefix + condition1;
        Object[] params = new Object[1];
        params[0] = 500;
        return read(query, params);
    }

    public List<String> allProducts() throws Exception {
        logger.info("======================= allProducts ===========================");
        String query = "SELECT P.Id, P.name, P.price  FROM \"" + Product.class.getName() + "\" AS P ";
        return read(query);
    }

    public List<String> maxPricePerDepartment() throws Exception {
        logger.info("======================= maxPricePerDepartment ===========================");
        String query = "SELECT D.name, P.depId, MAX(P.price) AS max_price  FROM \"" + Product.class.getName() + "\" AS P JOIN  \"" + Department.class.getName() + "\" D ON P.depId=D.id GROUP BY D.name, P.depId ORDER BY max_price DESC";
        return read(query);
    }

    public static void main(String[] args) throws Exception {
        JdbcV3Example example = new JdbcV3Example();
        example.setLookupHost("localhost");
        example.initialize();
        example.populate();

        example.showMetaData();
        example.insertDataExample();
        example.orderCustomerByFirstName();
        example.productUnitSoldAndTotalPaid();
        example.unitSoldPerProduct();
        example.productsBoughtPerCustomer();
        example.mostSoldProduct();
        example.productsByDepartmentAbovePrice();
        example.productsSoldAboveThreshold();
        example.departmentsWithProductsAbovePrice();
        example.departmentsWithPremiumProducts();
        example.allProducts();
        example.maxPricePerDepartment();
        example.createAndInsertExample("table3");
        String newTableName = example.createTableFromSelect(4);
        example.deleteTableRows("com.gigaspaces.demo.common.jdbc.Customer");
        if (newTableName != null)
            example.dropTable(newTableName);

    }

}
