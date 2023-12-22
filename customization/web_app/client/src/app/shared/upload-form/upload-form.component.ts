import { Component, OnInit, Input } from '@angular/core';
import { DataformService } from 'src/app/_services/dataform.service';
import { FileUploadModel } from 'src/app/_models/common/FileUploadModel';
import { ToastrService } from 'ngx-toastr';
import { NgxSpinnerService } from 'ngx-spinner';
import { Router } from '@angular/router';
import { CommonHelper } from 'src/app/_helpers/common.helper';

@Component({
  selector: 'app-upload-form',
  templateUrl: './upload-form.component.html',
  styleUrls: ['./upload-form.component.css']
})
export class UploadFormComponent implements OnInit {
  @Input() functionName:string;

  constructor(
    private dataFormService: DataformService,
    private toastr: ToastrService,
    private spinner: NgxSpinnerService,
    private router: Router
  ) { 

  }

  dataFile: any = {
    dataList: [],
  }

  uploadImageData: any = {
    functionName: '',
    file: null,
  }

  ngOnInit() {
    this.loadFile();
  }

  loadFile() {
    this.dataFormService.GetListFile_ByFolder(this.functionName).subscribe(x => {
      this.dataFile = x;
    });
  }

  onCopyUrl(val: string){
    const selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = val;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
  }

  onFileUploadChange(fileUploadModel: FileUploadModel) {
    this.uploadImageData.functionName = this.functionName;
    this.uploadImageData.file = fileUploadModel.fileSelected[0];
    this.spinner.show();
    this.dataFormService.UploadFile(this.uploadImageData).subscribe(x => {
      this.spinner.hide();
      if (x.errorCode !== '00') {
        this.toastr.error(x.message);
      } else {
        this.toastr.success(x.message);
        this.loadFile();
      }
    });
  }
}
