import { Component, OnInit } from '@angular/core';
import { BaseModel } from 'src/app/_models/base/BaseModel';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from 'src/app/_services/user.service';
import { UserClaimModel, UserSessionModel, UserInfoModel } from 'src/app/_models/user/UserSessionModel';
import { UserConst } from 'src/app/_models/const/UserConst';
import { CommonHelper } from 'src/app/_helpers/common.helper';
import { NgxSpinnerService } from 'ngx-spinner';
import { SystemService } from 'src/app/_services/system.service';
import { ToastrService } from 'ngx-toastr';
// import { CookieService } from 'ngx-cookie-service';
import { CookieService, CookieOptions } from 'ngx-cookie';

@Component({
  selector: 'app-login-layout',
  templateUrl: './login-layout.component.html',
})
export class LoginLayoutComponent implements OnInit {
  userName = '';
  password = '';
  private sub: any;
  returnUrl: '';
  // cookieOption:CookieOptions;
  expDate:Date;

  baseModel: BaseModel;
  constructor(
    private route: ActivatedRoute, 
    private router: Router, 
    private userService: UserService, 
    private spinner: NgxSpinnerService,
    private toastr: ToastrService,
    private systemService: SystemService,
    private cookieService: CookieService,
  ) {
    document.body.attributes.removeNamedItem('class');
    document.body.classList.add('hold-transition');
    // document.body.classList.add('bg-img');
    // document.body.style.setProperty('background-image', 'url(./assets/images/6.jpg)');
  }

  ngOnInit() {
    this.sub = this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'];
    });

    this.baseModel = new BaseModel();
    const modelReturn = this.userService.getUserSession();
    if (modelReturn != null) {
      this.router.navigate(['/']);
    }
  }

  login(): void {
    // this.cookieService.set("loginUsername", this.userName, 365, '../','',true,"Lax");
    this.spinner.show();
    this.userService.login(this.userName, this.password).subscribe(x => {
      this.spinner.hide();
      if(x.errorCode !== '00') {
        this.toastr.error(x.message);
      } else {
        // this.cookieService.set("loginUsername", this.userName, 365, '/','',true,"Lax");
        this.expDate = new Date();
        this.expDate.setDate(this.expDate.getDate() + 365);
        let cookieOption = {expires: this.expDate} as CookieOptions;
        this.cookieService.put("loginUsername", this.userName, cookieOption);
        this.systemService.Employee_GetByCode(this.userName).subscribe(y => {
          console.log(y);
          const userInfo = new UserInfoModel();
          userInfo.fullname = y.employee_name;
          userInfo.code = y.employee_code;
          userInfo.avatar = "";
  
          localStorage.setItem(UserConst.SS_USER_INFO, JSON.stringify(userInfo));
          if(this.returnUrl != null) this.router.navigate([this.returnUrl]);
          else this.router.navigate(['/']);
        });
      }
    }, error => {
      this.spinner.hide();
      this.baseModel = CommonHelper.getError(error);
    });
  }
}
