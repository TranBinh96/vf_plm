import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, ActivatedRoute } from '@angular/router';
import { CanActivate } from '@angular/router';
import { UserService } from '../_services/user.service';
import { CookieService } from 'ngx-cookie';
import { UserInfoModel } from '../_models/user/UserSessionModel';
import { SystemService } from '../_services/system.service';
import { UserConst } from '../_models/const/UserConst';

@Injectable({ providedIn: 'root' })
export class Auth implements CanActivate {
    constructor(
        private router: Router,
        private userService: UserService,
        private systemService: SystemService,
        private cookieService: CookieService,
    ) { 
        
    }

    sessionLogin:string;
    sessionLoginCheck: boolean;
    userInfo = new UserInfoModel();

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        // this.sessionLoginCheck = this.cookieService.check("loginUsername");
        // console.log("Session " + this.sessionLoginCheck + this.cookieService.get("loginUsername"));

        this.sessionLogin = this.cookieService.get("loginUsername");
        if(this.sessionLogin == null) {
            this.router.navigate(['login'], { queryParams: { returnUrl: state.url } });
            return false;
        } else {
            // this.sessionLogin = this.cookieService.get("loginUsername");
            this.userInfo = this.userService.getUserInfo();
            this.systemService.Employee_GetByCode(this.sessionLogin).subscribe(y => {
                const userInfo = new UserInfoModel();
                userInfo.fullname = y.employee_name;
                userInfo.code = y.employee_code;
                userInfo.avatar = y.avatarID;
                // userInfo.sectionID = y.SectionID;
                // userInfo.positionID = y.PositionID;
                // userInfo.groupID = y.GroupID;
        
                localStorage.setItem(UserConst.SS_USER_INFO, JSON.stringify(userInfo));
            });
        }
        return true;
    }
}
