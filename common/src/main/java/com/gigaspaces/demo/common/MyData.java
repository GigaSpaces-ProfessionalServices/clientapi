package com.gigaspaces.demo.common;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceProperty;

import java.util.Objects;

@SpaceClass
public class MyData {
    private Integer id;
    private long value = -1L;
    private Boolean processed;

    public MyData() {
    }

    @SpaceId(autoGenerate = false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @SpaceProperty(nullValue = "-1")
    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyData myData = (MyData) o;
        return value == myData.value && Objects.equals(id, myData.id) && Objects.equals(processed, myData.processed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, processed);
    }

    @Override
    public String toString() {
        return "MyData{" +
                "id=" + id +
                ", value=" + value +
                ", processed=" + processed +
                '}';
    }
}


