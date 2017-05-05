package com.everis.payroll.jsoup;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.text.MessageFormat;

/**
 * Created by mvasquec on 29-03-2017.
 */
public class PayrollScrapingProperties {
    private static PayrollScrapingProperties instance = new PayrollScrapingProperties();

    public static PayrollScrapingProperties getInstance() {
        return instance;
    }

    private Configuration config;
    private final static String PAYROLL_CONFIG_PROPERTIES = "payroll_config.properties";

    public final static String BASE_PATH_URL = "base_url";
    public final static String CONTEXT_PATH = "context_path";
    public final static String LOGIN_PATH = "login_path";
    public final static String PDF_PATH = "pdf_path";
    public final static String PROTOCOL = "protocol";
    public final static String PROXY_ENABLED = "proxy_enabled";
    public final static String HTTP_PROXY ="http_proxy";
    public final static String HTTP_PROXY_PORT = "http_proxy_port";
    public final static String USER_AGENT = "user_agent";
    public final static String RESUMES_PATH = "resumes_path";
    public final static String EVENT_TARGET_SELECTOR = "event_target_selector";
    public final static String EVENT_ARGUMENT_SELECTOR = "event_argument_selector";
    public final static String EVENT_VALIDATION_SELECTOR = "event_validation_selector";
    public final static String VIEWSTATE_SELECTOR = "viewstate_selector";
    public final static String SUBMIT_SELECTOR = "submit_selector";
    public final static String EVENT_TARGET_FORM_NAME = "event_target_form_name";
    public final static String EVENT_ARGUMENT_FORM_NAME = "event_argument_form_name";
    public final static String EVENT_VALIDATION_FORM_NAME = "event_validation_form_name";
    public final static String VIEWSTATE_FORM_NAME = "viewstate_form_name";
    public final static String USER_FORM_NAME = "user_form_name";
    public final static String PASSWORD_FORM_NAME = "password_form_name";
    public final static String SUBMIT_FORM_NAME = "submit_form_name";
    public final static String RESUMES_PAGE_REMOVE_LEFT = "resumes_page_remove_left";
    public final static String RESUMES_PAGE_REMOVE_RIGHT = "resumes_page_remove_right";
    public static final String HTTP_PROXY_HOST_KEY = "http_proxy_host_key";
    public static final String HTTP_PROXY_PORT_KEY = "http_proxy_port_key";

    private PayrollScrapingProperties() {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(params.properties()
                                .setFileName(PAYROLL_CONFIG_PROPERTIES));
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
