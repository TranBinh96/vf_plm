import { Injectable } from '@angular/core';
import { BaseService } from './base.service';
import { Observable } from 'rxjs';
import { ApiUrl } from '../_models/const/ApiUrl';
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class TaskscheduleService extends BaseService {

  // Job Category
  JobCategory_GetAll(searchData: any): Observable<any> {
    const data = this.getService<any>(ApiUrl.jobCategory_GetAll, searchData);
    return data;
  }
  JobCategory_GetByID(ID: any): Observable<any> {
    const params = new HttpParams().set('id', ID);
    const data = this.getService<any>(ApiUrl.jobCategory_GetByID, params);
    return data;
  }
  JobCategory_Update(editData: any){
    const data = this.postService<any>(ApiUrl.jobCategory_Update, editData);
    return data;
  }

  // Job Master
  JobMaster_GetAll(searchData: any): Observable<any> {
    const data = this.getService<any>(ApiUrl.jobMaster_GetAll, searchData);
    return data;
  }
  JobMaster_GetByID(ID: any): Observable<any> {
    const params = new HttpParams().set('uid', ID);
    const data = this.getService<any>(ApiUrl.jobMaster_GetByID, params);
    return data;
  }
  JobMaster_Create(editData: any){
    const data = this.postService<any>(ApiUrl.jobMaster_Create, editData);
    return data;
  }
  JobMaster_Update(editData: any){
    const data = this.postService<any>(ApiUrl.jobMaster_Update, editData);
    return data;
  }
  JobMaster_SyncTask(){
    const params = new HttpParams();
    const data = this.postService<any>(ApiUrl.jobMaster_SyncTask, params);
    return data;
  }

  // Job History
  JobHistory_GetAll(searchData: any): Observable<any> {
    const data = this.getService<any>(ApiUrl.jobHistory_GetAll, searchData);
    return data;
  }
  JobHistory_GetByID(ID: any): Observable<any> {
    const params = new HttpParams().set('id', ID);
    const data = this.getService<any>(ApiUrl.jobHistory_GetByID, params);
    return data;
  }

  //Task Schedule
  TaskSchedule_Run(name: any){
    const params = new HttpParams()
      .set("taskName", name);
    const data = this.postService<any>(ApiUrl.taskSchedule_Run, params);
    return data;
  }
}
