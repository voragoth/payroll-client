package com.everis.payroll.imp;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.everis.payroll.jsoup.HTMLConstants;
import com.everis.payroll.jsoup.PayrollCookiesProperties;
import com.everis.payroll.jsoup.PayrollScrapingProperties;
import com.everis.payroll.jsoup.domain.WageResume;

/**
 * Created by Manuel on 31/3/2017.
 */
public class PayrollClientJsoupHelper {

    private static final Integer MIN_RESUMES_PAGES = 1;

    private PayrollScrapingProperties scrapingProperties;
    private PayrollCookiesProperties cookiesProperties;
    private String userAgent;
    private String basePahtWithContext;
    private String loginPath;
    private String resumesPath;
    private Boolean proxyEnabled;

    public PayrollClientJsoupHelper() {
        scrapingProperties = PayrollScrapingProperties.getInstance();
        cookiesProperties = PayrollCookiesProperties.getInstance();
        basePahtWithContext = scrapingProperties.getValue(PayrollScrapingProperties.PROTOCOL)+HTMLConstants.SYMBOL_PROTOCOL;
        basePahtWithContext += scrapingProperties.getValue(PayrollScrapingProperties.BASE_PATH_URL);
        basePahtWithContext += HTMLConstants.SYMBOL_SLASH + scrapingProperties.getValue(PayrollScrapingProperties.CONTEXT_PATH);
        loginPath = basePahtWithContext+ HTMLConstants.SYMBOL_SLASH + scrapingProperties.getValue(PayrollScrapingProperties.LOGIN_PATH);
        resumesPath = basePahtWithContext+ HTMLConstants.SYMBOL_SLASH +scrapingProperties.getValue(PayrollScrapingProperties.RESUMES_PATH);
        userAgent = scrapingProperties.getValue(PayrollScrapingProperties.USER_AGENT);
        proxyEnabled = Boolean.valueOf(scrapingProperties.getValue(PayrollScrapingProperties.PROXY_ENABLED));
    }

    public String getLoginPath() {
        return loginPath;
    }

    public void checkAndEnableProxy(){
        if (proxyEnabled){
            System.setProperty(scrapingProperties.getValue(PayrollScrapingProperties.HTTP_PROXY_HOST_KEY),
                    scrapingProperties.getValue(PayrollScrapingProperties.HTTP_PROXY));
            System.setProperty(scrapingProperties.getValue(PayrollScrapingProperties.HTTP_PROXY_PORT_KEY),
                    scrapingProperties.getValue(PayrollScrapingProperties.HTTP_PROXY_PORT));
        }
    }

    public Map<String, String> createLoginForm(Connection.Response response, String user, String password){
        Map<String, String> form = new HashMap<>();
        Document document = null;
        try {
            document = response.parse();
            Element html = document.body();
            fillGenericData(html, form);
            String submitName = scrapingProperties.getValue(PayrollScrapingProperties.SUBMIT_FORM_NAME);
            String submitSelector = scrapingProperties.getValue(PayrollScrapingProperties.SUBMIT_SELECTOR);
            String submitValue = html.select(submitSelector).attr(HTMLConstants.ATTR_VALUE);
            String userName = scrapingProperties.getValue(PayrollScrapingProperties.USER_FORM_NAME);
            String passwordName = scrapingProperties.getValue(PayrollScrapingProperties.PASSWORD_FORM_NAME);
            form.put(userName, user);
            form.put(passwordName, password);
            form.put(submitName, submitValue);
        } catch (IOException e) {
            //TODO: logs
        }
        return form;
    }

