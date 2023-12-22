/*
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
 */

#include <unidefs.h>
#if defined(SUN)
#include <unistd.h>
#endif

#include <vf_custom.hxx>
#include <reportdatasource2010impl.hxx>

using namespace VF4::Soa::Custom::_2020_10;
using namespace Teamcenter::Soa::Server;

ReportDataSourceImpl::VFEcrCostResponse ReportDataSourceImpl::getEcrsCost(
		const std::vector<BusinessObjectRef<Teamcenter::BusinessObject> >& ecrs) {

	TC_write_syslog("\n[VF] Enter %s", __FUNCTION__);
	TC_write_syslog("\n[VF] ECRs num=%d", ecrs.size());
	ReportDataSourceImpl::VFEcrCostResponse res;

	for (int i = 0; i < ecrs.size(); i++) {
		BusinessObjectRef<Teamcenter::BusinessObject> ecrRef = ecrs[i];
		tag_t ecr = ecrRef->getTag();
		char *ecrObjString = NULL;
		ERROR_CHECK(AOM_ask_value_string(ecr, "object_string", &ecrObjString));
		TC_write_syslog("\n[VF] start processing ecr[%d]=\"%s\"", i,
				ecrObjString);
		MEM_free(ecrObjString);
		ecrObjString = NULL;

		char *sourcingProgram = NULL;
		ERROR_CHECK(
				AOM_ask_value_string(ecr, "vf6_vehicle_group", &sourcingProgram));
		string stProgram(sourcingProgram);
		if (tc_strcasecmp("C-SUV", sourcingProgram) == 0)
			stProgram = "C-CUV";
		if (tc_strcasecmp("EScooter", sourcingProgram) == 0)
			stProgram = "Escooter";
		if (sourcingProgram)
			MEM_free(sourcingProgram);
		sourcingProgram = NULL;
		// TODO: add battery handling case.
		TC_write_syslog("\n[VF] sourcing program=%s", stProgram.c_str());

		std::vector<tag_t> costForms;
		std::vector<int> isNulls;

		tag_t costImpactsRel;
		tag_t *costFormsTag = NULL;
		int costFormsCount = 0;
		ERROR_CHECK(
				TCTYPE_find_type("Vf6_change_forms", "Vf6_change_forms", &costImpactsRel));
		ERROR_CHECK(
				GRM_list_secondary_objects_only(ecr, costImpactsRel, &costFormsCount, &costFormsTag));
		for (int k = 0; k < costFormsCount; k++) {
			costForms.push_back(costFormsTag[k]);
		}

		//ecrRef->getTagArray("VF4_Cost_Impact", costForms, isNulls);

		std::vector<tag_t> modifiedCostForms;
		std::vector<std::string> partIds;
		std::set<std::string> partIdsSet;
		map<string, vector<tag_t>> partIdsAndCostFormList;

		TC_write_syslog("\n[VF] getting part IDs from cost forms %d",
				costForms.size());

		for (int j = 0; j < costForms.size(); j++) {
			tag_t costForm = costForms[j];
			date_t creationDate, lastModDate;
			int answer;
			ERROR_CHECK(
					AOM_ask_value_date(costForm, "creation_date", &creationDate));
			ERROR_CHECK(
					AOM_ask_value_date(costForm, "last_mod_date", &lastModDate));
			ERROR_CHECK(POM_compare_dates(lastModDate, creationDate, &answer));
			TC_write_syslog("\n[VF] answer=%d", answer);
			//if (answer > 0) {
			char *costFormName = NULL;
			string partID;
			ERROR_CHECK(
					AOM_ask_value_string(costForm, "object_name", &costFormName));

			std::vector<std::string> words;
			vf_split(costFormName, " - ", words);
			if (words.size() > 1) {
				partID = words[1];
				words.clear();
				vf_split(partID, "/", words);
				if (words.size() > 0)
					partID = words[0];
			}

			if (partID.size() > 2) {
				modifiedCostForms.push_back(costForm);
				partIdsSet.insert(partID);

				vector<tag_t> costFormList;
				if (partIdsAndCostFormList.find(partID)
						!= partIdsAndCostFormList.end()) {
					costFormList = partIdsAndCostFormList[partID];
				}

				costFormList.push_back(costForm);
				partIdsAndCostFormList[partID] = costFormList;
			}

			if (costFormName)
				MEM_free(costFormName);
			//}
		}
		partIds.insert(partIds.end(), partIdsSet.begin(), partIdsSet.end());
		TC_write_syslog("\n[VF] part IDs %d", partIds.size());

		TC_write_syslog("\n[VF] Create map");

		map<string, vector<string>> oldToNewParts;
		map<string, vector<string>> newToOldParts;
		createMaps(partIds, oldToNewParts, newToOldParts);
		TC_write_syslog("\n[VF] Created map %d, %d", oldToNewParts.size(),
				newToOldParts.size());

		vector<VFPartChange> changes;
		map<string, int> donePartIDs;

		TC_write_syslog("\n[VF] Loop part ids %d", partIds.size());
		for (string partID : partIds) {
			if (donePartIDs.find(partID) != donePartIDs.end()
					|| (newToOldParts.find(partID) == newToOldParts.end()
							&& oldToNewParts.find(partID) != oldToNewParts.end()
							&& oldToNewParts[partID].size() > 0))
				continue;

			VFPartChange change;
			string newPart = partID;

			//~~handle old parts
			vector<string> oldParts;
			if (newToOldParts.find(newPart) != newToOldParts.end())
				oldParts = newToOldParts[newPart];
			else
				oldParts.push_back(newPart);

			VFCost totalOldVFCost;
			TC_write_syslog("\n --oldParts=%d", oldParts.size());
			for (string oldPart : oldParts) {
				VFPartCost oldVFCost;
				oldVFCost.partNumber = oldPart;
				oldVFCost.partChangeType = PartChangeType::old_part;
				oldVFCost.partCost = getCurrentCost(oldPart, stProgram.c_str());

				change.partChanges.push_back(oldVFCost);

				totalOldVFCost.edndCost += oldVFCost.partCost.edndCost;
				totalOldVFCost.logisticCost += oldVFCost.partCost.logisticCost;
				totalOldVFCost.packingCost += oldVFCost.partCost.packingCost;
				totalOldVFCost.pieceCost += oldVFCost.partCost.pieceCost;
				totalOldVFCost.toolingCost += oldVFCost.partCost.toolingCost;
				totalOldVFCost.totalPieceCost +=
						oldVFCost.partCost.totalPieceCost;

				printCost("oldPart", oldVFCost.partCost);
				donePartIDs[oldPart] = 1;
			}

			//~~handle new parts
			vector<string> newParts;
			if (oldParts.size() > 0) {
				if (oldToNewParts.find(oldParts[0]) != oldToNewParts.end())
					newParts = oldToNewParts[oldParts[0]];
				else
					newParts.push_back(partID);
			}

			VFCost totalNewVFCost;
			TC_write_syslog("\n --newParts=%d", newParts.size());
			for (string newPart : newParts) {
				VFPartCost newPartCost;
				newPartCost.partNumber = newPart;
				newPartCost.partChangeType = PartChangeType::new_part;
				newPartCost.partCost = getVFCostFromEcr(ecr, newPart);

				double totalNewpartCostTemp = newPartCost.partCost.edndCost
						+ newPartCost.partCost.logisticCost
						+ newPartCost.partCost.packingCost
						+ newPartCost.partCost.pieceCost
						+ newPartCost.partCost.toolingCost;
				if (totalNewpartCostTemp <= 0.000000001
						&& totalNewpartCostTemp >= -0.000000001) {
					newPartCost.partCost.edndCost = totalOldVFCost.edndCost
							/ newParts.size();
					newPartCost.partCost.logisticCost =
							totalOldVFCost.logisticCost / newParts.size();
					newPartCost.partCost.packingCost =
							totalOldVFCost.packingCost / newParts.size();
					newPartCost.partCost.pieceCost = totalOldVFCost.pieceCost
							/ newParts.size();
					newPartCost.partCost.toolingCost =
							totalOldVFCost.toolingCost / newParts.size();
					newPartCost.partCost.totalPieceCost =
							totalOldVFCost.totalPieceCost / newParts.size();
				}

				change.partChanges.push_back(newPartCost);

				totalNewVFCost.edndCost += newPartCost.partCost.edndCost;
				totalNewVFCost.logisticCost +=
						newPartCost.partCost.logisticCost;
				totalNewVFCost.packingCost += newPartCost.partCost.packingCost;
				totalNewVFCost.pieceCost += newPartCost.partCost.pieceCost;
				totalNewVFCost.toolingCost += newPartCost.partCost.toolingCost;
				totalNewVFCost.totalPieceCost +=
						newPartCost.partCost.totalPieceCost;

				printCost("newPart", newPartCost.partCost);
				donePartIDs[newPart] = 1;
			}
			printCost("totalNewVFCost", totalNewVFCost);
			printCost("totalOldVFCost", totalOldVFCost);
//			change.deltaPartCost.edndCost = totalNewVFCost.edndCost
//					- totalOldVFCost.edndCost;
//			change.deltaPartCost.logisticCost = totalNewVFCost.logisticCost
//					- totalOldVFCost.logisticCost;
//			change.deltaPartCost.packingCost = totalNewVFCost.packingCost
//					- totalOldVFCost.packingCost;
//			change.deltaPartCost.pieceCost = totalNewVFCost.pieceCost
//					- totalOldVFCost.pieceCost;
//			change.deltaPartCost.toolingCost = totalNewVFCost.toolingCost
//					- totalOldVFCost.toolingCost;
//			change.deltaPartCost.totalPieceCost = totalNewVFCost.totalPieceCost
//					- totalOldVFCost.totalPieceCost;

			change.deltaPartCost.edndCost = 0;
			change.deltaPartCost.logisticCost = 0;
			change.deltaPartCost.packingCost = 0;
			change.deltaPartCost.pieceCost = 0;
			change.deltaPartCost.toolingCost = 0;
			change.deltaPartCost.totalPieceCost = 0;

			printCost("deltaPartCost", change.deltaPartCost);

			double totalNewCost = totalNewVFCost.edndCost
					+ totalNewVFCost.logisticCost + totalNewVFCost.packingCost
					+ totalNewVFCost.pieceCost + totalNewVFCost.toolingCost;
			TC_write_syslog("\n[VF] totalNewCost=%8.8f", totalNewCost);
			if ((totalNewCost > 0.000000001)
					|| (totalNewCost >= -0.000000001
							&& totalNewCost <= 0.000000001))
				changes.push_back(change);
		}

		for (int k = 0; k < changes.size(); k++) {
			TC_write_syslog("\n\nChange %d", k);
			for (int j = 0; j < changes[k].partChanges.size(); j++) {
				TC_write_syslog("\nPartChange %d", j);
				TC_write_syslog("\npartChangeType=%s",
						(changes[k].partChanges[j].partChangeType
								== PartChangeType::new_part) ?
								"new cost" : "old cost");
				TC_write_syslog("\nPartNumber=%s",
						changes[k].partChanges[j].partNumber);
				TC_write_syslog("\n edndCost=%8.2f",
						changes[k].partChanges[j].partCost.edndCost);
				TC_write_syslog("\n logisticCost=%8.2f",
						changes[k].partChanges[j].partCost.logisticCost);
				TC_write_syslog("\n packingCost=%8.2f",
						changes[k].partChanges[j].partCost.packingCost);
				TC_write_syslog("\n pieceCost=%8.2f",
						changes[k].partChanges[j].partCost.pieceCost);
				TC_write_syslog("\n toolingCost=%8.2f",
						changes[k].partChanges[j].partCost.toolingCost);
				TC_write_syslog("\n totalPieceCost=%8.2f",
						changes[k].partChanges[j].partCost.totalPieceCost);
			}

//			TC_write_syslog("\nDelta:");
//			TC_write_syslog("\nedndCost=%8.2f", changes[i].deltaPartCost.edndCost);
//			TC_write_syslog("\nedndCost=%8.2f", changes[i].deltaPartCost.logisticCost);
//			TC_write_syslog("\nedndCost=%8.2f", changes[i].deltaPartCost.packingCost);
//			TC_write_syslog("\nedndCost=%8.2f", changes[i].deltaPartCost.pieceCost);
//			TC_write_syslog("\nedndCost=%8.2f", changes[i].deltaPartCost.toolingCost);
//			TC_write_syslog("\nedndCost=%8.2f", changes[i].deltaPartCost.totalPieceCost);

			TC_write_syslog("\n------------------\n");
		}

		if (costFormsTag)
			MEM_free(costFormsTag);

		res.ecrAndPartsCost[ecr] = changes;

		TC_write_syslog("\n[VF] End processing ecr[%d]\n", i);
	}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~LOGIC
// result;
// loop_ecr in ecrs do
//		 getting cost impacts forms which have creation date != modification date ==> changingCostPartsForm list
//		 getting part ID list from changingCostPartsForm
//
//		 create oldToNewParts map
//		 create newToOldParts map
//
//		 create vfchanges
//       create alreadyHandledPartList
//		 Loop_partID the partIDlists
//          if partID in alreadyHandledPartList go_next
//			create vfPartChange
//			newPart = partID;
//			oldParts = newToOldParts[newPart];
//          if oldParts.len == 0 then oldParts.add(partID)
//			totalOldVFCost = 0;
//			loop_oldPart in oldParts
//				create oldVFPartCost
//				oldVFPartCost.pn = oldPart
//				oldVFPartCost.vfCost = getCurrentCost(oldPart)
//				oldVFPartCost.changeType = old
//				vfPartChange.add(oldVFPartCost);
//				totalOldVFCost += oldVFPartCost
//			end loop_oldPart
//
//			if (oldParts.len > 0) then newParts = oldToNewParts[oldParts[0]]
//			else  newParts.add(partID)
//
//
//			totalNewVFPartCost = 0
//			loop_newPart in newParts
//				create newVFPartCost
//				newVFPartCost.pn = newPart
//				newVFPartCost = getCostFromCurrentECR(newPart)
//				newVFPartCost.changeType = new
//				vfPartChange.add(newVFPartCost)
//				totalNewVFPartCost += newVFPartCost
//              alreadyHandledPartList.add(newPart)
//			end_loop_newPart
//
//			vfPartChange.deltaCost = totalNewVFPartCost - totalOldVFCost
//			vfchanges.add(vfchange)
//		 end loop_partID
// result.ecrAndPartsCost[ecr] = vfchanges
// endloop_ecr
//
//

//~~~~DUMMY Data
//	VFCost dummyDeltaCost;
//	dummyDeltaCost.edndCost=0;
//	dummyDeltaCost.logisticCost=0;
//	dummyDeltaCost.pieceCost=0;
//	dummyDeltaCost.toolingCost=0;
//	dummyDeltaCost.totalPieceCost=0;
//
//	ReportDataSourceImpl::VFEcrCostResponse res;
//
//	// ~~~~~~~~~~~replace case
//	// ~~~~~~~~~~~~~~~~~~~~~~
//	VFPartChange replaceCase;
//	VFPartCost vfPartCost1;
//	vfPartCost1.partCost.edndCost = 10;
//	vfPartCost1.partCost.pieceCost = 20;
//	vfPartCost1.partCost.toolingCost = 30;
//	vfPartCost1.partCost.logisticCost =32;
//	vfPartCost1.partCost.totalPieceCost= vfPartCost1.partCost.pieceCost + vfPartCost1.partCost.toolingCost + vfPartCost1.partCost.logisticCost;
//	vfPartCost1.partChangeType = PartChangeType::old_part;
//	vfPartCost1.partNumber = "P0001";
//
//	VFPartCost vfPartCost2;
//	vfPartCost2.partCost.edndCost = 30;
//	vfPartCost2.partCost.pieceCost = 20;
//	vfPartCost2.partCost.toolingCost = 40;
//	vfPartCost2.partCost.logisticCost =12;
//	vfPartCost2.partCost.totalPieceCost= vfPartCost2.partCost.pieceCost + vfPartCost2.partCost.toolingCost + vfPartCost2.partCost.logisticCost;
//	vfPartCost2.partChangeType = PartChangeType::new_part;
//	vfPartCost2.partNumber = "P0002";
//
//	replaceCase.partChanges.push_back(vfPartCost1);
//	replaceCase.partChanges.push_back(vfPartCost2);
//	replaceCase.deltaPartCost = dummyDeltaCost;
//
//	// ~~~~~~~~~~ split case
//	// ~~~~~~~~~~~~~~~~~~~~~~
//	VFPartChange splitCase;
//	vfPartCost1.partCost.edndCost = 15;
//	vfPartCost1.partCost.pieceCost = 24;
//	vfPartCost1.partCost.toolingCost = 33;
//	vfPartCost1.partCost.logisticCost =36;
//	vfPartCost1.partCost.totalPieceCost= vfPartCost1.partCost.pieceCost + vfPartCost1.partCost.toolingCost + vfPartCost1.partCost.logisticCost;
//	vfPartCost1.partChangeType = PartChangeType::old_part;
//	vfPartCost1.partNumber = "P0003";
//
//	vfPartCost2.partCost.edndCost = 40;
//	vfPartCost2.partCost.pieceCost = 53;
//	vfPartCost2.partCost.toolingCost = 23;
//	vfPartCost2.partCost.logisticCost =67;
//	vfPartCost2.partCost.totalPieceCost= vfPartCost2.partCost.pieceCost + vfPartCost2.partCost.toolingCost + vfPartCost2.partCost.logisticCost;
//	vfPartCost2.partChangeType = PartChangeType::new_part;
//	vfPartCost2.partNumber = "P0004";
//
//	VFPartCost vfPartCost3;
//	vfPartCost3.partCost.edndCost = 30;
//	vfPartCost3.partCost.pieceCost = 20;
//	vfPartCost3.partCost.toolingCost = 40;
//	vfPartCost3.partCost.logisticCost =12;
//	vfPartCost3.partCost.totalPieceCost= vfPartCost3.partCost.pieceCost + vfPartCost3.partCost.toolingCost + vfPartCost3.partCost.logisticCost;
//	vfPartCost3.partChangeType = PartChangeType::new_part;
//	vfPartCost3.partNumber = "P0005";
//
//	splitCase.partChanges.push_back(vfPartCost1);
//	splitCase.partChanges.push_back(vfPartCost2);
//	splitCase.partChanges.push_back(vfPartCost3);
//	splitCase.deltaPartCost = dummyDeltaCost;
//
//	// ~~~~~~~~~~ merge case
//	// ~~~~~~~~~~~~~~~~~~~~~~
//	VFPartChange mergeCase;
//	vfPartCost1.partCost.edndCost = 15;
//	vfPartCost1.partCost.pieceCost = 24;
//	vfPartCost1.partCost.toolingCost = 33;
//	vfPartCost1.partCost.logisticCost =36;
//	vfPartCost1.partCost.totalPieceCost= vfPartCost1.partCost.pieceCost + vfPartCost1.partCost.toolingCost + vfPartCost1.partCost.logisticCost;
//	vfPartCost1.partChangeType = PartChangeType::old_part;
//	vfPartCost1.partNumber = "P0006";
//
//	vfPartCost2.partCost.edndCost = 40;
//	vfPartCost2.partCost.pieceCost = 53;
//	vfPartCost2.partCost.toolingCost = 23;
//	vfPartCost2.partCost.logisticCost =67;
//	vfPartCost2.partCost.totalPieceCost= vfPartCost2.partCost.pieceCost + vfPartCost2.partCost.toolingCost + vfPartCost2.partCost.logisticCost;
//	vfPartCost2.partChangeType = PartChangeType::old_part;
//	vfPartCost2.partNumber = "P0007";
//
//	vfPartCost3.partCost.edndCost = 30;
//	vfPartCost3.partCost.pieceCost = 20;
//	vfPartCost3.partCost.toolingCost = 40;
//	vfPartCost3.partCost.logisticCost =12;
//	vfPartCost3.partCost.totalPieceCost= vfPartCost3.partCost.pieceCost + vfPartCost3.partCost.toolingCost + vfPartCost3.partCost.logisticCost;
//	vfPartCost3.partChangeType = PartChangeType::new_part;
//	vfPartCost3.partNumber = "P0008";
//
//	mergeCase.partChanges.push_back(vfPartCost1);
//	mergeCase.partChanges.push_back(vfPartCost2);
//	mergeCase.partChanges.push_back(vfPartCost3);
//	mergeCase.deltaPartCost = dummyDeltaCost;
//
//	// ~~~~~~~~~~ up revision case
//	// ~~~~~~~~~~~~~~~~~~~~~~
//	VFPartChange upRevCase;
//	vfPartCost1.partCost.edndCost = 10;
//	vfPartCost1.partCost.pieceCost = 20;
//	vfPartCost1.partCost.toolingCost = 30;
//	vfPartCost1.partCost.logisticCost =32;
//	vfPartCost1.partCost.packingCost = 123456789;
//	vfPartCost1.partCost.totalPieceCost= vfPartCost1.partCost.pieceCost + vfPartCost1.partCost.toolingCost + vfPartCost1.partCost.logisticCost;
//	vfPartCost1.partChangeType = PartChangeType::old_part;
//	vfPartCost1.partNumber = "";
//
//	vfPartCost2.partCost.edndCost = 30;
//	vfPartCost2.partCost.pieceCost = 20;
//	vfPartCost2.partCost.toolingCost = 40;
//	vfPartCost2.partCost.logisticCost =12;
//	vfPartCost2.partCost.totalPieceCost= vfPartCost2.partCost.pieceCost + vfPartCost2.partCost.toolingCost + vfPartCost2.partCost.logisticCost;
//	vfPartCost2.partChangeType = PartChangeType::new_part;
//	vfPartCost2.partNumber = "P0009";
//
//	upRevCase.partChanges.push_back(vfPartCost1);
//	upRevCase.partChanges.push_back(vfPartCost2);
//	upRevCase.deltaPartCost = dummyDeltaCost;
//
//	std::vector<VFPartChange> vfchanges;
//	vfchanges.push_back(replaceCase);
//	vfchanges.push_back(splitCase);
//	vfchanges.push_back(mergeCase);
//	vfchanges.push_back(upRevCase);
//
//	res.ecrAndPartsCost[ecrs[0]] = vfchanges;

	TC_write_syslog("\n[VF] Leave %s", __FUNCTION__);
	return res;
}

