import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TaskscheduleRoutingModule } from './taskschedule-routing.module';
import { JobCategoryListComponent } from './job-category-list/job-category-list.component';
import { JobCategoryEditComponent } from './job-category-edit/job-category-edit.component';
import { JobHistoryDetailComponent } from './job-history-detail/job-history-detail.component';
import { JobMasterEditComponent } from './job-master-edit/job-master-edit.component';
import { JobMasterListComponent } from './job-master-list/job-master-list.component';
import { TaskDetailComponent } from './task-detail/task-detail.component';
import { SharedModule } from '../shared/shared.module';
import { JobHistoryListComponent } from './job-history-list/job-history-list.component';

@NgModule({
  declarations: [JobCategoryListComponent, JobCategoryEditComponent, JobHistoryDetailComponent, JobMasterEditComponent, JobMasterListComponent, TaskDetailComponent, JobHistoryListComponent],
  imports: [
    SharedModule,
    CommonModule,
    TaskscheduleRoutingModule
  ]
})
export class TaskscheduleModule { }
