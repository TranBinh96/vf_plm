package com.teamcenter.vinfast.handlers;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesResponse;

import com.teamcenter.soa.client.model.ModelObject;
import com.vf.dialog.SimpleFrame;
import com.vf.utils.Query;

public class DMTargetCostReport extends AbstractHandler {
	private final Logger LOGGER;
	private final String TOKEN_STRING = ";";
	private final String TARGET_COST_NAME_SUFFIX = "_TargetCost";
	private final String PIECE_COST_NAME_SUFFIX = "_ActualPieceCost";
	private final String VEHICLE_USAGE = "_VehicleUsage";
	private final String PIECE_COST_FORM_TYPE = "Actual Piece Cost Form";
	private final String TARGET_COST_FORM_TYPE = "Target Cost Form";
	private final String FINAL = "Final quote ( PR level cost walk)";
	private final String INTERIM = "Interim quote ( Initial Sourcing or Change Form)";
	private final String BUYER = "Buyer Estimate ( No quote)";
	private final String ENGINEER = "Cost Engineering Estimate ( New added part/ no quote)";
	private static LinkedHashMap<Integer, CostObject> mapMasterData;
	private static LinkedHashMap<String, CostObject> mapCostData;
	private static ArrayList<String> listPart;
	private static SimpleFrame frame = null;
	private static TCSession session = null;
	private static TCComponentGroup group = null;
	private static TCComponentRole role = null;
	private static TCComponentUser user = null;
	private static String selectedCarPrg = null;
	private static long startTime = 0;
	private final String PART_NUMBER_HEADER = "Part Number";
	private final String PART_NAME_HEADER = "Part Name";
	private final String PURCHASE_LEVEL_HEADER = "Purchase Level";
	private final String CHANGE_INDEX_HEADER = "Change Index";
	private final String MODULE_HEADER = "Module Group";
	private final String PIECE_COST_TARGET_HEADER = "Piece Cost Target";
	private final String PIECE_COST_TARGET_CURR_HEADER = "Piece Cost Target Currency";
	private final String EDD_COST_TARGET_HEADR = "EDD Cost Target";
	private final String EDD_COST_TARGET_CURR_HEADR = "EDD Cost Target Currency";
	private final String TOOLING_COST_TARGET_HEADER = "Tooling Cost Target";
	private final String TOOLING_COST_TARGET_CURR_HEADER = "Tooling Cost Target Currency";
	private final String VN_ASEAN_LHD_BASE_ICE = "Vietnam/ASEAN LHD Base ICE";
	private final String VN_ASEAN_LHD_MIDDLE_ICE = "Vietnam/ASEAN LHD Middle ICE";
	private final String VN_ASEAN_LHD_HIGH_ICE = "Vietnam/ASEAN LHD High ICE";
	private final String VN_ASEAN_LHD_BASE_BEV = "Vietnam/ASEAN LHD Base BEV";
	private final String VN_ASEAN_LHD_MIDDLE_BEV = "Vietnam/ASEAN LHD Middle BEV";
	private final String VN_ASEAN_LHD_HIGH_BEV = "Vietnam/ASEAN LHD High BEV";
	private final String US_HIGH_BEV = "US High BEV";
	private final String US_BASE_BEV = "US Base BEV";
	private final String PLATFORM_FORM = "Platform Part";
	private final String OWNER_HEADER = "Owner";
	private final String TARGET_COST_SHEET = "Sheet1";
	private final String DATASET_TEMPLATE_NAME = "BOM_Extention_Infomation_Template_Testing";
	private final String REPORT_PREFIX = "BOM_Extention_Report_";
	
	private static String VINFAST_COST_BOM_IGNORE_STATUS_PREFERENCE = "VINFAST_COST_BOM_IGNORE_STATUS";
	private final String[] VINFAST_COST_BOM_IGNORE_STATUS_VALUE;
	
	private static String TEMP_DIR;
	private static SXSSFWorkbook wb;
	private static LinkedList<String> lsHeader;

	public LinkedList<String> getLsHeader() {
		return lsHeader;
	}

	public SXSSFWorkbook getWb() {
		return wb;
	}

