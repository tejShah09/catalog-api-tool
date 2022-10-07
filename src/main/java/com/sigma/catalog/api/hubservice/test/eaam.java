package com.sigma.catalog.api.hubservice.test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class eaam { 

    public static void main(String[] args) throws IOException {

        // String command = "powershell.exe your command";
        // Getting the version

        
        try {
            Class jobclass = Class.forName("ecapi.readbundlesheet_0_1.ReadBundleSheet");
            jobclass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}