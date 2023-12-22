export class UserClaimModel {
    accessToken: string;
}

export class UserInfoModel {
    fullname: string;
    code: string;
    avatar: string;
    sectionID: string;
    groupID: string;
    positionID: string;
}

export class UserSessionModel {
    fullName: string;
    avatar: string;
    moreInfo: any;
    isAdmin: boolean;
    actionUrls: string[];
    constructor() {
        this.actionUrls = [];
    }

    zipActionUrl() {
        this.moreInfo.mnuModel.forEach(parent => {
            parent.childMenu1.forEach(child => {
                this.actionUrls.push(child.actionUrl);
            });

            parent.childMenu2.forEach(child => {
                this.actionUrls.push(child.actionUrl);
            });

            parent.childMenu3.forEach(child => {
                this.actionUrls.push(child.actionUrl);
            });

            parent.childMenu4.forEach(child => {
                this.actionUrls.push(child.actionUrl);
            });
        });

    }
}

export class ExtFuncPermission {
    extFunCode: string;
    hasPermission: boolean = false;
}