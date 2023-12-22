import { Routes, RouterModule } from '@angular/router';
import { SiteLayoutComponent } from './_layout/site-layout/site-layout.component';
import { HomeComponent } from './home/home.component';
import { LoginLayoutComponent } from './_layout/login-layout/login-layout.component';
import { Auth } from './_helpers/auth';
import { NoPermissionComponent } from './_common-component/no-permission/no-permission.component';
import { PageNotFoundComponent } from './_common-component/page-not-found/page-not-found.component';
import { NgModule } from '@angular/core';


const routes: Routes = [
    // main layout
    {
        path: '',
        component: SiteLayoutComponent,
        children: [
            { path: '', component: HomeComponent, pathMatch: 'full', canActivate: [Auth]},
            { path: 'home/:id', component: HomeComponent, pathMatch: 'full', canActivate: [Auth]},
            { path: 'khong-co-quyen-truy-cap', component: NoPermissionComponent },
            // { path: 'page-not-found', component: PageNotFoundComponent },
            { path: 'system', loadChildren: './system/system.module#SystemModule'},
            { path: 'datapuller', loadChildren: './datapuller/datapuller.module#DatapullerModule', canActivate: [Auth]},
            { path: 'tcsystem', loadChildren: './tcsystem/tcsystem.module#TcsystemModule', canActivate: [Auth]},
            { path: 'taskschedule', loadChildren: './taskschedule/taskschedule.module#TaskscheduleModule', canActivate: [Auth]},
            { path: 'sap', loadChildren: './sap/sap.module#SapModule', canActivate: [Auth]},
        ]
    },
    // Login layout
    {
        path: 'login',
        component: LoginLayoutComponent
    },
    // otherwise redirect to home
    { path: '**', redirectTo: 'page-not-found' },
    { path: 'aaa', component: PageNotFoundComponent }
];
@NgModule({
    imports: [
        RouterModule.forRoot(routes, { scrollPositionRestoration:'enabled'})
    ],
    exports: [
        RouterModule
    ]
})
export class AppRoutingModule { }
