package com.teamcenter.integration.report;

import com.teamcenter.rac.kernel.TCComponent;

public class ShopModel {
	private TCComponent shopObject = null;
	private TCComponent topObject = null;
	private TCComponent shopBopObject = null;
	private String program = "";
	private String shopName = "";
	private String shopID = "";
	private String shopDesc = "";
	private String topID = "";
	private String shopBopID = "";
	private boolean isBom150 = false;

	public TCComponent getShopObject() {
		return shopObject;
	}

	public void setShopObject(TCComponent shopObject) {
		this.shopObject = shopObject;
	}

	public TCComponent getTopObject() {
		return topObject;
	}

	public void setTopObject(TCComponent topObject) {
		this.topObject = topObject;
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}

	public String getShopID() {
		return shopID;
	}

	public void setShopID(String shopID) {
		this.shopID = shopID;
	}

	public String getShopDesc() {
		return shopDesc;
	}

	public void setShopDesc(String shopDesc) {
		this.shopDesc = shopDesc;
	}

	public String getTopID() {
		return topID;
	}

	public void setTopID(String topID) {
		this.topID = topID;
	}

	public boolean isBom150() {
		return isBom150;
	}

	public void setBom150(boolean isBom150) {
		this.isBom150 = isBom150;
	}

	public String getProgram() {
		return program;
	}

	public void setProgram(String program) {
		this.program = program;
	}

	public TCComponent getShopBopObject() {
		return shopBopObject;
	}

	public void setShopBopObject(TCComponent shopBopObject) {
		this.shopBopObject = shopBopObject;
	}

	public String getShopBopID() {
		return shopBopID;
	}

	public void setShopBopID(String shopBopID) {
		this.shopBopID = shopBopID;
	}
}
