import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { NgxSpinnerService } from 'ngx-spinner';
import { DatapullerService } from 'src/app/_services/datapuller.service';
import { DataformService } from 'src/app/_services/dataform.service';

@Component({
  selector: 'app-revision-rule-edit',
  templateUrl: './revision-rule-edit.component.html',
  styleUrls: ['./revision-rule-edit.component.css']
})
export class RevisionRuleEditComponent implements OnInit {
  @Input() bsModalRef: BsModalRef;
  @Input() id: string;
  @Output() acceptBtnModalClick = new EventEmitter();

  dataItem: any = {
    id: '',
    name: '',
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
      this.dataPullerService.RevisionRuleMaster_GetByID(this.id).subscribe(x => {
        this.dataItem = x;
      });
    }
  }

  onLoadDataForm() {

  }

  acceptBtnModalClicked(){
    this.spinner.show();
    this.dataPullerService.RevisionRuleMaster_Update(this.dataItem).subscribe(x => {
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
