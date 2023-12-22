import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../shared/shared.module';
import { SapRoutingModule } from './sap-routing.module';
import { SapMasterialMasterListComponent } from './sap-masterial-master-list/sap-masterial-master-list.component';
import { SapMasterialMasterEditComponent } from './sap-masterial-master-edit/sap-masterial-master-edit.component';

@NgModule({
  declarations: [SapMasterialMasterListComponent, SapMasterialMasterEditComponent],
  imports: [
    SharedModule,
    CommonModule,
    SapRoutingModule
  ]
})
export class SapModule { }
