package com.example.demo.util;



import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;

public class HashUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String hash(Map<String, Object> body) throws Exception {
        return Base64.getEncoder()
                .encodeToString(mapper.writeValueAsBytes(body));
    }
}