import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { NgxSpinnerService } from 'ngx-spinner';
import { DatapullerService } from 'src/app/_services/datapuller.service';
import { DataformService } from 'src/app/_services/dataform.service';

@Component({
  selector: 'app-order-multi-update',
  templateUrl: './order-multi-update.component.html',
  styleUrls: ['./order-multi-update.component.css']
})
export class OrderMultiUpdateComponent implements OnInit {
  @Input() bsModalRef: BsModalRef;
  @Input() idList;
  @Output() acceptBtnModalClick = new EventEmitter();

  dataItem: any = {
    idList: '',
    status: '',
  }

  dataForm: any = {
    typeList: [],
    statusList: [],
  }

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
    this.dataItem.idList = this.idList;
  }

  onLoadDataForm() {
    this.dataFormService.DataPullerList_GetDataForm().subscribe(x => {
      this.dataForm = x;
    });
  }

  acceptBtnModalClicked(){
    this.spinner.show();
    this.dataPullerService.DataPuller_MultiUpdate(this.dataItem).subscribe(x => {
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
