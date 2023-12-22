import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { NgxSpinnerService } from 'ngx-spinner';
import { DatapullerService } from 'src/app/_services/datapuller.service';
import { DataformService } from 'src/app/_services/dataform.service';

@Component({
  selector: 'app-bom-verify-master-multi-edit',
  templateUrl: './bom-verify-master-multi-edit.component.html',
  styleUrls: ['./bom-verify-master-multi-edit.component.css']
})
export class BomVerifyMasterMultiEditComponent implements OnInit {
  @Input() bsModalRef: BsModalRef;
  @Input() idList;
  @Input() typeUpdate: String;
  @Output() acceptBtnModalClick = new EventEmitter();

  actionMultiForm:any = {
    idList:[],
    emailList:[],
    typeUpdate:'',
  }

  EmailList: any = [];

  title: string = 'Remove email address';

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
    this.actionMultiForm.idList = this.idList;
    this.actionMultiForm.typeUpdate = this.typeUpdate;
    if(this.typeUpdate == 'ADD') {
      this.title = 'Add email address';
    }
  }

  onLoadDataForm() {
    
  }

  acceptBtnModalClicked(){
    this.spinner.show();
    this.EmailList.forEach(element => {
      if (element.Email != "") {
        this.actionMultiForm.emailList.push(element.Email);
      }
    });
    
    this.dataPullerService.BomVerifyMaster_MultiUpdate(this.actionMultiForm).subscribe(x => {
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

  onAddEmail() {
    this.EmailList.push({
      RowIndex: this.EmailList.length + 1,
      Email: "",
    });
  }

  onRemoveEmail(id) {
    var itemIndex = this.EmailList.findIndex(obj => obj.RowIndex == id);
    if (itemIndex > -1) {
      this.EmailList.splice(itemIndex, 1);
    }
    var rowIndex = 0;
    this.EmailList.forEach(element => {
      rowIndex++;
      element.RowIndex = rowIndex;
    });
  }
}
