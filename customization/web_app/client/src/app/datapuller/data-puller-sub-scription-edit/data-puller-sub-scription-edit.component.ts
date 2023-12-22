import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { NgxSpinnerService } from 'ngx-spinner';
import { DatapullerService } from 'src/app/_services/datapuller.service';
import { DataformService } from 'src/app/_services/dataform.service';

@Component({
  selector: 'app-data-puller-sub-scription-edit',
  templateUrl: './data-puller-sub-scription-edit.component.html',
  styleUrls: ['./data-puller-sub-scription-edit.component.css']
})
export class DataPullerSubScriptionEditComponent implements OnInit {
  @Input() bsModalRef: BsModalRef;
  @Input() id: string;
  @Output() acceptBtnModalClick = new EventEmitter();

  dataItem: any = {
    id: '',
    tc_event_type: '',
    object_type: '',
    event_id: null,
  }

  dataForm: any = {
    eventList: [],
    tcEventTypeList: [],
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
      this.dataPullerService.DataPullerSubscriptionHandler_GetByID(this.id).subscribe(x => {
        this.dataItem = x;
        this.dataItem.event_id = String(this.dataItem.event_id);
      });
    }
  }

  onLoadDataForm() {
    this.dataFormService.DataPullerSubscriptionList_GetDataForm().subscribe(x => {
      this.dataForm = x;
    });
  }

  acceptBtnModalClicked(){
    this.spinner.show();
    this.dataPullerService.DataPullerSubscriptionHandler_Update(this.dataItem).subscribe(x => {
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

  onChangeType(event){
    this.dataItem.event_id = event[0].value;
  }
}
