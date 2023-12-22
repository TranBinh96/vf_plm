import { NgModule } from '@angular/core';
import { SystemRoutingModule } from './system-routing.module';
import { SharedModule } from '../shared/shared.module';
import { EmployeeListComponent } from './employee-list/employee-list.component';
import { EmployeeEditComponent } from './employee-edit/employee-edit.component';

@NgModule({
  declarations: [
  EmployeeListComponent,
  EmployeeEditComponent],
  imports: [
    SharedModule,
    SystemRoutingModule
    
  ],
  exports: [

  ],
})
export class SystemModule { }