    private void fillGenericData(Element html, Map<String, String> form){
        String eventTargetName = scrapingProperties.getValue(PayrollScrapingProperties.EVENT_TARGET_FORM_NAME);
        String eventTargetSelector = scrapingProperties.getValue(PayrollScrapingProperties.EVENT_TARGET_SELECTOR);
        String eventTargetValue = html.select(eventTargetSelector).attr(HTMLConstants.ATTR_VALUE);

        String eventArgumentName = scrapingProperties.getValue(PayrollScrapingProperties.EVENT_ARGUMENT_FORM_NAME);
        String eventArgumentSelector = scrapingProperties.getValue(PayrollScrapingProperties.EVENT_ARGUMENT_SELECTOR);
        String eventArgumentValue = html.select(eventArgumentSelector).attr(HTMLConstants.ATTR_VALUE);

        String viewStateName = scrapingProperties.getValue(PayrollScrapingProperties.VIEWSTATE_FORM_NAME);
        String viewStateSelector = scrapingProperties.getValue(PayrollScrapingProperties.VIEWSTATE_SELECTOR);
        String viewStateValue = html.select(viewStateSelector).attr(HTMLConstants.ATTR_VALUE);


        String eventValidationName = scrapingProperties.getValue(PayrollScrapingProperties.EVENT_VALIDATION_FORM_NAME);
        String eventValidationSelector = scrapingProperties.getValue(PayrollScrapingProperties.EVENT_VALIDATION_SELECTOR);
        String eventValidationValue = html.select(eventValidationSelector).attr(HTMLConstants.ATTR_VALUE);

        form.put(eventTargetName, eventTargetValue);
        form.put(eventArgumentName, eventArgumentValue);
        form.put(viewStateName, viewStateValue);
        form.put(eventValidationName, eventValidationValue);
    }

    public Connection.Response loadResponsefromGET(String url, Boolean followRedirects, boolean ignoreContentType, Map<String, String> cookies){
        return loadResponse(url, followRedirects, ignoreContentType, cookies, null, Connection.Method.GET);
    }

    public Connection.Response loadResponsefromPOST(String url, Boolean followRedirects, boolean ignoreContentType, Map<String, String> cookies, Map<String, String> data){
        return loadResponse(url, followRedirects, ignoreContentType, cookies, data, Connection.Method.POST);
    }

    private Connection.Response loadResponse(String url, Boolean followRedirects,  boolean ignoreContentType, Map<String, String> cookies,  Map<String, String> data, Connection.Method method){
        Connection connectionResponse = Jsoup.connect(url).followRedirects(followRedirects).userAgent(this.userAgent).method(method);
        Connection.Response response = null;
        if (data != null && !data.isEmpty()) {
            connectionResponse.data(data);
        }
        if (cookies != null && !cookies.isEmpty()) {
            connectionResponse.cookies(cookies);
        }
        try{
            response = connectionResponse.execute();
        } catch(IOException ioe) {
            //TODO: logs
        }
        return response;
    }

    public String createAutoPay(String autopay, String user) {
        String search = cookiesProperties.getValue(PayrollCookiesProperties.AUTOPAY_USER, StringUtils.EMPTY);
        String replacement = cookiesProperties.getValue(PayrollCookiesProperties.AUTOPAY_USER, user);
        autopay = autopay.replace(search, replacement);
        search = cookiesProperties.getValue(PayrollCookiesProperties.AUTOPAY_DATA_GAME);
        replacement = cookiesProperties.getValue(PayrollCookiesProperties.AUTOPAY_DATA_GAME_EVERIS);
        autopay = autopay.replace(search, replacement);
        search = cookiesProperties.getValue(PayrollCookiesProperties.AUTOPAY_USER_SELECTED, StringUtils.EMPTY);
        replacement = cookiesProperties.getValue(PayrollCookiesProperties.AUTOPAY_USER_SELECTED, user);
        autopay = autopay.replace(search, replacement);
        return autopay;
    }

    public Map<String,String> createBasicCookies(Map<String, String> cookies, String user) {
        String autopayKey = cookiesProperties.getValue(PayrollCookiesProperties.AUTOPAY_COOKIE);
        String autopayValue = cookies.get(autopayKey);
        autopayValue = createAutoPay(autopayValue, user);
        cookies.put(autopayKey, autopayValue);
        return cookies;
    }

