package com.thunderwarn.thunderwarn.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Created by ivofernandes on 24/09/15.
 */
public class FileOperations {

    private static FileOperations instance = new FileOperations();

    public static FileOperations getInstance(){
        return instance;
    }

    public String loadJSONFromAsset(InputStream stream) throws IOException {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            stream.close();
        }

        String jsonString = writer.toString();
        return jsonString;
    }
}
