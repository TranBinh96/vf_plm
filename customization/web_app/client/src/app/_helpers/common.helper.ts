import { BaseModel } from '../_models/base/BaseModel';
import { HttpErrorResponse } from '@angular/common/http';
import { SelectItem } from '../_models/common/SelectItem';
import { UserConst } from '../_models/const/UserConst';
import { stringify } from 'querystring';
export class CommonHelper {

    public static getError(error: HttpErrorResponse): BaseModel {
        let baseModel: BaseModel;
        baseModel = new BaseModel();
        if (error.error instanceof ErrorEvent) {
            // A client-side or network error occurred. Handle it accordingly.
            // console.error('An error occurred:', error.error.message);
            baseModel.errorCode = '01';
            baseModel.message = error.error.message;
        } else {
            // The backend returned an unsuccessful response code.
            // The response body may contain clues as to what went wrong,
            // console.error(`Backend returned code ${error.status}, ` + `body was: ${error.error}`);
            switch (error.status) {
                case 400:
                    baseModel.errorCode = '01';
                    baseModel.message = error.error.error_description;
                    break;
                case 401:
                    baseModel.errorCode = '02';
                    localStorage.removeItem(UserConst.SS_USER_CLAIM);
                    localStorage.removeItem(UserConst.SS_USER_INFO);
                    break;
                default:
                    baseModel.errorCode = '01';
                    baseModel.message = error.message;
                    break;
            }

        }
        return baseModel;
    }

    public static getMonths(): SelectItem[] {
        let months: SelectItem[];
        months = [];
        let today = new Date();
        for (let index = 1; index < 12; index++) {
            let selectItem = new SelectItem();
            selectItem.text = index + '';
            selectItem.value = index + '';
            selectItem.selected = (today.getMonth() + 1) === index ? true : false;
            months.push(selectItem);
        }
        return months;
    }

    public static getYears(): SelectItem[] {
        let years: SelectItem[];
        years = [];
        let today = new Date();
        for (let index = 1980; index < 2050; index++) {
            let selectItem = new SelectItem();
            selectItem.text = index + '';
            selectItem.value = index + '';
            selectItem.selected = today.getFullYear() === index ? true : false;
            years.push(selectItem);
        }
        return years;
    }

    public static generateRandomString(string_length): string {
        let random_string = '';
        let random_ascii;
        for (let i = 0; i < string_length; i++) {
            random_ascii = Math.floor((Math.random() * 25) + 97);
            random_string += String.fromCharCode(random_ascii)
        }
        return random_string
    }

    public static getDateMath(value: string): Date | null {
        const splitDate = value.split('/');
        const day = Number(splitDate[0]);
        const month = Number(splitDate[1]) - 1;
        const year = Number(splitDate[2]);
        return new Date(year, month, day);

    }

    public static getDayName(date) {
        var days = ['CN', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];
        var dayName = days[date.getDay()];
        return dayName;
    }

    public static validDateMask(value: string, c: string = '/'): boolean {
        if (c == '' || c == null)
            return false;

        let splitDate = value.split(c);
        if (splitDate.length != 3)
            return false;

        if (splitDate[0].length != 2 || splitDate[1].length != 2 || splitDate[2].length != 4)
            return false;
        return true;
    }

    // public static makeRowsSameHeight() {
    //     setTimeout(() => {
    //         if ($('.ui-table-scrollable-wrapper').length) {
    //             let wrapper = $('.ui-table-scrollable-wrapper');
    //             wrapper.each(function () {
    //                 let w = $(this);
    //                 let frozen_rows: any = w.find('.ui-table-frozen-view tr');
    //                 let unfrozen_rows = w.find('.ui-table-unfrozen-view tr');
    //                 for (let i = 0; i < frozen_rows.length; i++) {
    //                     if (frozen_rows.eq(i).height() > unfrozen_rows.eq(i).height()) {
    //                         unfrozen_rows.eq(i).height(frozen_rows.eq(i).height());
    //                     } else {
    //                         frozen_rows.eq(i).height(unfrozen_rows.eq(i).height());
    //                     }
    //                 }
    //             });
    //         }
    //     });
    // }

    public static getPermissionKeys(keys: string[]): any {
        let dataReturn: any = {};
        const modelReturn = localStorage.getItem(UserConst.SS_USER_INFO);
        let extFuncs: string[] = [];
        if (modelReturn !== null) {

            const userSessionModel = JSON.parse(modelReturn);
            if (userSessionModel.isAdmin) {
                keys.forEach(element => {
                    dataReturn[element] = true;
                });
                return dataReturn;
            }

            extFuncs = userSessionModel.moreInfo.extFunc
        }

        keys.forEach(element => {
            const result = extFuncs.includes(element);
            if (result)
                dataReturn[element] = true;
            else
                dataReturn[element] = false;
        });

        return dataReturn;
    }
}
