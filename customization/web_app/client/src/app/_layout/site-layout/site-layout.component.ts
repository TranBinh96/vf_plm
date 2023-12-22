import { Component, OnInit } from '@angular/core';
import { DynamicScriptLoaderService } from 'src/app/_services/loadscript.service';
import { CommonService } from 'src/app/_services/common.service';

@Component({
  selector: 'app-site-layout',
  templateUrl: './site-layout.component.html',
})
export class SiteLayoutComponent implements OnInit {

  constructor(private dynamicScriptLoader: DynamicScriptLoaderService) { }

  ngOnInit() {
    document.body.attributes.removeNamedItem('class');
    
    if(document.body.attributes.getNamedItem('style')){
      document.body.attributes.removeNamedItem('style');
    }
    
    document.body.classList.add('layout-top-nav');
    document.body.classList.add('skin-blue');
    //this.loadScripts();
  }

  private loadScripts() {
    // You can load multiple scripts by just providing the key as argument into load method of the service
    this.dynamicScriptLoader.load('template').then(data => {
      // Script Loaded Successfully
    }).catch(error => console.log(error));
  }
}
