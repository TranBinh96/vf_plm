import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TcsystemRoutingModule } from './tcsystem-routing.module';
import { BmideListComponent } from './bmide-list/bmide-list.component';
import { SharedModule } from '../shared/shared.module';
import { LovDetailComponent } from './lov-detail/lov-detail.component';

@NgModule({
  declarations: [BmideListComponent, LovDetailComponent],
  imports: [
    SharedModule,
    CommonModule,
    TcsystemRoutingModule
  ]
})
export class TcsystemModule { }