    public boolean validateMainMenu(Connection.Response mainMenuResponse) {
        return true;
    }

    public void updateCookies(Map<String, String> targetCookies, Map<String, String> newCookies) {
        for (Map.Entry<String, String> cookie : newCookies.entrySet()) {
            if (targetCookies.containsKey(cookie.getKey())){
                continue;
            } else {
                targetCookies.put(cookie.getKey(), cookie.getValue());
            }
        }
    }

    public String getResumesPath() {
        return resumesPath;
    }

    public List<WageResume> parseResumesFromDoc(Connection.Response resumesResponse) {
        List<WageResume> resumes = new ArrayList<>();
        try {
            Document html = resumesResponse.parse();
            Element body = html.body();
            Elements table = body.select(HTMLConstants.TAG_TABLE);
            Elements rows = table.select(HTMLConstants.TAG_TR);
            for (int i = 1; i < (rows.size() - 1); i++) {
                WageResume resume = parseResumeFromDataCell(rows.get(i));
                resumes.add(resume);
            }
        } catch (IOException e) {
            //TODO: LOGS
        }
        return resumes;
    }

    private WageResume parseResumeFromDataCell(Element element) {
        WageResume resume = new WageResume();
        Elements cells = element.select(HTMLConstants.TAG_TD);
        String[] dataCells = new String[cells.size()];
        for (int i = 0; i < cells.size(); i++) {
            dataCells[i] = cleanDataCells(cells.get(i).text()) ;
        }
        try {
            NumberFormat format = NumberFormat.getInstance(new Locale("es", "ES"));
            resume.setDate(dataCells[0]);
            resume.setPeriodicity(dataCells[1]);
            if (dataCells[2].isEmpty()) {
                resume.setGrossTaxable(new Long(0));
            } else {
                resume.setGrossTaxable(Long.valueOf(dataCells[2]));
            }
            if (dataCells[3].isEmpty()) {
                resume.setGrossPayExempt(new Long(0));
            } else {
                resume.setGrossPayExempt(Long.valueOf(dataCells[3]));
            }
            if (dataCells[4].isEmpty()) {
                resume.setLegalDiscounts(new Long(0));
            } else {
                resume.setLegalDiscounts(Long.valueOf(dataCells[4]));
            }
            if (dataCells[5].isEmpty()) {
                resume.setOtherDiscounts(new Long(0));
            } else {
                resume.setOtherDiscounts(Long.valueOf(dataCells[5]));
            }
            if (dataCells[6].isEmpty()) {
                resume.setNetSalary(new Long(0));
            } else {
                resume.setNetSalary(Long.valueOf(dataCells[6]));
            }
            if (dataCells[7].isEmpty()) {
                resume.setWorkedDays(new Double(0));
            } else {
                Number number = format.parse(dataCells[7]);
                resume.setWorkedDays(new Double(number.doubleValue()));
            }
            if (dataCells[8].isEmpty()) {
                resume.setLicencedDays(new Double(0));
            } else {
                Number number = format.parse(dataCells[8]);
                resume.setLicencedDays(new Double(number.doubleValue()));
            }
            resume.setPdfUrl(createPdfUrl(resume.getDate()));
        } catch (ParseException | NumberFormatException ex){
            System.out.println("se cae");
            //TODO: logs
        }
        return resume;
    }

    private String createPdfUrl(String date) {
        String url = scrapingProperties.getValue(PayrollScrapingProperties.PDF_PATH, date);
        url = basePahtWithContext + HTMLConstants.SYMBOL_SLASH + url;
        return url;
    }

    private String cleanDataCells(String cell){
        String cleanCell = StringEscapeUtils.escapeHtml4(cell);
        cleanCell = cleanCell.replace(HTMLConstants.SCAPED_CHAR_BLANK_SPACE, StringUtils.EMPTY);
        cleanCell = cleanCell.replace(HTMLConstants.SYMBOL_DOT, StringUtils.EMPTY);
        return cleanCell;
    }

