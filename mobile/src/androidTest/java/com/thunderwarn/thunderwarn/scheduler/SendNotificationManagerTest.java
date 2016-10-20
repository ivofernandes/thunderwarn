package com.thunderwarn.thunderwarn.scheduler;

import android.content.Context;
import android.test.InstrumentationTestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ivofernandes on 27/12/15.
 */
public class SendNotificationManagerTest extends InstrumentationTestCase {

    private String jsonString = "";

    private SendNotificationManager sendNotificationManager =
            com.thunderwarn.thunderwarn.scheduler.SendNotificationManager.getInstance();

    @Test
    public void testSendNotification() throws JSONException {

        Context context = this.getInstrumentation().getTargetContext().getApplicationContext();

        JSONObject json = new JSONObject(readFileFromAssets("abc.txt", context));

        sendNotificationManager.fireNotications(json);

    }


    public static String readFileFromAssets(String fileName, Context c) {
        try {
            InputStream is = c.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String text = new String(buffer);

            return text;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}
