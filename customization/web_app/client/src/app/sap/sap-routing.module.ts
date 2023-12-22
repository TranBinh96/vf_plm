import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SapMasterialMasterListComponent } from './sap-masterial-master-list/sap-masterial-master-list.component';

const routes: Routes = [
  {path:'sap-material-master-list', component: SapMasterialMasterListComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SapRoutingModule { }
