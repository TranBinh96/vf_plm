import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { NgxSpinnerService } from 'ngx-spinner';
import { DataformService } from 'src/app/_services/dataform.service';
import { TcsystemService } from 'src/app/_services/tcsystem.service';
import { forEach } from 'lodash';

@Component({
  selector: 'app-lov-detail',
  templateUrl: './lov-detail.component.html',
  styleUrls: ['./lov-detail.component.css']
})
export class LovDetailComponent implements OnInit {
  @Input() bsModalRef: BsModalRef;
  @Input() objectName: string;
  @Input() propertyName: string;

  dataItem: any = {
    objectName: '',
    propertyName: '',
  }

  constructor(
    private tcSystemService:TcsystemService,
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
    this.tcSystemService.LovValue_GetList(this.objectName, this.propertyName).subscribe(x => {
      this.dataItem = x;
    });
  }

  onLoadDataForm() {

  }
}
