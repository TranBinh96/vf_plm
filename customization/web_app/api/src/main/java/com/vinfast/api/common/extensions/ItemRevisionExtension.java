package com.vinfast.api.common.extensions;

import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.connect.client.AppXSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.teamcenter.soa.client.Connection;

public class ItemRevisionExtension {
    private static final Logger LOGGER = LogManager.getLogger(ItemRevisionExtension.class);

    public static ModelObject getLatestRevision(Item item, Connection conn) throws NotLoadedException {
        TCCommonExtension.loadObject(new String[]{item.getUid()}, conn);
        ModelObject[] itemRevs = item.get_revision_list();
        DataManagementService.getService(AppXSession.getConnection()).getProperties(itemRevs, new String[]{"item_revision_id"});
        ModelObject latestRev = null;
        int maxRevNum = 1;
        for (int i = 0; i < itemRevs.length; i++) {
            if (!itemRevs[i].getPropertyDisplayableValue("item_revision_id").contains(".")) {
                int revNum = 1;
                try {
                    revNum = Integer.valueOf(itemRevs[i].getPropertyDisplayableValue("item_revision_id"));
                } catch (Exception ex) {
                    latestRev = itemRevs[i];
                }
                if (revNum >= maxRevNum) {
                    maxRevNum = revNum;
                    latestRev = itemRevs[i];
                }
            }
        }
        return latestRev;
    }
}
