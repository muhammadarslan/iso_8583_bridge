package com.avcora.iso8583.bridge.common;

/**
 * @author: daniel
 */
public class Field {

    private Integer id;
    private String value;

    public Field(Integer id, String value) {
        this.id = id;
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Field))
            return false;
        Field that = (Field) o;
        return this.id != null && this.id.equals(that.id);
    }

    public int hashCode() {
        int hash = 29;
        hash = 17 * hash + (id == null ? 0 : id.hashCode());
        return hash;
    }
}

