package com.gigaspaces.demo.client;

import com.gigaspaces.demo.common.jdbc.Customer;
import com.gigaspaces.demo.common.jdbc.Department;
import com.gigaspaces.demo.common.jdbc.Product;
import com.gigaspaces.demo.common.jdbc.Purchase;
import com.j_spaces.core.client.SQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.openspaces.extensions.QueryExtension.sum;


public abstract class JdbcV3Client extends Client {

    private static final Logger logger = LoggerFactory.getLogger(JdbcV3Client.class);

    protected final String EXPLAIN_PLAN_PREFIX = "EXPLAIN ANALYZE FOR "; // Can be replaced by EXPLAIN PLAN FOR

    protected String lookupHost;
    protected Connection connection;

    public JdbcV3Client() {
        super();
    }

    public void setLookupHost(String lookupHost) {
        this.lookupHost = lookupHost;
    }

    public void setConnection(Connection connection) { this.connection = connection; }


    public void initialize() throws Exception {
        super.initialize();
        this.connection = connect();
    }

    public Connection connect() throws SQLException {
        Properties properties = new Properties();
        // properties.put("readModifiers", ReadModifiers.MEMORY_ONLY_SEARCH);
        return DriverManager.getConnection(String.format("jdbc:gigaspaces:v3://%s:4174/%s", lookupHost, gigaSpace.getSpaceName()), properties);
    }

    public void populate() {
        ArrayList<Department> departments = new ArrayList<>(4);
        departments.add(new Department(1, "toys"));
        departments.add(new Department(2, "food"));
        departments.add(new Department(3, "sport"));
        departments.add(new Department(4, "kitchen-aid"));
        departments.forEach(d -> gigaSpace.write(d));
        ArrayList<Product> products = new ArrayList<>(8);
        products.add(new Product(1, "Train", 11.1, 1));
        products.add(new Product(2, "Doll", 17.8, 1));
        products.add(new Product(3, "Chocolate", 10.0, 2));
        products.add(new Product(4, "Candy", 5.0, 2));
        products.add(new Product(5, "Basketball", 150.0, 3));
        products.add(new Product(6, "Football", 180.0, 3));
        products.add(new Product(7, "Coffee-machine", 999.0, 4));
        products.add(new Product(8, "Ice-maker", 555.0, 4));
        products.forEach(p -> gigaSpace.write(p));
        ArrayList<Customer> customers = new ArrayList<>(4);
        customers.add(new Customer("Cohen", "Avi", Date.valueOf("1980-02-01"), 1));
        customers.add(new Customer("Cohen", "Sara", Date.valueOf("1984-03-05"), 2));
        customers.add(new Customer("Levi", "Sara", Date.valueOf("1989-11-11"), 3));
        customers.add(new Customer("Levi", "Moshe", Date.valueOf("2000-02-03"), 4));
        customers.forEach(c -> gigaSpace.write(c));
        ArrayList<Purchase> purchese = new ArrayList<>(8);
        purchese.add(new Purchase(1, 1, 1));
        purchese.add(new Purchase(2, 2, 5));
        purchese.add(new Purchase(3, 3, 5));
        purchese.add(new Purchase(4, 4, 4));
        purchese.add(new Purchase(5, 2, 4));
        purchese.add(new Purchase(6, 2, 1));
        purchese.add(new Purchase(7, 3, 3));
        purchese.add(new Purchase(8, 1, 2));
        purchese.forEach(p -> gigaSpace.write(p));
    }

    /* begin showMetaData and helper methods */
    private void readTable(String table) throws SQLException {
        String query = "SELECT * FROM \"" + table + "\"";

        try (Statement statement = connection.createStatement();) {
            dumpResult(statement.executeQuery(query));
        } catch (Throwable t) {
            logger.info("Fail to run query:" + t);
            t.printStackTrace();
        }
    }

