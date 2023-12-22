import { Injectable } from '@angular/core';
import { BaseService } from './base.service';
import { Observable } from 'rxjs';
import { ApiUrl } from '../_models/const/ApiUrl';
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class TcsystemService extends BaseService {
  //bmide
  BmideObject_GetList(searchData: any): Observable<any> {
    const params = new HttpParams()
      .set("objectType", searchData.objectType);
    const data = this.getService<any>(ApiUrl.bmideObject_GetList, params);
    return data;
  }

  //LOV
  LovValue_GetList(objectName: string, propertyName: string): Observable<any> {
    const params = new HttpParams()
      .set("objectName", objectName)
      .set("propertyName", propertyName);
    const data = this.getService<any>(ApiUrl.lovValue_GetList, params);
    return data;
  }
}
