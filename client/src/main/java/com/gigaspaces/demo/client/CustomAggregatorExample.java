package com.gigaspaces.demo.client;


import com.gigaspaces.demo.common.aggregator.Courses;
import com.gigaspaces.demo.common.aggregator.Department;
import com.gigaspaces.demo.common.aggregator.Student;
import com.gigaspaces.demo.common.aggregator.StudentCourses;
import com.gigaspaces.query.aggregators.AggregationResult;
import com.gigaspaces.query.aggregators.AggregationSet;
import com.j_spaces.core.client.SQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;


public class CustomAggregatorExample extends Client {

    private static final Logger logger = LoggerFactory.getLogger(CustomAggregatorExample.class);

    public static final int NUM_STUDENTS = 2;
    public static final int N_COURSES = 50000;
    public static final int N_COURSE_PER_STUDENT = 1000;
    public static final int BATCH_SIZE = 5000;

    public static void main(String[] args) throws Exception {

        CustomAggregatorExample example = new CustomAggregatorExample();
        example.initialize();
        example.writeData();
        example.runAggregator();
        example.runWithoutAggregator();
    }


    public void writeData(){
        ArrayList<Department> departments = new ArrayList<>(4);
        departments.add(new Department(1, "law"));
        departments.add(new Department(2, "accounting"));
        departments.add(new Department(3, "science"));
        departments.add(new Department(4, "art"));
        departments.forEach(d->gigaSpace.write(d));
        ArrayList<Courses> courses = new ArrayList<>(BATCH_SIZE);
        for (int k = 1; k <= N_COURSES; k++) {
            courses.add(new Courses(k,"Course_"+ k,k+1500.0,1 + k % 4));
            if (k % BATCH_SIZE == 0) {
                gigaSpace.writeMultiple(courses.toArray());
                courses = new ArrayList<>(BATCH_SIZE);
            }
        }
        if (courses.size() > 0) gigaSpace.writeMultiple(courses.toArray());

        ArrayList<Student> students = new ArrayList<>(NUM_STUDENTS);
        for (int k = 1; k <= NUM_STUDENTS; k++)
            students.add(new  Student("Cohen_"+k,"Avi_"+k, Date.valueOf("1980-02-01"),k));
        students.forEach(c-> gigaSpace.write(c));
        ArrayList<StudentCourses> studentCourses = new ArrayList<>(N_COURSE_PER_STUDENT);
        for (int k = 1; k<= NUM_STUDENTS; k++){
            for (int c = 1; c<= N_COURSE_PER_STUDENT; c++){
                studentCourses.add(new StudentCourses(k,c));
            }
            gigaSpace.writeMultiple(studentCourses.toArray());
            studentCourses = new ArrayList<>(N_COURSE_PER_STUDENT);
        }
    }

    public void runWithoutAggregator() throws Exception {

        SQLQuery<Courses> studentCourses = new SQLQuery<Courses>(Courses.class, "id in (?)");
        studentCourses.setParameter(1, getStudentCoursesIds(1));
        long start = System.currentTimeMillis();
        Courses[] results = gigaSpace.readMultiple(studentCourses);
        long end = System.currentTimeMillis();
        logger.info("Run using query condition (without aggregator) got: {} courses, took {} ms", results.length, (end-start));

    }

    public void runAggregator() {
        SQLQuery<Courses> studentCoursesQuery = new SQLQuery<Courses>(Courses.class, "");
        CustomInAggregator customInAggregator  = new CustomInAggregator("id", getStudentCoursesIds(1));
        AggregationSet aggregationSet = new AggregationSet();
        aggregationSet.add(customInAggregator);
        long start = System.currentTimeMillis();
        AggregationResult result = gigaSpace.aggregate(studentCoursesQuery, aggregationSet);
        long end = System.currentTimeMillis();

        ArrayList<Object> results = (ArrayList<Object>) result.get(0);

        logger.info("Run using custom IN Aggregator got: {} courses, took {} ms", results.size(), (end-start));

    }

    public Collection<Object> getStudentCoursesIds(int studentId){
        HashSet<Object> ids = new HashSet<>(CustomAggregatorExample.N_COURSE_PER_STUDENT);
        SQLQuery<StudentCourses> sqlQuery = new SQLQuery<>(StudentCourses.class, "studentId=?");
        sqlQuery.setParameter(1, studentId);
        sqlQuery.setProjections("courseId");

        StudentCourses[] results = gigaSpace.readMultiple(sqlQuery);
        Arrays.stream(results).forEach(sc-> ids.add(sc.getCourseId()));
        logger.info("getStudentCoursesIds: "+ ids.size());
        return ids;
    }

}
