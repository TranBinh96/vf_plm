import { Component, OnInit, ChangeDetectorRef, HostListener, TemplateRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap';
import { UserInfoModel } from 'src/app/_models/user/UserSessionModel';
import { BasePagingModel } from 'src/app/_models/base/BasePagingModel';
import { CommonService } from 'src/app/_services/common.service';
import { UserService } from 'src/app/_services/user.service';
import { NgxSpinnerService } from 'ngx-spinner';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';
import swal from 'sweetalert2';
import { DataformService } from 'src/app/_services/dataform.service';
import { TcsystemService } from 'src/app/_services/tcsystem.service';
import { element } from 'protractor';

@Component({
  selector: 'app-bmide-list',
  templateUrl: './bmide-list.component.html',
  styleUrls: ['./bmide-list.component.css']
})
export class BmideListComponent implements OnInit {
  id: string;
  propertyName: string;
  bsModalRefEdit: BsModalRef;
  bsModalRefLOV: BsModalRef;

  userInfo:UserInfoModel;
  loadingIndicator: boolean = false;

  hasChanged:boolean = false;

  searchData: any = {
    objectType: '',
    name: '',
  }

  dataForm: any = {
    clientTypeList: [],
  }

  pagingModel = new BasePagingModel();

  dataList:any = [];

  constructor(
    private tcsystemService:TcsystemService,
    private dataFormService:DataformService,
    private changeDetection:ChangeDetectorRef,
    private commonService:CommonService,
    private userService:UserService,
    private spinner:NgxSpinnerService,
    private toastr:ToastrService,
    private router:Router
  ) { 
    this.userInfo = this.userService.getUserInfo();
  }

  public innerHeight: any;
  @HostListener('window:resize', ['$event'])
  onResize(event){
    this.innerHeight = window.innerHeight - 140;
  }

  permissionData:any = {
    IsPer: false
  };

  picPermissionData:any = {
    IsPer: false
  };

  onInit(){
    
  }

  ngOnInit() {
    this.innerHeight = window.innerHeight - 140;
    this.loadDataForm();
  }

  loadData() {
    this.loadingIndicator = true;
    this.tcsystemService.BmideObject_GetList(this.searchData).subscribe(x => {
      this.loadingIndicator = false;
      this.pagingModel = x;
      this.dataList = x.dataList;
    });
  }

  loadDataForm() {
    this.dataFormService.BmideObjectList_GetDataForm().subscribe(x => {
      this.dataForm = x;
      console.log(this.dataForm);
    });
  }

  onAcceptBtnModalClick(data: any) {
    this.hasChanged = false;
    if (data.errorCode == "00") {
      this.hasChanged = true;
    }
  }

  triggerEnter(event) {
    if (event.key == "Enter") {
      this.loadData();
    }
  }

  triggerChangeText(event) {
    this.onFilterData();
  }

  onChangeClientType(event){
    if(event.length > 0) this.searchData.clientType = event[0].value;
    else this.searchData.clientType = "";
    this.onFilterData();
  }

  onFilterData() {
    this.dataList = [];
    for(let element of this.pagingModel.dataList) {
      if(this.searchData.clientType != '' && this.searchData.clientType != null) {
        if(element.clientType != this.searchData.clientType)
        continue;
      }

      if(element.realValue.includes(this.searchData.name) || element.displayValue.includes(this.searchData.name)) {
        this.dataList.push(element);
      }
    }
  }

  onCopyClipboard(val: string){
    const selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = val;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
  }

  openPopupLOVDetail(template: TemplateRef<any>, propertyName: string) {
    this.propertyName = propertyName;
    this.bsModalRefLOV = this.commonService.openModal(template, null, 'modal-lg');
    this.commonService.bsmodalService.onHide.subscribe((reason: string) => {
    });
  }
}
