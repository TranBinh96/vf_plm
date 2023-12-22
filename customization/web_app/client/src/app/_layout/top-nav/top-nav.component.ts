import { Router, NavigationEnd } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { UserInfoModel } from 'src/app/_models/user/UserSessionModel';
import { UserService } from 'src/app/_services/user.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-top-nav',
  templateUrl: './top-nav.component.html',
})
export class TopNavComponent implements OnInit {
  userInfo = new UserInfoModel();

  homeActive: boolean;  
  ordersActive: boolean;
  expensesActive: boolean;
  vendorsActive: boolean;
  productsActive: boolean;
  reportActive: boolean;
  settingActive: boolean;
  importexportActive: boolean;

  constructor
  (
    private userService: UserService,
    private router: Router
  ) { 
    router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe((event: NavigationEnd) => {
      this.updateMenu(event.url);
    });
  }

  ngOnInit() {
    this.userInfo = this.userService.getUserInfo();
  }

  initMenuState(){
    this.homeActive = false;
    this.ordersActive = false;
    this.vendorsActive = false;
    this.productsActive = false;
    this.reportActive = false;
    this.settingActive = false;
    this.expensesActive = false;
    this.importexportActive = false;
  }

  updateMenu(url){
    var res = url.split("/");
    if(res.length >= 4) url = "/" + res[1] + "/" + res[2];
    this.initMenuState();
    switch(url){
      case "/order/po-list":
        case "/order/po-edit":
          case "/order/po-detail":
            case "/order/pr-list":
              case "/order/pr-edit":
                case "/order/pr-detail":
                  case "/order/roq-list":
                    case "/order/po-detail-list":
                      case "/order/pr-detail-list":
                        case "/order/pr-check":
                          case "/order/po-print":
                            case "/order/pr-print":
        this.ordersActive = true; break;
      case "/expenses/expenses-list":
        this.expensesActive = true; break;
      case "/order/invoice-list":
        case "/order/invoice-item-list":
          case "/order/er-list":
            case "/order/eo-list":
              case "/order/er-check":
                case "/order/eo-list":
                  case "/order/eo-item-list":
                    case "/order/er-print":
        this.importexportActive = true; break;
      case "/supplier/supplier-list":
        case "/supplier/supplier-edit":
          case "/supplier/supplier-detail":
            case "/supplier/contact-list":
              case "/supplier/suppliermistake-list":
                case "/supplier/contract-pri-list":
                  case "/supplier/supplier-check-list":
        this.vendorsActive = true; break;
      case "/product/group-list":
        case "/product/item-list":
        this.productsActive = true; break;
      case "/setting/unit-list":
        case "/setting/paymentterm-list":
          case "/supplier/mistaketype-list":
            case "/supplier/tag-list":
        this.settingActive = true; break;
      case "/report/report-list":
        case "/report/report-by-category":
        this.reportActive = true; break;
      default:
        this.homeActive = true; break;
    }
  }

}
