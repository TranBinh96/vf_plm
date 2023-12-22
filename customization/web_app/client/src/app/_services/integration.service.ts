import { Injectable } from '@angular/core';
import { BaseService } from './base.service';
import { Observable } from 'rxjs';
import { ApiUrl } from '../_models/const/ApiUrl';
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class IntegrationService extends BaseService {
  frsDSA_Download(id: any): Observable<any> {
    const params = new HttpParams()
      .set('id', id);
    const data = this.getService<any>(ApiUrl.frsDSA_Download, params);
    return data;
  }

  frsInternal_Download(id: any): Observable<any> {
    const params = new HttpParams()
      .set('id', id);
    const data = this.getService<any>(ApiUrl.frsInternal_Download, params);
    return data;
  }
}
