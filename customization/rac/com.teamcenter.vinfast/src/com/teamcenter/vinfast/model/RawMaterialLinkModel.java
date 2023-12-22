package com.teamcenter.vinfast.model;

import java.text.DecimalFormat;

import com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost;
import com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostElement;
import com.vf4.services.rac.custom._2020_10.ReportDataSource.VFPartCost;

import vfplm.soa.common.service.VFCostItemRevision.ECRCostData;

public class RawMaterialLinkModel {
	private String hPartNumber = "";
	private String hPName = "";
	private String blankPartNumber = "";
	private String blankPartName = "";
	private String blankMaterial = "";
	private String blankCoating = "";
	private String blankThickness = "";
	private String blankWidth = "";
	private String blankLength = "";
	private String coilPartNumber = "";
	private String coilPartName = "";
	private String coilMaterial = "";
	private String coilCoating = "";
	private String coilThickness = "";
	private String coilWidth = "";
	private String coil2PartNumber = "";
	private String coil2PartName = "";
	private String coil2Material = "";
	private String coil2Coating = "";
	private String coil2Thickness = "";
	private String coil2Width = "";
	private VFCost partCost;
	private double pieceCost;
	private double packingCost;
	private double logisticCost;
	private double labourCost;
	private double tax;
	private double toolingCost;
	private double edndCost;
	private static final DecimalFormat df = new DecimalFormat("0.00");

	public String gethPartNumber() {
		return hPartNumber;
	}

	public void sethPartNumber(String hPartNumber) {
		this.hPartNumber = hPartNumber;
	}

	public String gethPName() {
		return hPName;
	}

	public void sethPName(String hPName) {
		this.hPName = hPName;
	}

	public String getBlankPartNumber() {
		return blankPartNumber;
	}

	public void setBlankPartNumber(String blankPartNumber) {
		this.blankPartNumber = blankPartNumber;
	}

	public String getBlankPartName() {
		return blankPartName;
	}

	public void setBlankPartName(String blankPartName) {
		this.blankPartName = blankPartName;
	}

	public String getCoilPartNumber() {
		return coilPartNumber;
	}

	public void setCoilPartNumber(String coilPartNumber) {
		this.coilPartNumber = coilPartNumber;
	}

	public String getCoilPartName() {
		return coilPartName;
	}

	public void setCoilPartName(String coilPartName) {
		this.coilPartName = coilPartName;
	}

	public String getBlankMaterial() {
		return blankMaterial;
	}

	public void setBlankMaterial(String blankMaterial) {
		this.blankMaterial = blankMaterial;
	}

	public String getBlankCoating() {
		return blankCoating;
	}

	public void setBlankCoating(String blankCoating) {
		this.blankCoating = blankCoating;
	}

	public String getBlankThickness() {
		return blankThickness;
	}

	public void setBlankThickness(String blankThickness) {
		this.blankThickness = blankThickness;
	}

	public String getBlankWidth() {
		return blankWidth;
	}

	public void setBlankWidth(String blankWidth) {
		this.blankWidth = blankWidth;
	}

	public String getBlankLength() {
		return blankLength;
	}

	public void setBlankLength(String blankLength) {
		this.blankLength = blankLength;
	}

	public String getCoilMaterial() {
		return coilMaterial;
	}

	public void setCoilMaterial(String coilMaterial) {
		this.coilMaterial = coilMaterial;
	}

	public String getCoilCoating() {
		return coilCoating;
	}

	public void setCoilCoating(String coilCoating) {
		this.coilCoating = coilCoating;
	}

	public String getCoilThickness() {
		return coilThickness;
	}

	public void setCoilThickness(String coilThickness) {
		this.coilThickness = coilThickness;
	}

	public String getCoilWidth() {
		return coilWidth;
	}

	public void setCoilWidth(String coilWidth) {
		this.coilWidth = coilWidth;
	}

	public void setPartCost(VFCost partCost) {
		this.partCost = partCost;
		pieceCost = this.partCost.totalPieceCost;
		edndCost = this.partCost.edndCost;
		toolingCost = this.partCost.toolingCost;
	}

	public String getPieceCost() {
		return df.format(pieceCost);
	}

	public double getPackingCost() {
		return packingCost;
	}

	public double getLogisticCost() {
		return logisticCost;
	}

	public double getLabourCost() {
		return labourCost;
	}

	public double getTax() {
		return tax;
	}

	public String getToolingCost() {
		return df.format(toolingCost);
	}

	public String getEdndCost() {
		return df.format(edndCost);
	}

	public String getCoil2PartNumber() {
		return coil2PartNumber;
	}

	public void setCoil2PartNumber(String coil2PartNumber) {
		this.coil2PartNumber = coil2PartNumber;
	}

	public String getCoil2PartName() {
		return coil2PartName;
	}

	public void setCoil2PartName(String coil2PartName) {
		this.coil2PartName = coil2PartName;
	}

	public String getCoil2Material() {
		return coil2Material;
	}

	public void setCoil2Material(String coil2Material) {
		this.coil2Material = coil2Material;
	}

	public String getCoil2Coating() {
		return coil2Coating;
	}

	public void setCoil2Coating(String coil2Coating) {
		this.coil2Coating = coil2Coating;
	}

	public String getCoil2Thickness() {
		return coil2Thickness;
	}

	public void setCoil2Thickness(String coil2Thickness) {
		this.coil2Thickness = coil2Thickness;
	}

	public String getCoil2Width() {
		return coil2Width;
	}

	public void setCoil2Width(String coil2Width) {
		this.coil2Width = coil2Width;
	}
}
