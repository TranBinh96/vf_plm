import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UploadComponent } from './upload/upload.component';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ModalModule, TooltipModule, TabsModule, TypeaheadModule, BsDatepickerModule, BsDropdownModule } from 'ngx-bootstrap';
import { ToastrModule } from 'ngx-toastr';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
// import { MenuModule, PaginatorModule, ButtonModule } from 'primeng/primeng';
// import { TreeviewModule } from 'ngx-treeview';
import { FileSaverModule } from 'ngx-filesaver';
import { SweetAlert2Module } from '@sweetalert2/ngx-sweetalert2';
import { NgxSpinnerModule } from 'ngx-spinner';
// import { TableModule } from 'primeng/table';
// import { ContextMenuModule, ContextMenuComponent } from 'ngx-contextmenu';
import { SafeHtmlPipe } from '../_directives/safehtml.pipe';
import { UiSwitchModule } from 'ngx-ui-switch';
import { CKEditorModule } from 'ckeditor4-angular';
import { NgxBarcodeModule } from 'ngx-barcode';
// import { NgxGraphModule } from '@swimlane/ngx-graph';
import { UploadFormComponent } from './upload-form/upload-form.component';
import { WorkingCalendarComponent } from './working-calendar/working-calendar.component';
import { CurrentDocumentMarkComponent } from './current-document-mark/current-document-mark.component';
import { NgxSelectModule, INgxSelectOptions } from 'ngx-select-ex';

const CustomSelectOptions:INgxSelectOptions = {
  optionValueField: 'value',
  optionTextField: 'text',
  keepSelectedItems: true,
  allowClear: true,
};

@NgModule({
  declarations: [
    UploadComponent, 
    SafeHtmlPipe, 
    UploadFormComponent, 
    WorkingCalendarComponent, 
    CurrentDocumentMarkComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    ModalModule.forRoot(),
    ToastrModule.forRoot(),
    NgxDatatableModule,
    TooltipModule.forRoot(),
    // ContextMenuModule.forRoot(),
    TabsModule.forRoot(),
    TypeaheadModule.forRoot(),
    BsDatepickerModule.forRoot(),
    // TreeviewModule.forRoot(),
    FileSaverModule,
    SweetAlert2Module.forRoot(),
    NgxSpinnerModule,
    // TableModule,
    // MenuModule,
    // PaginatorModule,
    // ButtonModule,
    BsDropdownModule.forRoot(),
    UiSwitchModule.forRoot({ }),
    CKEditorModule,
    NgxBarcodeModule,
    // NgxGraphModule,
    NgxSelectModule.forRoot(CustomSelectOptions),
  ],
  exports: [
    CommonModule, 
    FormsModule,
    HttpClientModule,
    ModalModule,
    ToastrModule,
    NgxDatatableModule,
    TooltipModule,
    // ContextMenuModule,
    TabsModule,
    TypeaheadModule,
    BsDatepickerModule,
    // TreeviewModule,
    FileSaverModule,
    SweetAlert2Module,
    NgxSpinnerModule,
    // TableModule,
    // MenuModule,
    // PaginatorModule,
    // ButtonModule,
    BsDropdownModule,
    UploadComponent,
    SafeHtmlPipe,
    UiSwitchModule,
    CKEditorModule,
    NgxBarcodeModule,
    // NgxGraphModule,
    UploadFormComponent,
    WorkingCalendarComponent,
    CurrentDocumentMarkComponent,
    NgxSelectModule,
  ]
})
export class SharedModule { }
