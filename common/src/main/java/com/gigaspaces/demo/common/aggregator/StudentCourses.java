package com.gigaspaces.demo.common.aggregator;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceIndex;
import com.gigaspaces.metadata.index.SpaceIndexType;


@SpaceClass
public class StudentCourses {
    private String id;
    private Integer studentId;
    private Integer courseId;


    public StudentCourses() {
    }

    public StudentCourses( Integer customerId, Integer productId) {
        this.studentId = customerId;
        this.courseId = productId;
    }



    @SpaceId (autoGenerate = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }




    @SpaceIndex
    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }


    @SpaceIndex
    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }


}
