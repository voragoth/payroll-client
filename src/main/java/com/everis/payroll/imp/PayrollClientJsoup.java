package com.everis.payroll.imp;

import com.everis.payroll.PayrollClient;
import com.everis.payroll.jsoup.domain.WageResume;
import org.jsoup.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PayrollClientJsoup implements PayrollClient {

    private PayrollClientJsoupHelper helper;
    private Map<String, String> cookies;
    private Integer totalPages;

    public PayrollClientJsoup() {
        this.helper = new PayrollClientJsoupHelper();
        helper.checkAndEnableProxy();
    }

    @Override
    public boolean doLogin(String user, String password) {
        String loginPath = helper.getLoginPath();
        Connection.Response loginResponse = helper.loadResponsefromGET(loginPath, false, false, null);
        Map<String, String> data = helper.createLoginForm(loginResponse, user, password);
        Map<String, String> cookies = helper.createBasicCookies(loginResponse.cookies(), user);
        Connection.Response logingCompletedResponse = helper.loadResponsefromPOST(loginPath, true, false, cookies, data);
        String mainMenuPath = (logingCompletedResponse.url() == null) ? null : logingCompletedResponse.url().toString();
        Connection.Response mainMenuResponse = helper.loadResponsefromGET(mainMenuPath, false, false, cookies);
        helper.updateCookies(cookies, logingCompletedResponse.cookies());
        this.cookies = cookies;
        return helper.validateMainMenu(mainMenuResponse);
    }

    public List<WageResume> getWageResumesByPage(int page) {
        String resumesPath = helper.getResumesPath();
        Connection.Response resumesResponse = helper.loadResponsefromGET(resumesPath, false, false, this.cookies);
        if (page > 1) {
            Map<String, String> data = helper.getDataForPageResumes(resumesResponse, page);
            resumesResponse = helper.loadResponsefromPOST(resumesPath, false, false, this.cookies, data);
        }
        List<WageResume> resumes = helper.parseResumesFromDoc(resumesResponse);
        return resumes;
    }

    @Override
    public Integer getWageResumesTotalPages() {
        if (totalPages == null){
            String resumesPath = helper.getResumesPath();
            Connection.Response resumesResponse = helper.loadResponsefromGET(resumesPath, false, false, this.cookies);
            this.totalPages = helper.getTotalPages(resumesResponse);
        }

        return this.totalPages;
    }

    @Override
    public List<WageResume> getAllWageResumes() {
        List<WageResume> wageResumes = new ArrayList<>();
        for (int i = 1; i <= this.getWageResumesTotalPages(); i++) {
            wageResumes.addAll(this.getWageResumesByPage(i));
        }
        return wageResumes;
    }

    @Override
    public String saveWagePDF(WageResume resume, String destination) {
        return helper.saveWagePDF(resume, destination, this.cookies);
    }

    @Override
    public WageResume findWageResumeByDate(String dateId) {
        int total = this.getWageResumesTotalPages();
        WageResume result = null;
        for(int i = 1; i < total; i++){
            List<WageResume> resumes = this.getWageResumesByPage(i);
            for (WageResume wageResume : resumes) {
                if (dateId != null && dateId.equals(wageResume.getDate())) {
                    result = wageResume;
                    break;
                }
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }
    
    @Override
    public byte[] getWagePDF(WageResume resume) {
        return helper.getPDFWage(resume, cookies);
    }
}