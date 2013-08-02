package org.jana.linkedin;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jana.linkedin.SocialDataLI.Url;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ReaderLinkedin extends ActiveBatchBusinessService {

  private static Logger log = Logger.getLogger(ReaderLinkedin.class);
  String temp;
  Pattern jsonCntP = null;
  Matcher jsonCntPMatch = null;
  Matcher rsidMatch = null;
  HttpsURLConnection loginRequest = null;
  HttpURLConnection homeRequest, searchRequest, loginRedirectRequest = null;
  Vector<String> LinkedInCookies = new Vector<String>();

  /*
   * Replace the username and password here
   */
  String userName = "USERNAME";
  String password = "PASSWORD";
  HashMap<Integer, String> industryCodeMap = new HashMap<Integer, String>();

  public static void main(String... args) {
    PropertyConfigurator.configure("log4j/log4j.properties");
    for (SocialDataLI obj : new ReaderLinkedin().runSearch("Bhatia", "People")) {
      log.info("##########################################");
      log.info(obj.toString());
      log.info("##########################################");
    }
  }

  @Override
  public List<? extends SocialDataLI> runSearch(String query, String type) {
    List<SocialDataLI> dataList = new ArrayList();
    log.debug("query: " + query);

    String temp;

    // GET_LOGIN_PARAMETER_FROM_HOME
    HttpURLConnection homeRequest, searchRequest, loginRedirectRequest = null;
    industryCodes();
    try {
      homeRequest = getPageRequest("https://www.linkedin.com/uas/login", "");

      if (homeRequest.getResponseCode() == 200) {

        StringBuffer response = getResponse(homeRequest);
        Pattern sourceAliasInputPattern = Pattern.compile("<input [^>]*sourceAlias[^>]*value=[\"|']([^(\"|')]+)[\"|'][^>]*>");
        Matcher sourceAliasInputMatch = sourceAliasInputPattern.matcher(response);
        sourceAliasInputMatch.find();
        String sourceAliasLoginParam = sourceAliasInputMatch.group(1);

        Pattern csrfTokenInputPattern = Pattern.compile("<input [^>]*csrfToken[^>]*value=[\"|']([^(\"|')]+)[\"|'][^>]*>");
        Matcher csrfTokenInputMatch = csrfTokenInputPattern.matcher(response);
        csrfTokenInputMatch.find();
        String csrfTokenLoginParam = csrfTokenInputMatch.group(1);

        // LOGIN_PART
        String loginParam = "session_key=" + URLEncoder.encode(userName, "UTF-8") + "&session_password="
            + URLEncoder.encode(password, "UTF-8") + "&csrfToken=" + URLEncoder.encode(csrfTokenLoginParam, "UTF-8") + "&sourceAlias="
            + URLEncoder.encode(sourceAliasLoginParam, "UTF-8") + "&signin=" + URLEncoder.encode("Sign In", "UTF-8");
        log.debug("loginParam --->" + loginParam);

        HttpsURLConnection loginRequest = (HttpsURLConnection) new URL("https://www.linkedin.com/uas/login-submit").openConnection();
        loginRequest.setRequestMethod("POST");
        loginRequest.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        loginRequest.setRequestProperty("Content-Length", "" + Integer.toString(loginParam.getBytes().length));
        loginRequest.setRequestProperty("Host", "www.linkedin.com");
        loginRequest.setDoOutput(true);
        loginRequest.setInstanceFollowRedirects(false);

        DataOutputStream wr = new DataOutputStream(loginRequest.getOutputStream());
        wr.writeBytes(loginParam);
        wr.flush();
        wr.close();

        Thread.sleep(3000);
        log.debug("loginRequest status code--->" + loginRequest.getResponseCode());
        if ((loginRequest.getResponseCode() == 302) || (loginRequest.getResponseCode() == 301)) {

          log.debug("New location after login--->" + loginRequest.getHeaderField("Location"));
          // CHECK_LOGIN_AND_PROCESS_COOKIE
          if (loginRequest.getHeaderField("Location") == null) {
            log.info("Unable to login");
            System.exit(0);
          }

          List<String> cookies = loginRequest.getHeaderFields().get("Set-Cookie");
          LinkedInCookies.clear();
          for (String cookie : cookies) {
            LinkedInCookies.add(cookie);
          }
          log.debug(cookies);
          loginRedirectRequest = getPageRequest(loginRequest.getHeaderField("Location"), "");

          Thread.sleep(3000);
          log.debug("New location after redirect--->" + loginRedirectRequest.getResponseCode());
          if (loginRedirectRequest.getResponseCode() == 200) {
            if (type.equalsIgnoreCase("People"))
              dataList = searchPeople(query);
            else if (type.equalsIgnoreCase("Company"))
              dataList = searchCompany(query);
          } else {
            log.debug("New location after redirect is not 200--->" + loginRedirectRequest.getResponseCode());
          }
        }
      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // TODO search implementation
    return dataList;
  }

  private List<SocialDataLI> searchCompany(String query) {
    List<SocialDataLI> dataList = new ArrayList();
    Integer noOfPages = 0;
    int page = 1;
    try {
      String cook = getCookie();
      String cmpUrl = "http://www.linkedin.com/vsearch/c?keywords=" + query + "&trk=tyah";
      do {
        searchRequest = getPageRequest(cmpUrl, cook);

        log.debug("searchRequest status code--->" + searchRequest.getResponseCode());
        if (searchRequest.getResponseCode() == 200) {

          StringBuffer sResponse = getResponse(searchRequest);

          Pattern jsonCntP = Pattern
              .compile("<code id=\"voltron_company_search_json-content\" style=\"display:none;\"><!--(.*?)--></code>");
          Matcher jsonCntPMatch = jsonCntP.matcher(sResponse);

          if (jsonCntPMatch.find()) {
            String jsonCntPParam = jsonCntPMatch.group(1);

            Pattern rsidP = Pattern.compile("rsid=([0-9]{0,})");
            Matcher rsidMatch = rsidP.matcher(jsonCntPParam);

            if (rsidMatch.find()) {

              if (page == 1)
                noOfPages = getNoOfPage(jsonCntPParam.toString());
              ArrayList al = new ArrayList();
              // add elements to al, including duplicates
              HashSet hs = new HashSet();

              Pattern pattern = Pattern.compile("pid=([0-9]{0,})");
              java.util.regex.Matcher m = pattern.matcher(jsonCntPMatch.group(1));
              while (m.find()) {
                al.add(m.group(1));
              }
              hs.addAll(al);
              al.clear();
              al.addAll(hs);
              for (int p = 0; p < al.size(); p++) {

                String linkedinId = al.get(p).toString().trim();
                SocialDataLI sData = new SocialDataLI();
                sData.setLinkedinId(linkedinId);
                sData.setType("Company");
                HttpURLConnection profilePageRequest = getPageRequest("http://www.linkedin.com/company/" + linkedinId, cook);

                log.debug("profilePageRequest status code--->" + profilePageRequest.getResponseCode());
                if (profilePageRequest.getResponseCode() == 200) {
                  log.debug("Company ID----->" + linkedinId);
                  StringBuffer pageResponse = getResponse(profilePageRequest);

                  sData.setName(getPatternValue(pageResponse, "<h1 class=\"name\">(.*?)</h1>"));

                  sData.setDescription(getPatternValue(pageResponse,
                      "<div class=\"container description\">+\\s<div class=\"text-logo\">+\\s<p>(.*?)</p>"));

                  sData.setSpecialties(getPatternValue(pageResponse, "<h3>Specialties</h3>+\\s<p>(.*?)</p>"));

                  String followersCount = getPatternValue(pageResponse, "<p class=\"followers-count\">.*<strong>([0-9,]{0,})</strong>");
                  if (!followersCount.equalsIgnoreCase(""))
                    sData.setNumFollowers(Integer.parseInt(followersCount.replaceAll(",", "")));

                  sData.setIndustries(getPatternValue(pageResponse, "<h[1-4]>Industry</h[1-4]>.*<p>(.*?)</p>"));

                  sData.setWebsiteUrl(getPatternValue(pageResponse,
                      "<h[1-4]>Website</h[1-4]>.*((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])</a>"));

                  dataList.add(sData);
                } else {
                  log.debug("profilePageRequest status code is not 200--->" + profilePageRequest.getResponseCode());
                }
              }

            } else {
              log.debug("rsid's not found");
            }
          } else {
            log.debug("First json Content not found");
          }

        } else {
          log.debug("searchRequest status code is not 200--->" + searchRequest.getResponseCode());
        }

        page++;
        cmpUrl = "http://www.linkedin.com/vsearch/c?keywords=" + query + "&trk=tyah&page_num=" + page;

      } while (page <= noOfPages);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return dataList;
  }

  /**
   * This method used for people search
   * 
   * @param query
   * @return List<SocialDataLI>
   */
  private List<SocialDataLI> searchPeople(String query) {
    List<SocialDataLI> dataList = new ArrayList();
    Integer noOfPages = 0;
    int page = 1;

    try {
      String cook = getCookie();

      searchRequest = getPageRequest("http://www.linkedin.com/vsearch/p?adv=true&trk=advsrch", cook);
      Thread.sleep(3000);
      log.debug("searchRequest status code--->" + searchRequest.getResponseCode());

      if (searchRequest.getResponseCode() == 200) {

        StringBuffer sResponse = getResponse(searchRequest);

        Pattern jsonCntP = Pattern.compile("<code id=\"voltron_people_search_json-content\" style=\"display:none;\"><!--(.*?)--></code>");
        Matcher jsonCntPMatch = jsonCntP.matcher(sResponse);

        if (jsonCntPMatch.find()) {
          String jsonCntPParam = jsonCntPMatch.group(1);
          log.debug("json Content --->" + jsonCntPParam);

          Pattern rsidP = Pattern.compile("rsid=([0-9]{0,})&trk=");
          Matcher rsidMatch = rsidP.matcher(jsonCntPParam);

          if (rsidMatch.find()) {
            String advUrl = "http://www.linkedin.com/vsearch/p?keywords=" + query.replaceAll(" ", "%20")
                + "&openAdvancedForm=true&locationType=Y&rsid=" + rsidMatch.group(1) + "&orig=FCTD";
            log.debug(advUrl);

            do {

              HttpURLConnection advSearchRequest = getPageRequest(advUrl, cook);
              log.debug(advUrl);
              log.debug("advSearchRequest status code--->" + advSearchRequest.getResponseCode());

              if (advSearchRequest.getResponseCode() == 200) {

                StringBuffer aResponse = getResponse(advSearchRequest);

                if (page == 1)
                  noOfPages = getNoOfPage(aResponse.toString());

                jsonCntPMatch = jsonCntP.matcher(aResponse);
                if (jsonCntPMatch.find()) {
                  log.debug("adv search json Content --->" + jsonCntPMatch.group(1));

                  ArrayList al = new ArrayList();
                  // add elements to al, including duplicates
                  HashSet hs = new HashSet();

                  Pattern pattern = Pattern.compile("pid=([0-9]{0,})");
                  java.util.regex.Matcher m = pattern.matcher(jsonCntPMatch.group(1));
                  while (m.find()) {
                    log.debug("Pid's found--->" + m.group(1));
                    al.add(m.group(1));
                  }
                  hs.addAll(al);
                  al.clear();
                  al.addAll(hs);
                  for (int p = 0; p < al.size(); p++) {

                    String linkedinId = al.get(p).toString().trim();
                    SocialDataLI sData = new SocialDataLI();
                    System.out.println("linkedinId----" + linkedinId);
                    sData.setLinkedinId(linkedinId);
                    sData.setType("People");
                    Thread.sleep(3000);
                    HttpURLConnection profilePageRequest = getPageRequest("http://www.linkedin.com/profile/view?id=" + linkedinId, cook);

                    log.debug("profilePageRequest status code--->" + profilePageRequest.getResponseCode());
                    if (profilePageRequest.getResponseCode() == 200) {

                      StringBuffer pageResponse = getResponse(profilePageRequest);
                      System.out.println("firstName----" + getPatternValue(pageResponse, "firstName\":\"(.*?)\""));
                      sData.setFirstName(getPatternValue(pageResponse, "firstName\":\"(.*?)\""));

                      sData.setLastName(getPatternValue(pageResponse, "lastName\":\"(.*?)\""));

                      sData.setHeadline(getPatternValue(pageResponse, "memberHeadline\":\"(.*?)\""));

                      String numConnections = getPatternValue(pageResponse, "numberOfConnections\":([0-9]{0,})");
                      if (!numConnections.equalsIgnoreCase(""))
                        sData.setNumConnections(Integer.parseInt(numConnections));

                      Pattern summaryp = Pattern.compile("showSummarySection\":(true|false),[^>]*\"summary_lb\":\"(.*?)\"");
                      java.util.regex.Matcher summarym = summaryp.matcher(pageResponse.toString());
                      if (summarym.find()) {
                        sData.setSummary(summarym.group(2));
                      }

                      sData.setPublicProfileUrl(getPatternValue(pageResponse, "\"canonicalUrlToShow\":\"(.*?)\""));

                      sData.setPictureUrl(getPatternValue(pageResponse, "mem_pic\":\"(.*?)\""));

                      Integer endrosementCount = 0;
                      Pattern eCountp = Pattern.compile("endorsementCount\":([0-9]{0,})");
                      java.util.regex.Matcher eCountm = eCountp.matcher(pageResponse.toString());
                      while (eCountm.find()) {
                        endrosementCount += Integer.parseInt(eCountm.group(1));
                      }

                      // log.debug("Total Endrosement Count--->" + endrosementCount);
                      sData.setNumRecommenders(endrosementCount);
                      Pattern specialp = Pattern.compile("],\"name\":\"(.*?)\",\"endorsementCount\":");
                      java.util.regex.Matcher specialm = specialp.matcher(pageResponse.toString());
                      while (specialm.find()) {
                        sData.setSpecialties(specialm.group(1));

                      }
                      Pattern twitterp = Pattern.compile("twitterHandle\":\"(.*)\"");
                      java.util.regex.Matcher twitterm = twitterp.matcher(pageResponse.toString());
                      while (twitterm.find()) {
                        sData.setTwitterId(twitterm.group(1));
                      }

                      String industryCode = getPatternValue(pageResponse, "industryID\":([0-9]{0,})");
                      if (!industryCode.equalsIgnoreCase(""))
                        sData.setIndustries(industryCodeMap.get(Integer.parseInt(industryCode)));

                      Pattern connectionsCappedp = Pattern.compile("i18n_numconnectionsformattable\":\"(.*?)\"");
                      java.util.regex.Matcher connectionsCappedm = connectionsCappedp.matcher(pageResponse.toString());
                      if (connectionsCappedm.find()) {
                        sData.setNumConnectionsCapped(true);
                      }

                      Pattern memUrlResP = Pattern.compile("\"websites\":(.*?)}],");
                      java.util.regex.Matcher memUrlResM = memUrlResP.matcher(pageResponse.toString());
                      if (memUrlResM.find()) {
                        JSONParser parser = new JSONParser();
                        Object obj = parser.parse(memUrlResM.group(1) + "}]");
                        JSONArray a = (JSONArray) obj;

                        Vector<Url> memberUrlResources = null;

                        Url urlV = sData.new Url();
                        memberUrlResources = new Vector<Url>(a.size());
                        for (Object o : a) {
                          JSONObject url = (JSONObject) o;
                          urlV.setUrl(url.get("URL").toString());
                          log.trace("url--------------" + url.get("URL").toString());
                          memberUrlResources.add(urlV);
                        }

                        sData.setMemberUrlResources(memberUrlResources);

                      }

                      dataList.add(sData);

                    }
                  }
                } else {
                  log.debug("adv search json content not found");
                }
              } else {
                log.debug("advSearchRequest status code is not 200--->" + advSearchRequest.getResponseCode());
              }
              page++;
              advUrl = "http://www.linkedin.com/vsearch/p?keywords=" + query.replaceAll(" ", "%20")
                  + "&openAdvancedForm=true&locationType=Y&rsid=" + rsidMatch.group(1) + "&orig=FCTD&page_num=" + page;

            } while (page <= noOfPages);
          } else {
            log.debug("rsid's not found");
          }
        } else {
          log.debug("First json Content not found");
        }

      } else {
        log.debug("searchRequest status code is not 200--->" + searchRequest.getResponseCode());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return dataList;
  }

  /**
   * This method used to get Number of pages
   * 
   * @param param
   * @return noOfPages
   */
  private Integer getNoOfPage(String param) {
    Integer resultCount = null;
    Integer noOfPages = null;
    Pattern rc = Pattern.compile("resultCount\":([0-9]{0,})");
    java.util.regex.Matcher rsc = rc.matcher(param.toString());
    if (rsc.find()) {
      resultCount = Integer.parseInt(rsc.group(1));
    }
    if (resultCount > 100)
      noOfPages = 10;
    else if (resultCount % 10 > 0) {
      noOfPages = (resultCount / 10) + 1;
    }
    return noOfPages;
  }

  /**
   * This method used to get page response
   * 
   * @param request
   * @return response
   */
  private StringBuffer getResponse(HttpURLConnection request) {
    StringBuffer response = null;
    try {
      BufferedReader ld = new BufferedReader(new InputStreamReader(request.getInputStream()));
      response = new StringBuffer();
      while ((temp = ld.readLine()) != null) {
        response.append(temp);
      }
      ld.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return response;

  }

  /**
   * This method used to get cookie value
   * 
   * @return cook
   */
  private String getCookie() {
    String cook = "";

    for (String cookie : LinkedInCookies) {
      cook += cookie.split(";", 2)[0] + "; ";
    }
    return cook;
  }

  /**
   * Method used to get page
   * 
   * @param url
   * @param cook
   * @return page
   */
  private HttpURLConnection getPageRequest(String url, String cook) {
    HttpURLConnection page = null;
    try {
      Thread.sleep(3000);
      page = (HttpURLConnection) new URL(url).openConnection();
      page.setRequestProperty("Host", "www.linkedin.com");
      page.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.5; rv:16.0) Gecko/20100101 Firefox/16.0");
      page.setDoInput(true);
      if (!cook.equalsIgnoreCase(""))
        page.addRequestProperty("Cookie", cook);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return page;
  }

  /**
   * Method used to get pattern value
   * 
   * @param pageResponse
   * @param patternRule
   * @return value
   */
  private String getPatternValue(StringBuffer pageResponse, String patternRule) {
    String value = "";
    Pattern pattern = Pattern.compile(patternRule);
    java.util.regex.Matcher matcher = pattern.matcher(pageResponse);
    if (matcher.find())
      value = matcher.group(1).toString().trim();
    log.debug("Parsed value----> " + value);
    return value;
  }

  /*
   * Reference: https://developer.linkedin.com/documents/industry-codes
   */
  private void industryCodes() {

    industryCodeMap.put(47, "Accounting");
    industryCodeMap.put(94, "Airlines/Aviation");
    industryCodeMap.put(120, "Alternative Dispute Resolution");
    industryCodeMap.put(125, "Alternative Medicine");
    industryCodeMap.put(127, "Animation");
    industryCodeMap.put(19, "Apparel & Fashion");
    industryCodeMap.put(50, "Architecture & Planning");
    industryCodeMap.put(111, "Arts and Crafts");
    industryCodeMap.put(53, "Automotive");
    industryCodeMap.put(52, "Aviation & Aerospace");
    industryCodeMap.put(41, "Banking");
    industryCodeMap.put(12, "Biotechnology");
    industryCodeMap.put(36, "Broadcast Media");
    industryCodeMap.put(49, "Building Materials");
    industryCodeMap.put(138, "Business Supplies and Equipment");
    industryCodeMap.put(129, "Capital Markets");
    industryCodeMap.put(54, "Chemicals");
    industryCodeMap.put(90, "Civic & Social Organization");
    industryCodeMap.put(51, "Civil Engineering");
    industryCodeMap.put(128, "Commercial Real Estate");
    industryCodeMap.put(118, "Computer & Network Security");
    industryCodeMap.put(109, "Computer Games");
    industryCodeMap.put(3, "Computer Hardware");
    industryCodeMap.put(5, "Computer Networking");
    industryCodeMap.put(4, "Computer Software");
    industryCodeMap.put(48, "Construction");
    industryCodeMap.put(24, "Consumer Electronics");
    industryCodeMap.put(25, "Consumer Goods");
    industryCodeMap.put(91, "Consumer Services");
    industryCodeMap.put(18, "Cosmetics");
    industryCodeMap.put(65, "Dairy");
    industryCodeMap.put(1, "Defense & Space");
    industryCodeMap.put(99, "Design");
    industryCodeMap.put(69, "Education Management");
    industryCodeMap.put(132, "E-Learning");
    industryCodeMap.put(112, "Electrical/Electronic Manufacturing");
    industryCodeMap.put(28, "Entertainment");
    industryCodeMap.put(86, "Environmental Services");
    industryCodeMap.put(110, "Events Services");
    industryCodeMap.put(76, "Executive Office");
    industryCodeMap.put(122, "Facilities Services");
    industryCodeMap.put(63, "Farming");
    industryCodeMap.put(43, "Financial Services");
    industryCodeMap.put(38, "Fine Art");
    industryCodeMap.put(66, "Fishery");
    industryCodeMap.put(34, "Food & Beverages");
    industryCodeMap.put(23, "Food Production");
    industryCodeMap.put(101, "Fund-Raising");
    industryCodeMap.put(26, "Furniture");
    industryCodeMap.put(29, "Gambling & Casinos");
    industryCodeMap.put(145, "Glass, Ceramics & Concrete");
    industryCodeMap.put(75, "Government Administration");
    industryCodeMap.put(148, "Government Relations");
    industryCodeMap.put(140, "Graphic Design");
    industryCodeMap.put(124, "Health, Wellness and Fitness");
    industryCodeMap.put(68, "Higher Education");
    industryCodeMap.put(14, "Hospital & Health Care");
    industryCodeMap.put(31, "Hospitality");
    industryCodeMap.put(137, "Human Resources");
    industryCodeMap.put(134, "Import and Export");
    industryCodeMap.put(88, "Individual & Family Services");
    industryCodeMap.put(147, "Industrial Automation");
    industryCodeMap.put(84, "Information Services");
    industryCodeMap.put(96, "Information Technology and Services");
    industryCodeMap.put(42, "Insurance");
    industryCodeMap.put(74, "International Affairs");
    industryCodeMap.put(141, "International Trade and Development");
    industryCodeMap.put(6, "Internet");
    industryCodeMap.put(45, "Investment Banking");
    industryCodeMap.put(46, "Investment Management");
    industryCodeMap.put(73, "Judiciary");
    industryCodeMap.put(77, "Law Enforcement");
    industryCodeMap.put(9, "Law Practice");
    industryCodeMap.put(10, "Legal Services");
    industryCodeMap.put(72, "Legislative Office");
    industryCodeMap.put(30, "Leisure, Travel & Tourism");
    industryCodeMap.put(85, "Libraries");
    industryCodeMap.put(116, "Logistics and Supply Chain");
    industryCodeMap.put(143, "Luxury Goods & Jewelry");
    industryCodeMap.put(55, "Machinery");
    industryCodeMap.put(11, "Management Consulting");
    industryCodeMap.put(95, "Maritime");
    industryCodeMap.put(97, "Market Research");
    industryCodeMap.put(80, "Marketing and Advertising");
    industryCodeMap.put(135, "Mechanical or Industrial Engineering");
    industryCodeMap.put(126, "Media Production");
    industryCodeMap.put(17, "Medical Devices");
    industryCodeMap.put(13, "Medical Practice");
    industryCodeMap.put(139, "Mental Health Care");
    industryCodeMap.put(71, "Military");
    industryCodeMap.put(56, "Mining & Metals");
    industryCodeMap.put(35, "Motion Pictures and Film");
    industryCodeMap.put(37, "Museums and Institutions");
    industryCodeMap.put(115, "Music");
    industryCodeMap.put(114, "Nanotechnology");
    industryCodeMap.put(81, "Newspapers");
    industryCodeMap.put(100, "Non-Profit Organization Management");
    industryCodeMap.put(57, "Oil & Energy");
    industryCodeMap.put(113, "Online Media");
    industryCodeMap.put(123, "Outsourcing/Offshoring");
    industryCodeMap.put(87, "Package/Freight Delivery");
    industryCodeMap.put(146, "Packaging and Containers");
    industryCodeMap.put(61, "Paper & Forest Products");
    industryCodeMap.put(39, "Performing Arts");
    industryCodeMap.put(15, "Pharmaceuticals");
    industryCodeMap.put(131, "Philanthropy");
    industryCodeMap.put(136, "Photography");
    industryCodeMap.put(117, "Plastics");
    industryCodeMap.put(107, "Political Organization");
    industryCodeMap.put(67, "Primary/Secondary Education");
    industryCodeMap.put(83, "Printing");
    industryCodeMap.put(105, "Professional Training & Coaching");
    industryCodeMap.put(102, "Program Development");
    industryCodeMap.put(79, "Public Policy");
    industryCodeMap.put(98, "Public Relations and Communications");
    industryCodeMap.put(78, "Public Safety");
    industryCodeMap.put(82, "Publishing");
    industryCodeMap.put(62, "Railroad Manufacture");
    industryCodeMap.put(64, "Ranching");
    industryCodeMap.put(44, "Real Estate");
    industryCodeMap.put(40, "Recreational Facilities and Services");
    industryCodeMap.put(89, "Religious Institutions");
    industryCodeMap.put(144, "Renewables & Environment");
    industryCodeMap.put(70, "Research");
    industryCodeMap.put(32, "Restaurants");
    industryCodeMap.put(27, "Retail");
    industryCodeMap.put(121, "Security and Investigations");
    industryCodeMap.put(7, "Semiconductors");
    industryCodeMap.put(58, "Shipbuilding");
    industryCodeMap.put(20, "Sporting Goods");
    industryCodeMap.put(33, "Sports");
    industryCodeMap.put(104, "Staffing and Recruiting");
    industryCodeMap.put(22, "Supermarkets");
    industryCodeMap.put(8, "Telecommunications");
    industryCodeMap.put(60, "Textiles");
    industryCodeMap.put(130, "Think Tanks");
    industryCodeMap.put(21, "Tobacco");
    industryCodeMap.put(108, "Translation and Localization");
    industryCodeMap.put(92, "Transportation/Trucking/Railroad");
    industryCodeMap.put(59, "Utilities");
    industryCodeMap.put(106, "Venture Capital & Private Equity");
    industryCodeMap.put(16, "Veterinary");
    industryCodeMap.put(93, "Warehousing");
    industryCodeMap.put(133, "Wholesale");
    industryCodeMap.put(142, "Wine and Spirits");
    industryCodeMap.put(119, "Wireless");
    industryCodeMap.put(103, "Writing and Editing");
  }
}
