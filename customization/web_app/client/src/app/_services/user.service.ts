import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { UserSessionModel, UserInfoModel } from '../_models/user/UserSessionModel';
import { UserConst } from '../_models/const/UserConst';
import { ApiUrl } from '../_models/const/ApiUrl';
import { BaseService } from './base.service';

@Injectable({
    providedIn: 'root'
})

export class UserService extends BaseService {
    //#region system account
    login(userName: string, password: string) {
        const params = new HttpParams()
            .append('userName', userName)
            .append('password', password);
        const data = this.postService<any>(ApiUrl.userLogin, params);
        console.log(ApiUrl.userLogin);
        return data;
    }

    getUserInfo(): UserInfoModel {
        const modelReturn = localStorage.getItem(UserConst.SS_USER_INFO);
        if (modelReturn !== null) {
            const userInfoModel = JSON.parse(modelReturn);
            return userInfoModel;
        }
        return null;
    }

    getUserSession(): UserSessionModel {
        const modelReturn = localStorage.getItem(UserConst.SS_USER_INFO);
        if (modelReturn !== null) {
            const userSessionModel = JSON.parse(modelReturn);
            return userSessionModel;
        }
        return null;
    }

    logout() {
        localStorage.removeItem(UserConst.SS_USER_CLAIM);
        localStorage.removeItem(UserConst.SS_USER_INFO);
    }

    changePassword(loginName: string, currentPass: string, newPass: string, reNewPass: string) {
        const params = new HttpParams()
            .append('loginName', loginName)
            .append('currentPass', currentPass)
            .append('newPass', newPass)
            .append('reNewPass', reNewPass);

        const data = this.postService<any>(ApiUrl.userChangePassword, params);
        return data;
    }
    //#endregion
}
