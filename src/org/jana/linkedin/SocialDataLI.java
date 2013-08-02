package org.luca.linkedin;

import java.util.Vector;

public class SocialDataLI {

  public static final String PUBLIC_VIEW = "http://www.linkedin.com/profile/view?id=";

  private String linkedinId;
  private String firstName;
  private String lastName;
  private String maidenName;
  private String headline;
  private Integer numConnections;
  private Integer numFollowers;
  private Boolean numConnectionsCapped;
  private String summary;
  private String industries;
  private String description;
  private String currentShare;
  private String specialties;
  private Integer numRecommenders;
  private Vector<Account> imAccounts;
  private Vector<TwitterAccount> twitterAccounts;
  private Vector<Url> memberUrlResources;
  private String pictureUrl;
  private String publicProfileUrl;
  private ProfileRequest apiStandardProfileRequest;

  private String type;

  public String getId() {
    return linkedinId;
  }

  public void setId(String id) {
    this.linkedinId = id;
  }

  public String getLinkedinId() {
    return linkedinId;
  }

  public void setLinkedinId(String linkedinId) {
    this.linkedinId = linkedinId;
  }

  public String getName() {
    return firstName;
  }

  public void setName(String name) {
    this.firstName = name;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getMaidenName() {
    return maidenName;
  }

  public void setMaidenName(String maidenName) {
    this.maidenName = maidenName;
  }

  public String getHeadline() {
    return headline;
  }

  public void setHeadline(String headline) {
    this.headline = headline;
  }

  public Integer getNumConnections() {
    return numConnections;
  }

  public void setNumConnections(Integer numConnections) {
    this.numConnections = numConnections;
  }

  public Boolean getNumConnectionsCapped() {
    return numConnectionsCapped;
  }

  public void setNumConnectionsCapped(Boolean numConnectionsCapped) {
    this.numConnectionsCapped = numConnectionsCapped;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getSpecialties() {
    return specialties;
  }

  public void setSpecialties(String specialties) {
    this.specialties = specialties;
  }

  public Integer getNumRecommenders() {
    return numRecommenders;
  }

  public void setNumRecommenders(Integer numRecommenders) {
    this.numRecommenders = numRecommenders;
  }

  public Vector<Account> getImAccounts() {
    return imAccounts;
  }

  public String getImAccountsDB() {
    if (imAccounts == null)
      return null;
    else {
      String value = "";
      for (Account a : imAccounts) {
        value += ", " + a.providerAccountName;
      }
      return value.length() > 0 ? value.substring(1) : value;
    }
  }

  public void setImAccounts(Vector<Account> imAccounts) {
    this.imAccounts = imAccounts;
  }

  public Vector<TwitterAccount> getTwitterAccounts() {
    return twitterAccounts;
  }

  public String getTwitterAccountsDB() {
    if (twitterAccounts == null)
      return null;
    else {
      String value = "";
      for (Account a : twitterAccounts) {
        value += ", " + a.providerAccountName;
      }
      return value.length() > 0 ? value.substring(1) : value;
    }
  }

  public void setTwitterAccounts(Vector<TwitterAccount> twitterAccounts) {
    this.twitterAccounts = twitterAccounts;
  }

  public String getTwitterId() {
    if (this.twitterAccounts == null)
      return null;
    else if (this.twitterAccounts.size() < 1)
      return null;
    else
      return this.twitterAccounts.get(0).getProviderAccountId();
  }

  public void setTwitterId(String twitterId) {
    this.twitterAccounts = new Vector<TwitterAccount>();
    TwitterAccount id = new TwitterAccount();
    id.setProviderAccountId(twitterId);
    this.twitterAccounts.add(id);
  }

  public Vector<Url> getMemberUrlResources() {
    return memberUrlResources;
  }

  public String getMemberUrlResourcesDB() {
    if (memberUrlResources == null)
      return null;
    else {
      String value = "";
      for (Url a : memberUrlResources) {
        value += "\n" + a.url;
      }
      return value.length() > 0 ? value.substring(1) : value;
    }
  }

  public void setMemberUrlResources(Vector<Url> memberUrlResources) {
    this.memberUrlResources = memberUrlResources;
  }

  public String getWebsiteUrl() {
    if (this.memberUrlResources == null)
      return null;
    else if (this.memberUrlResources.size() < 1)
      return null;
    else
      return this.memberUrlResources.get(0).getUrl();
  }

  public void setWebsiteUrl(String websiteUrl) {
    this.memberUrlResources = new Vector<SocialDataLI.Url>();
    Url url = new Url();
    url.setUrl(websiteUrl);
    this.memberUrlResources.add(url);
  }

  public String getLogoUrl() {
    return pictureUrl;
  }

  public void setLogoUrl(String logoUrl) {
    this.pictureUrl = logoUrl;
  }

  public String getPictureUrl() {
    return pictureUrl;
  }

  public void setPictureUrl(String pictureUrl) {
    this.pictureUrl = pictureUrl;
  }

  public String getPublicProfileUrl() {
    return publicProfileUrl;
  }

  public void setPublicProfileUrl(String publicProfileUrl) {
    this.publicProfileUrl = publicProfileUrl;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setApiStandardProfileRequest(ProfileRequest apiStandardProfileRequest) {
    this.apiStandardProfileRequest = apiStandardProfileRequest;
  }

  public ProfileRequest getApiStandardProfileRequest() {
    return apiStandardProfileRequest;
  }

  public void setNumFollowers(Integer numFollowers) {
    this.numFollowers = numFollowers;
  }

  public Integer getNumFollowers() {
    return numFollowers;
  }

  public String getIndustries() {
    return industries;
  }

  public void setIndustries(String industries) {
    this.industries = industries;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCurrentShare() {
    return currentShare;
  }

  public void setCurrentShare(String currentShare) {
    this.currentShare = currentShare;
  }

  public class Url {
    private String url;
    private String name;

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  public class TwitterAccount extends Account {

  }

  public class Account {
    private String providerAccountId;
    private String providerAccountName;

    public void setProviderAccountId(String providerAccountId) {
      this.providerAccountId = providerAccountId;
    }

    public String getProviderAccountId() {
      return providerAccountId;
    }

    public void setProviderAccountName(String providerAccountName) {
      this.providerAccountName = providerAccountName;
    }

    public String getProviderAccountName() {
      return providerAccountName;
    }
  }

  public class ProfileRequest {
    private String url;
    private Headers headers;

    public void setUrl(String url) {
      this.url = url;
    }

    public String getUrl() {
      return url;
    }

    public void setHeaders(Headers headers) {
      this.headers = headers;
    }

    public Headers getHeaders() {
      return headers;
    }
  }

  public class Headers {
    private HttpHeader httpHeader;

    public void setHttpHeader(HttpHeader httpHeader) {
      this.httpHeader = httpHeader;
    }

    public HttpHeader getHttpHeader() {
      return httpHeader;
    }
  }

  public class HttpHeader {
    private String name;
    private String value;

    public void setName(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  public String toString() {
    String name = "";
    if (firstName != null)
      name += firstName;
    if (maidenName != null)
      name += maidenName;
    if (lastName != null)
      name += lastName;
    String linkTo;
    if (type.equals("company")) {
      linkTo = "http://www.linkedin.com/company/" + linkedinId;
    } else {
      linkTo = publicProfileUrl;
    }
    String desc = "";
    if (summary != null)
      desc += "Summary\n" + summary + "\n";
    if (description != null)
      desc += "Description\n" + description + "\n";
    if (industries != null)
      desc += "Industries\n" + industries + "\n";
    if (imAccounts != null)
      desc += "Im Accounts: " + getImAccountsDB() + "\n";
    if (twitterAccounts != null)
      desc += "Twitter Accounts: " + getTwitterAccountsDB() + "\n";
    if (memberUrlResources != null)
      desc += "Web sites: " + getMemberUrlResourcesDB();
    String popularity = "";
    if (type.equals("company")) {
      popularity = numFollowers + " followers";
    } else if (numConnections != null) {
      popularity = numConnections + (numConnectionsCapped != null && numConnectionsCapped.booleanValue() ? "+ " : " ") + "connections";
    }
    return new StringBuffer().append(" linkedinId: ").append(linkedinId).append("\n avatar: ").append(pictureUrl).append("\n name: ")
        .append(name.trim()).append("\n linkTo: ").append(linkTo).append("\n popularity: ").append(popularity).append("\n category: ")
        .append(type).append("\n description: ").append(desc.trim()).toString();
  }
}