ReportDataSourceImpl::FGValidationOutput ReportDataSourceImpl::validateFamilyGroup(
		const FGValidationInput& input) {
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);


	tag_t item = NULLTAG, rev = NULLTAG;

	ReportDataSourceImpl::FGValidationOutput output;
	int failedNum = 0;
	int warningNum = 0;
	vector<string> errorMsgs;

//	input.itemId = "CHS20010118";
//	input.revId = "01";
//	input.revisionRule = "Working; Any Status";
//	input.savedVariant2LoadBom = "B_BEV-High";
//	input.variant2Validate = "B_BEV-High";
//	input.program = "VF31";
//	input.itemType = "VF4_Design";

	TC_write_syslog("\nInput:");
	TC_write_syslog("\n itemId=%s", input.itemId.c_str());
	TC_write_syslog("\n revId=%s", input.revId.c_str());
	TC_write_syslog("\n revisionRule=%s", input.revisionRule.c_str());
	TC_write_syslog("\n savedVariant2LoadBom=%s", input.savedVariant2LoadBom.c_str());
	TC_write_syslog("\n variant2Validate=%s", input.variant2Validate.c_str());
	TC_write_syslog("\n program=%s", input.program.c_str());
	TC_write_syslog("\n itemType=%s", input.itemType.c_str());
	TC_write_syslog("\n");

	map<string, FGValidationResult> filteredRules;
	findItemAndRev(input.itemId.c_str(), input.itemType.c_str(),
			input.revId.c_str(), item, rev, errorMsgs);
	if (item && rev) {
		getFilteredRules(input.program.c_str(), input.variant2Validate.c_str(),
				filteredRules, errorMsgs);

		tag_t topLine = NULLTAG;
		getTopLine(item, rev, input.revisionRule.c_str(),
				input.savedVariant2LoadBom.c_str(), topLine, errorMsgs);
		map<string, vector<FGValidationPartInfo>> fgsInBOM;
		TC_write_syslog("\n Start loop BOM:\n");
		groupFGFromBomRecursive(topLine, fgsInBOM, errorMsgs);
		TC_write_syslog("\n fgsAndPartInfos = %d", fgsInBOM.size());

		printFGinBOM(fgsInBOM);

		if (errorMsgs.size() == 0) validateFG(filteredRules, fgsInBOM, output, failedNum, warningNum);
	} else {
		TC_write_syslog("\n *item || rev null");
		string msg = "Cannot found part \"" + input.itemId + "/" + input.revId
				+ "\" with type \"" + input.itemType + "\"";
		errorMsgs.push_back(msg);
	}

	finalizeOutput(input, failedNum, warningNum, errorMsgs, output);
	printOutput(output);

	TC_write_syslog("\n[VF] LEAVE %s\n", __FUNCTION__);
	return output;
}

