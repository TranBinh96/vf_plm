import { AppconfigService } from './../_services/appconfig.service';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { UserService } from '../_services/user.service';
import { ActivatedRoute, Router } from '@angular/router';
import { BsModalRef } from 'ngx-bootstrap';
import { BaseListModel } from '../_models/base/BaseListModel';
import { NgxSpinnerService } from 'ngx-spinner';
import { ToastrService } from 'ngx-toastr';
import { CommonService } from '../_services/common.service';
import { SystemService } from '../_services/system.service';
import { UserInfoModel } from '../_models/user/UserSessionModel';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  @ViewChild('detailTemplate') detailTemplate: TemplateRef<any>;
  id: string;
  bsModalRef: BsModalRef;
  userInfo:UserInfoModel = new UserInfoModel();
  private sub: any;

  searchData: any = {
    uid: '',
  }

  dataForm: any = {
    newsTypeList: [],
  }

  permissionData:any = {
    isPer: false
  };

  listModel = new BaseListModel();

  constructor(
    private route: ActivatedRoute,
    private systemService:SystemService,
    private commonService:CommonService,
    private userService:UserService,
    private spinner:NgxSpinnerService,
    private toastr:ToastrService,
    private router:Router,
    private appService:AppconfigService,
  ) { 
    this.userInfo = this.userService.getUserInfo(); 
  }

  ngOnInit() {
    // this.searchData.employeeCode = this.userInfo.code;
    // this.loadData();
    // this.appService = this.appService.apiBaseUrl;
  }

  // loadDataForm() {

  // }

  // loadPermission(){
  //   this.systemService.CheckPermission(WorkingProcessConst.APP_ADM_NEWS, this.userInfo.code, null).subscribe(x => {
  //     this.permissionData = x;
  //   });
  // }

  loadData() {
    
  }

  // openPopupDetail(template: TemplateRef<any>, id: string) {
  //   this.id = id;
  //   this.bsModalRef = this.commonService.openModal(template, null, 'modal-lg full-modal');
  //   this.commonService.bsmodalService.onHide.subscribe((reason: string) => { });
  // }
}
