package com.teamcenter.vinfast.model;

public class VerifyPDFResultModel {
  private String fileName = "";
  
  private String vfteID = "";
  
  private String errorMessage = "";
  
  public String getFileName() {
    return this.fileName;
  }
  
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
  
  public String getVfteID() {
    return this.vfteID;
  }
  
  public void setVfteID(String vfteID) {
    this.vfteID = vfteID;
  }
  
  public String getErrorMessage() {
    return this.errorMessage;
  }
  
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
