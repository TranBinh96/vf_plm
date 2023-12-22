import { Injectable } from '@angular/core';
import { BaseService } from './base.service';
import { Observable } from 'rxjs';
import { ApiUrl } from '../_models/const/ApiUrl';
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class SapService extends BaseService{
  SapMaterialMaster_GetAll(searchData: any): Observable<any> {
    const data = this.getService<any>(ApiUrl.sapMaterialMaster_GetAll, searchData);
    return data;
  }
  SapMaterialMaster_GetByID(ID: any): Observable<any> {
    const params = new HttpParams().set('id', ID);
    const data = this.getService<any>(ApiUrl.sapMaterialMaster_GetByID, params);
    return data;
  }
}
