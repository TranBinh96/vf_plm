import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { ApiUrl } from '../_models/const/ApiUrl';
import { BaseService } from './base.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})

export class SystemService extends BaseService {
    //Employee
    Employee_GetAll(searchData: any): Observable<any> {
        const data = this.getService<any>(ApiUrl.employee_GetAll, searchData);
        return data;
    }
    employee_Create(editData: any){
        const data = this.postService<any>(ApiUrl.employee_Create, editData);
        return data;
    }
    Employee_Update(editData: any){
        const data = this.postService<any>(ApiUrl.employee_Update, editData);
        return data;
    }
    Employee_GetByCode(employeeCode: string) {
        const params = new HttpParams()
        .set('code', employeeCode);
        const data = this.getService<any>(ApiUrl.employee_GetByCode, params);
        return data;
    }
    //---------
    RequestMaster_GetByID(ID: any): Observable<any> {
        const params = new HttpParams().set('id', ID);
        const data = this.getService<any>(ApiUrl.requestMaster_GetByID, params);
        return data;
    }
    RequestMaster_GetCompBySampleType(requestNo: any, sampleTypeID: any): Observable<any> {
        const params = new HttpParams()
          .set('requestNo', requestNo)
          .set('sampleTypeID', sampleTypeID);
        const data = this.getService<any>(ApiUrl.requestMaster_GetCompBySampleType, params);
        return data;
    }
    CheckPermission(appID, employeeCode, state, picGroup = null, sectionID = null) {
        const params = new HttpParams()
            .set('appID', appID)
            .set('employeeCode', employeeCode)
            .set('state', state)
            .set('picGroup', picGroup)
            .set('sectionID', sectionID);
        const data = this.getService<any>(ApiUrl.checkPermission, params);
        return data;
    }
    CalendarMonth_GetAll(searchData: any) : Observable<any> {
        const data = this.getService<any>(ApiUrl.calendarMonth_GetAll, searchData);
        return data;
    }
}
