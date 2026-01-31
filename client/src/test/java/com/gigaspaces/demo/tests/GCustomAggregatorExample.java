package com.gigaspaces.demo.tests;

import com.gigaspaces.demo.client.CustomAggregatorExample;
import com.gigaspaces.demo.common.aggregator.Courses;
import com.gigaspaces.demo.common.aggregator.Department;
import com.gigaspaces.demo.common.aggregator.Student;
import com.gigaspaces.demo.common.aggregator.StudentCourses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openspaces.core.GigaSpace;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration test for CustomAggregatorExample.
 * Tests writing aggregator data and running custom IN aggregator queries.
 */
@ExtendWith(RemoteProxyExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GCustomAggregatorExample {

    private GigaSpace gigaSpace;
    private CustomAggregatorExample customAggregatorExample;

    @BeforeAll
    void beforeAll() {
        customAggregatorExample = new CustomAggregatorExample();
        customAggregatorExample.setGigaSpace(gigaSpace);
    }

    @Test
    @Order(1)
    void writeData() {
        customAggregatorExample.writeData();

        int departmentCount = gigaSpace.count(new Department());
        assertEquals(4, departmentCount, "Should have written 4 departments");

        int courseCount = gigaSpace.count(new Courses());
        assertEquals(CustomAggregatorExample.N_COURSES, courseCount,
                "Should have written " + CustomAggregatorExample.N_COURSES + " courses");

        int studentCount = gigaSpace.count(new Student());
        assertEquals(CustomAggregatorExample.NUM_STUDENTS, studentCount,
                "Should have written " + CustomAggregatorExample.NUM_STUDENTS + " students");

        int studentCoursesCount = gigaSpace.count(new StudentCourses());
        assertEquals(CustomAggregatorExample.NUM_STUDENTS * CustomAggregatorExample.N_COURSE_PER_STUDENT,
                studentCoursesCount,
                "Should have written " + (CustomAggregatorExample.NUM_STUDENTS * CustomAggregatorExample.N_COURSE_PER_STUDENT) + " student-course associations");
    }

    @Test
    @Order(2)
    void runAggregator() throws Exception {
        customAggregatorExample.runAggregator();
    }

    @Test
    @Order(3)
    void runWithoutAggregator() throws Exception {
        customAggregatorExample.runWithoutAggregator();
    }
}
