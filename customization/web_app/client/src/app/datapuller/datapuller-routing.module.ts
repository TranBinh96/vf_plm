import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { BomVerifyMasterListComponent } from './bom-verify-master-list/bom-verify-master-list.component';
import { EmailSenderListComponent } from './email-sender-list/email-sender-list.component';
import { OrderListComponent } from './order-list/order-list.component';
import { RevisionRuleListComponent } from './revision-rule-list/revision-rule-list.component';
import { DataPullerEventListComponent } from './data-puller-event-list/data-puller-event-list.component';
import { TechnicianMapListComponent } from './technician-map-list/technician-map-list.component';
import { DataPullerSubScriptionListComponent } from './data-puller-sub-scription-list/data-puller-sub-scription-list.component';

const routes: Routes = [
  {path:'datapuller-list', component: OrderListComponent},
  {path:'datapuller-event-list', component: DataPullerEventListComponent},
  {path:'datapuller-subscription-list', component: DataPullerSubScriptionListComponent},
  {path:'bomVerify-list', component: BomVerifyMasterListComponent},
  {path:'emailSender-list', component: EmailSenderListComponent},
  {path:'revisionRule-list', component: RevisionRuleListComponent},
  {path:'technician-map-list', component: TechnicianMapListComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DatapullerRoutingModule { }
