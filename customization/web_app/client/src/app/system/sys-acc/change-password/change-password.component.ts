import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { BaseModel } from 'src/app/_models/base/BaseModel';
import { UserService } from 'src/app/_services/user.service';
import { CommonHelper } from 'src/app/_helpers/common.helper';
import { ToastrService } from 'ngx-toastr';
import { NgxSpinnerService } from 'ngx-spinner';
import { Router } from '@angular/router';
import { UserInfoModel } from 'src/app/_models/user/UserSessionModel';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
})
export class ChangePasswordComponent implements OnInit {
  currentPass = '';
  newPass = '';
  reNewPass = '';
  baseModel: BaseModel;
  userInfo: UserInfoModel = new UserInfoModel();

  constructor(
    public bsModalRef: BsModalRef,
    private userService: UserService,
    public toastrService: ToastrService,
    private spinner: NgxSpinnerService,
    private router: Router
  ) {
    this.userInfo = this.userService.getUserInfo();
   }

  ngOnInit() {
    this.baseModel = new BaseModel();
  }

  changePassword() {
    const isValid = this.validData();
    if (!isValid) {
      return;
    }
    this.spinner.show();
    this.userService.changePassword(this.userInfo.code, this.currentPass, this.newPass, this.reNewPass).subscribe(x => {
      this.spinner.hide();
      if (x.errorCode !== '00') {
        this.toastrService.error(x.message);
      } else {
        this.toastrService.success(x.message);
        this.currentPass = '';
        this.newPass = '';
        this.reNewPass = '';
        this.bsModalRef.hide();
      }
    });
  }

  private validData(): boolean {
    if (this.currentPass === '') {
      this.toastrService.error('Input current password');
      return false;
    }

    if (this.newPass === '') {
      this.toastrService.error('Input new password');
      return false;
    }

    if (this.reNewPass === '') {
      this.toastrService.error('Confirm new password');
      return false;
    }
    return true;
  }
}
