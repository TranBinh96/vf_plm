import { AppconfigService } from './_services/appconfig.service';
import { NgModule, Injector, APP_INITIALIZER  } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { AppComponent } from './app.component';
import { SiteLayoutComponent } from './_layout/site-layout/site-layout.component';
import { SiteHeaderComponent } from './_layout/site-header/site-header.component';
import { TopNavComponent } from './_layout/top-nav/top-nav.component';
import { HomeComponent } from './home/home.component';
import { AppRoutingModule } from './app-routing.module';
import { LoginLayoutComponent } from './_layout/login-layout/login-layout.component';
import { ChangePasswordComponent } from './system/sys-acc/change-password/change-password.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NoPermissionComponent } from './_common-component/no-permission/no-permission.component';
import { PageNotFoundComponent } from './_common-component/page-not-found/page-not-found.component';
import { VarDirective } from './_helpers/ngVar.directive';
import { DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SharedModule } from './shared/shared.module';
import { SiteFooterComponent } from './_layout/site-footer/site-footer.component';
import { ChangepassLayoutComponent } from './_layout/changepass-layout/changepass-layout.component';

// import { CookieService } from 'ngx-cookie-service';
import { CookieService, CookieModule } from 'ngx-cookie';
export let InjectorInstance: Injector;

@NgModule({
  imports: [
    AppRoutingModule,
    BrowserModule,
    FormsModule,
    SharedModule,
    BrowserAnimationsModule,
    CookieModule.forRoot(),
  ],
  declarations: [
    // Common
    AppComponent,
    SiteLayoutComponent,
    SiteHeaderComponent,
    TopNavComponent,
    HomeComponent,
    NoPermissionComponent,
    PageNotFoundComponent,
    ChangePasswordComponent,
    LoginLayoutComponent,
    VarDirective,
    SiteFooterComponent,
    ChangepassLayoutComponent,
  ],
  entryComponents: [
    ChangePasswordComponent,
  ],
  providers: [
    DatePipe,
    {
      provide: APP_INITIALIZER,
      multi: true,
      deps: [AppconfigService],
      useFactory: (appConfigService: AppconfigService) => {
        return () => {
          //Make sure to return a promise!
          return appConfigService.loadAppConfig();
        };
      }
    }
    // CookieService,
  ],
  exports: [
    RouterModule,
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  constructor(private injector: Injector) {
    InjectorInstance = this.injector;
  }
}
