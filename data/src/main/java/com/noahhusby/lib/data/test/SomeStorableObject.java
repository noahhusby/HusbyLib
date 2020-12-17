package com.noahhusby.lib.data.test;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SomeStorableObject {

    @Expose
    @SerializedName("whatsShakenBacon")
    public String bob;
    @Expose
    public int magic;

    @Expose
    private List<String> work = new ArrayList<>();

    public String nonStor = "";

    public SomeStorableObject() {}

    public SomeStorableObject(String bob, int magic) {
        this.bob = bob;
        this.magic = magic;
        this.nonStor = nonStor;
    }
}