    private List<String> dumpResult(ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        StringBuilder sb = new StringBuilder();
        for (int k = 1; k <= columnsNumber; k++) {
            if (k > 1) sb.append(",  ");
            sb.append(rsmd.getColumnName(k));
        }

        List<String> rows = new ArrayList<>();
        while (resultSet.next()) {
            sb.append("\n");
            StringBuilder row = new StringBuilder();
            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1) row.append(",  ");
                String columnValue = resultSet.getString(i);
                row.append(columnValue);
            }
            sb.append(row);
            rows.add(row.toString());
        }
        logger.info(sb.toString());
        return rows;
    }


    public void showMetaData() throws Exception {
        Connection connection = connect();
        logger.info("================ information_schema_catalog_name==========");
        readTable("information_schema_catalog_name");
        logger.info("==================== tables ===============================");
        readTable("tables");
        logger.info("==================== columns ===============================");
        readTable("columns");
    }
    /* end showMetaData */

    /* database metadata examples */
    public void deleteTableRows(String tableName) throws SQLException {
        String customerDeleteSql = "DELETE FROM \"" + tableName + "\" ";
        PreparedStatement ps = connection.prepareStatement(customerDeleteSql);
        ps.execute();
    }

    public boolean dropTable(String tableName) throws SQLException {
        String dropSql = "DROP TABLE \"" + tableName + "\"";
        PreparedStatement ps = connection.prepareStatement(dropSql);
        return ps.execute();
    }

    public void createAndInsertExample(String tableName) {
        String createTableSql = "CREATE TABLE " + tableName + " (ID int NOT NULL, LastName VARCHAR(255) NOT NULL, FirstName VARCHAR(255), Age int, PRIMARY KEY (ID))";
        String insertSql = "INSERT INTO " + tableName + " (ID, LastName, FirstName, Age)\n" +
                "VALUES (1, 'Choen', 'Avi', 30)\n";
        try ( PreparedStatement ps = connection.prepareStatement(createTableSql); ) {
            ps.execute();
        } catch (SQLException e) {
            logger.info("Fail to create table in createAndInsertExample:" + e);
            ;
        }
        try ( PreparedStatement ps = connection.prepareStatement(insertSql); ) {
            ps.execute();
            logger.info("New table " + tableName + " was created, and one record inserted");
        }catch (SQLException e) {
            logger.info("Fail to insert values in createAndInsertExample:" + e);
        }
    }

    /*
       returns table name
     */
    public String createTableFromSelect(int customerID) {
        //Create a filtered table holding Purchases of given customer
        String newTableName = "Purchases_" + customerID;
        String query = "CREATE TABLE " + newTableName + " AS SELECT id, customerId, productId, amount FROM \"com.gigaspaces.demo.common.jdbc.Purchase\" WHERE customerId=? ";
        try ( PreparedStatement preparedStatement = connection.prepareStatement(query); ) {
            preparedStatement.setInt(1, customerID);
            preparedStatement.execute();
            logger.info(newTableName + " created with Purchases of Customer: " + customerID);
            return newTableName;

        } catch (SQLException e) {
            logger.info("Fail to run createTableFromSelect:" + e);
            ;
        }
        return null;
    }
    /* end database metadata examples */


    protected List<String> read(String query, Object[] parameters) throws Exception {
        String explain = EXPLAIN_PLAN_PREFIX + " " + query;
        logger.info("Query : " + query);
        try ( PreparedStatement preparedStatement = connection.prepareStatement(explain); ) {
            for (int k = 0; k < parameters.length; k++)
                preparedStatement.setObject(k + 1, parameters[k]);

            dumpResult(preparedStatement.executeQuery());
        } catch (Throwable t) {
            logger.info("Fail to run explain plan:" + t);
            t.printStackTrace();
        }
        logger.info("Results:");
        try ( PreparedStatement preparedStatement = connection.prepareStatement(query); ) {
            for (int k = 0; k < parameters.length; k++)
                preparedStatement.setObject(k + 1, parameters[k]);

            return dumpResult(preparedStatement.executeQuery());
        } catch (Throwable t) {
            logger.info("Fail to run query:" + t);
            t.printStackTrace();
            throw(t);
        }
    }


    protected List<String> read(String query) throws Exception {
        String explain = EXPLAIN_PLAN_PREFIX + " " + query;

        logger.info("Query : " + query);
        try ( Statement statement = connection.createStatement(); ) {
            dumpResult(statement.executeQuery(explain));
        } catch (Throwable e) {
            logger.info("Fail to run explain plan:" + e);
            e.printStackTrace();
        }
        logger.info("Results:");
        try ( Statement statement = connection.createStatement(); ) {
            return dumpResult(statement.executeQuery(query));
        } catch (Throwable t) {
            logger.info("Fail to run query:" + t);
            t.printStackTrace();
            throw(t);
        }
    }

    /*
       Example of getting a SUM of a column using GigaSpaces API
     */
    public void sumQueryExtensionExample() {
        SQLQuery<Product> sqlQuery = new SQLQuery<>(Product.class, "rownum < 3");
        Double sum = sum(gigaSpace, sqlQuery, "price");
        Object[] results = gigaSpace.readMultiple(sqlQuery);
        if (results != null)
            logger.info("got results: " + results.length + " sum: " + sum);
    }

    /*
       Run populate to create test data, this is just an example for insert
    */
    public void insertDataExample() throws SQLException {
        String customerInsertSql = "INSERT INTO \"com.gigaspaces.demo.common.jdbc.Customer\" (id, firstName, lastName, birthday) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(customerInsertSql);
        ps.setInt(1, 101);
        ps.setString(2, "George");
        ps.setString(3, "Washington");
        ps.setDate(4, java.sql.Date.valueOf("2000-02-22"));
        ps.execute();
    }
}
