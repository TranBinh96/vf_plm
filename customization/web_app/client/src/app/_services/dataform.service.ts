import { Injectable } from '@angular/core';
import { BaseService } from './base.service';
import { HttpParams } from '@angular/common/http';
import { ApiUrl } from '../_models/const/ApiUrl';

@Injectable({
  providedIn: 'root'
})
export class DataformService extends BaseService {
  DataPullerList_GetDataForm(){
    const data = this.getService<any>(ApiUrl.dataPullerList_GetDataForm, null);
    return data;
  }

  BomVerifyMasterList_GetDataForm(){
    const data = this.getService<any>(ApiUrl.bomVerifyMasterList_GetDataForm, null);
    return data;
  }

  BomVerifyMasterUpdate_GetDataForm(){
    const data = this.getService<any>(ApiUrl.bomVerifyMasterUpdate_GetDataForm, null);
    return data;
  }

  EmailSenderList_GetDataForm(){
    const data = this.getService<any>(ApiUrl.emailSenderList_GetDataForm, null);
    return data;
  }

  BmideObjectList_GetDataForm() {
    const data = this.getService<any>(ApiUrl.bmideObjectList_GetDataForm, null);
    return data;
  }

  JobMasterList_GetDataForm() {
    const data = this.getService<any>(ApiUrl.jobMasterList_GetDataForm, null);
    return data;
  }

  JobHistoryList_GetDataForm() {
    const data = this.getService<any>(ApiUrl.jobHistoryList_GetDataForm, null);
    return data;
  }

  TaskScheduleList_GetDataForm() {
    const data = this.getService<any>(ApiUrl.taskScheduleList_GetDataForm, null);
    return data;
  }

  TechnicianMapUpdate_GetDataForm() {
    const data = this.getService<any>(ApiUrl.technicianMapUpdate_GetDataForm, null);
    return data;
  }

  SapMaterialMasterList_GetDataForm() {
    const data = this.getService<any>(ApiUrl.sapMaterialMasterList_GetDataForm, null);
    return data;
  }

  DataPullerSubscriptionList_GetDataForm() {
    const data = this.getService<any>(ApiUrl.dataPullerSubscriptionList_GetDataForm, null);
    return data;
  }

  GetListFile_ByFolder(functionName: any){
    const params = new HttpParams()
        .set('functionName', functionName);
    const data = this.getService<any>(ApiUrl.GetListFile_ByFolder, params);
    return data;
  }

  UploadFile(inputData: any){
    const formData = new FormData();
        formData.append('functionName', inputData.functionName);
        formData.append('file', inputData.file);
    const data = this.postService<any>(ApiUrl.UploadFile, formData);
    return data;
  }
  
  DataForm_RequestCompReport(id: any){
    const params = new HttpParams()
        .set('requestNo', id);
    const data = this.getService<any>(ApiUrl.GetDataForm_RequestCompReport, params);
    return data;
  }
}
