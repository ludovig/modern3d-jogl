package com.mundoludo.modern3d.framework.component;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author GBarbieri
 */
public class Pair {

    private String name = "";
    private List<Integer> attributes = new ArrayList<>();

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public List<Integer> attributes() {
        return attributes;
    }

    public void attributes(ArrayList<Integer> attributes) {
        this.attributes = attributes;
    }

}
