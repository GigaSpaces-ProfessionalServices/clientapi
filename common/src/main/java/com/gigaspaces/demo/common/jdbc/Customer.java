package com.gigaspaces.demo.common.jdbc;

import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceIndex;
import com.gigaspaces.annotation.pojo.SpaceRouting;
import com.gigaspaces.metadata.index.SpaceIndexType;

import java.sql.Date;

public class Customer {
    String lastName;
    String firstName;
    Date birthday;
    Integer id;

    public Customer(String lastName, String firstName, Date birthday, Integer id) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthday = birthday;
        this.id = id;
    }

    public Customer() {
    }

    @SpaceId
    @SpaceRouting
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    @SpaceIndex(type = SpaceIndexType.ORDERED)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }




    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
}
