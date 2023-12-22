import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { NgxSpinnerService } from 'ngx-spinner';
import { DatapullerService } from 'src/app/_services/datapuller.service';
import { DataformService } from 'src/app/_services/dataform.service';
import { TaskscheduleService } from 'src/app/_services/taskschedule.service';

@Component({
  selector: 'app-job-history-detail',
  templateUrl: './job-history-detail.component.html',
  styleUrls: ['./job-history-detail.component.css']
})
export class JobHistoryDetailComponent implements OnInit {
  @Input() bsModalRef: BsModalRef;
  @Input() id: string;

  dataItem: any = {
    id: "",
    job_uid: "",
    job_name: "",
    job_category_name: "",
    task_name: "",
    status: "",
    statusColor: "",
    message: "",
    remark: "",
    start_time: "",
    end_time: "",
  }

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
    this.taskScheduleService.JobHistory_GetByID(this.id).subscribe(x => {
      this.dataItem = x;
    });
  }

  onLoadDataForm() {

  }
}
