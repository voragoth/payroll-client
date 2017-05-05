package com.everis.payroll.jsoup;

import java.text.MessageFormat;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * Created by mvasquec on 29-03-2017.
 */
public class PayrollCookiesProperties {

    private static PayrollCookiesProperties instance = new PayrollCookiesProperties();
    public final static String AUTOPAY_USER = "autopay_user";
    public final static String AUTOPAY_DATA_GAME = "autopay_data_game";
    public final static String AUTOPAY_DATA_GAME_EVERIS = "autopay_data_game_everis";
    public final static String AUTOPAY_USER_SELECTED = "autopay_user_selected";
    public final static String AUTOPAY_COOKIE = "autopay_cookie";
    private Configuration config;

    public static PayrollCookiesProperties getInstance() {
        return instance;
    }

    private final static String PAYROLL_COOKIES_PROPERTIES = "payroll_cookies.properties";

    private PayrollCookiesProperties() {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(params.properties()
                                .setFileName(PAYROLL_COOKIES_PROPERTIES));
        try {
            config = builder.getConfiguration();
        } catch(ConfigurationException cex){
            // loading of the configuration file failed
        }
    }


    public String getValue(String key, Object ... values){
        String value = config.getString(key);
        if (values != null && values.length > 0) {
            value = MessageFormat.format(value, values);
        }
        return value;
    }
}
