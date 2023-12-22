import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { NgxSpinnerService } from 'ngx-spinner';
import { DatapullerService } from 'src/app/_services/datapuller.service';
import { DataformService } from 'src/app/_services/dataform.service';
import { element } from 'protractor';

@Component({
  selector: 'app-technician-map-edit',
  templateUrl: './technician-map-edit.component.html',
  styleUrls: ['./technician-map-edit.component.css']
})
export class TechnicianMapEditComponent implements OnInit {
  @Input() bsModalRef: BsModalRef;
  @Input() id: string;
  @Output() acceptBtnModalClick = new EventEmitter();

  dataItem: any = {
    id: '',
    employee_code: '',
    key_word: '',
    key_word_list: [],
    is_active: false,
  }

  KeyWordList: any = [];

  dataForm: any = {
    employeeList: [],
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
      this.dataPullerService.TechnicianMap_GetByID(this.id).subscribe(x => {
        this.dataItem = x;
        var rowIndex = 0;
        if(this.dataItem.key_word_list != null) {
          this.dataItem.key_word_list.forEach(element => {
            this.KeyWordList.push({
              RowIndex: rowIndex + 1,
              KeyWord: element,
            });
          });
        }
      });
    }
  }

  onLoadDataForm() {
    this.dataFormService.TechnicianMapUpdate_GetDataForm().subscribe(x => {
      this.dataForm = x;
    });
  }

  acceptBtnModalClicked(){
    this.spinner.show();
    this.dataItem.key_word_list = [];
    this.KeyWordList.forEach(element => {
      if (element.KeyWord != "") {
        this.dataItem.key_word_list.push(element.KeyWord);
      }
    });
    this.dataPullerService.TechnicianMap_Update(this.dataItem).subscribe(x => {
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

  onChangeEmployee(event){
    this.dataItem.employee_code = event[0].value;
  }

  onChangeActive() {
    this.dataItem.is_active = !this.dataItem.is_active;
  }

  onAddKeyWord() {
    this.KeyWordList.push({
      RowIndex: this.KeyWordList.length + 1,
      Email: "",
    });
  }

  onRemoveKeyWord(id) {
    var itemIndex = this.KeyWordList.findIndex(obj => obj.RowIndex == id);
    if (itemIndex > -1) {
      this.KeyWordList.splice(itemIndex, 1);
    }
    var rowIndex = 0;
    this.KeyWordList.forEach(element => {
      rowIndex++;
      element.RowIndex = rowIndex;
    });
  }
}
