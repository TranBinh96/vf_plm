import { Component, OnInit, Input, Output, EventEmitter, ViewChild } from '@angular/core';
import { FileUploadModel } from 'src/app/_models/common/FileUploadModel';
import { CommonHelper } from 'src/app/_helpers/common.helper';

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
})
export class UploadComponent implements OnInit {
  @ViewChild('fileInput') myVar1: any;
  @Input() multiple: boolean = false;
  @Input() cssClass: string;
  @Output() onFileChanged = new EventEmitter();

  fileCount = 0;
  fileName: string = '';
  id: string;
  constructor() {
    this.id = CommonHelper.generateRandomString(10);
  }

  ngOnInit() {

  }

  onFileChange(event) {
    console.log('choose image');
    const fileUploadModel = new FileUploadModel();
    if (event.target.files.length > 0) {
      fileUploadModel.fileSelected = event.target.files;
      // this.fileCount = fileUploadModel.fileSelected.length;
      if (!this.multiple){
        this.fileName = fileUploadModel.fileSelected[0].name;
        this.fileCount = 1;
      }
    } 
    else {
      fileUploadModel.errorCode = "01";
      fileUploadModel.message = "No file choose";
    }
    this.onFileChanged.emit(fileUploadModel);
    this.resetSelect();
  }

  clearFileChoose(){
    this.fileName = '';
    const fileUploadModel = new FileUploadModel();
    this.onFileChanged.emit(fileUploadModel);
  }

  setFileName(name: string){
    this.fileName = name;
    this.fileCount = 1;
  }

  resetSelect() {
    this.myVar1.nativeElement.value = '';
  }
}
