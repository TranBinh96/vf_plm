import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DatapullerRoutingModule } from './datapuller-routing.module';
import { SharedModule } from '../shared/shared.module';
import { OrderListComponent } from './order-list/order-list.component';
import { OrderEditComponent } from './order-edit/order-edit.component';
import { BomVerifyMasterEditComponent } from './bom-verify-master-edit/bom-verify-master-edit.component';
import { BomVerifyMasterListComponent } from './bom-verify-master-list/bom-verify-master-list.component';
import { BomVerifyMasterMultiEditComponent } from './bom-verify-master-multi-edit/bom-verify-master-multi-edit.component';
import { EmailSenderListComponent } from './email-sender-list/email-sender-list.component';
import { EmailSenderEditComponent } from './email-sender-edit/email-sender-edit.component';
import { RevisionRuleListComponent } from './revision-rule-list/revision-rule-list.component';
import { RevisionRuleEditComponent } from './revision-rule-edit/revision-rule-edit.component';
import { OrderMultiUpdateComponent } from './order-multi-update/order-multi-update.component';
import { DataPullerEventListComponent } from './data-puller-event-list/data-puller-event-list.component';
import { DataPullerEventEditComponent } from './data-puller-event-edit/data-puller-event-edit.component';
import { TechnicianMapListComponent } from './technician-map-list/technician-map-list.component';
import { TechnicianMapEditComponent } from './technician-map-edit/technician-map-edit.component';
import { DataPullerSubScriptionListComponent } from './data-puller-sub-scription-list/data-puller-sub-scription-list.component';
import { DataPullerSubScriptionEditComponent } from './data-puller-sub-scription-edit/data-puller-sub-scription-edit.component';

@NgModule({
  declarations: [
    OrderListComponent,
    OrderEditComponent,
    BomVerifyMasterEditComponent,
    BomVerifyMasterListComponent,
    BomVerifyMasterMultiEditComponent,
    EmailSenderListComponent,
    EmailSenderEditComponent,
    RevisionRuleListComponent,
    RevisionRuleEditComponent,
    OrderMultiUpdateComponent,
    DataPullerEventListComponent,
    DataPullerEventEditComponent,
    TechnicianMapListComponent,
    TechnicianMapEditComponent,
    DataPullerSubScriptionListComponent,
    DataPullerSubScriptionEditComponent
  ],
  imports: [
    SharedModule,
    CommonModule,
    DatapullerRoutingModule
  ]
})
export class DatapullerModule { }
