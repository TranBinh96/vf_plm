import { Injectable, ChangeDetectorRef } from '@angular/core';
import { BsModalRef, BsModalService, ModalOptions } from 'ngx-bootstrap';
import { Subscription, combineLatest } from 'rxjs';
import * as XLSX from 'xlsx';
import * as FileSaver from 'file-saver';

@Injectable({
    providedIn: 'root'
})
export class CommonService {
    bsModalRef: BsModalRef =null;
    subscriptions: Subscription[] = [];
    bsmodalService: BsModalService;
    changeDetection: ChangeDetectorRef;
    constructor(private modalService: BsModalService) {

    }

    /**
     * Mở component modal
     * @param component Component chứa nội dung của modal template bao gồm cả header và body
     * @param modalData dữ liệu truyền đến modal để bên component nhận được(dùng khi chuyển dữ liệu sang component popup)
     * @param cssClass class css muốn đặt thêm vào ví dụ như: modal-lg full-modal để hiển thị modal full màn hình
     */
    openModal(component: any, modalData: any = null, cssClass: string = '', hideEvent = false) {
        if (hideEvent) {
            const _combine = combineLatest(
                this.modalService.onHide
            ).subscribe(() => this.changeDetection.markForCheck());
            this.subscriptions.push(_combine);
        }
        this.bsModalRef = this.modalService.show(component, {
            backdrop: 'static',
            keyboard: false,
            ignoreBackdropClick: true,
            class: cssClass,
            initialState: { modalData }
        });
        // this.bsModalRef.content.data = data;
        this.bsmodalService = this.modalService;
        return this.bsModalRef;
    }

    fileType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8';
    fileExtension = '.xlsx';

    public exportExcel(jsonData: any[], fileName: string) {
        const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(jsonData);
        const wb: XLSX.WorkBook = { Sheets: { 'data': ws }, SheetNames: ['data'] };
        const excelBuffer: any = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
        this.saveExcelFile(excelBuffer, fileName);
    }

    private saveExcelFile(buffer: any, fileName: string): void {
        const data: Blob = new Blob([buffer], {type: this.fileType});
        FileSaver.saveAs(data, fileName + this.fileExtension);
    }
}
