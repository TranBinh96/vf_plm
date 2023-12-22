import { Component, OnInit } from '@angular/core';
import { UserService } from 'src/app/_services/user.service';
import { Router } from '@angular/router';
import { BsModalRef, BsModalService, ModalOptions } from 'ngx-bootstrap/modal';
import { ChangePasswordComponent } from 'src/app/system/sys-acc/change-password/change-password.component';
import { UserSessionModel, UserInfoModel } from 'src/app/_models/user/UserSessionModel';
import { CommonService } from 'src/app/_services/common.service';
import { CookieService } from 'ngx-cookie';

@Component({
  selector: 'app-site-header',
  templateUrl: './site-header.component.html',
  styleUrls: ['./site-header.component.css']
})
export class SiteHeaderComponent implements OnInit {
  bsModalRef: BsModalRef;
  userInfo = new UserInfoModel();
  constructor(
    private userService: UserService,
    private router: Router,
    private modalService: BsModalService,
    private commonService: CommonService,
    private cookieService: CookieService,
  ) { 

  }

  ngOnInit() {
    this.userInfo = this.userService.getUserInfo();
    console.log(this.userInfo.fullname);
  }

  logout() {
    this.cookieService.remove("loginUsername");
    this.userService.logout();
    this.router.navigate(['login']);
  }

  openModalChangePassword() {
    this.commonService.openModal(ChangePasswordComponent, { });
  }

  goToLink(){
    // window.open("http://192.168.102.29:8882/#!/dmsViewFile/7155", "_blank");
  }
}