    public Integer getTotalPages(Connection.Response resumesResponse) {
        Integer total = MIN_RESUMES_PAGES;
        try {
            Document html = resumesResponse.parse();
            Element body = html.body();
            Element table = body.select(HTMLConstants.TAG_TABLE).first();
            Elements rows = table.select(HTMLConstants.TAG_TR);
            Elements cells = rows.last().select(HTMLConstants.TAG_TD);
            String strPages = cells.text();
            strPages = StringEscapeUtils.escapeHtml4(strPages);
            strPages = strPages.replace(HTMLConstants.SCAPED_CHAR_BLANK_SPACE, HTMLConstants.SYMBOL_WHITE_SPACE);
            String[] pages = strPages.split(HTMLConstants.SYMBOL_WHITE_SPACE);
            total = pages.length;
        }catch (IOException e){
            //TODO: logs
        }
        return total;
    }

    public Map<String, String> getDataForPageResumes(Connection.Response resumes,  int page) {
        Map<String, String> form = new HashMap<>();
        try {
            String strPage = String.valueOf(page);
            Document html = resumes.parse();
            Element body = html.body();
            fillGenericData(body, form);
            String eventTargetName = scrapingProperties.getValue(PayrollScrapingProperties.EVENT_TARGET_FORM_NAME);
            String eventTargetValue = getEventTargetFromBody(body, strPage);
            form.put(eventTargetName, eventTargetValue);

        } catch (IOException e) {
            //TODO: logs
        }
        return form;
    }

    private String getEventTargetFromHREF(String href) {
        String leftReplace = scrapingProperties.getValue(PayrollScrapingProperties.RESUMES_PAGE_REMOVE_LEFT);
        String rightReplace = scrapingProperties.getValue(PayrollScrapingProperties.RESUMES_PAGE_REMOVE_RIGHT);
        String eventTarget = href.replace(leftReplace, StringUtils.EMPTY);
        eventTarget = eventTarget.replace(rightReplace, StringUtils.EMPTY);
        return eventTarget;
    }

    private String getEventTargetFromBody(Element body, String page){
        String eventTarget = null;
        Element table = body.select(HTMLConstants.TAG_TABLE).first();
        Elements rows = table.select(HTMLConstants.TAG_TR);
        Elements cells = rows.last().select(HTMLConstants.TAG_TD);
        Elements hiperLinks = cells.select(HTMLConstants.TAG_A);
        for (int i = 0; i < hiperLinks.size(); i++){
            String hiperLInkText = hiperLinks.get(i).text();
            if (page.equals(hiperLInkText)){
                eventTarget = getEventTargetFromHREF(hiperLinks.get(i).attr(HTMLConstants.ATTR_HREF));
                break;
            }
        }
        return eventTarget;
    }

    public String saveWagePDF(WageResume resume, String destination, Map<String, String> cookies) {
        String destinationFile = null;
        try {
            byte[] pdfBytes = getPDFWage(resume, cookies);
            String fileName = resume.getDate() + HTMLConstants.SYMBOL_DOT + HTMLConstants.EXTENSION_PDF;
            destinationFile = destination + HTMLConstants.SYMBOL_SLASH + fileName;
            FileUtils.writeByteArrayToFile(new File(destinationFile), pdfBytes);
        } catch (IOException e) {
            destinationFile = null;
        }
        return destinationFile;
    }
    
    public byte[] getPDFWage(WageResume resume, Map<String, String> cookies){
        byte[] pdfBytes = null;
        String pdfUrl = resume.getPdfUrl();
        if (pdfUrl == null || pdfUrl.isEmpty()){
            pdfUrl = this.createPdfUrl(resume.getDate());
        }
        try {
            pdfBytes = Jsoup.connect(pdfUrl).userAgent(this.userAgent).cookies(cookies).ignoreContentType(true).execute().bodyAsBytes();
        } catch (IOException e) {
            //TODO: add logs
        }
        return pdfBytes;
    }
}
