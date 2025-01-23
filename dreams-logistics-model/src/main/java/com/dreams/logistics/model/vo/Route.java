package com.dreams.logistics.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Route implements Serializable {
    private String path;
    private String name;
    private String icon;
    private String component;
    private String layout;
    private Boolean hideInMenu;
    private List<Route> routes;

}