import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { NgxSpinnerService } from 'ngx-spinner';
import { DatapullerService } from 'src/app/_services/datapuller.service';
import { DataformService } from 'src/app/_services/dataform.service';
import { TaskscheduleService } from 'src/app/_services/taskschedule.service';

@Component({
  selector: 'app-job-master-edit',
  templateUrl: './job-master-edit.component.html',
  styleUrls: ['./job-master-edit.component.css']
})
export class JobMasterEditComponent implements OnInit {
  @Input() bsModalRef: BsModalRef;
  @Input() id: string;
  @Output() acceptBtnModalClick = new EventEmitter();

  dataItem: any = {
    uid: '',
    job_name: '',
    job_category_id: '',
    task_name: '',
    remark: '',
    fileList: [],
  }

  dataForm: any = {
    jobCategoryList: [],
  }

  title: string = "New";
  buttonName: string = "ADD NEW";

  constructor(
    private taskScheduleService:TaskscheduleService,
    private dataFormService:DataformService, 
    private toastr: ToastrService,
    private spinner: NgxSpinnerService,
  ) { 
    
  }

  ngOnInit() {
    this.onLoadDataForm();
    this.onLoadData();
  }

  onLoadData() {
    if(this.id != null) {
      this.title = "Update";
      this.buttonName = "UPDATE";
      this.taskScheduleService.JobMaster_GetByID(this.id).subscribe(x => {
        this.dataItem = x;
        this.dataItem.job_category_id = String(this.dataItem.job_category_id);
      });
    }
  }

  onLoadDataForm() {
    this.dataFormService.JobMasterList_GetDataForm().subscribe(x => {
      this.dataForm = x;
    });
  }

  acceptBtnModalClicked(){
    this.spinner.show();
    this.taskScheduleService.JobMaster_Update(this.dataItem).subscribe(x => {
      this.spinner.hide();
      if (x.errorCode !== '00') {
        this.toastr.error(x.message);
      } else {
        this.toastr.success(x.message);
        this.acceptBtnModalClick.emit(x);
        this.bsModalRef.hide();
      }
    });
  }

  onChangeStatus(event){
    this.dataItem.status = event[0].value;
  }

  onChangeType(event){
    this.dataItem.type = event[0].value;
  }
}
