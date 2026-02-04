package com.gigaspaces.demo.tests;

import com.gigaspaces.demo.DockerTestEnv;
import com.gigaspaces.demo.client.JdbcV3Example;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openspaces.core.GigaSpace;

import java.sql.Connection;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(RemoteProxyExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FJdbcV3Example {

    private GigaSpace gigaSpace;
    private JdbcV3Example jdbcV3Example;

    @BeforeAll
    void beforeAll() throws Exception {
        jdbcV3Example = new JdbcV3Example();
        jdbcV3Example.setGigaSpace(gigaSpace);
        jdbcV3Example.setLookupHost(DockerTestEnv.getInstance().getLookupHost());
        Connection connection = jdbcV3Example.connect();
        jdbcV3Example.setConnection(connection);
        jdbcV3Example.populate();
    }

    @Test
    @Order(1)
    void showMetaData() {
        assertDoesNotThrow(() -> jdbcV3Example.showMetaData());
    }
    
    @Test
    @Order(2)
    void insertDataExample() {
        assertDoesNotThrow(() -> jdbcV3Example.insertDataExample());
    }

    @Test
    @Order(3)
    void orderCustomerByFirstName() throws Exception {
        List<String> result = jdbcV3Example.orderCustomerByFirstName();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).contains("Sara"));
        assertTrue(result.get(0).contains("Cohen"));
    }

    @Test
    @Order(4)
    void productUnitSoldAndTotalPaid() throws Exception {
        List<String> result = jdbcV3Example.productUnitSoldAndTotalPaid();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(5)
    void unitSoldPerProduct() throws Exception {
        List<String> result = jdbcV3Example.unitSoldPerProduct();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(6)
    void productsBoughtPerCustomer() throws Exception {
        List<String> result = jdbcV3Example.productsBoughtPerCustomer();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(7)
    void mostSoldProduct() throws Exception {
        List<String> result = jdbcV3Example.mostSoldProduct();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).contains("Doll"));
    }

    @Test
    @Order(8)
    void productsByDepartmentAbovePrice() throws Exception {
        List<String> result = jdbcV3Example.productsByDepartmentAbovePrice();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(9)
    void productsSoldAboveThreshold() throws Exception {
        List<String> result = jdbcV3Example.productsSoldAboveThreshold();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(10)
    void departmentsWithProductsAbovePrice() throws Exception {
        List<String> result = jdbcV3Example.departmentsWithProductsAbovePrice();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(11)
    void departmentsWithPremiumProducts() throws Exception {
        List<String> result = jdbcV3Example.departmentsWithPremiumProducts();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(12)
    void allProducts() throws Exception {
        List<String> result = jdbcV3Example.allProducts();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(14)
    void maxPricePerDepartment() throws Exception {
        List<String> result = jdbcV3Example.maxPricePerDepartment();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).contains("kitchen-aid"));
    }

    @Test
    @Order(15)
    void createAndInsertExample() {
        assertDoesNotThrow(() -> jdbcV3Example.createAndInsertExample("table3"));
    }

    @Test
    @Order(16)
    void createTableFromSelect() {
        assertDoesNotThrow(() -> {
            String newTableName = jdbcV3Example.createTableFromSelect(4);
            if (newTableName != null) {
                jdbcV3Example.dropTable(newTableName);
            }
        });
    }

    @Test
    @Order(17)
    void deleteTableRows() {
        assertDoesNotThrow(() -> jdbcV3Example.deleteTableRows("com.gigaspaces.demo.common.jdbc.Customer"));
    }
}
