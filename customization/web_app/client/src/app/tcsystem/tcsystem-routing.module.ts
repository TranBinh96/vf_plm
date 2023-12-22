import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { BmideListComponent } from './bmide-list/bmide-list.component';

const routes: Routes = [
  {path:'bmide-list', component: BmideListComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TcsystemRoutingModule { }