void printOutput(ReportDataSourceImpl::FGValidationOutput output) {
	TC_write_syslog("\n[VF]Validation Output:");
	TC_write_syslog("\n[VF]  Final Validation Result:%s",
			output.finalValidationResult.c_str());
	TC_write_syslog(
			"\n[VF]  Validation Result (code - description - min - max - result - parts info:");
	for (int i = 0; i < output.validationResults.size(); i++) {
		ReportDataSourceImpl::FGValidationResult *res = &(output.validationResults[i]);
		TC_write_syslog("\n[VF]    %s-%s-%8.2f-%8.2f-%s-parts info:", res->fgCode.c_str(),
				res->fgDescription.c_str(), res->minQuantity, res->maxQuantity,
				res->validationResult.c_str());
		for (int j = 0; j < res->relevantPartsInfo.size(); j++) {
			ReportDataSourceImpl::FGValidationPartInfo *partInfo = &(res->relevantPartsInfo[j]);
			char *partStr = NULL;
			AOM_ask_value_string(partInfo->partRev->getTag(), "object_string",
					&partStr);

			TC_write_syslog("\n[VF]      %s - %8.2f", partStr, partInfo->quantity);

			SAFE_SM_FREE(partStr);
		}
	}
}

void getFilteredRules(const char *program, const char *variant,
		map<string, ReportDataSourceImpl::FGValidationResult> &fgCodesAndValResults,
		vector<string> &errorMsgs) {
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	map<string, string> qryInput;
	qryInput["Program"] = program;
	vector<tag_t> rules;

	queryObj("VF_Family_Group_Rule", qryInput, rules, errorMsgs);
	TC_write_syslog("\n[VF] rules count = %d", rules.size());
	fgCodesAndValResults.clear();
	TC_write_syslog("\n[VF]input variant is %s", variant);
	for (auto rule : rules) {
		char *fgCode           = NULL,
			 *validationType   = NULL,
			 *ruleVariant      = NULL,
			 *ruleDesc         = NULL;

		double maxQuantity = -1, minQuantity = -1;

		ERROR_CHECK(AOM_ask_value_string(rule, "vf4_fg_code", &fgCode));
		ERROR_CHECK(AOM_ask_value_string(rule, "object_desc", &ruleDesc));
		ERROR_CHECK(
				AOM_ask_value_string(rule, "vf4_val_type", &validationType));
		ERROR_CHECK(AOM_ask_value_string(rule, "vf4_variant", &ruleVariant));
		ERROR_CHECK(AOM_ask_value_double(rule, "vf4_min_quantity", &minQuantity));
		ERROR_CHECK(AOM_ask_value_double(rule, "vf4_max_quantity", &maxQuantity));
		TC_write_syslog("\n[VF] filtering %s-%s-%s-%s-%8.2f-%8.2f", fgCode, ruleDesc,
				validationType, ruleVariant, minQuantity, maxQuantity);

		ReportDataSourceImpl::FGValidationResult filteredRule;
		filteredRule.fgCode = fgCode;
		filteredRule.fgDescription = ruleDesc;
		filteredRule.maxQuantity = maxQuantity;
		filteredRule.minQuantity = minQuantity;
		filteredRule.validationResult = validationType;
		filteredRule.fgValidationType = validationType;

		if (tc_strlen(variant) == 0) {
			// only get empty variant rule
			if (tc_strlen(ruleVariant) == 0) {
				fgCodesAndValResults[fgCode] = filteredRule;
			}
		} else {
			// get with priority: rule variant == variant >> empty rule variant
			if (tc_strcasecmp(ruleVariant, variant) == 0) {
				fgCodesAndValResults[fgCode] = filteredRule;
			} else if (tc_strlen(ruleVariant) == 0
					&& fgCodesAndValResults.find(fgCode)
							== fgCodesAndValResults.end()) {
				fgCodesAndValResults[fgCode] = filteredRule;
			}
		}

		SAFE_SM_FREE(fgCode);
		SAFE_SM_FREE(validationType);
		SAFE_SM_FREE(ruleVariant);
		SAFE_SM_FREE(ruleDesc);
	}

	TC_write_syslog("\n[VF] filtered RULE:");
	for (auto fgCodeAndValResult : fgCodesAndValResults) {
		TC_write_syslog("\n[VF]  %s - %s - %s - %s - %8.2f - %8.2f",
				fgCodeAndValResult.first.c_str(),
				fgCodeAndValResult.second.fgCode.c_str(),
				fgCodeAndValResult.second.fgDescription.c_str(),
				fgCodeAndValResult.second.validationResult.c_str(),
				fgCodeAndValResult.second.minQuantity,
				fgCodeAndValResult.second.maxQuantity);
	}

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}

