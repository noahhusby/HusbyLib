package com.noahhusby.lib.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;

/**
 * @author Noah Husby
 */
@UtilityClass
public class HusbyUtil {
    public final Gson GSON;

    static {
        GSON = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }
}
