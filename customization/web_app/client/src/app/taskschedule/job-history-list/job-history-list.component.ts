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
import { TaskscheduleService } from 'src/app/_services/taskschedule.service';

@Component({
  selector: 'app-job-history-list',
  templateUrl: './job-history-list.component.html',
  styleUrls: ['./job-history-list.component.css']
})
export class JobHistoryListComponent implements OnInit {
  id: string;
  bsModalRefDetail: BsModalRef;

  userInfo:UserInfoModel;
  loadingIndicator: boolean = false;

  hasChanged:boolean = false;

  searchData: any = {
    JobName: '',
    JobCategoryID: '',
    Status: '',
    StartFromDate: '',
    StartToDate: '',
    SortColumn: '',
    SortColumnDir: '',
    PageIndex: 1,
    ItemsPerPage: 25,
  }

  dataForm: any = {
    jobCategoryList: [],
    statusList: [],
  }

  rowPerPage:any = [15, 25, 50];
  pagingModel = new BasePagingModel();

  constructor(
    private taskscheduleService:TaskscheduleService,
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

  onInit(){
    
  }

  ngOnInit() {
    this.innerHeight = window.innerHeight - 175;
    this.loadDataForm();
  }

  loadData() {
    this.loadingIndicator = true;
    this.taskscheduleService.JobHistory_GetAll(this.searchData).subscribe(x => {
      this.loadingIndicator = false;
      this.pagingModel = x;
    });
  }

  loadDataForm() {
    this.dataFormService.JobHistoryList_GetDataForm().subscribe(x => {
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

  openPopupDetail(template: TemplateRef<any>, id: any = null) {
    this.id = id;
    this.commonService.changeDetection = this.changeDetection;
    this.bsModalRefDetail = this.commonService.openModal(template, null, 'modal-lg');
    this.commonService.bsmodalService.onHide.subscribe((reason: string) => {
      if(this.hasChanged){
        this.loadData();
        this.hasChanged = false;
      }
    });
  }

  openPopupSync(template: TemplateRef<any>) {
    const swalDelete = swal.mixin({
      customClass: {
        confirmButton: 'btn btn-success',
        cancelButton: 'btn btn-default'
      },
      buttonsStyling: false
    })
    swalDelete.fire({
      title: 'Are you sure want to sync?',
      text: 'Sync!',
      type: 'success',
      showCancelButton: true,
      cancelButtonText: 'Cancel',
      confirmButtonText: 'Sync',
      reverseButtons: true
    }).then((result) => {
      if (result.value) {
        this.spinner.show();
        this.taskscheduleService.JobMaster_SyncTask().subscribe(x => {
          this.spinner.hide();
          if (x.errorCode !== '00') {
            this.toastr.error(x.message);
          } else {
            swalDelete.fire(
              'Sync!',
              'Your job has been synced.',
              'success'
            )
            this.loadData();
          }
        });
      }
    })
  }

  openPopupRunTask(taskName: String) {
    const swalDelete = swal.mixin({
      customClass: {
        confirmButton: 'btn btn-success',
        cancelButton: 'btn btn-default'
      },
      buttonsStyling: false
    })
    swalDelete.fire({
      title: 'Are you sure want to run this job?',
      text: 'Run',
      type: 'success',
      showCancelButton: true,
      cancelButtonText: 'Cancel',
      confirmButtonText: 'Run',
      reverseButtons: true
    }).then((result) => {
      if (result.value) {
        this.spinner.show();
        this.taskscheduleService.TaskSchedule_Run(taskName).subscribe(x => {
          this.spinner.hide();
          if (x.errorCode !== '00') {
            this.toastr.error(x.message);
          } else {
            swalDelete.fire(
              'Sync!',
              'Your job is running.',
              'success'
            )
            this.loadData();
          }
        });
      }
    })
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

  onChangeStatus(event){
    if(event.length > 0) this.searchData.Status = event[0].value;
    else this.searchData.Status = "";
    this.loadData();
  }

  onChangeJobCategory(event){
    if(event.length > 0) this.searchData.JobCategoryID = event[0].value;
    else this.searchData.JobCategoryID = "";
    this.loadData();
  }
}
