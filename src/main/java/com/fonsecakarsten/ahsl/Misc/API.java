package com.fonsecakarsten.ahsl.Misc;

import com.fonsecakarsten.ahsl.Assignments.AssignmentDetail;
import com.fonsecakarsten.ahsl.Assignments.CurrentAssignment;
import com.fonsecakarsten.ahsl.Calendar.calendar;
import com.fonsecakarsten.ahsl.Constants;
import com.fonsecakarsten.ahsl.Grades.Course;
import com.fonsecakarsten.ahsl.Grades.GradeDetail;
import com.fonsecakarsten.ahsl.LoopMail.MailDetail;
import com.fonsecakarsten.ahsl.LoopMail.MailEntry;
import com.fonsecakarsten.ahsl.News.NewsArticle;
import com.fonsecakarsten.ahsl.News.NewsDetail;
import com.fonsecakarsten.ahsl.RemoteDebug;
import com.fonsecakarsten.ahsl.ReportCard.ReportCourse;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.BasicHttpContext;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class API {
    private static API instance;

    private boolean loginStatus;

    private CookieStore authCookies;

    private Document portal;
    private Document coursePortal;
    private Document loopMail;
    private Document loopMailOutbox;
    private Document Notices;

    private String username;
    private String password;
    private String loginTestUrl;
    private String portalUrl;

    public static synchronized API get() {
        return (instance != null) ? instance : (instance = new API());
    }

    public void setAuthCookies(CookieStore authCookies) {
        this.authCookies = authCookies;
    }

    public CookieStore getAuthCookies() {
        return authCookies;
    }

    public void setLoginTestUrl(String testUrl) {
        this.loginTestUrl = testUrl;
    }

    public void setCredentials(String username, String password, String portalUrl) {
        this.username = username;
        this.password = password;
        this.portalUrl = portalUrl;
    }

    public void logIn() throws IOException {
        // make request to portal URL and store cookies
        CookieStore cookies = Utils.getCookies(portalUrl);
        setAuthCookies(cookies);

        // make request to login page and store form_data_id value
        Document loginForm = Utils.getJsoupDocFromUrl(portalUrl + "/portal/login?d=x", portalUrl, authCookies);
        String formDataId = loginForm.select("input#form_data_id").attr("value");

        BasicHttpContext context = Utils.getCookifiedHttpContext(authCookies);

        HttpPost httpPost = new HttpPost(portalUrl + "/portal/login?etarget=login_form");
        List<NameValuePair> nameValuePairs = new ArrayList<>(4);
        nameValuePairs.add(new BasicNameValuePair("login_name", username));
        nameValuePairs.add(new BasicNameValuePair("password", password));
        nameValuePairs.add(new BasicNameValuePair("event.login.x", "0"));
        nameValuePairs.add(new BasicNameValuePair("event.login.y", "0"));
        //supply stored form_data_id value as necessary parameter
        nameValuePairs.add(new BasicNameValuePair("form_data_id", formDataId));

        HttpClient client = Utils.getNewHttpClient();
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        client.execute(httpPost, context);
    }

    public void logOut() {
        HttpClient client = Utils.getNewHttpClient();
        BasicHttpContext context = Utils.getCookifiedHttpContext(authCookies);
        HttpGet httpGet = new HttpGet(portalUrl + "/portal/logout?d=x");

        try {
            client.execute(httpGet, context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setAuthCookies(null);
    }

    public boolean isLoggedIn(boolean deep) {
        if (!deep) return loginStatus;

        HttpClient client = Utils.getNewHttpClient();
        BasicHttpContext context = Utils.getCookifiedHttpContext(authCookies);
        HttpGet httpGet = new HttpGet(loginTestUrl);
        HttpResponse response;

        client.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);

        try {
            response = client.execute(httpGet, context);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        BasicStatusLine responseStatus = (BasicStatusLine) response.getStatusLine();
        //    System.out.println("CODE: " + responseStatus.getStatusCode() + " " + responseStatus.getReasonPhrase());
        return (loginStatus = (responseStatus.getStatusCode() == HttpStatus.SC_OK));
    }

    public void refreshPortal() throws IOException {
        if (!isLoggedIn(true)) return;

        portal = Utils.getJsoupDocFromUrl(portalUrl, portalUrl, authCookies);
        loopMail = Utils.getJsoupDocFromUrl(portalUrl + "/mail/inbox?d=x", portalUrl, authCookies);
        loopMailOutbox = Utils.getJsoupDocFromUrl(portalUrl + "/mail/outbox?d=x", portalUrl, authCookies);
        //  Locker = Utils.getJsoupDocFromUrl(portalUrl + "/locker2/view?d=x", portalUrl, authCookies);
    }

    public void refreshMainPortal() throws IOException {
        if (!isLoggedIn(true)) return;

        portal = Utils.getJsoupDocFromUrl(portalUrl, portalUrl, authCookies);
    }


    public void refreshLoopMail() throws IOException {
        if (!isLoggedIn(true)) return;

        loopMail = Utils.getJsoupDocFromUrl(portalUrl + "/mail/inbox?d=x", portalUrl, authCookies);
        loopMailOutbox = Utils.getJsoupDocFromUrl(portalUrl + "/mail/outbox?d=x", portalUrl, authCookies);
    }

    public void refreshLocker() throws IOException {
        if (!isLoggedIn(true)) return;

        Document locker = Utils.getJsoupDocFromUrl(portalUrl + "/locker2/view?d=x", portalUrl, authCookies);
    }

    public void preventCookieExpire() throws IOException {
        if (!isLoggedIn(false)) return;

        HttpClient client = Utils.getNewHttpClient();
        BasicHttpContext context = Utils.getCookifiedHttpContext(authCookies);
        HttpGet httpGet = new HttpGet(loginTestUrl);

        try {
            client.execute(httpGet, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPortalTitle() {
        return portal.title();
    }

    public String getReportCardDate() {
        return portal.select("div.custom_data").select("div.list_title").text().trim();
    }

    // Get data from table on website
    public List<Course> getCourses() {
        List<Course> courses = new ArrayList<>();

        if (portal == null) return courses;

        // select everything in the table holding the grades
        Element courseBlock = portal.select("div.content").first();
        // select each class row
        Elements gradeRows = courseBlock.select("div.ajax_accordion");

        for (int i = 0; i < gradeRows.size(); i++) {
            Element classReport = gradeRows.get(i);
            boolean isNotPublished = false;

            // create new empty course to store course information
            Course newCourse = new Course();

            // only single element present for subject, select the name
            Elements subject = classReport.select("td.course");
            newCourse.setName(subject.text());

            String period = classReport.select("td.period").text().trim();
            String letterGrade = classReport.select("div.float_l.grade").text().trim();
            String pctGrade = classReport.select("div.float_l.percent").text().trim();
            String numZeros = classReport.select("div.float_l.zeros").text().replace("Zeros:", "").trim();
            Elements progLinks = classReport.select("td.pr_link");

	    	/*// if no grades listed, grade must be listed as 'None Published', so select that
            if(grades.size() == 0) {
	    		grades = courseRow.select("td.list_text_light");
	    		isNotPublished = true;
	    	}*/

            // if grades is still empty, row is invalid, so skip
            //if(courseRow..size() == 0) continue;

            // if grades aren't published, add to the list and skip to the next row
            /*if(isNotPublished) {
                courses.add(newCourse);
	    		continue;
	    	}*/

            newCourse.setLetterGrade(letterGrade);
            newCourse.setPercentGrade(pctGrade);
            try {
                newCourse.setNumZeros(Integer.parseInt(numZeros));
            } catch (NumberFormatException e) {
                newCourse.setNumZeros(0);
            }
            // select the links for the progress report
            Elements link = progLinks.select("a");

            if (!(link.size() == 0) && link.size() == 1) {
                String detailsUrl = portalUrl + link.attr("href");

                newCourse.setDetailsUrl(Utils.getPrintViewifiedUrl(detailsUrl));

            }

            if (newCourse.getDetailsUrl() == null)
                RemoteDebug.debug("course details is null", link.outerHtml());

            newCourse.setPeriod(period);

            courses.add(newCourse);
        }

        return courses;
    }

//    public Map<String,List<GradeDetail>> getGradeDetails(Course course) throws Exception {
//        List<GradeDetail> detailsList = new ArrayList<>();
//
//        // construct and send a GET request to the URL where the Course grade details are stored
//        Document detailsPage = Utils.getJsoupDocFromUrl(course.getDetailsUrl(), portalUrl, authCookies);
//
//        if (detailsPage == null) {
//            RemoteDebug.debug("detailsPage is null", "Points data for grade details is weird");
//            //return ;
//        }
//
//        GradeDetail lastDetail = new GradeDetail();
//        String publishDate = detailsPage.body().select("span.off").first().text();
//        lastDetail.setLastPublishDate(publishDate);
//        detailsList.add(lastDetail);
//
//        // Karsten 2-22-15 Stuff for % in each Category
//        Element Score_category = detailsPage.body().select("td.padding_r20").select("table").select("tbody").first();
//        Elements row = Score_category.select("tr");
//        List<GradeDetail> breakList = new ArrayList<>();
//
//        for (int i = 1; i < row.size(); i++) {
//            Element classReport = row.get(i);
//            GradeDetail newDetail2 = new GradeDetail();
//
//            String category = classReport.select("td.list_label_grey").text().trim();
//            System.out.println(category);
//            newDetail2.setScoreCategory(category);
//            String percent = classReport.select("td.list_text").text().trim();
//            System.out.println(percent);
//            newDetail2.setScorePercent(percent);
//
//            breakList.add(newDetail2);
//        }
//
//        // select all rows of the table containing grade details
//        Elements details = detailsPage.body().select("tbody.general_body tr");
//
//        if (details == null)
//            RemoteDebug.debug("details is null in getGradeDetails", detailsPage.html());
//
//        //for (Element detail : details) {
//        for (Element detail : details != null ? details : null) {
//            GradeDetail newDetail = new GradeDetail();
//
//            // select all individual elements in each row
//            Elements data = detail.select("td");
//
//            for (int i = 0; i < data.size(); i++) {
//                if (i == 0) newDetail.setCategory(data.get(0).text().trim());
//                if (i == 1) newDetail.setDueDate(data.get(1).text().trim());
//                if (i == 2) newDetail.setDetailName(data.get(2).text().trim());
//                if (i == 5) newDetail.setComment(data.get(5).text().trim());
//                if (i == 6) newDetail.setSubmissions(data.get(6).text().trim());
//            }
//
//            if (data.size() >= 5) {
//                // should be an entry of the form 'n / m = x%', where n, m, and x are numbers
//                String scoreData = data.get(4).text().trim();
//
//                // if the score is a numerical and properly formatted entry, split the grade into total
//                // points and earned points
//                if (!(scoreData.length() == 0)
//                        && Character.isDigit(scoreData.charAt(0))
//                        && scoreData.contains(" = ")
//                        && scoreData.contains(" / ")) {
//
//                    // split into 2 halves: one for percent, and other for raw score
//                    String[] scoreComps = scoreData.split(" = ");
//
//                    // store the percent value from the appropriate half
//                    String displayPct = scoreComps[1].trim();
//
//                    newDetail.setDisplayPercent(displayPct);
//
//                    // split the part before the percent and '=' into respective parts and parse
//                    String[] scoreParts = scoreComps[0].split(" / ");
//
//                    try {
//                        newDetail.setPointsEarned(Double.parseDouble(scoreParts[0].trim()));
//                        newDetail.setTotalPoints(Double.parseDouble(scoreParts[1].trim()));
//                    } catch (NumberFormatException e) {
//                        RemoteDebug.debugException("Points data for grade details is weird", e);
//                        // if numbers aren't formatted properly, something is weird, so set to empty/invalid to be safe
//                        newDetail.setPointsEarned(0.0d);
//                        newDetail.setTotalPoints(0.0d);
//
//                        newDetail.setDisplayPercent(Constants.EMPTY_INDIC);
//                        newDetail.setDisplayScore("");
//                    }
//
//                    String displayScore = scoreComps[0].replaceAll(" ", "");
//                    newDetail.setDisplayScore(displayScore);
//                } else {
//                    boolean isExtraCredit = false;
//
//                    try {
//                        newDetail.setTotalPoints(Double.parseDouble(data.get(4).text().trim()));
//                    } catch (NumberFormatException e) {
//                        isExtraCredit = true;
//                    }
//
//                    if (isExtraCredit) {
//                        if (data.get(3).text().trim().length() == 0)
//                            newDetail.setPointsEarned(0.0d);
//
//                        try {
//                            newDetail.setPointsEarned(Double.parseDouble(data.get(3).text().trim()));
//                        } catch (NumberFormatException e) {
//                            e.printStackTrace();
//                            // if numbers aren't formatted properly, something is weird, so set to empty/invalid to be safe
//                            newDetail.setPointsEarned(0.0d);
//                            newDetail.setTotalPoints(0.0d);
//                        }
//                    }
//
//                    newDetail.setDisplayPercent(data.get(4).text().trim());
//                    newDetail.setDisplayScore(data.get(3).text().trim());
//                }
//            } else {
//                newDetail.setDisplayPercent(Constants.EMPTY_INDIC);
//                newDetail.setDisplayScore("");
//            }
//            detailsList.add(newDetail);
//        }
//        Map<String,List<GradeDetail>> map = new HashMap<>();
//        map.put("regular", detailsList);
//        map.put("second", breakList);
//        return map;
//    }


    public List<GradeDetail> getGradeDetails(Course course) throws Exception {
        List<GradeDetail> detailsList = new ArrayList<>();

        // construct and send a GET request to the URL where the Course grade details are stored
        Document detailsPage = Utils.getJsoupDocFromUrl(course.getDetailsUrl(), portalUrl, authCookies);

        if (detailsPage == null) {
            RemoteDebug.debug("detailsPage is null", "Points data for grade details is weird");
            return detailsList;
        }

        GradeDetail lastDetail = new GradeDetail();
        String publishDate = detailsPage.body().select("span.off").first().text();
        lastDetail.setLastPublishDate(publishDate);
        detailsList.add(lastDetail);

        // select all rows of the table containing grade details
        Elements details = detailsPage.body().select("tbody.general_body tr");

        if (details == null)
            RemoteDebug.debug("details is null in getGradeDetails", detailsPage.html());

        //for (Element detail : details) {
        for (Element detail : details != null ? details : null != null ? details != null ? details : null : null) {
            GradeDetail newDetail = new GradeDetail();

            // select all individual elements in each row
            Elements data = detail.select("td");

            for (int i = 0; i < data.size(); i++) {
                if (i == 0) newDetail.setCategory(data.get(0).text().trim());
                if (i == 1) newDetail.setDueDate(data.get(1).text().trim());
                if (i == 2) newDetail.setDetailName(data.get(2).text().trim());
                if (i == 5) newDetail.setComment(data.get(5).text().trim());
                if (i == 6) newDetail.setSubmissions(data.get(6).text().trim());
            }

            if (data.size() >= 5) {
                // should be an entry of the form 'n / m = x%', where n, m, and x are numbers
                String scoreData = data.get(4).text().trim();

                // if the score is a numerical and properly formatted entry, split the grade into total
                // points and earned points
                if (!(scoreData.length() == 0)
                        && Character.isDigit(scoreData.charAt(0))
                        && scoreData.contains(" = ")
                        && scoreData.contains(" / ")) {

                    // split into 2 halves: one for percent, and other for raw score
                    String[] scoreComps = scoreData.split(" = ");

                    // store the percent value from the appropriate half
                    String displayPct = scoreComps[1].trim();

                    newDetail.setDisplayPercent(displayPct);

                    // split the part before the percent and '=' into respective parts and parse
                    String[] scoreParts = scoreComps[0].split(" / ");

                    try {
                        newDetail.setPointsEarned(Double.parseDouble(scoreParts[0].trim()));
                        newDetail.setTotalPoints(Double.parseDouble(scoreParts[1].trim()));
                    } catch (NumberFormatException e) {
                        RemoteDebug.debugException("Points data for grade details is weird", e);
                        // if numbers aren't formatted properly, something is weird, so set to empty/invalid to be safe
                        newDetail.setPointsEarned(0.0d);
                        newDetail.setTotalPoints(0.0d);

                        newDetail.setDisplayPercent(Constants.EMPTY_INDIC);
                        newDetail.setDisplayScore("");
                    }

                    String displayScore = scoreComps[0].replaceAll(" ", "");
                    newDetail.setDisplayScore(displayScore);
                } else {
                    boolean isExtraCredit = false;

                    try {
                        newDetail.setTotalPoints(Double.parseDouble(data.get(4).text().trim()));
                    } catch (NumberFormatException e) {
                        isExtraCredit = true;
                    }

                    if (isExtraCredit) {
                        if (data.get(3).text().trim().length() == 0)
                            newDetail.setPointsEarned(0.0d);

                        try {
                            newDetail.setPointsEarned(Double.parseDouble(data.get(3).text().trim()));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            // if numbers aren't formatted properly, something is weird, so set to empty/invalid to be safe
                            newDetail.setPointsEarned(0.0d);
                            newDetail.setTotalPoints(0.0d);
                        }
                    }

                    newDetail.setDisplayPercent(data.get(4).text().trim());
                    newDetail.setDisplayScore(data.get(3).text().trim());
                }
            } else {
                newDetail.setDisplayPercent(Constants.EMPTY_INDIC);
                newDetail.setDisplayScore("");
            }
            detailsList.add(newDetail);
        }

        return detailsList;
    }

    public List<GradeDetail> getBreakdown(Course course) throws Exception {
        List<GradeDetail> breakList = new ArrayList<>();

        // construct and send a GET request to the URL where the Course grade details are stored
        Document detailsPage = Utils.getJsoupDocFromUrl(course.getDetailsUrl(), portalUrl, authCookies);

        if (detailsPage == null) {
            RemoteDebug.debug("detailsPage is null", "Points data for grade details is weird");
            return breakList;
        }

        Element Score_category = detailsPage.body().select("div").first().select(":eq(5)").select("td.padding_r20").select("table").select("tbody").first();
        Elements row = Score_category.select("tr");

        for (int i = 1; i < row.size(); i++) {
            Element classReport = row.get(i);
            GradeDetail newbreakDown = new GradeDetail();

            if (classReport.children().size() == 2) {
                String Category = classReport.select("td.list_label_grey").text().trim();
                newbreakDown.setScoreCategory(Category);

                String percent = classReport.select("td.list_text").text().trim();
                newbreakDown.setScorePercent(percent);
            }

            if (classReport.children().size() == 3) {
                String Category = classReport.select("td.list_label_grey").text().trim();
                newbreakDown.setScoreCategory(Category);

                Element weight1 = classReport.select("td.list_text").first();
                String weight = classReport.select("td.list_text").first().text().trim();
                newbreakDown.setScoreWeight(weight);

                Element percent1 = weight1.nextElementSibling();
                String percent = percent1.text().trim();
                newbreakDown.setScorePercent(percent);
            }

            breakList.add(newbreakDown);
        }
        return breakList;
    }


    public List<CurrentAssignment> getCurrentAssignments() {
        List<CurrentAssignment> assignments = new ArrayList<>();

        if (portal == null) return assignments;

        // select everything in the table holding the assignments
        Elements assignmentsBlock = portal.body().select("table.table_basic");

        for (Element assignmentRow : assignmentsBlock) {

            // create new empty assignment to store course information
            CurrentAssignment assignment = new CurrentAssignment();

            // select the assignment details that include the title, respective
            // course, and due date
            Elements details = assignmentRow.select("td.column.padding_5");

            // if assignments are empty, they are invalid, so skip
            if (details.size() == 0) continue;

            for (int i = 2; i <= 4; i++) {
                Element currAssignment = details.get(i);
                String currAssignmentTxt = currAssignment.text().trim();

                if (i == 2) {
                    Elements link = currAssignment.select("a");
                    Elements idk = link.select("href");

                    if (!(link.size() == 0) && link.size() == 1) {
                        String detailsUrl = Utils.getPrintViewifiedUrl(portalUrl + link.first().attr("href"));
                        assignment.setDetailsUrl(detailsUrl);
                    }

                    if (assignment.getDetailsUrl() == null)
                        RemoteDebug.debug("mail details is null", link.outerHtml());

                    assignment.setName(currAssignmentTxt);
                } else if (i == 3) assignment.setCourseName(currAssignmentTxt);
                else if (i == 4) assignment.setDueDate(currAssignmentTxt);
            }
            assignments.add(assignment);
        }

        return assignments;
    }

    public AssignmentDetail getAssignmentDetails(CurrentAssignment assignment) throws Exception {
        AssignmentDetail details = new AssignmentDetail();

        // construct and send a GET request to the URL where the assignment details are stored
        Document detailsPage = Utils.getJsoupDocFromUrl(assignment.getDetailsUrl(), portalUrl, authCookies);

        // select the div containing assignment details
        Elements detailBlock = detailsPage.body().select("div.course");

        if (detailBlock == null)
            RemoteDebug.debug("detailBlock is null in getAssignmentDetails", detailsPage.html());

        assert detailBlock != null;
        if (detailBlock.size() == 0) return details;

        String assignTitle = detailBlock.select("div.title_page").text().trim();
        String assignAudience = detailBlock.select("div.highlight_box").text().trim();
        String assignExplanation = detailBlock.select("div.content").html();
        String assignAttach = detailBlock.select("div.container").html();

        details.setName(assignTitle);
        details.setTargetAudience(assignAudience);
        details.setExplanation(assignExplanation);
        details.setAttachments(assignAttach);

        Elements assignDetails = detailBlock.select("td.info");

        for (Element detail : assignDetails) {
            details.addDetail(detail.text());
        }

        return details;
    }


    // Get data from table on website
    public List<MailEntry> getMailInbox() {
        List<MailEntry> mail = new ArrayList<>();

        if (loopMail == null) return mail;

        // retrieve the table listing the emails
        Elements tables = loopMail.select("table.list_padding");

        if (tables.size() == 0) return mail;

        Element mailTable = tables.first();

        // select all mail listing rows after the first one because it is a header row
        Elements mailListing = mailTable.select("tr:gt(0)");

        for (Element currListing : mailListing) {
            MailEntry currEntry = new MailEntry();
            Elements mailInfo = currListing.select("td.list_text");

            for (int i = 0; i < mailInfo.size(); i++) {
                Element currInfo = mailInfo.get(i);
                String currInfoTxt = currInfo.text().trim();

                if (i == 0) currEntry.setTimestamp(currInfoTxt);
                else if (i == 1) currEntry.setInvolvedParties(currInfoTxt);
                else if (i == 2) {
                    Elements link = currInfo.select("a[href]");

                    if (!(link.size() == 0) && link.size() == 1) {
                        String mailContentUrl = Utils.getPrintViewifiedUrl(portalUrl + link.first().attr("href"));
                        currEntry.setContentUrl(mailContentUrl);
                    }

                    if (currEntry.getContentUrl() == null)
                        RemoteDebug.debug("mail details is null", link.outerHtml());

                    currEntry.setSubject(currInfoTxt);
                }
            }

            mail.add(currEntry);
        }

        return mail;
    }

    public List<MailEntry> getMailOutbox() {
        List<MailEntry> mailOutbox = new ArrayList<>();

        if (loopMailOutbox == null) return mailOutbox;

        // retrieve the table listing the emails
        Elements tables1 = loopMailOutbox.select("table.list_padding");

        if (tables1.size() == 0) return mailOutbox;

        Element mailTable = tables1.first();

        // select all mail listing rows after the first one because it is a header row
        Elements mailListing = mailTable.select("tr:gt(1)");

        for (Element currListing : mailListing) {
            MailEntry currEntry = new MailEntry();
            Elements mailInfo = currListing.select("td.list_text");

            for (int i = 0; i < mailInfo.size(); i++) {
                Element currInfo = mailInfo.get(i);
                String currInfoTxt = currInfo.text().trim();

                if (i == 0) currEntry.setTimestamp(currInfoTxt);
                else if (i == 1) currEntry.setInvolvedParties(currInfoTxt);
                else if (i == 2) {
                    Elements link = currInfo.select("a[href]");

                    if (!(link.size() == 0) && link.size() == 1) {
                        String mailContentUrl = Utils.getPrintViewifiedUrl(portalUrl + link.first().attr("href"));
                        currEntry.setContentUrl(mailContentUrl);
                    }

                    if (currEntry.getContentUrl() == null)
                        RemoteDebug.debug("mail details is null", link.outerHtml());

                    currEntry.setSubject(currInfoTxt);
                }
            }

            mailOutbox.add(currEntry);
        }

        return mailOutbox;
    }

    public MailDetail getMailDetails(MailEntry entry) throws Exception {
        MailDetail details = new MailDetail();

        // construct and send a GET request to the URL where the mail content is stored
        Elements mailBlock = Utils.getJsoupDocFromUrl(entry.getContentUrl(), portalUrl, authCookies).select("div:eq(0) table");


        if (mailBlock.size() == 0) return details;

        Element infoBlock = mailBlock.get(1);
        Element contentBlock = mailBlock.get(2);

        String TO_STRING = "<b>To:</b>";
        String FROM_STRING = "<b>Date:</b>";
        String END_DATE_STRING = "<b>Subject:</b>";

        String info = infoBlock.select("td:eq(0)").html();

        String from, to;

        if (info.contains(TO_STRING) && info.contains(FROM_STRING)) {
            from = info
                    .substring(0, info.indexOf(TO_STRING))
                    .replaceAll("<br />", "");


            to = info
                    .substring(info.indexOf(TO_STRING), info.indexOf(FROM_STRING))
                    .replaceAll("viewed", "<b>viewed</b>")
                    .replaceAll("<br />", "");

        } else if (info.contains(FROM_STRING)) {
            from = info
                    .substring(0, info.indexOf(FROM_STRING))
                    .replaceAll("<br />", "");
            to = "";
        } else {
            from = Constants.EMPTY_INDIC;
            to = Constants.EMPTY_INDIC;
        }

        String date = info.substring(info.indexOf(FROM_STRING), info.indexOf(END_DATE_STRING));
        String subject = info.substring(info.indexOf(END_DATE_STRING)).replace("<b>Subject:</b>", "").replace("<br>", "");
        String content = contentBlock.html();

        //System.out.println(date);

        details.setFrom(from);
        details.setTo(to);
        details.setDate(date);
        details.setSubject(subject);
        details.setContent(content);

        return details;
    }

    public MailDetail getMailDetailsOutbox(MailEntry entry) throws Exception {
        MailDetail details = new MailDetail();

        // construct and send a GET request to the URL where the mail content is stored
        Elements mailBlock = Utils.getJsoupDocFromUrl(entry.getContentUrl(), portalUrl, authCookies).select("div:eq(0) table");

        if (mailBlock.size() == 0) return details;
        Element infoBlock = mailBlock.get(1);
        Element contentBlock = mailBlock.get(2);

        String TO_STRING = "<b>To:</b>";
        String FROM_STRING = "<b>Date:</b>";
        String END_DATE_STRING = "<b>Subject:</b>";

        String info = infoBlock.select("td:eq(0)").html();

        String from, to;

        if (info.contains(TO_STRING) && info.contains(FROM_STRING)) {
            from = info
                    .substring(0, info.indexOf(TO_STRING))
                    .replaceAll("<br />", "");


            to = info
                    .substring(info.indexOf(TO_STRING), info.indexOf(FROM_STRING))
                    .replaceAll("viewed", "<b>viewed</b>")
                    .replaceAll("<br />", "");

        } else if (info.contains(FROM_STRING)) {
            from = info
                    .substring(0, info.indexOf(FROM_STRING))
                    .replaceAll("<br />", "");
            to = "";
        } else {
            from = Constants.EMPTY_INDIC;
            to = Constants.EMPTY_INDIC;
        }

        String date = info.substring(info.indexOf(FROM_STRING), info.indexOf(END_DATE_STRING));
        String subject = info.substring(info.indexOf(END_DATE_STRING)).replace("<b>Subject:</b>", "").replace("<br>", "");
        String content = contentBlock.html();

        details.setFrom(from);
        details.setTo(to);
        details.setDate(date);
        details.setSubject(subject);
        details.setContent(content);

        return details;
    }

    public List<ReportCourse> getReportCard() {
        List<ReportCourse> report = new ArrayList<>();

        if (portal == null) return report;

        // get the table for the report grades
        Elements table_holder = portal.select("div.custom_data");

        Element table = table_holder.select("table").first().select("tbody").first();

        Elements table_tr = table.select("tr");

        for (int i = 1; i < table_tr.size(); i++) {
            Element classReportCard = table_tr.get(i);
            ReportCourse newReportCourse = new ReportCourse();

            String period = classReportCard.select(":eq(0)").select("CENTER").select("font").first().text().trim();
            String course = classReportCard.select(":eq(1)").select("font").text().trim();
            String grade = classReportCard.select(":eq(3)").select("CENTER").select("font").text().replace("3", "").trim();

            //System.out.println(course);

            newReportCourse.setPeriod(period);
            newReportCourse.setCourse(course);
            newReportCourse.setLetterGrade(grade);

            report.add(newReportCourse);
        }
        return report;
    }

    public List<calendar> getCalendar() {
        List<calendar> calendarList = new ArrayList<>();

        if (portal == null) return calendarList;

        Elements calHolder = portal.select("div.home_left").select("div.cal_content_holder").select(":gt(1)").select("div.ajax_accordion.due_page");

//        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

//        for (Element element : calHolder) {
//            calendar newCal1 = new calendar();
//            // Get name of day
//            String dayNameString1 = element.select("div.padding_3").select("div.cal_label_day_has.float_l").html();
//            System.out.println(dayNameString1);
//            newCal1.setWeekDay(dayNameString1);

        // FOR EACH DAY (from string array days) get all of the ajax due pages
        // I WANT FOR EACH DAY GET ALL THE AJAX DUE PAGES IN THAT DAY

//            Elements calHolder2 = calHolder.select("div.ajax_accordion.due_page");

        // Get each day, ie Monday, Tuesday etc...
        for (int i = 0; i < calHolder.size(); i++) {
            Element day = calHolder.get(i);
            calendar newCalendar = new calendar();

               /* // Get name of day
                Element dayNameString = day.parent();
                String dayNameString1 = dayNameString.select("div.padding_3").select("div.cal_label_day_has.float_l").text();
                System.out.println(dayNameString1);
                newCalendar.setWeekDay(dayNameString1); */

            // Get assignment name
            String title = day.select("td.column.item_title").select("a").html().trim();
            System.out.println(title);
            newCalendar.setAssignment(title);

            // Get assignment type ie. Quiz, Homework etc..
            String title_type = day.select("td.column.width_r").text().trim();
            System.out.println(title_type);
            newCalendar.setAssignmentType(title_type);

            // Get course for the assignment
            String course = day.select(":eq(1)").select("div.cal_text_course").select("strong").html().trim();
            System.out.println(course);
            newCalendar.setCourseName(course);
//                calendarList.add(newCal1);
            calendarList.add(newCalendar);

            System.out.println("--------");
        }
//            break;
//        }

        System.out.println(calendarList.size());

        return calendarList;
    }


    public List<NewsArticle> getNews() {
        List<NewsArticle> news = new ArrayList<>();

        if (portal == null) return news;

        Elements newsModule = portal.body().select("div.module_content");

        if (newsModule.size() == 0) return news;

        // get 1 'module' tables from the last one.
        Element newsBlock = newsModule.get(newsModule.size() - 2);

        if (newsBlock == null) return news;

        // select everything in the div holding the article names
        // Select each article
        Elements articles = newsModule.select("div.content_item.news_for_delete");

        for (int i = 0; i < articles.size(); i++) {
            // create new empty news article to store the article info
            NewsArticle article = new NewsArticle();
            Element articleDiv = articles.get(i);

            // select info according to how it is ordered/structured in the HTML
            Element title = articleDiv.select("a").first();
            String meta = articleDiv.ownText();
            String articleDateText;

            int idx = 0;
            while (!Character.isDigit(meta.charAt(idx))) idx++;

            articleDateText = meta.substring(idx);

            meta = meta.substring(0, idx);

            // split the author field into the author's name and the author's type
            String authorData[];

            try {
                authorData = meta.trim().split(" - ");
            } catch (NullPointerException e) {
                e.printStackTrace();
                authorData = null;
            }

            if (authorData != null) {
                for (int j = 0; j < authorData.length; j++) {
                    if (j == 0) article.setAuthor(authorData[0].trim());
                    else if (j == 1) article.setAuthorType(authorData[1].trim());
                }
            } else {
                article.setDisplayAuthor(meta.trim());
            }

            article.setArticleName(title.text().trim());
            article.setDatePosted(articleDateText.trim());

            if (title.hasAttr("href")) {
                String detailsUrl = Utils.getPrintViewifiedUrl(portalUrl + title.attr("href"));
                article.setArticleUrl(detailsUrl);
            }

            if (article.getArticleUrl() == null)
                RemoteDebug.debug("mail details is null", title.outerHtml());

            news.add(article);
        }

        return news;
    }

    public NewsDetail getNewsDetails(NewsArticle article) throws Exception {
        NewsDetail details = new NewsDetail();

        // construct and send a GET request to the URL where the news article info is stored
        Document detailsPage = Utils.getJsoupDocFromUrl(article.getArticleUrl(), portalUrl, authCookies);

        // select the block containing news article info
        Elements infoBlock = detailsPage.body().select("div.published");

        if (infoBlock == null) RemoteDebug.debug("v is null in getNewsDetails", detailsPage.html());

        //if (infoBlock.size() == 0) return details;
        if ((infoBlock != null ? infoBlock.size() : 0) == 0) return details;

        // select block containing more specific details
        Elements newsDetails = infoBlock.select("div.highlight_box td");

        String newsTitle = infoBlock.select("div.title_page").text().trim();
        String newsContent = infoBlock.select("div.content").html();

        details.setTitle(newsTitle);
        details.setContent(newsContent);

        for (Element detail : newsDetails) {
            details.addDetail(detail.text());
        }

        return details;
    }


}