void getTopLine(tag_t item, tag_t rev, const char *revisionRule,
		const char *savedVariant, tag_t &topLine, vector<string> &errorMsgs) {
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	int contextsCount = 0;
	tag_t *contexts = NULL;
	tag_t contextRelType = NULLTAG;
	char *itemUid = NULL, *revUid = NULL;
	ITK__convert_tag_to_uid(item, &itemUid);
	ITK__convert_tag_to_uid(item, &revUid);
	TC_write_syslog("\n[VF]input: %s||%s||%s||%s", itemUid, revUid, revisionRule,
			savedVariant);
	ERROR_CHECK(
			TCTYPE_find_type("Smc0HasVariantConfigContext", "Smc0HasVariantConfigContext", &contextRelType));
	ERROR_CHECK(
			GRM_list_secondary_objects_only(item, contextRelType, &contextsCount, &contexts));
	tag_t svr = NULLTAG;
	if (contextsCount == 1) {
		TC_write_syslog("\n[VF]found context");
		tag_t context = contexts[0];
		int svrsCount = 0;
		tag_t *svrs = NULL;
		tag_t imanRefRelType = NULLTAG;
		ERROR_CHECK(
				TCTYPE_find_type("IMAN_reference", "IMAN_reference", &imanRefRelType));
		ERROR_CHECK(
				GRM_list_secondary_objects_only(context, imanRefRelType, &svrsCount, &svrs));
		TC_write_syslog("\n[VF] svrsCount=%d", svrsCount);
		for (int i = 0; i < svrsCount; i++) {
			tag_t svrTmp = svrs[i];
			char *svrName = NULL;

			ERROR_CHECK(AOM_ask_value_string(svrTmp, "object_name", &svrName));
			TC_write_syslog("\n[VF] svrName=%s", svrName);
			if (tc_strcasecmp(svrName, savedVariant) == 0) {
				svr = svrTmp;
				SAFE_SM_FREE(svrName);
				break;
			}

			SAFE_SM_FREE(svrName);
		}

		SAFE_SM_FREE(svrs);
	} else {
		TC_write_syslog("\n[VF] context count not value %d", contextsCount);
		string errorMsg =
				"Cannot find context or there are more than one context in the input item.";
		errorMsgs.push_back(errorMsg);
	}

	if (svr != NULLTAG ) {
		char *svrUid = NULL;
		ITK__convert_tag_to_uid(svr, &svrUid);
		TC_write_syslog("\n[VF]found svr %s", svrUid);
		tag_t window = NULLTAG;
		tag_t topL = NULLTAG;
		tag_t revRule NULLTAG;

		ERROR_CHECK(CFM_find(revisionRule, &revRule));
		ERROR_CHECK(BOM_create_window(&window));
		ERROR_CHECK(BOM_set_window_top_line(window, item, rev, NULL, &topL));
		ERROR_CHECK(BOM_window_apply_variant_configuration(window, 1, &svr));
		ERROR_CHECK(BOM_set_window_config_rule(window, revRule));
		topLine = topL;
		TC_write_syslog("\n[VF] TOPLINE=%d", topLine);
		SAFE_SM_FREE(svrUid);
	} else {
		TC_write_syslog("\n[VF] cannot find svr %s", savedVariant);
		string errorMsg = "Cannot find saved variant \"" + string(savedVariant)
				+ "\"";
		errorMsgs.push_back(errorMsg);
	}

	SAFE_SM_FREE(contexts);
	SAFE_SM_FREE(revUid);
	SAFE_SM_FREE(itemUid);
	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}