	public DMTargetCostReport() {
		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		TCPreferenceService preferenceService = session.getPreferenceService();
		VINFAST_COST_BOM_IGNORE_STATUS_VALUE = preferenceService.getStringValues(VINFAST_COST_BOM_IGNORE_STATUS_PREFERENCE);
		lsHeader = new LinkedList<String>();
		lsHeader.add(PART_NUMBER_HEADER);
		lsHeader.add(PART_NAME_HEADER);
		lsHeader.add(PURCHASE_LEVEL_HEADER);
		lsHeader.add(CHANGE_INDEX_HEADER);
		lsHeader.add(MODULE_HEADER);
		lsHeader.add(PIECE_COST_TARGET_HEADER);
		lsHeader.add(EDD_COST_TARGET_HEADR);
		lsHeader.add(TOOLING_COST_TARGET_HEADER);
		lsHeader.add(VN_ASEAN_LHD_BASE_BEV);
		lsHeader.add(VN_ASEAN_LHD_MIDDLE_BEV);
		lsHeader.add(VN_ASEAN_LHD_HIGH_BEV);
		lsHeader.add(VN_ASEAN_LHD_BASE_ICE);
		lsHeader.add(VN_ASEAN_LHD_MIDDLE_ICE);
		lsHeader.add(VN_ASEAN_LHD_HIGH_ICE);
		lsHeader.add(US_BASE_BEV);
		lsHeader.add(US_HIGH_BEV);
		lsHeader.add(PLATFORM_FORM);
		lsHeader.add(OWNER_HEADER);
		TEMP_DIR = System.getenv("tmp");
		LOGGER = Logger.getLogger(this.getClass());
		
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		SwingUtilities.invokeLater(new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				mapMasterData = new LinkedHashMap<Integer, CostObject>();
				mapCostData = new LinkedHashMap<String, CostObject>();
				// multiMappingCostObj = ArrayListMultimap.create();
				listPart = new ArrayList<String>();

				AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();

				session = (TCSession) app.getSession();
				group = session.getCurrentGroup();
				role = session.getCurrentRole();
				user = session.getUser();

				InterfaceAIFComponent[] bomLine = app.getTargetComponents();

				if (bomLine != null) {
					createDialog(bomLine);
				} else {
					MessageBox.post("Please Select One Bom Line.", "Error", MessageBox.ERROR);
				}

			}
		});
		return null;
	}

	public void createDialog(final InterfaceAIFComponent[] bomLine) {

		ImageIcon frame_Icon = new ImageIcon(getClass().getResource("/icons/KIT.png"));
		Icon ok_Icon = new ImageIcon(getClass().getResource("/icons/ok.png"));
		Icon cancel_Icon = new ImageIcon(getClass().getResource("/icons/cancel_16.png"));

		frame = new SimpleFrame();
		frame.setTitle("Target & Usage Report");
		frame.setIconImage(frame_Icon.getImage());
		frame.setMinimumSize(new Dimension(500, 200));
		frame.label1.setText("<html>The report is based on selected BOM structure.<br/>This action will take few minutes.</html>");
		frame.label1.setHorizontalAlignment(SwingConstants.CENTER);
		frame.btnLeft.setIcon(ok_Icon);
		frame.btnLeft.setText("Continue");

		frame.btnRight.setIcon(cancel_Icon);
		frame.btnRight.setText("Cancel");

		frame.btnLeft.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				Job job = new Job("Creating Report") {
					@Override
					protected IStatus run(IProgressMonitor arg0) {
						// TODO call function query data
						IStatus status = new IStatus() {

							@Override
							public boolean matches(int arg0) {
								// TODO Auto-generated method stub
								return false;
							}

							@Override
							public boolean isOK() {
								// TODO Auto-generated method stub
								return false;
							}

							@Override
							public boolean isMultiStatus() {
								// TODO Auto-generated method stub
								return false;
							}

							@Override
							public int getSeverity() {
								// TODO Auto-generated method stub
								return 0;
							}

							@Override
							public String getPlugin() {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public String getMessage() {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public Throwable getException() {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public int getCode() {
								// TODO Auto-generated method stub
								return 0;
							}

							@Override
							public IStatus[] getChildren() {
								// TODO Auto-generated method stub
								return null;
							}
						};
						frame.btnLeft.setEnabled(false);
						startTime = System.currentTimeMillis();
						int i = 0;
						for (InterfaceAIFComponent oneLine : bomLine) {
							traverseBOM(i, (TCComponentBOMLine) oneLine);
							i++;
						}
						queryData(listPart);
						publishReport();

						frame.dispose();
						return status;
					}
				};
				session.queueOperation(job);
			}

		});

		frame.btnRight.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}

		});

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void queryData(ArrayList<String> listUniqueBomLine) {

		StringBuilder vehicleUsageNames = new StringBuilder();
		for (String partNum : listUniqueBomLine) {
			if (vehicleUsageNames.length() > 0) {
				vehicleUsageNames.append(TOKEN_STRING);
			}
			vehicleUsageNames.append(partNum).append(VEHICLE_USAGE);
		}

		StringBuilder costObjectIDs = new StringBuilder();
		for (String partNum : listUniqueBomLine) {
			if (costObjectIDs.length() > 0) {
				costObjectIDs.append(TOKEN_STRING);
			}
			costObjectIDs.append(partNum);
		}

		/* query all vehicle usage form */
		LinkedHashMap<String, String> queryVehicleUsage = new LinkedHashMap<String, String>();
		queryVehicleUsage.put("Name", vehicleUsageNames.toString());
		queryVehicleUsage.put("Type", "VF4_VehicleUsageForm");
		TCComponent[] vehicleUsage = query("General...", queryVehicleUsage);

		/* query all cost item */
		LinkedHashMap<String, String> queryCostItem = new LinkedHashMap<String, String>();
		queryCostItem.put("Item ID", costObjectIDs.toString());
		queryCostItem.put("Type", "VF4_Cost");
		TCComponent[] costItems = query("___Admin Item...", queryCostItem);

		LinkedHashSet<String> listValidCostItem = new LinkedHashSet<String>();

		if (costItems != null) {
			DataManagementService.getService(session).refreshObjects(costItems);
			for (int i = 0; i < costItems.length; ++i) {
				try {
					TCComponentItem aCostItem = (TCComponentItem) costItems[i];
					String partNum = "";
					if (aCostItem.getProperty("item_id") != null) {
						partNum = aCostItem.getProperty("item_id");
					}
					TCComponent[] costRevsCompo = aCostItem.getRelatedComponents("revision_list");
					if (costRevsCompo != null) {
						for (int k = costRevsCompo.length - 1; k >= 0; --k) {
							TCComponentItemRevision aCostRev = (TCComponentItemRevision) costRevsCompo[k];
							TCComponent[] costCompo = aCostRev.getRelatedComponents("VF4_SourcingCostFormRela");
							String creationDate = "";
							String EDDCostTarget = "";
							String toolingCostTarget = "";
							String pieceCostTarget = "";
							String pieceCostDateModify = "";
							String facilityInvestAmount = "";
							String facilityInvestCurr = "";
							String miscellaneousCost = "";
							String miscellaneousCurr = "";
							String pieceCostValueStatus = "";
							String pieceCostCurr = "";
							String protoPiecePrice = "";
							String protoPiecePriceCurr = "";
							String protoToolingCurr = "";
							String protoToolingValue = "";
							String qualityFinance = "";
							String refurToolingCurr = "";
							String refurToolingValue = "";
							String RNOReferNumber = "";
							String supplierLogicCost = "";
							String supplierPackageAmount = "";
							String toolingInvestValue = "";
							String toolingInvestCurr = "";
							String totalCost = "";
							String totalCostCurr = "";
							String EdndCurr = "";
							String EdndCost = "";
							String releaseStatus = "";
							String industrializeCurr = "";
							String industrializeCost = "";
							boolean hasRightAccess = false; 
							if (aCostRev.getProperty("creation_date") != null) {
								creationDate = aCostRev.getProperty("creation_date");
							}
							if(aCostRev.getProperty("release_status_list") != null) {
								releaseStatus = aCostRev.getProperty("release_status_list");
							}
							
							/*ignore base line revision*/
							String []partStatus = releaseStatus.split(",");
							List<String> ignoreStatus = Arrays.asList(VINFAST_COST_BOM_IGNORE_STATUS_VALUE);
							boolean isIgnore = false;
							if(partStatus != null && partStatus.length > 0) {
								for(String str : ignoreStatus) {
									if(Arrays.asList(partStatus).contains(str)) {
										LOGGER.info("[BOMCostReport] Ignore revision: " + partNum + "/" + (k + 1));
										isIgnore = true;
									}
								}
							}
							if(isIgnore) {
								continue;
							}
							/*ignore base line revision*/
							
							if (costCompo.length != 0) {
								for (int x = 0; x < costCompo.length; ++x) {
									TCComponentForm costForm = (TCComponentForm) costCompo[x];
									String formType = costForm.getProperty("object_type");
									if (formType.compareToIgnoreCase(TARGET_COST_FORM_TYPE) == 0) {
										/* get target cost information */
										String[] requestProp = { "vf4_ednd_cost_value_target", "vf4_tooling_invest_target", "vf4_piece_cost_value_target" };
										String[] responseProp = costForm.getProperties(requestProp);
										if (responseProp[0] != null) {
											EDDCostTarget = responseProp[0];
										}
										if (responseProp[1] != null) {
											toolingCostTarget = responseProp[1];
										}
										if (responseProp[2] != null) {
											pieceCostTarget = responseProp[2];
										}
									} else if (formType.compareToIgnoreCase(PIECE_COST_FORM_TYPE) == 0) {
										/* get piece cost information */
										String[] requestProp = { "vf4_facility_invest_amount", "vf4_facility_invest_curr", "vf4_miscellaneous_cost", "vf4_miscellaneous_cost_curr",
												"vf4_piece_cost_curr", "vf4_piece_cost_value_status", "vf4_proto_piece_price", "vf4_proto_piece_price_curr", "vf4_proto_tooling_curr",
												"vf4_proto_tooling_value", "vf4_quality_of_finance", "vf4_refurbish_tooling_curr", "vf4_refurbish_tooling_value", "vf4_rno_refer_number",
												"vf4_supplier_logisis_cost", "vf4_supplier_package_amount", "vf4_tooling_invest_value", "vf4_tooling_invtest_curr", "vf4_total_cost_manually",
												"vf4_total_cost_status_curr", "vf4Ednd_curr", "vf4EdndCost", "vf4_industrialization_cost","vf4_industrialization_curr" };
										String[] responseProp = costForm.getProperties(requestProp);
										if (responseProp[0] != null) {
											facilityInvestAmount = responseProp[0];
										}
										if (responseProp[1] != null) {
											facilityInvestCurr = responseProp[1];
										}
										if (responseProp[2] != null) {
											miscellaneousCost = responseProp[2];
										}
										if (responseProp[3] != null) {
											miscellaneousCurr = responseProp[3];
										}
										if (responseProp[4] != null) {
											pieceCostCurr = responseProp[4];
										}
										if (responseProp[5] != null) {
											pieceCostValueStatus = responseProp[5];
										}
										if (responseProp[6] != null) {
											protoPiecePrice = responseProp[6];
										}
										if (responseProp[7] != null) {
											protoPiecePriceCurr = responseProp[7];
										}
										if (responseProp[8] != null) {
											protoToolingCurr = responseProp[8];
										}
										if (responseProp[9] != null) {
											protoToolingValue = responseProp[9];
										}
										if (responseProp[10] != null) {
											qualityFinance = responseProp[10];
										}
										if (responseProp[11] != null) {
											refurToolingCurr = responseProp[11];
										}
										if (responseProp[12] != null) {
											refurToolingValue = responseProp[12];
										}
										if (responseProp[13] != null) {
											RNOReferNumber = responseProp[13];
										}
										if (responseProp[14] != null) {
											supplierLogicCost = responseProp[14];
										}
										if (responseProp[15] != null) {
											supplierPackageAmount = responseProp[15];
										}
										if (responseProp[16] != null) {
											toolingInvestValue = responseProp[16];
										}
										if (responseProp[17] != null) {
											toolingInvestCurr = responseProp[17];
										}
										if (responseProp[18] != null) {
											totalCost = responseProp[18];
										}
										if (responseProp[19] != null) {
											totalCostCurr = responseProp[19];
										}
										if (responseProp[20] != null) {
											EdndCurr = responseProp[20];
										}
										if (responseProp[21] != null) {
											EdndCost = responseProp[21];
										}
										if (responseProp[22] != null) {
											industrializeCost = responseProp[22];
										}
										if (responseProp[23] != null) {
											industrializeCurr = responseProp[23];
										}
									}
								}
								hasRightAccess = true;
							}
							/* store cost data to temp memory */

							CostObject costExisted = mapCostData.get(partNum);
							if (costExisted == null) {
								CostObject costNew = new CostObject();

									costNew.setEDDCostTarget(EDDCostTarget);
									costNew.setToolingCostTarget(toolingCostTarget);
									costNew.setPieceCostTarget(pieceCostTarget);

									/*
									 * update latest tooling cost to mapping
									 */
									costNew.setFacilityInvestAmount(facilityInvestAmount);
									costNew.setFacilityInvestCurr(facilityInvestCurr);
									costNew.setMiscellaneousCost(miscellaneousCost);
									costNew.setMiscellaneousCurr(miscellaneousCurr);
									costNew.setProtoPiecePrice(protoPiecePrice);
									costNew.setProtoPiecePriceCurr(protoPiecePriceCurr);
									costNew.setProtoToolingCurr(protoToolingCurr);
									costNew.setProtoToolingValue(protoToolingValue);
									costNew.setRefurToolingCurr(refurToolingCurr);
									costNew.setRefurToolingValue(refurToolingValue);
									costNew.setToolingInvestValue(toolingInvestValue);
									costNew.setToolingInvestCurr(toolingInvestCurr);
									costNew.setEdndCost(EdndCost);
									costNew.setEdndCurr(EdndCurr);
									costNew.setIndustrializeCost(industrializeCost);
									costNew.setIndustrializeCurr(industrializeCurr);

								if (qualityFinance.compareToIgnoreCase(INTERIM) == 0 || qualityFinance.compareToIgnoreCase("") == 0) {
									/* update interim actual cost */
									costNew.setPieceCostCurr(pieceCostCurr);
									costNew.setPieceCostValueStatus(pieceCostValueStatus);
									costNew.setSupplierLogicCost(supplierLogicCost);
									costNew.setSupplierPackageAmount(supplierPackageAmount);
									costNew.setTotalCost(totalCost);
									costNew.setTotalCostCurr(totalCostCurr);
								}
								if (qualityFinance.compareToIgnoreCase(FINAL) == 0) {
									/* update final actual cost */
									costNew.setFinalPieceCostCurr(pieceCostCurr);
									costNew.setFinalPieceCostValueStatus(pieceCostValueStatus);
									costNew.setFinalSupplierLogicCost(supplierLogicCost);
									costNew.setFinalSupplierPackageAmount(supplierPackageAmount);
									costNew.setFinalTotalCost(totalCost);
									costNew.setFinalTotalCostCurr(totalCostCurr);
								}
								if (qualityFinance.compareToIgnoreCase(BUYER) == 0) {
									/* update buyer actual cost */
									costNew.setBuyerPieceCostCurr(pieceCostCurr);
									costNew.setBuyerPieceCostValueStatus(pieceCostValueStatus);
									costNew.setBuyerSupplierLogicCost(supplierLogicCost);
									costNew.setBuyerSupplierPackageAmount(supplierPackageAmount);
									costNew.setBuyerTotalCost(totalCost);
									costNew.setBuyerTotalCostCurr(totalCostCurr);
								}
								if (qualityFinance.compareToIgnoreCase(ENGINEER) == 0) {
									/* update engineer actual cost */
									costNew.setEngineerPieceCostCurr(pieceCostCurr);
									costNew.setEngineerPieceCostValueStatus(pieceCostValueStatus);
									costNew.setEngineerSupplierLogicCost(supplierLogicCost);
									costNew.setEngineerSupplierPackageAmount(supplierPackageAmount);
									costNew.setEngineerTotalCost(totalCost);
									costNew.setEngineerTotalCostCurr(totalCostCurr);
								}
								costNew.setRevisionCreationDate(creationDate);
								costNew.setRightAccess(hasRightAccess);
								mapCostData.put(partNum, costNew);
							} else {
									/*
									 * update latest target cost to mapping
									 */
									costExisted.setEDDCostTarget(EDDCostTarget);
									costExisted.setToolingCostTarget(toolingCostTarget);
									costExisted.setPieceCostTarget(pieceCostTarget);

									/*
									 * update latest tooling cost to mapping
									 */
									costExisted.setFacilityInvestAmount(facilityInvestAmount);
									costExisted.setFacilityInvestCurr(facilityInvestCurr);
									costExisted.setMiscellaneousCost(miscellaneousCost);
									costExisted.setMiscellaneousCurr(miscellaneousCurr);
									costExisted.setProtoPiecePrice(protoPiecePrice);
									costExisted.setProtoPiecePriceCurr(protoPiecePriceCurr);
									costExisted.setProtoToolingCurr(protoToolingCurr);
									costExisted.setProtoToolingValue(protoToolingValue);
									costExisted.setRefurToolingCurr(refurToolingCurr);
									costExisted.setRefurToolingValue(refurToolingValue);
									costExisted.setToolingInvestValue(toolingInvestValue);
									costExisted.setToolingInvestCurr(toolingInvestCurr);
									costExisted.setEdndCost(EdndCost);
									costExisted.setEdndCurr(EdndCurr);
									costExisted.setIndustrializeCost(industrializeCost);
									costExisted.setIndustrializeCurr(industrializeCurr);
								if (qualityFinance.compareToIgnoreCase(INTERIM) == 0 || qualityFinance.compareToIgnoreCase("") == 0) {
									/* update interim actual cost */
									costExisted.setPieceCostCurr(pieceCostCurr);
									costExisted.setPieceCostValueStatus(pieceCostValueStatus);
									costExisted.setSupplierLogicCost(supplierLogicCost);
									costExisted.setSupplierPackageAmount(supplierPackageAmount);
									costExisted.setTotalCost(totalCost);
									costExisted.setTotalCostCurr(totalCostCurr);
								}
								if (qualityFinance.compareToIgnoreCase(FINAL) == 0) {
									/* update final actual cost */
									costExisted.setFinalPieceCostCurr(pieceCostCurr);
									costExisted.setFinalPieceCostValueStatus(pieceCostValueStatus);
									costExisted.setFinalSupplierLogicCost(supplierLogicCost);
									costExisted.setFinalSupplierPackageAmount(supplierPackageAmount);
									costExisted.setFinalTotalCost(totalCost);
									costExisted.setFinalTotalCostCurr(totalCostCurr);
								}
								if (qualityFinance.compareToIgnoreCase(BUYER) == 0) {
									/* update buyer actual cost */
									costExisted.setBuyerPieceCostCurr(pieceCostCurr);
									costExisted.setBuyerPieceCostValueStatus(pieceCostValueStatus);
									costExisted.setBuyerSupplierLogicCost(supplierLogicCost);
									costExisted.setBuyerSupplierPackageAmount(supplierPackageAmount);
									costExisted.setBuyerTotalCost(totalCost);
									costExisted.setBuyerTotalCostCurr(totalCostCurr);
								}
								if (qualityFinance.compareToIgnoreCase(ENGINEER) == 0) {
									/* update engineer actual cost */
									costExisted.setEngineerPieceCostCurr(pieceCostCurr);
									costExisted.setEngineerPieceCostValueStatus(pieceCostValueStatus);
									costExisted.setEngineerSupplierLogicCost(supplierLogicCost);
									costExisted.setEngineerSupplierPackageAmount(supplierPackageAmount);
									costExisted.setEngineerTotalCost(totalCost);
									costExisted.setEngineerTotalCostCurr(totalCostCurr);
								}
								costExisted.setRevisionCreationDate(creationDate);
								costExisted.setRightAccess(hasRightAccess);
							}
							/*18Jun2020-add break to show only latest cost revision*/
							break;
						}
					}
					listValidCostItem.add(partNum);
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
			
			/* validate object cost has been created */
			for (Map.Entry<Integer, CostObject> entryMaster : mapMasterData.entrySet()) {
				CostObject costMaster = (CostObject) entryMaster.getValue();
				String partNum = costMaster.getPartNum();
				CostObject costData = mapCostData.get(partNum);
				if (costData != null) {
					costMaster.setEDDCostTarget(costData.getEDDCostTarget());
					costMaster.setToolingCostTarget(costData.getToolingCostTarget());
					costMaster.setPieceCostTarget(costData.getPieceCostTarget());
					costMaster.setFacilityInvestAmount(costData.getFacilityInvestAmount());
					costMaster.setFacilityInvestCurr(costData.getFacilityInvestCurr());
					costMaster.setMiscellaneousCost(costData.getMiscellaneousCost());
					costMaster.setMiscellaneousCurr(costData.getMiscellaneousCurr());
					costMaster.setPieceCostValueStatus(costData.getPieceCostValueStatus());
					costMaster.setPieceCostCurr(costData.getPieceCostCurr());
					costMaster.setProtoPiecePrice(costData.getProtoPiecePrice());
					costMaster.setProtoPiecePriceCurr(costData.getProtoPiecePriceCurr());
					costMaster.setProtoToolingValue(costData.getProtoToolingValue());
					costMaster.setProtoToolingCurr(costData.getProtoToolingCurr());
					costMaster.setQualityFinance(costData.getQualityFinance());
					costMaster.setRefurToolingCurr(costData.getRefurToolingCurr());
					costMaster.setRefurToolingValue(costData.getRefurToolingValue());
					costMaster.setRNOReferNumber(costData.getRNOReferNumber());
					costMaster.setSupplierLogicCost(costData.getSupplierLogicCost());
					costMaster.setSupplierPackageAmount(costData.getSupplierPackageAmount());
					costMaster.setToolingInvestCurr(costData.getToolingInvestCurr());
					costMaster.setToolingInvestValue(costData.getToolingInvestValue());
					costMaster.setTotalCost(costData.getTotalCost());
					costMaster.setTotalCostCurr(costData.getTotalCostCurr());
					costMaster.setEdndCost(costData.getEdndCost());
					costMaster.setEdndCurr(costData.getEdndCurr());
					costMaster.setIndustrializeCost(costData.getIndustrializeCost());
					costMaster.setIndustrializeCurr(costData.getIndustrializeCurr());
					
					/* final cost */
					costMaster.setFinalPieceCostCurr(costData.getFinalPieceCostCurr());
					costMaster.setFinalPieceCostValueStatus(costData.getFinalPieceCostValueStatus());
					costMaster.setFinalSupplierLogicCost(costData.getFinalSupplierLogicCost());
					costMaster.setFinalSupplierPackageAmount(costData.getFinalSupplierPackageAmount());
					costMaster.setFinalTotalCost(costData.getFinalTotalCost());
					costMaster.setFinalTotalCostCurr(costData.getFinalTotalCostCurr());

					/* update buyer actual cost */
					costMaster.setBuyerPieceCostCurr(costData.getBuyerPieceCostCurr());
					costMaster.setBuyerPieceCostValueStatus(costData.getBuyerPieceCostValueStatus());
					costMaster.setBuyerSupplierLogicCost(costData.getBuyerSupplierLogicCost());
					costMaster.setBuyerSupplierPackageAmount(costData.getBuyerSupplierPackageAmount());
					costMaster.setBuyerTotalCost(costData.getBuyerTotalCost());
					costMaster.setBuyerTotalCostCurr(costData.getBuyerTotalCostCurr());

					/* update engineer actual cost */
					costMaster.setEngineerPieceCostCurr(costData.getEngineerPieceCostCurr());
					costMaster.setEngineerPieceCostValueStatus(costData.getEngineerPieceCostValueStatus());
					costMaster.setEngineerSupplierLogicCost(costData.getEngineerSupplierLogicCost());
					costMaster.setEngineerSupplierPackageAmount(costData.getEngineerSupplierPackageAmount());
					costMaster.setEngineerTotalCost(costData.getEngineerTotalCost());
					costMaster.setEngineerTotalCostCurr(costData.getEngineerTotalCostCurr());

					costMaster.setRightAccess(costData.isRightAccess());
				}
				if (listValidCostItem.contains(partNum)) {
					costMaster.setFound(true);
				}
			}
			
		}
		
		if (vehicleUsage != null) {
			DataManagementService.getService(session).refreshObjects(vehicleUsage);
			for (int j = 0; j != vehicleUsage.length; ++j) {
				TCComponentForm vehiForm = (TCComponentForm) vehicleUsage[j];
				try {
					// vehiForm.refresh();
					String formName = vehiForm.getProperty("object_name");
					String partNum = "";
					String VNASEANBaseICE = "";
					String VNASEANMiddleICE = "";
					String VNASEANHighICE = "";
					String VNASEANBaseBEV = "";
					String VNASEANMiddleBEV = "";
					String VNASEANHighBEV = "";
					String USHighBEV = "";
					String USBaseBev = "";
					String commonPart = "";
					String platformPart = "";
					if (formName != null) {
						AIFComponentContext[] aifPrimary = vehiForm.getPrimary();
						if(aifPrimary.length > 1) {
							System.out.println("[TargetCostReport] Vehicle usage form attached to 2 part: " + formName);
							continue;
						}
						if(aifPrimary.length == 0) {
							System.out.println("[TargetCostReport] Vehicle usage form doesn't attach to any part: " + formName);
							continue;
						}
						TCComponent primaryComp = (TCComponent) aifPrimary[0].getComponent();
						partNum = primaryComp.getProperty("item_id");

						if (vehiForm.getProperty("vf4_vn_asean_base_bev") != null) {
							VNASEANBaseBEV = vehiForm.getProperty("vf4_vn_asean_base_bev");
						}
						if (vehiForm.getProperty("vf4_vn_asean_middle_bev") != null) {
							VNASEANMiddleBEV = vehiForm.getProperty("vf4_vn_asean_middle_bev");
						}
						if (vehiForm.getProperty("vf4_vn_asean_high_bev") != null) {
							VNASEANHighBEV = vehiForm.getProperty("vf4_vn_asean_high_bev");
						}
						if (vehiForm.getProperty("vf4_vn_asean_base_ice") != null) {
							VNASEANBaseICE = vehiForm.getProperty("vf4_vn_asean_base_ice");
						}
						if (vehiForm.getProperty("vf4_vn_asean_middle_ice") != null) {
							VNASEANMiddleICE = vehiForm.getProperty("vf4_vn_asean_middle_ice");
						}
						if (vehiForm.getProperty("vf4_vn_asean_high_ice") != null) {
							VNASEANHighICE = vehiForm.getProperty("vf4_vn_asean_high_ice");
						}
						if (vehiForm.getProperty("vf4_us_high_bev") != null) {
							USHighBEV = vehiForm.getProperty("vf4_us_high_bev");
						}
						if (vehiForm.getProperty("vf4_boolean_1") != null) {
							USBaseBev = vehiForm.getProperty("vf4_boolean_1");
						}
						if (vehiForm.getProperty("vf4_boolean_2") != null) {
							commonPart = vehiForm.getProperty("vf4_boolean_2");
						}
						if (vehiForm.getProperty("vf4_platform_part") != null) {
							platformPart = vehiForm.getProperty("vf4_platform_part");
						}
						for (Map.Entry<Integer, CostObject> entry : mapMasterData.entrySet()) {
							CostObject obj = (CostObject) entry.getValue();
							if (obj.getPartNum().compareToIgnoreCase(partNum) == 0) {
								obj.setVNASEANBaseBEV(VNASEANBaseBEV);
								obj.setVNASEANHighBEV(VNASEANHighBEV);
								obj.setVNASEANMiddleBEV(VNASEANMiddleBEV);
								obj.setVNASEANBaseICE(VNASEANBaseICE);
								obj.setVNASEANHighICE(VNASEANHighICE);
								obj.setVNASEANMiddleICE(VNASEANMiddleICE);
								obj.setUSBaseBev(USBaseBev);
								obj.setUSHighBEV(USHighBEV);
								obj.setCommonPart(commonPart);
								obj.setPlatformPart(platformPart);
							}
						}
					} else {
						continue;
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private TCComponent[] query(String queryName, LinkedHashMap<String, String> queryInput) {
		TCComponent[] objects = null;
		try {
			SavedQueryService QRservices = SavedQueryService.getService(session);
			FindSavedQueriesCriteriaInput qry[] = new FindSavedQueriesCriteriaInput[1];
			FindSavedQueriesCriteriaInput qurey = new FindSavedQueriesCriteriaInput();
			String name[] = { queryName };
			String desc[] = { "" };
			qurey.queryNames = name;
			qurey.queryDescs = desc;
			qurey.queryType = 0;

			qry[0] = qurey;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
			ModelObject[] result = responce1.savedQueries;

			SavedQueryInput qc = new SavedQueryInput();
			SavedQueryInput qc_v[] = new SavedQueryInput[1];

			qc.query = (TCComponentQuery) result[0];
			qc.entries = new String[queryInput.size()];
			qc.values = new String[queryInput.size()];
			int i = 0;
			for (Entry<String, String> pair : queryInput.entrySet()) {
				qc.entries[i] = pair.getKey();
				qc.values[i] = pair.getValue();
				i++;
			}
			qc_v[0] = qc;

			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);

			SavedQueryResults[] results = responce.arrayOfResults;

			if (results[0].numOfObjects != 0) {
				objects = results[0].objects;
			} else {
				System.out.println("NO Cost Object FOUND");
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objects;
	}

	public void writeHeaderLine(String sheetName, LinkedList<String> stDisplayList) {
		SXSSFSheet spreadsheet = wb.createSheet(sheetName);
		Font headerFont = wb.createFont();
		// headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerFont.setBold(true);
		CellStyle headerCellStyle = wb.createCellStyle();
		headerCellStyle.setFont(headerFont);
		// headerCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
		headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
		// headerCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		// XSSFRow headerRow = spreadsheet.createRow(0);

		// int counter = 0;
		//
		// for (String temp : stDisplayList) {
		// getHeaderCell(counter, headerRow, temp, headerCellStyle);
		// counter++;
		// }
		// wb.getSheet(sheetName).createFreezePane(0, 1);
	}

	private XSSFCell getHeaderCell(int id, XSSFRow headerRow, String displayValue, CellStyle headerCellStyle) {
		XSSFCell cell = headerRow.createCell(id);
		cell.setCellValue(displayValue);
		cell.setCellStyle(headerCellStyle);
		return cell;
	}

	@SuppressWarnings("rawtypes")
	public void writeARowReport() {

		// TODO loop propmap and write file
		SXSSFSheet spreadsheet = wb.getSheet("Sheet1");
		int rownum = 2;
		SXSSFWorkbook wb = spreadsheet.getWorkbook();

		CellStyle headerCellStyle = wb.createCellStyle();
		headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
		headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerCellStyle.setBorderTop(BorderStyle.THIN);
		headerCellStyle.setBorderBottom(BorderStyle.THIN);
		headerCellStyle.setBorderLeft(BorderStyle.THIN);
		headerCellStyle.setBorderRight(BorderStyle.THIN);

		for (Map.Entry entry : mapMasterData.entrySet()) {
			CostObject partData = (CostObject) entry.getValue();
			SXSSFRow row = spreadsheet.createRow(rownum);

			SXSSFCell levelCell = row.createCell(0);
			levelCell.setCellValue(partData.getBomLevel());
			levelCell.setCellStyle(headerCellStyle);

			SXSSFCell moduleCell = row.createCell(1);
			moduleCell.setCellValue(partData.getModuleName());
			moduleCell.setCellStyle(headerCellStyle);
			
			SXSSFCell mainModuleCell = row.createCell(2);
			mainModuleCell.setCellValue(partData.getMainModuleGrp());
			mainModuleCell.setCellStyle(headerCellStyle);
			
			SXSSFCell moduleNameCell = row.createCell(3);
			moduleNameCell.setCellValue(partData.getModuleGrp());
			moduleNameCell.setCellStyle(headerCellStyle);

			SXSSFCell partNumCell = row.createCell(4);
			partNumCell.setCellValue(partData.getPartNum());
			partNumCell.setCellStyle(headerCellStyle);

			SXSSFCell partNameCell = row.createCell(5);
			partNameCell.setCellValue(partData.getPartName());
			partNameCell.setCellStyle(headerCellStyle);

			SXSSFCell procurementCell = row.createCell(6);
			procurementCell.setCellValue(partData.getProcurementLvl());
			procurementCell.setCellStyle(headerCellStyle);

			SXSSFCell supplierType = row.createCell(7);
			supplierType.setCellValue(partData.getSupplierType());
			supplierType.setCellStyle(headerCellStyle);

			SXSSFCell designAssumCell = row.createCell(8);
			designAssumCell.setCellValue(partData.getDesignAssumption());
			designAssumCell.setCellStyle(headerCellStyle);

			SXSSFCell weightCell = row.createCell(9);
			weightCell.setCellValue(partData.getWeight());
			weightCell.setCellStyle(headerCellStyle);
			
			SXSSFCell estiWeightCell = row.createCell(10);
			estiWeightCell.setCellValue(partData.getEstimatedWeight());
			estiWeightCell.setCellStyle(headerCellStyle);
			
			SXSSFCell measuredWeightCell = row.createCell(11);
			measuredWeightCell.setCellValue(partData.getMeasuredWeight());
			measuredWeightCell.setCellStyle(headerCellStyle);
			
			SXSSFCell materialCell = row.createCell(12);
			materialCell.setCellValue(partData.getMaterial());
			materialCell.setCellStyle(headerCellStyle);

			SXSSFCell sorNumCell = row.createCell(13);
			sorNumCell.setCellValue(partData.getSorNum());
			sorNumCell.setCellStyle(headerCellStyle);

			SXSSFCell sorNameCell = row.createCell(14);
			sorNameCell.setCellValue(partData.getSorName());
			sorNameCell.setCellStyle(headerCellStyle);

			SXSSFCell eddCostCell = row.createCell(15);
			if (partData.isRightAccess()) {
				if (partData.getEDDCostTarget().isEmpty()) {
					eddCostCell.setCellValue(0);
				} else {
					eddCostCell.setCellValue(Double.valueOf(partData.getEDDCostTarget()));
				}
			} else {
				if (!partData.isFound()) {
					eddCostCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					eddCostCell.setCellValue("NO ACCESS");
				}
			}

			eddCostCell.setCellStyle(headerCellStyle);

			SXSSFCell toolCostCell = row.createCell(16);
			if (partData.isRightAccess()) {
				if (partData.getToolingCostTarget().isEmpty()) {
					toolCostCell.setCellValue(0);
				} else {
					toolCostCell.setCellValue(Double.valueOf(partData.getToolingCostTarget()));
				}
			} else {
				if (!partData.isFound()) {
					toolCostCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					toolCostCell.setCellValue("NO ACCESS");
				}
			}
			toolCostCell.setCellStyle(headerCellStyle);

			SXSSFCell pieceCostCell = row.createCell(17);
			if (partData.isRightAccess()) {
				if (partData.getPieceCostTarget().isEmpty()) {
					pieceCostCell.setCellValue(0);
				} else {
					pieceCostCell.setCellValue(Double.valueOf(partData.getPieceCostTarget()));
				}
			} else {
				if (!partData.isFound()) {
					pieceCostCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					pieceCostCell.setCellValue("NO ACCESS");
				}
			}
			pieceCostCell.setCellStyle(headerCellStyle);

			SXSSFCell interimPieceCostCell = row.createCell(18);
			if (partData.isRightAccess()) {
				if (partData.getPieceCostValueStatus().isEmpty()) {
					interimPieceCostCell.setCellValue(0);
				} else {
					interimPieceCostCell.setCellValue(Double.valueOf(partData.getPieceCostValueStatus()));
				}
			} else {
				if (!partData.isFound()) {
					interimPieceCostCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					interimPieceCostCell.setCellValue("NO ACCESS");
				}
			}
			interimPieceCostCell.setCellStyle(headerCellStyle);

			SXSSFCell interimPieceCostCurrCell = row.createCell(19);
			if (partData.isRightAccess()) {
				interimPieceCostCurrCell.setCellValue(partData.getPieceCostCurr());
			} else {
				if (!partData.isFound()) {
					interimPieceCostCurrCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					interimPieceCostCurrCell.setCellValue("NO ACCESS");
				}
			}
			interimPieceCostCurrCell.setCellStyle(headerCellStyle);

			SXSSFCell interimSupplierPackingCell = row.createCell(20);
			if (partData.isRightAccess()) {
				if (partData.getSupplierPackageAmount().isEmpty()) {
					interimSupplierPackingCell.setCellValue(0);
				} else {
					interimSupplierPackingCell.setCellValue(Double.valueOf(partData.getSupplierPackageAmount()));
				}
			} else {
				if (!partData.isFound()) {
					interimSupplierPackingCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					interimSupplierPackingCell.setCellValue("NO ACCESS");
				}
			}
			interimSupplierPackingCell.setCellStyle(headerCellStyle);

			SXSSFCell interimSupplierLogisticCell = row.createCell(21);
			if (partData.isRightAccess()) {
				if (partData.getSupplierLogicCost().isEmpty()) {
					interimSupplierLogisticCell.setCellValue(0);
				} else {
					interimSupplierLogisticCell.setCellValue(Double.valueOf(partData.getSupplierLogicCost()));
				}
			} else {
				if (!partData.isFound()) {
					interimSupplierLogisticCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					interimSupplierLogisticCell.setCellValue("NO ACCESS");
				}
			}
			interimSupplierLogisticCell.setCellStyle(headerCellStyle);

			SXSSFCell interimTotalCostCell = row.createCell(22);
			if (partData.isRightAccess()) {
				if (partData.getTotalCost().isEmpty()) {
					interimTotalCostCell.setCellValue(0);
				} else {
					interimTotalCostCell.setCellValue(Double.valueOf(partData.getTotalCost()));
				}
			} else {
				if (!partData.isFound()) {
					interimTotalCostCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					interimTotalCostCell.setCellValue("NO ACCESS");
				}
			}
			interimTotalCostCell.setCellStyle(headerCellStyle);

			SXSSFCell finalPieceCostCell = row.createCell(23);
			if (partData.isRightAccess()) {
				if (partData.getFinalPieceCostValueStatus().isEmpty()) {
					finalPieceCostCell.setCellValue(0);
				} else {
					finalPieceCostCell.setCellValue(Double.valueOf(partData.getFinalPieceCostValueStatus()));
				}
			} else {
				if (!partData.isFound()) {
					finalPieceCostCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					finalPieceCostCell.setCellValue("NO ACCESS");
				}
			}
			finalPieceCostCell.setCellStyle(headerCellStyle);

			SXSSFCell finalPieceCostCurrCell = row.createCell(24);
			if (partData.isRightAccess()) {
				finalPieceCostCurrCell.setCellValue(partData.getFinalPieceCostCurr());
			} else {
				if (!partData.isFound()) {
					finalPieceCostCurrCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					finalPieceCostCurrCell.setCellValue("NO ACCESS");
				}
			}
			finalPieceCostCurrCell.setCellStyle(headerCellStyle);

			SXSSFCell finalSupplierPackingCell = row.createCell(25);
			if (partData.isRightAccess()) {
				if (partData.getFinalSupplierPackageAmount().isEmpty()) {
					finalSupplierPackingCell.setCellValue(0);
				} else {
					finalSupplierPackingCell.setCellValue(Double.valueOf(partData.getFinalSupplierPackageAmount()));
				}
			} else {
				if (!partData.isFound()) {
					finalSupplierPackingCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					finalSupplierPackingCell.setCellValue("NO ACCESS");
				}
			}
			finalSupplierPackingCell.setCellStyle(headerCellStyle);

			SXSSFCell finalSupplierLogisticCell = row.createCell(26);
			if (partData.isRightAccess()) {
				if (partData.getFinalSupplierLogicCost().isEmpty()) {
					finalSupplierLogisticCell.setCellValue(0);
				} else {
					finalSupplierLogisticCell.setCellValue(Double.valueOf(partData.getFinalSupplierLogicCost()));
				}
			} else {
				if (!partData.isFound()) {
					finalSupplierLogisticCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					finalSupplierLogisticCell.setCellValue("NO ACCESS");
				}
			}
			finalSupplierLogisticCell.setCellStyle(headerCellStyle);

			SXSSFCell finalTotalCostCell = row.createCell(27);
			if (partData.isRightAccess()) {
				if (partData.getFinalTotalCost().isEmpty()) {
					finalTotalCostCell.setCellValue(0);
				} else {
					finalTotalCostCell.setCellValue(Double.valueOf(partData.getFinalTotalCost()));
				}
			} else {
				if (!partData.isFound()) {
					finalTotalCostCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					finalTotalCostCell.setCellValue("NO ACCESS");
				}
			}
			finalTotalCostCell.setCellStyle(headerCellStyle);

			SXSSFCell buyerPieceCostCell = row.createCell(28);
			if (partData.isRightAccess()) {
				if (partData.getBuyerPieceCostValueStatus().isEmpty()) {
					buyerPieceCostCell.setCellValue(0);
				} else {
					buyerPieceCostCell.setCellValue(Double.valueOf(partData.getBuyerPieceCostValueStatus()));
				}
			} else {
				if (!partData.isFound()) {
					buyerPieceCostCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					buyerPieceCostCell.setCellValue("NO ACCESS");
				}
			}
			buyerPieceCostCell.setCellStyle(headerCellStyle);

			SXSSFCell buyerPieceCostCurrCell = row.createCell(29);
			if (partData.isRightAccess()) {
				buyerPieceCostCurrCell.setCellValue(partData.getBuyerPieceCostCurr());
			} else {
				if (!partData.isFound()) {
					buyerPieceCostCurrCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					buyerPieceCostCurrCell.setCellValue("NO ACCESS");
				}
			}
			buyerPieceCostCurrCell.setCellStyle(headerCellStyle);

			SXSSFCell buyerSupplierPackingCell = row.createCell(30);
			if (partData.isRightAccess()) {
				if (partData.getBuyerSupplierPackageAmount().isEmpty()) {
					buyerSupplierPackingCell.setCellValue(0);
				} else {
					buyerSupplierPackingCell.setCellValue(Double.valueOf(partData.getBuyerSupplierPackageAmount()));
				}
			} else {
				if (!partData.isFound()) {
					buyerSupplierPackingCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					buyerSupplierPackingCell.setCellValue("NO ACCESS");
				}
			}
			buyerSupplierPackingCell.setCellStyle(headerCellStyle);

			SXSSFCell buyerSupplierLogisticCell = row.createCell(31);
			if (partData.isRightAccess()) {
				if (partData.getBuyerSupplierLogicCost().isEmpty()) {
					buyerSupplierLogisticCell.setCellValue(0);
				} else {
					buyerSupplierLogisticCell.setCellValue(Double.valueOf(partData.getBuyerSupplierLogicCost()));
				}
			} else {
				if (!partData.isFound()) {
					buyerSupplierLogisticCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					buyerSupplierLogisticCell.setCellValue("NO ACCESS");
				}
			}
			buyerSupplierLogisticCell.setCellStyle(headerCellStyle);

			SXSSFCell buyerTotalCostCell = row.createCell(32);
			if (partData.isRightAccess()) {
				if (partData.getBuyerTotalCost().isEmpty()) {
					buyerTotalCostCell.setCellValue(0);
				} else {
					buyerTotalCostCell.setCellValue(Double.valueOf(partData.getBuyerTotalCost()));
				}
			} else {
				if (!partData.isFound()) {
					buyerTotalCostCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					buyerTotalCostCell.setCellValue("NO ACCESS");
				}
			}
			buyerTotalCostCell.setCellStyle(headerCellStyle);

			SXSSFCell engineerPieceCostCell = row.createCell(33);
			if (partData.isRightAccess()) {
				if (partData.getEngineerPieceCostValueStatus().isEmpty()) {
					engineerPieceCostCell.setCellValue(0);
				} else {
					engineerPieceCostCell.setCellValue(Double.valueOf(partData.getEngineerPieceCostValueStatus()));
				}
			} else {
				if (!partData.isFound()) {
					engineerPieceCostCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					engineerPieceCostCell.setCellValue("NO ACCESS");
				}
			}
			engineerPieceCostCell.setCellStyle(headerCellStyle);

			SXSSFCell engineerPieceCostCurrCell = row.createCell(34);
			if (partData.isRightAccess()) {
				engineerPieceCostCurrCell.setCellValue(partData.getEngineerPieceCostCurr());
			} else {
				if (!partData.isFound()) {
					engineerPieceCostCurrCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					engineerPieceCostCurrCell.setCellValue("NO ACCESS");
				}
			}
			engineerPieceCostCurrCell.setCellStyle(headerCellStyle);

			SXSSFCell engineerSupplierPackingCell = row.createCell(35);
			if (partData.isRightAccess()) {
				if (partData.getEngineerSupplierPackageAmount().isEmpty()) {
					engineerSupplierPackingCell.setCellValue(0);
				} else {
					engineerSupplierPackingCell.setCellValue(Double.valueOf(partData.getEngineerSupplierPackageAmount()));
				}
			} else {
				if (!partData.isFound()) {
					engineerSupplierPackingCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					engineerSupplierPackingCell.setCellValue("NO ACCESS");
				}
			}
			engineerSupplierPackingCell.setCellStyle(headerCellStyle);

			SXSSFCell engineerSupplierLogisticCell = row.createCell(36);
			if (partData.isRightAccess()) {
				if (partData.getEngineerSupplierLogicCost().isEmpty()) {
					engineerSupplierLogisticCell.setCellValue(0);
				} else {
					engineerSupplierLogisticCell.setCellValue(Double.valueOf(partData.getEngineerSupplierLogicCost()));
				}
			} else {
				if (!partData.isFound()) {
					engineerSupplierLogisticCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					engineerSupplierLogisticCell.setCellValue("NO ACCESS");
				}
			}
			engineerSupplierLogisticCell.setCellStyle(headerCellStyle);

			SXSSFCell engineerTotalCostCell = row.createCell(37);
			if (partData.isRightAccess()) {
				if (partData.getEngineerTotalCost().isEmpty()) {
					engineerTotalCostCell.setCellValue(0);
				} else {
					engineerTotalCostCell.setCellValue(Double.valueOf(partData.getEngineerTotalCost()));
				}
			} else {
				if (!partData.isFound()) {
					engineerTotalCostCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					engineerTotalCostCell.setCellValue("NO ACCESS");
				}
			}
			engineerTotalCostCell.setCellStyle(headerCellStyle);

			SXSSFCell edndCostCell = row.createCell(38);
			if (partData.isRightAccess()) {
				if (partData.getEdndCost().isEmpty()) {
					edndCostCell.setCellValue(0);
				} else {
					edndCostCell.setCellValue(Double.valueOf(partData.getEdndCost()));
				}
			} else {
				if (!partData.isFound()) {
					edndCostCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					edndCostCell.setCellValue("NO ACCESS");
				}
			}
			edndCostCell.setCellStyle(headerCellStyle);

			SXSSFCell edndCostCurrCell = row.createCell(39);
			if (partData.isRightAccess()) {
				edndCostCurrCell.setCellValue(partData.getEdndCurr());
			} else {
				if (!partData.isFound()) {
					edndCostCurrCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					edndCostCurrCell.setCellValue("NO ACCESS");
				}
			}
			edndCostCurrCell.setCellStyle(headerCellStyle);

			SXSSFCell protoToolValueCell = row.createCell(40);
			if (partData.isRightAccess()) {
				if (partData.getProtoToolingValue().isEmpty()) {
					protoToolValueCell.setCellValue(0);
				} else {
					protoToolValueCell.setCellValue(Double.valueOf(partData.getProtoToolingValue()));
				}
			} else {
				if (!partData.isFound()) {
					protoToolValueCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					protoToolValueCell.setCellValue("NO ACCESS");
				}
			}
			protoToolValueCell.setCellStyle(headerCellStyle);

			SXSSFCell protoToolCurrCell = row.createCell(41);
			if (partData.isRightAccess()) {
				protoToolCurrCell.setCellValue(partData.getProtoToolingCurr());
			} else {
				if (!partData.isFound()) {
					protoToolCurrCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					protoToolCurrCell.setCellValue("NO ACCESS");
				}
			}
			protoToolCurrCell.setCellStyle(headerCellStyle);

			SXSSFCell protoPiecePriceCell = row.createCell(42);
			if (partData.isRightAccess()) {
				if (partData.getProtoPiecePrice().isEmpty()) {
					protoPiecePriceCell.setCellValue(0);
				} else {
					protoPiecePriceCell.setCellValue(Double.valueOf(partData.getProtoPiecePrice()));
				}
			} else {
				if (!partData.isFound()) {
					protoPiecePriceCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					protoPiecePriceCell.setCellValue("NO ACCESS");
				}
			}
			protoPiecePriceCell.setCellStyle(headerCellStyle);

			SXSSFCell protoPiecePriceCurrCell = row.createCell(43);
			if (partData.isRightAccess()) {
				protoPiecePriceCurrCell.setCellValue(partData.getProtoPiecePriceCurr());
			} else {
				if (!partData.isFound()) {
					protoPiecePriceCurrCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					protoPiecePriceCurrCell.setCellValue("NO ACCESS");
				}
			}
			protoPiecePriceCurrCell.setCellStyle(headerCellStyle);

			SXSSFCell toolInvestValueCell = row.createCell(44);
			if (partData.isRightAccess()) {
				if (partData.getToolingInvestValue().isEmpty()) {
					toolInvestValueCell.setCellValue(0);
				} else {
					toolInvestValueCell.setCellValue(Double.valueOf(partData.getToolingInvestValue()));
				}
			} else {
				if (!partData.isFound()) {
					toolInvestValueCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					toolInvestValueCell.setCellValue("NO ACCESS");
				}
			}
			toolInvestValueCell.setCellStyle(headerCellStyle);

			SXSSFCell toolInvestCurrCell = row.createCell(45);
			if (partData.isRightAccess()) {
				toolInvestCurrCell.setCellValue(partData.getToolingInvestCurr());
			} else {
				if (!partData.isFound()) {
					toolInvestCurrCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					toolInvestCurrCell.setCellValue("NO ACCESS");
				}
			}
			toolInvestCurrCell.setCellStyle(headerCellStyle);

			SXSSFCell refurBishValueCell = row.createCell(46);
			if (partData.isRightAccess()) {
				if (partData.getRefurToolingValue().isEmpty()) {
					refurBishValueCell.setCellValue(0);
				} else {
					refurBishValueCell.setCellValue(Double.valueOf(partData.getRefurToolingValue()));
				}
			} else {
				if (!partData.isFound()) {
					refurBishValueCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					refurBishValueCell.setCellValue("NO ACCESS");
				}
			}
			refurBishValueCell.setCellStyle(headerCellStyle);

			SXSSFCell refurBishCurrCell = row.createCell(47);
			if (partData.isRightAccess()) {
				refurBishCurrCell.setCellValue(partData.getRefurToolingCurr());
			} else {
				if (!partData.isFound()) {
					refurBishCurrCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					refurBishCurrCell.setCellValue("NO ACCESS");
				}
			}
			refurBishCurrCell.setCellStyle(headerCellStyle);

			SXSSFCell facilityInvestCell = row.createCell(48);
			if (partData.isRightAccess()) {
				if (partData.getFacilityInvestAmount().isEmpty()) {
					facilityInvestCell.setCellValue(0);
				} else {
					facilityInvestCell.setCellValue(Double.valueOf(partData.getFacilityInvestAmount()));
				}
			} else {
				if (!partData.isFound()) {
					facilityInvestCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					facilityInvestCell.setCellValue("NO ACCESS");
				}
			}
			facilityInvestCell.setCellStyle(headerCellStyle);

			SXSSFCell facilityInvestCurrCell = row.createCell(49);
			if (partData.isRightAccess()) {
				facilityInvestCurrCell.setCellValue(partData.getFacilityInvestCurr());
			} else {
				if (!partData.isFound()) {
					facilityInvestCurrCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					facilityInvestCurrCell.setCellValue("NO ACCESS");
				}
			}
			facilityInvestCurrCell.setCellStyle(headerCellStyle);

			SXSSFCell miscellaValueCell = row.createCell(50);
			if (partData.isRightAccess()) {
				if (partData.getMiscellaneousCost().isEmpty()) {
					miscellaValueCell.setCellValue(0);
				} else {
					miscellaValueCell.setCellValue(Double.valueOf(partData.getMiscellaneousCost()));
				}
			} else {
				if (!partData.isFound()) {
					miscellaValueCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					miscellaValueCell.setCellValue("NO ACCESS");
				}
			}
			miscellaValueCell.setCellStyle(headerCellStyle);

			SXSSFCell miscellaCurrCell = row.createCell(51);
			if (partData.isRightAccess()) {
				miscellaCurrCell.setCellValue(partData.getMiscellaneousCurr());
			} else {
				if (!partData.isFound()) {
					miscellaCurrCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					miscellaCurrCell.setCellValue("NO ACCESS");
				}
			}
			miscellaCurrCell.setCellStyle(headerCellStyle);
			
			SXSSFCell industrialCostCell = row.createCell(52);
			if (partData.isRightAccess()) {
				industrialCostCell.setCellValue(partData.getIndustrializeCost());
			} else {
				if (!partData.isFound()) {
					industrialCostCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					industrialCostCell.setCellValue("NO ACCESS");
				}
			}
			industrialCostCell.setCellStyle(headerCellStyle);
			
			SXSSFCell industrialCurrCell = row.createCell(53);
			if (partData.isRightAccess()) {
				industrialCurrCell.setCellValue(partData.getIndustrializeCurr());
			} else {
				if (!partData.isFound()) {
					industrialCurrCell.setCellValue("NOT FOUND");
				}
				if (partData.isFound()) {
					industrialCurrCell.setCellValue("NO ACCESS");
				}
			}
			industrialCurrCell.setCellStyle(headerCellStyle);
			
			SXSSFCell vnBaseBEV = row.createCell(54);
			if (partData.getVNASEANBaseBEV().compareTo("True") == 0 || partData.getCommonPart().compareTo("True") == 0) {
				vnBaseBEV.setCellValue("x");
			}
			vnBaseBEV.setCellStyle(headerCellStyle);

			SXSSFCell vnMiddleBEV = row.createCell(55);
			if (partData.getVNASEANMiddleBEV().compareTo("True") == 0 || partData.getCommonPart().compareTo("True") == 0) {
				vnMiddleBEV.setCellValue("x");
			}
			vnMiddleBEV.setCellStyle(headerCellStyle);

			SXSSFCell vnHighBEV = row.createCell(56);
			if (partData.getVNASEANHighBEV().compareTo("True") == 0 || partData.getCommonPart().compareTo("True") == 0) {
				vnHighBEV.setCellValue("x");
			}
			vnHighBEV.setCellStyle(headerCellStyle);

			SXSSFCell vnBaseICE = row.createCell(57);
			if (partData.getVNASEANBaseICE().compareTo("True") == 0 || partData.getCommonPart().compareTo("True") == 0) {
				vnBaseICE.setCellValue("x");
			}
			vnBaseICE.setCellStyle(headerCellStyle);

			SXSSFCell vnMiddleICE = row.createCell(58);
			if (partData.getVNASEANMiddleICE().compareTo("True") == 0 || partData.getCommonPart().compareTo("True") == 0) {
				vnMiddleICE.setCellValue("x");
			}
			vnMiddleICE.setCellStyle(headerCellStyle);

			SXSSFCell vnHighICE = row.createCell(59);
			if (partData.getVNASEANHighICE().compareTo("True") == 0 || partData.getCommonPart().compareTo("True") == 0) {
				vnHighICE.setCellValue("x");
			}
			vnHighICE.setCellStyle(headerCellStyle);

			SXSSFCell usBaseBEV = row.createCell(60);
			if (partData.getUSBaseBev().compareTo("True") == 0 || partData.getCommonPart().compareTo("True") == 0) {
				usBaseBEV.setCellValue("x");
			}
			usBaseBEV.setCellStyle(headerCellStyle);

			SXSSFCell usHighBEV = row.createCell(61);
			if (partData.getUSHighBEV().compareTo("True") == 0 || partData.getCommonPart().compareTo("True") == 0) {
				usHighBEV.setCellValue("x");
			}
			usHighBEV.setCellStyle(headerCellStyle);

			SXSSFCell commonPart = row.createCell(62);
			if (partData.getCommonPart().compareTo("True") == 0) {
				commonPart.setCellValue("x");
			}
			commonPart.setCellStyle(headerCellStyle);
			
			SXSSFCell platformPart = row.createCell(63);
			if(partData.getPlatformPart().compareTo("True") == 0) {
				platformPart.setCellValue("x");
			}
			platformPart.setCellStyle(headerCellStyle);
			
			// XSSFCell ownerCell = row.createCell(17);
			// ownerCell.setCellValue(obj.getOwner());
			rownum++;
		}

		// for (int kz = 0; kz < 57; kz++) {
		// spreadsheet.autoSizeColumn(kz);
		// }
	}

	private void publishReport() {
		try {
			File report = Query.downloadFirstNameRefOfDataset(session, Query.QUERY_JES_DATASET, Query.QUERY_JES_ENTRY_DATASET_NAME, DATASET_TEMPLATE_NAME, TEMP_DIR, REPORT_PREFIX, startTime);

			InputStream fileIn = new FileInputStream(report);
			XSSFWorkbook template = new XSSFWorkbook(fileIn);
			DMTargetCostReport.wb = new SXSSFWorkbook(template, 500);

			// writeHeaderLine(TARGET_COST_SHEET, lsHeader);
			writeARowReport();
			// fileIn.close();
			Runtime.getRuntime().gc();
			FileOutputStream fos = new FileOutputStream(report);
			DMTargetCostReport.wb.write(fos);
			fos.close();
			DMTargetCostReport.wb.dispose();
			report.setWritable(false);
			Desktop.getDesktop().open(report);

		} catch (IOException | TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void traverseBOM(int key, TCComponentBOMLine parent_BOMLine) {
		// TODO Auto-generated method stub
		try {
			CostObject dataReport = new CostObject();
			/* get part information */
			String bomlineID = parent_BOMLine.getProperty("bl_item_item_id");
			String name = parent_BOMLine.getProperty("bl_item_object_name");
			String purchaseLv = parent_BOMLine.getProperty("VL5_purchase_lvl_vf");
			String changeIndex = parent_BOMLine.getProperty("VL5_change_index");
			String moduleGroup = parent_BOMLine.getProperty("VL5_module_group");
			String bomLevel = parent_BOMLine.getProperty("bl_level_starting_0");
			String supplierType = parent_BOMLine.getProperty("vf4_DsnRev_supplier_type");
			double weight = parent_BOMLine.getDoubleProperty("vf4_catia_weight");
			double estimatedWeight = parent_BOMLine.getDoubleProperty("vf4_estimated_weight");
			double measuredWeight = parent_BOMLine.getDoubleProperty("vf4_measured_weight");
			String moduleGrp = "";
			String mainModuleGrp = "";
			String material = "";
			String sorName = "";
			String sorNumber = "";

			TCComponentItemRevision itemRev = parent_BOMLine.getItemRevision();
			if (itemRev != null) {
				if (itemRev.getProperty("vf4_catia_material") != null) {
					material = itemRev.getProperty("vf4_catia_material");
				}
				if (itemRev.getProperty("vf4_sor_name") != null) {
					sorName = itemRev.getProperty("vf4_sor_name");
				}
				if (itemRev.getProperty("vf4_sor_number_rev") != null) {
					sorNumber = itemRev.getProperty("vf4_sor_number_rev");
				}
			} else {
				LOGGER.error("[BOMCostReport] Cannot get item revision form bomline: " + bomlineID);
			}
			if(parent_BOMLine.getProperty("VL5_module_name") != null) {
				moduleGrp = parent_BOMLine.getProperty("VL5_module_name");
			}
			if(parent_BOMLine.getProperty("VL5_main_module") != null) {
				mainModuleGrp = parent_BOMLine.getProperty("VL5_main_module");
			}
			dataReport.setPartNum(bomlineID);
			dataReport.setPartName(name);
			dataReport.setDesignAssumption(changeIndex);
			dataReport.setProcurementLvl(purchaseLv);
			dataReport.setModuleName(moduleGroup);
			dataReport.setBomLevel(bomLevel);
			dataReport.setSupplierType(supplierType);
			dataReport.setWeight(weight);
			dataReport.setMaterial(material);
			dataReport.setSorName(sorName);
			dataReport.setSorNum(sorNumber);
			dataReport.setModuleGrp(moduleGrp);
			dataReport.setMainModuleGrp(mainModuleGrp);
			dataReport.setEstimatedWeight(estimatedWeight);
			dataReport.setMeasuredWeight(measuredWeight);
			mapMasterData.put(key, dataReport);
			if (!listPart.contains(bomlineID)) {
				listPart.add(bomlineID);
			}

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
