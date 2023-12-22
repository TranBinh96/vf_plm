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
import { DatapullerService } from 'src/app/_services/datapuller.service';
import { DataformService } from 'src/app/_services/dataform.service';

@Component({
  selector: 'app-bom-verify-master-list',
  templateUrl: './bom-verify-master-list.component.html',
  styleUrls: ['./bom-verify-master-list.component.css']
})
export class BomVerifyMasterListComponent implements OnInit {
  id: string;
  typeUpdate: string;
  bsModalRefDetail: BsModalRef;
  bsModalRefEdit: BsModalRef;
  bsModelRefMultiUpdate: BsModalRef;

  userInfo:UserInfoModel;
  loadingIndicator: boolean = false;

  hasChanged:boolean = false;

  searchData: any = {
    ProgramName: '',
    ModuleName: '',
    NotifyUser: '',
    BomType: '',
    RevisionRule: '',
    SortColumn: '',
    SortColumnDir: '',
    PageIndex: 1,
    ItemsPerPage: 25,
  }

  dataForm: any = {
    bomTypeList: [],
    programList: [],
    moduleList: [],
    revisionRuleList: [],
  }

  idList = [];

  selected = [];

  rowPerPage:any = [15, 25, 50];
  pagingModel = new BasePagingModel();

  constructor(
    private dataPullerService:DatapullerService,
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
    this.innerHeight = window.innerHeight - 175;
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
    this.innerHeight = window.innerHeight - 175;
    this.loadDataForm();
  }

  loadData() {
    this.loadingIndicator = true;
    this.dataPullerService.BomVerifyMaster_GetAll(this.searchData).subscribe(x => {
      this.loadingIndicator = false;
      this.pagingModel = x;
    });
    this.selected = [];
  }

  loadDataForm() {
    this.dataFormService.BomVerifyMasterList_GetDataForm().subscribe(x => {
      this.dataForm = x;
    });
  }

  setPage(pageInfo) {
    if (pageInfo.count != null) this.searchData.PageIndex = pageInfo.offset + 1;
    this.loadData();
  }

  onSort(sortInfo) {
    this.searchData.SortColumn = sortInfo.sorts[0].prop;
    this.searchData.SortColumnDir = sortInfo.sorts[0].dir;
    this.loadData();
  }

  onChangeRowPerPage(event){
    this.searchData.ItemsPerPage = event.target.value;
    this.loadData();
  }

  openPopupMultiEdit(template: TemplateRef<any>, typeUpdate: string) {
    this.typeUpdate = typeUpdate;
    this.commonService.changeDetection = this.changeDetection;
    this.bsModelRefMultiUpdate = this.commonService.openModal(template, null, 'modal-lg');
    this.commonService.bsmodalService.onHide.subscribe((reason: string) => {
      if(this.hasChanged){
        this.loadData();
        this.hasChanged = false;
      }
    });
  }

  openPopupEdit(template: TemplateRef<any>, id: any = null) {
    this.id = id;
    this.commonService.changeDetection = this.changeDetection;
    this.bsModalRefEdit = this.commonService.openModal(template, null, 'modal-lg');
    this.commonService.bsmodalService.onHide.subscribe((reason: string) => {
      if(this.hasChanged){
        this.loadData();
        this.hasChanged = false;
      }
    });
  }

  openPopupDetail(template: TemplateRef<any>, id: any) {
    this.id = id;
    this.commonService.changeDetection = this.changeDetection;
    this.bsModalRefDetail = this.commonService.openModal(template, null, 'modal-lg full-modal');
    this.commonService.bsmodalService.onHide.subscribe((reason: string) => {
      if(this.hasChanged){
        this.loadData();
        this.hasChanged = false;
      }
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

  onChangeBomType(event){
    if(event.length > 0) this.searchData.BomType = event[0].value;
    else this.searchData.BomType = "";
    this.loadData();
  }

  onChangeProgram(event){
    if(event.length > 0) this.searchData.ProgramName = event[0].value;
    else this.searchData.ProgramName = "";
    this.loadData();
  }

  onChangeModule(event){
    if(event.length > 0) this.searchData.ModuleName = event[0].value;
    else this.searchData.ModuleName = "";
    this.loadData();
  }

  onChangeRevisionRule(event){
    if(event.length > 0) this.searchData.RevisionRule = event[0].value;
    else this.searchData.RevisionRule = "";
    this.loadData();
  }

  onSelect({ selected }) {
    this.selected.splice(0, this.selected.length);
    this.selected.push(...selected);

    this.idList = [];
    this.selected.forEach(element => {
      if(element.id != null) this.idList.push(element.id);
    });
  }
}
