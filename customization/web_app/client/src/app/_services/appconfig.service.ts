import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AppconfigService {
  private appConfig: any;

  constructor(private http: HttpClient) { }

  //return this.http.get('/plmassist/assets/config/UrlConfig.json')
  loadAppConfig() {
    return this.http.get('/plmassist/assets/config/UrlConfig.json')
      .toPromise()
      .then(data => {
        this.appConfig = data;
      });
  }

  get apiBaseUrl() {
    if (!this.appConfig) {
      throw Error('Config file not loaded!');
    }

    return this.appConfig.apiBaseUrl;
  }
}
