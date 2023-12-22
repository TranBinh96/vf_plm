import { Injectable } from '@angular/core';
import { BaseService } from './base.service';
import { Observable } from 'rxjs';
import { ApiUrl } from '../_models/const/ApiUrl';
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class DatapullerService extends BaseService {
  //data puller
  DataPuller_GetAll(searchData: any): Observable<any> {
    const data = this.getService<any>(ApiUrl.dataPuller_GetAll, searchData);
    return data;
  }
  DataPuller_GetByID(ID: any): Observable<any> {
    const params = new HttpParams().set('id', ID);
    const data = this.getService<any>(ApiUrl.dataPuller_GetByID, params);
    return data;
  }
  DataPuller_Create(editData: any){
    const data = this.postService<any>(ApiUrl.dataPuller_Create, editData);
    return data;
  }
  DataPuller_Edit(editData: any){
    const data = this.postService<any>(ApiUrl.dataPuller_Update, editData);
    return data;
  }
  DataPuller_MultiUpdate(editData: any){
    const data = this.postService<any>(ApiUrl.dataPuller_MultiUpdate, editData);
    return data;
  }
  //bom verify
  BomVerifyMaster_GetAll(searchData: any): Observable<any> {
    const data = this.getService<any>(ApiUrl.bomVerifyMaster_GetAll, searchData);
    return data;
  }
  BomVerifyMaster_GetByID(ID: any): Observable<any> {
    const params = new HttpParams()
    .set('id', ID)
    const data = this.getService<any>(ApiUrl.bomVerifyMaster_GetByID, params);
    return data;
  }
  BomVerifyMaster_Update(editData: any){
    const data = this.postService<any>(ApiUrl.bomVerifyMaster_Update, editData);
    return data;
  }
  BomVerifyMaster_MultiUpdate(editData: any) {
    const data = this.postService<any>(ApiUrl.bomVerifyMaster_MultiUpdate, editData);
    return data;
  }
  //email sender
  EmailSender_GetAll(searchData: any): Observable<any> {
    const data = this.getService<any>(ApiUrl.emailSender_GetAll, searchData);
    return data;
  }
  EmailSender_GetByID(ID: any): Observable<any> {
    const params = new HttpParams().set('id', ID);
    const data = this.getService<any>(ApiUrl.emailSender_GetByID, params);
    return data;
  }
  EmailSender_Update(editData: any){
    const data = this.postService<any>(ApiUrl.emailSender_Update, editData);
    return data;
  }
  //revision rule
  RevisionRuleMaster_GetAll(searchData: any): Observable<any> {
    const data = this.getService<any>(ApiUrl.revisionRuleMaster_GetAll, searchData);
    return data;
  }
  RevisionRuleMaster_GetByID(ID: any): Observable<any> {
    const params = new HttpParams().set('id', ID);
    const data = this.getService<any>(ApiUrl.revisionRuleMaster_GetByID, params);
    return data;
  }
  RevisionRuleMaster_Update(editData: any){
    const data = this.postService<any>(ApiUrl.revisionRuleMaster_Update, editData);
    return data;
  }
  //data puller status
  DataPullerEventMaster_GetAll(searchData: any): Observable<any> {
    const data = this.getService<any>(ApiUrl.dataPullerEventMaster_GetAll, searchData);
    return data;
  }
  DataPullerEventMaster_GetByID(ID: any): Observable<any> {
    const params = new HttpParams().set('id', ID);
    const data = this.getService<any>(ApiUrl.dataPullerEventMaster_GetByID, params);
    return data;
  }
  DataPullerEventMaster_Update(editData: any){
    const data = this.postService<any>(ApiUrl.dataPullerEventMaster_Update, editData);
    return data;
  }
  //data puller subscription
  DataPullerSubscriptionHandler_GetAll(searchData: any): Observable<any> {
    const data = this.getService<any>(ApiUrl.dataPullerSubscriptionHandler_GetAll, searchData);
    return data;
  }
  DataPullerSubscriptionHandler_GetByID(ID: any): Observable<any> {
    const params = new HttpParams().set('id', ID);
    const data = this.getService<any>(ApiUrl.dataPullerSubscriptionHandler_GetByID, params);
    return data;
  }
  DataPullerSubscriptionHandler_Update(editData: any){
    const data = this.postService<any>(ApiUrl.dataPullerSubscriptionHandler_Update, editData);
    return data;
  }
  //Auto Assign Ticket
  TechnicianMap_GetAll(searchData: any): Observable<any> {
    const data = this.getService<any>(ApiUrl.technicianMap_GetAll, searchData);
    return data;
  }
  TechnicianMap_GetByID(ID: any): Observable<any> {
    const params = new HttpParams().set('id', ID);
    const data = this.getService<any>(ApiUrl.technicianMap_GetByID, params);
    return data;
  }
  TechnicianMap_Update(editData: any){
    const data = this.postService<any>(ApiUrl.technicianMap_Update, editData);
    return data;
  }
}
