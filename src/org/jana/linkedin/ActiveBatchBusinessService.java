package org.luca.linkedin;

import java.util.List;

import org.apache.log4j.Logger;

public abstract class ActiveBatchBusinessService {
  private Logger log = Logger.getLogger(ActiveBatchBusinessService.class);
  private boolean addDelayBetweenCalls = true;
  protected int waitTimeCall = 1;

  /**
   * It determinate if the run will have a forced delay between the query execution.<br>
   * Default is true.
   * 
   * @return flag status
   */
  public boolean isDelayBetweenCalls() {
    return addDelayBetweenCalls;
  }

  public void setDelayBetweenCalls(boolean addDelayBetweenCalls) {
    log.trace("setDelayBetweenCalls: " + addDelayBetweenCalls);
    this.addDelayBetweenCalls = addDelayBetweenCalls;
  }

  abstract public List<? extends Object> runSearch(String query, String type);
}
