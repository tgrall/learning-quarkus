package org.windr.demo;

import java.text.MessageFormat;

public class Main {

    public static void main(String[] args) {
        String json = MessageFormat.format("'{'\"name\":\"{0}\",\"age\":{1}'}'", "Thomas", 50);
        System.out.println(json);
    }
}
