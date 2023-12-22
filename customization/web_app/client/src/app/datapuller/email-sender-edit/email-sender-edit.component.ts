import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { NgxSpinnerService } from 'ngx-spinner';
import { DatapullerService } from 'src/app/_services/datapuller.service';
import { DataformService } from 'src/app/_services/dataform.service';

@Component({
  selector: 'app-email-sender-edit',
  templateUrl: './email-sender-edit.component.html',
  styleUrls: ['./email-sender-edit.component.css']
})
export class EmailSenderEditComponent implements OnInit {
  @Input() bsModalRef: BsModalRef;
  @Input() id: string;
  @Output() acceptBtnModalClick = new EventEmitter();

  dataItem: any = {
    id: '',
    email_to: '',
    message: '',
    status: '',
  }

  EmailList: any = [];

  dataForm: any = {
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
    this.dataPullerService.EmailSender_GetByID(this.id).subscribe(x => {
      this.dataItem = x;
      var rowIndex = 0;
      if(this.dataItem.email_to != "") {
        if(this.dataItem.email_to.includes(";")) {
          this.dataItem.email_to.split(";").forEach(element => {
            rowIndex++;
            this.EmailList.push({
              RowIndex: rowIndex + 1,
              Email: element,
            });
          });
        } else {
          this.EmailList.push({
            RowIndex: rowIndex + 1,
            Email: this.dataItem.email_to,
          });
        }
      }
    });
  }

  onLoadDataForm() {
    this.dataFormService.EmailSenderList_GetDataForm().subscribe(x => {
      this.dataForm = x;
    });
  }

  acceptBtnModalClicked(){
    this.spinner.show();
    let email = "";
    this.EmailList.forEach(element => {
      if (element.Email != "") {
        email += element.Email + ";";
      }
    });
    this.dataItem.email_to = email;
    
    this.dataPullerService.EmailSender_Update(this.dataItem).subscribe(x => {
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

  onChangeStatus(event){
    this.dataItem.status = event[0].value;
  }
}
