import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { JobCategoryListComponent } from './job-category-list/job-category-list.component';
import { JobMasterListComponent } from './job-master-list/job-master-list.component';
import { JobHistoryListComponent } from './job-history-list/job-history-list.component';

const routes: Routes = [
  {path:'job-category-list', component: JobCategoryListComponent},
  {path:'job-master-list', component: JobMasterListComponent},
  {path:'job-history-list', component: JobHistoryListComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TaskscheduleRoutingModule { }
