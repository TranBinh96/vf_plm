import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { NgxSpinnerService } from 'ngx-spinner';
import { DatapullerService } from 'src/app/_services/datapuller.service';
import { DataformService } from 'src/app/_services/dataform.service';

@Component({
  selector: 'app-sap-masterial-master-edit',
  templateUrl: './sap-masterial-master-edit.component.html',
  styleUrls: ['./sap-masterial-master-edit.component.css']
})
export class SapMasterialMasterEditComponent implements OnInit {
  @Input() bsModalRef: BsModalRef;
  @Input() id: string;
  @Output() acceptBtnModalClick = new EventEmitter();

  dataItem: any = {
    id: '',
    type: '',
    uid: '',
    failed_counter: '',
    error_log: '',
    status: '',
  }

  dataForm: any = {
    typeList: [],
    statusList: [],
  }

  title: string = "New";
  buttonName: string = "ADD NEW";

  constructor(
    private dataPullerService:DatapullerService,
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
      this.dataPullerService.DataPuller_GetByID(this.id).subscribe(x => {
        this.dataItem = x;
      });
    }
  }

  onLoadDataForm() {
    this.dataFormService.DataPullerList_GetDataForm().subscribe(x => {
      this.dataForm = x;
    });
  }

  acceptBtnModalClicked(){
    this.spinner.show();
    if(this.id == null) {
      this.dataPullerService.DataPuller_Create(this.dataItem).subscribe(x => {
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
      this.dataPullerService.DataPuller_Edit(this.dataItem).subscribe(x => {
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
