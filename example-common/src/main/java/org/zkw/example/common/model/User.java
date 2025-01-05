package org.zkw.example.common.model;

import java.io.Serializable;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-05
 */
public class User implements Serializable {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

