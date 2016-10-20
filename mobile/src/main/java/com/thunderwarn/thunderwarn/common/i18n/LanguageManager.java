package com.thunderwarn.thunderwarn.common.i18n;

import com.thunderwarn.thunderwarn.common.Log;
import com.thunderwarn.thunderwarn.common.configuration.ConfigurationManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by ivofernandes on 28/10/15.
 */
public class LanguageManager {

    //
    private static final String TAG = "LanguageManager";

    // Singleton
    private static LanguageManager instance = new LanguageManager();

    private LanguageManager(){}

    public static LanguageManager getInstance(){
        return instance;
    }

    // Fields
    private ConfigurationManager configurationManager = ConfigurationManager.getInstance();

    // public methods

    /**
     *
     * @return
     * TODO i18n - process this list of langauges into languages.json and test
     * http://bugs.openweathermap.org/projects/api/boards/3
     *
    Russian - ru, Italian - it, Spanish - sp, Ukrainian - ua, German - de,
    Romanian - ro, Polish - pl, Finnish - fi, Dutch - nl, French - fr, Bulgarian - bg,
    Swedish - se, Chinese Traditional - zh_tw, Chinese Simplified - zh_cn, Turkish - tr ,
    Czech - cz, Galician - gl, Vietnamese - vi, Arabic - ar, Macedonian - mk, Slovak - sk
     */
    public String getLanguage(){
        // Default language is english
        String language = "en";

        String localLanguage = Locale.getDefault().getLanguage();

        try {
            JSONObject languages = configurationManager.getJsonLanguages();

            if(languages.has(localLanguage)) {
                String languageToReturn =
                        languages.getString(localLanguage);

                if (languageToReturn != null) {
                    return languageToReturn;
                }
            }
        } catch (JSONException e) {
            Log.e(TAG,"Error reading the");
        }
        return language;
    }

}
