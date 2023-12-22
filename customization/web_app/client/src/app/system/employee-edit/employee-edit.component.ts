import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { NgxSpinnerService } from 'ngx-spinner';
import { DatapullerService } from 'src/app/_services/datapuller.service';
import { DataformService } from 'src/app/_services/dataform.service';
import { SystemService } from 'src/app/_services/system.service';

@Component({
  selector: 'app-employee-edit',
  templateUrl: './employee-edit.component.html',
  styleUrls: ['./employee-edit.component.css']
})
export class EmployeeEditComponent implements OnInit {
  @Input() bsModalRef: BsModalRef;
  @Input() code: string;
  @Output() acceptBtnModalClick = new EventEmitter();

  dataItem: any = {
    employee_code: '',
    employee_name: '',
    email: '',
    phone_number: '',
    service_desk_id: '',
    service_desk_name: '',
    remark: '',
  }

  title: string = "New";
  buttonName: string = "ADD NEW";

  constructor(
    private systemService:SystemService,
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
    if(this.code != null) {
      this.title = "Update";
      this.buttonName = "UPDATE";
      this.systemService.Employee_GetByCode(this.code).subscribe(x => {
        this.dataItem = x;
      });
    }
  }

  onLoadDataForm() {

  }

  acceptBtnModalClicked(){
    this.spinner.show();
    if(this.code == null) {
      this.systemService.employee_Create(this.dataItem).subscribe(x => {
        this.spinner.hide();
        if (x.errorCode !== '00') {
          this.toastr.error(x.message);
        } else {
          this.toastr.success(x.message);
          this.acceptBtnModalClick.emit(x);
          this.bsModalRef.hide();
        }
      });
    } else {
      this.systemService.Employee_Update(this.dataItem).subscribe(x => {
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
  }

  onChangeStatus(event){
    this.dataItem.status = event[0].value;
  }

  onChangeType(event){
    this.dataItem.type = event[0].value;
  }
}