void groupFGFromBomRecursive(tag_t bomline,
		map<string, vector<ReportDataSourceImpl::FGValidationPartInfo>> &fgsAndPartInfos,
		vector<string> &errorMsgs) {
	//TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	if (errorMsgs.size() > 0) return;

	int childrenCount = 0;
	tag_t *children = NULL;

	int revAttr = NULLTAG, itemAttr = NULLTAG, quantityAttr = NULLTAG,
			levelAttr = NULLTAG, uomAttr = NULLTAG;

	tag_t item = NULLTAG, rev = NULLTAG;
	char *quantity = NULL, *uom = NULL;
	int level = 0;
	ERROR_CHECK(BOM_line_look_up_attribute("bl_item", &itemAttr));
	ERROR_CHECK(BOM_line_look_up_attribute("bl_revision", &revAttr));
	ERROR_CHECK(BOM_line_look_up_attribute("bl_quantity", &quantityAttr));
	ERROR_CHECK(BOM_line_look_up_attribute("bl_level_starting_0", &levelAttr));
	ERROR_CHECK(BOM_line_look_up_attribute("bl_item_uom_tag", &uomAttr));
	ERROR_CHECK(BOM_line_ask_attribute_tag(bomline, revAttr, &rev));
	ERROR_CHECK(BOM_line_ask_attribute_tag(bomline, itemAttr, &item));
	ERROR_CHECK(
			BOM_line_ask_attribute_string(bomline, quantityAttr, &quantity));
	ERROR_CHECK(BOM_line_ask_attribute_string(bomline, uomAttr, &uom));
	ERROR_CHECK(BOM_line_ask_attribute_int(bomline, levelAttr, &level));

	char *revStr = NULL;
	char *fgCode = NULL;
	if (rev != NULL)
	{
		ERROR_CHECK(AOM_ask_value_string(rev, "object_string", &revStr));
	}
	else
	{
		ERROR_CHECK(AOM_ask_value_string(item, "object_string", &revStr));
		errorMsgs.push_back(string("Cannot find revision of item \"") + revStr + "\" when applied the current revision rule!");
	}
	ERROR_CHECK(AOM_ask_value_string(item, "vf4_fg_code", &fgCode));

	if (rev != NULL && tc_strlen(fgCode) > 0) {
		vector<ReportDataSourceImpl::FGValidationPartInfo> partInfos;
		if (fgsAndPartInfos.find(fgCode) != fgsAndPartInfos.end()) {
			partInfos = fgsAndPartInfos[fgCode];
		}

		double fQuantity = 0;
		if (tc_strlen(quantity) > 0)
			fQuantity = atof(quantity);

		bool revExisted = false;

		ReportDataSourceImpl::FGValidationPartInfo partInfo;
		partInfo.partRev = rev;

		int partRevIndex = 0;
		for (; partRevIndex < partInfos.size(); partRevIndex++)
		{
			char *partRevStr = NULL;
			tag_t partRev = partInfos[partRevIndex].partRev;
			ERROR_CHECK(AOM_ask_value_string(partRev, "object_string", &partRevStr));
			if (tc_strcmp(partRevStr, revStr) == 0)
			{
				revExisted = true;
				SAFE_SM_FREE(partRevStr);
				break;
			}

			SAFE_SM_FREE(partRevStr);
		}

		if (!revExisted)
		{
			partInfo.quantity = fQuantity;
		}
		else
		{
			partInfo.quantity = partInfos[partRevIndex].quantity + fQuantity;
			partInfos.erase(std::next(partInfos.begin(), partRevIndex));
		}

		partInfos.push_back(partInfo);
		fgsAndPartInfos[fgCode] = partInfos;
	}

	TC_write_syslog("\n[VF]");
	for (int j = 0; j < level; j++)
		TC_write_syslog("|");
	TC_write_syslog("%s - %s - %s", revStr, uom, quantity);

	if (rev != NULL)
	{
		ERROR_CHECK(
					BOM_line_ask_all_child_lines(bomline, &childrenCount, &children));
		for (int i = 0; i < childrenCount; i++) {
			groupFGFromBomRecursive(children[i], fgsAndPartInfos, errorMsgs);
		}
	}

	SAFE_SM_FREE(children);
	SAFE_SM_FREE(revStr);
	SAFE_SM_FREE(quantity);
	SAFE_SM_FREE(fgCode);
	SAFE_SM_FREE(uom);

	//TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}

void validateFG(const map<string, ReportDataSourceImpl::FGValidationResult> &filteredRules,
		const map<string, vector<ReportDataSourceImpl::FGValidationPartInfo>> &fgsInBOM,
		ReportDataSourceImpl::FGValidationOutput &output, int &failedNum, int &warningNum) {
	for (auto fgCodeAndValResult : filteredRules) {
		const char *fgCode = fgCodeAndValResult.first.c_str();
		ReportDataSourceImpl::FGValidationResult valResult = fgCodeAndValResult.second;
		double totalCount = 0;
		if (fgsInBOM.find(fgCode) != fgsInBOM.end()) {
			vector<ReportDataSourceImpl::FGValidationPartInfo> partInfos =
					fgsInBOM.find(fgCode)->second;
			for (auto partInfo : partInfos) {
				totalCount += partInfo.quantity;
			}

			valResult.relevantPartsInfo = partInfos;
		}
		if ((totalCount < valResult.minQuantity
				|| totalCount > valResult.maxQuantity)) {
			if (valResult.validationResult.compare("Mandatory") == 0) {
				valResult.validationResult = "Failed";
				failedNum++;
			} else {
				valResult.validationResult = "Warning";
				warningNum++;
			}
		} else {
			valResult.validationResult = "Passed";
		}

		valResult.fgCountInBom = totalCount;
		output.validationResults.push_back(valResult);
	}
}

void printFGinBOM(const map<string, vector<ReportDataSourceImpl::FGValidationPartInfo>> &fgsInBOM) {
	for (auto fgAndPatInfos : fgsInBOM) {
		TC_write_syslog("\n[VF] %s:", fgAndPatInfos.first.c_str());
		vector<ReportDataSourceImpl::FGValidationPartInfo> partInfos = fgAndPatInfos.second;
		for (auto partInfo : partInfos) {
			char *partStr = NULL;
			ERROR_CHECK(
					AOM_ask_value_string(partInfo.partRev->getTag(), "object_string", &partStr));
			TC_write_syslog("\n[VF]   %s-%8.2f", partStr, partInfo.quantity);
			SAFE_SM_FREE(partStr);
		}
	}
}

void finalizeOutput(const ReportDataSourceImpl::FGValidationInput &input, const int failedNum,
		const int warningNum, const vector<string> &errorMsgs,
		ReportDataSourceImpl::FGValidationOutput &output) {
	output.input = input;
	output.errorMessages = errorMsgs;
	if (output.validationResults.size() > 0) {

		output.finalValidationResult = errorMsgs.size() > 0 ? "Error(s) occurred when validating!" : (failedNum > 0) ? "Failed" :
										warningNum > 0 ? "Warning" : "Passed";
	} else {
		output.errorMessages.push_back(
				"Cannot find any relevant Family Group Validation Rules");
	}

	TC_write_syslog("\n[VF] Manged error messages:");
	for (string errorMsg : output.errorMessages) {
		TC_write_syslog("\n[VF]  %s", errorMsg.c_str());
	}
}


ReportDataSourceImpl::VFPartCostCalcResponse ReportDataSourceImpl::calculatePartsCost ( const std::vector< std::string >& partNumbers, const std::string programName, const std::string isDryRun )
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	partNumbers;isDryRun;programName;
	VFPartCostCalcResponse output;

	vector<string> errorMsgs;
	// Not using asterisk as it's better to skip JF, MP, Flex parts
	const string partType("VF4_Design;VF4_Cell_Design;VF4_BP_Design;VF4_Compo_Design;VF3_Scooter_part");
	for (string partNumber : partNumbers)
	{
		std::vector<std::string> words;
		std::string partMakeBuy("");
		vf_split(partNumber, ";", words);
		if (words.size() == 2)
		{
			partNumber = words[0];
			partMakeBuy = words[1];
		}

		tag_t part = getPart(partNumber, partType, errorMsgs);
		tag_t costRev = getCostRev(partNumber, errorMsgs);
		if (costRev != NULLTAG && part != NULLTAG)
		{
			printObjString(part, "part");
			printObjString(costRev, "costRev");

			VFPartCost finalCost;
			bool isMadePart = isMakePart(part, partMakeBuy);
			bool isBuyPart = isBoughtPart(part, partMakeBuy);
			if (isMadePart)
			{
				VFCost targetCost = getTargetCost(part, costRev);
				VFCost engCost = getEngCost(part, costRev, partMakeBuy);
				VFCost ecrCost = getEcrCostMadePart(part, costRev);

				vector<VFCost> sortedCosts;// descending
				putAndSort(sortedCosts, targetCost);
				putAndSort(sortedCosts, engCost);
				putAndSort(sortedCosts, ecrCost);

				bool isAssembly = checkIsAssembly(part);
				bool isIncludePieceCostOnMadeAssembly = getLogicalProperty(costRev, "vf4_is_included_h_asem_cost");

				finalCost = getFinalCostMadePart(sortedCosts, isAssembly, isIncludePieceCostOnMadeAssembly);
				setAllCostNull(finalCost.costHistory.ecrCost);
				setAllCostNull(finalCost.costHistory.engCost);
				setAllCostNull(finalCost.costHistory.targetCost);
				setAllCostNull(finalCost.costHistory.sapEdndCost);
				setAllCostNull(finalCost.costHistory.sapToolingCost);
				setAllCostNull(finalCost.costHistory.sapPieceCost);
				setAllCostNull(finalCost.costHistory.stCost);

				finalCost.costHistory.ecrCost = ecrCost;
				finalCost.costHistory.engCost = engCost;
				finalCost.costHistory.targetCost = targetCost;

				if (VF_IS_DEBUG) TC_write_syslog("\n[VF]");
				if (VF_IS_DEBUG) TC_write_syslog("\n[VF]******* %s:", partNumber.c_str());
				printCost("ecrCost", ecrCost);
				printCost("targetCost", targetCost);
				printCost("engCost", engCost);
			}
			else if (isBuyPart)
			{
				tag_t sourcePart = NULLTAG;
				VFCost stCost = getStCost(part, costRev, sourcePart, partMakeBuy);
				VFCost sapCostTooling = getSapCost(part, partMakeBuy, costRev, "tooling");
				VFCost sapCostEDnD = getSapCost(part, partMakeBuy, costRev, "ednd");
				VFCost sapCostPiece = getSapCost(part, partMakeBuy, costRev, "piece");
				VFCost targetCost = getTargetCost(part, costRev);
				VFCost engCost = getEngCost(part, costRev, partMakeBuy);
				VFCost ecrCost = getEcrCostBuyPart(part, costRev, sourcePart);
				VFCost ecrCostHistory = getEcrCostMadePart(part, costRev);

				vector<VFCost> sortedCosts;// descending
//				sortedCosts.push_back(sapCostTooling);sortedCosts[sortedCosts.size() - 1].costType = sapCostTooling.costType;
//				sortedCosts.push_back(sapCostEDnD);sortedCosts[sortedCosts.size() - 1].costType = sapCostEDnD.costType;
//				sortedCosts.push_back(sapCostPiece);sortedCosts[sortedCosts.size() - 1].costType = sapCostPiece.costType;
				putAndSort(sortedCosts, sapCostPiece);
				putAndSort(sortedCosts, sapCostEDnD);
				putAndSort(sortedCosts, sapCostTooling);
				putAndSort(sortedCosts, targetCost);
				putAndSort(sortedCosts, engCost);
				putAndSort(sortedCosts, ecrCost);
				putAndSort(sortedCosts, stCost);

				finalCost = getFinalCostBuyPart(sortedCosts);
				setAllCostNull(finalCost.costHistory.ecrCost);
				setAllCostNull(finalCost.costHistory.engCost);
				setAllCostNull(finalCost.costHistory.targetCost);
				setAllCostNull(finalCost.costHistory.sapEdndCost);
				setAllCostNull(finalCost.costHistory.sapToolingCost);
				setAllCostNull(finalCost.costHistory.sapPieceCost);
				setAllCostNull(finalCost.costHistory.stCost);

				finalCost.costHistory.sapPieceCost = sapCostPiece;
				finalCost.costHistory.sapPieceCost.costType = "Schedule Agreement - Piece cost";
				finalCost.costHistory.sapToolingCost = sapCostTooling;
				finalCost.costHistory.sapToolingCost.costType = "Schedule Agreement - Tooling cost";
				finalCost.costHistory.sapEdndCost = sapCostEDnD;
				finalCost.costHistory.sapEdndCost.costType = "Schedule Agreement - Ednd cost";
				finalCost.costHistory.stCost = stCost;
				finalCost.costHistory.ecrCost = ecrCostHistory;
				finalCost.costHistory.engCost = engCost;
				finalCost.costHistory.targetCost = targetCost;
				if (VF_IS_DEBUG) TC_write_syslog("\n[VF]");
				if (VF_IS_DEBUG) TC_write_syslog("\n[VF]******* %s:", partNumber.c_str());
				printCost("sapCostPiece", sapCostPiece);
				printCost("sapCostTooling", sapCostTooling);
				printCost("sapCostEDnD", sapCostEDnD);
				printCost("stCost", stCost);
				printCost("ecrCost", ecrCost);
				printCost("ecrCostHistory", ecrCost);
				printCost("targetCost", targetCost);
				printCost("engCost", engCost);
			}

			TC_write_syslog("\n---------------------------------");

			TC_write_syslog("\n");
			TC_write_syslog("\nPiece Cost: ");
			TC_write_syslog("\n cost type =%s", finalCost.pieceCost.costType.c_str());
			TC_write_syslog("\n cost      =%8.8f", finalCost.pieceCost.cost);
			TC_write_syslog("\n cost date =%lld", finalCost.pieceCost.approvalTimeStamp);

			TC_write_syslog("\n");
			TC_write_syslog("\nPacking Cost: ");
			TC_write_syslog("\n cost type =%s", finalCost.packingCost.costType.c_str());
			TC_write_syslog("\n cost      =%8.8f", finalCost.packingCost.cost);
			TC_write_syslog("\n cost date =%lld", finalCost.packingCost.approvalTimeStamp);

			TC_write_syslog("\n");
			TC_write_syslog("\nLogistic Cost: ");
			TC_write_syslog("\n cost type =%s", finalCost.logisticCost.costType.c_str());
			TC_write_syslog("\n cost      =%8.8f", finalCost.logisticCost.cost);
			TC_write_syslog("\n cost date =%lld", finalCost.logisticCost.approvalTimeStamp);

			TC_write_syslog("\n");
			TC_write_syslog("\nLabour Cost: ");
			TC_write_syslog("\n cost type =%s", finalCost.labourCost.costType.c_str());
			TC_write_syslog("\n cost      =%8.8f", finalCost.labourCost.cost);
			TC_write_syslog("\n cost date =%lld", finalCost.labourCost.approvalTimeStamp);

			TC_write_syslog("\n");
			TC_write_syslog("\nTax Cost: ");
			TC_write_syslog("\n cost type =%s", finalCost.tax.costType.c_str());
			TC_write_syslog("\n cost      =%8.8f", finalCost.tax.cost);
			TC_write_syslog("\n cost date =%lld", finalCost.tax.approvalTimeStamp);

			TC_write_syslog("\n");
			TC_write_syslog("\nEDnD Cost: ");
			TC_write_syslog("\n cost type =%s", finalCost.edndCost.costType.c_str());
			TC_write_syslog("\n cost      =%8.8f", finalCost.edndCost.cost);
			TC_write_syslog("\n cost date =%lld", finalCost.edndCost.approvalTimeStamp);

			TC_write_syslog("\n");
			TC_write_syslog("\nTooling Cost: ");
			TC_write_syslog("\n cost type =%s", finalCost.toolingCost.costType.c_str());
			TC_write_syslog("\n cost      =%8.8f", finalCost.toolingCost.cost);
			TC_write_syslog("\n cost date =%lld", finalCost.toolingCost.approvalTimeStamp);



			TC_write_syslog("\n---------------------------------");
			printCost("Final Cost", finalCost.partCost);
			output.result[partNumber] = finalCost;


			if ((tc_strcmp(isDryRun.c_str(), "True") != 0 && tc_strcmp(isDryRun.c_str(), "true") != 0) && (finalCost.partCost.totalPieceCost != VF_DOUBLE_NULL || finalCost.partCost.toolingCost != VF_DOUBLE_NULL || finalCost.partCost.edndCost != VF_DOUBLE_NULL))
			{
				TC_write_syslog("\n[VF] dryrun = false");
				tag_t costItem = NULLTAG;
				AOM_ask_value_tag(costRev, "items_tag", &costItem);
				int retcode = ITK_ok;
				if (costItem)
				{
					TC_write_syslog("\n[VF] start updating cost item");
					retcode += AOM_refresh(costItem, TRUE);
					if (!retcode && finalCost.partCost.edndCost != VF_DOUBLE_NULL) retcode += AOM_set_value_double(costItem, "vf4_final_edd_cost", finalCost.partCost.edndCost);
					if (!retcode && finalCost.partCost.labourCost != VF_DOUBLE_NULL) retcode += AOM_set_value_double(costItem, "vf4_final_labor_cost", finalCost.partCost.labourCost);
					if (!retcode && finalCost.partCost.logisticCost != VF_DOUBLE_NULL) retcode += AOM_set_value_double(costItem, "vf4_final_logistic_cost", finalCost.partCost.logisticCost);
					if (!retcode && finalCost.partCost.packingCost != VF_DOUBLE_NULL) retcode += AOM_set_value_double(costItem, "vf4_final_packing_cost", finalCost.partCost.packingCost);
					if (!retcode && finalCost.partCost.pieceCost != VF_DOUBLE_NULL) retcode += AOM_set_value_double(costItem, "vf4_final_piece_cost", finalCost.partCost.pieceCost);

					if (!retcode && finalCost.partCost.pieceCost != VF_DOUBLE_NULL) retcode += AOM_set_value_double(costItem, "vf4_final_tooling_cost", finalCost.partCost.toolingCost);
					if (!retcode && finalCost.partCost.totalPieceCost != VF_DOUBLE_NULL) retcode += AOM_set_value_double(costItem, "vf4_final_total_piece_cost", finalCost.partCost.totalPieceCost);
					if (!retcode) retcode += AOM_set_value_string(costItem, "vf4_final_piece_cost_status", finalCost.partCost.totalPieceCostStatus.c_str());

					if (!retcode) retcode += AOM_save_with_extensions(costItem);
					if (!retcode) retcode += AOM_refresh(costItem, FALSE);
				}

				if (retcode)
				{
					char *text = NULL;
					EMH_ask_error_text(retcode, &text);
					if (text)
					{
						string err("ERROR: Cannot set final cost to cost item with error: ");
						err.append(text);
						errorMsgs.push_back(err);
						TC_write_syslog("\n[VF] %s", err.c_str());
						MEM_free(text);
					}
				}
				else
				{
					TC_write_syslog("\n[VF] updating cost item successfully");
				}

			}
			else
			{
				TC_write_syslog("\n[VF] dryrun = true or all cost are empty ");
			}

		}
		else
		{
			string err("ERROR: Cannot find cost object of part \"");
			err.append(partNumber).append("\"!");
			errorMsgs.push_back(err);
		}
	}


	output.errorMessages.insert(output.errorMessages.begin(), errorMsgs.begin(), errorMsgs.end());
	printOutput(output);

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s\n", __FUNCTION__);


	return output;
}



