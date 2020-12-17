package com.noahhusby.lib.data.test;

import com.google.gson.Gson;
import com.noahhusby.lib.data.sql.Credentials;
import com.noahhusby.lib.data.sql.ISQLDatabase;
import com.noahhusby.lib.data.sql.MySQL;
import com.noahhusby.lib.data.storage.StorageHashMap;
import com.noahhusby.lib.data.storage.StorageList;
import com.noahhusby.lib.data.storage.compare.ValueComparator;
import com.noahhusby.lib.data.storage.handlers.LocalStorageHandler;
import com.noahhusby.lib.data.storage.handlers.SQLStorageHandler;

import javax.swing.*;
import java.io.File;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        new Main().onEnable();
    }

    private StorageList<SomeStorableObject> list = new StorageList<>(SomeStorableObject.class);
    //private StorageHashMap<String, SomeStorableObject> list = new StorageHashMap<>(SomeStorableObject.class);

    public void onEnable() {


        //Local File
        //LocalStorageHandler localStorageHandler = new LocalStorageHandler(new File("test1.json"));
        //localStorageHandler.setPriority(-1);
        //list.registerHandler(localStorageHandler);

        // SQL Database
        ISQLDatabase database = new MySQL(new Credentials("127.0.0.1", 3306, "root", "", "sledgehammer"));
        list.registerHandler(new SQLStorageHandler(database, "somestorableobject", "whatsShakenBacon,magic,work", "TEXT(255),INT,JSON"));

        list.setComparator(new ValueComparator("whatsShakenBacon"));
        showMenu();
    }

    public void showMenu() {
        String action = JOptionPane.showInputDialog("Enter 'load' to load, 'save' to save, 'add' to create new object," +
                "'clear' to clear the list.");

        switch (action) {
            case "load":
                list.load();
                break;
            case "save":
                list.save();
                break;
            case "add":
                String whatsShaking = JOptionPane.showInputDialog("What's Shaking?");
                int magic = Integer.parseInt(JOptionPane.showInputDialog("Enter an int"));
                list.add(new SomeStorableObject(whatsShaking, magic));
                //list.add(new SomeStorableObject(whatsShaking, magic));
                break;
            case "clear":
                list.clear();
                break;
            case "list":
                for(SomeStorableObject s : list)
                    System.out.println(new Gson().toJson(s));
                break;
        }

        showMenu();
    }
}
