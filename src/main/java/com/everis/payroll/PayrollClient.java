package com.everis.payroll;

import com.everis.payroll.jsoup.domain.WageResume;

import java.util.List;

public interface PayrollClient {

    boolean doLogin(String user, String password);

    List<WageResume> getWageResumesByPage(int page);

    Integer getWageResumesTotalPages();

    List<WageResume> getAllWageResumes();
    
    WageResume findWageResumeByDate(String dateId);

    String saveWagePDF(WageResume resume, String destination);
    
    byte[] getWagePDF(WageResume resume);


}
