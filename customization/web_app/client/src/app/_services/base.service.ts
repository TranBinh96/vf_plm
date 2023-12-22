import { AppconfigService } from './appconfig.service';
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FileSaverService } from 'ngx-filesaver';
@Injectable({
    providedIn: 'root'
})
export class BaseService {
    apiDomain: string;
    constructor(
        private client: HttpClient, 
        private fileSaverService: FileSaverService,
        private appService: AppconfigService,
    ) { this.apiDomain = this.appService.apiBaseUrl; }

    public getService<T>(apiUrl: string, params: HttpParams): Observable<any> {
        console.log(this.apiDomain);
        return this.client.get<T>(this.apiDomain + apiUrl, { params });
    }

    public postService<T>(apiUrl: string, params: any): Observable<any> {
        const httpOptions = {
            headers: new HttpHeaders({
                Accept: 'application/json',
                Authorization: 'bearer '
            })
        };

        return this.client.post<T>(this.apiDomain + apiUrl,
            params,
            httpOptions
        );
    }

    public getDownloadFile(apiUrl: string, params: any): Observable<Blob> {
        const modelReturn = localStorage.getItem("abc");
        let accessToken = '';
        if (modelReturn !== null) {
            const userSessionModel = JSON.parse(modelReturn);
            accessToken = userSessionModel.accessToken;
        }

        const headers = {
            Authorization: 'Bearer ' + accessToken
        };

        return this.client.get(this.apiDomain + apiUrl, {
            headers,
            responseType: 'blob',
            params
        });
    }
}
