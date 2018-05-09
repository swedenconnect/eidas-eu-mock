package eu.eidas.SimpleProtocol;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

public class ComplexAddressAttribute implements Serializable {


	private static final long serialVersionUID = 2454905207294622352L;

	protected String addressId;

	protected String poBox;

	protected String locatorDesignator;

	protected String locatorName;

	protected String addressArea;

	protected String thoroughFare;

	protected String postName;

	protected String adminUnitFirstLine;

	protected String adminUnitSecondLine;

	protected String postCode;

	protected String fullCVAddress;

	@XmlElement(name="address_id")
	public String getAddressId() {
		return addressId;
	}

	public void setAddressId(String addressId) {
		this.addressId = addressId;
	}

	@XmlElement(name="po_box")
	public String getPoBox() {
		return poBox;
	}

	public void setPoBox(String poBox) {
		this.poBox = poBox;
	}

	@XmlElement(name="locator_designator")
	public String getLocatorDesignator() {
		return locatorDesignator;
	}

	public void setLocatorDesignator(String locatorDesignator) {
		this.locatorDesignator = locatorDesignator;
	}

	@XmlElement(name="locator_name")
	public String getLocatorName() {
		return locatorName;
	}

	public void setLocatorName(String locatorName) {
		this.locatorName = locatorName;
	}

	@XmlElement(name="cv_address_area")
	public String getAddressArea() {
		return addressArea;
	}

	public void setAddressArea(String addressArea) {
		this.addressArea = addressArea;
	}

	@XmlElement(name="thoroughfare")
	public String getThoroughFare() {
		return thoroughFare;
	}

	public void setThoroughFare(String thoroughFare) {
		this.thoroughFare = thoroughFare;
	}

	@XmlElement(name="post_name")
	public String getPostName() {
		return postName;
	}

	public void setPostName(String postName) {
		this.postName = postName;
	}

	@XmlElement(name="admin_unit_first_line")
	public String getAdminUnitFirstLine() {
		return adminUnitFirstLine;
	}

	public void setAdminUnitFirstLine(String adminUnitFirstLine) {
		this.adminUnitFirstLine = adminUnitFirstLine;
	}

	@XmlElement(name="admin_unit_second_line")
	public String getAdminUnitSecondLine() {
		return adminUnitSecondLine;
	}

	public void setAdminUnitSecondLine(String adminUnitSecondLine) {
		this.adminUnitSecondLine = adminUnitSecondLine;
	}

	@XmlElement(name="post_code")
	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	@XmlElement(name="full_cvaddress")
	public String getFullCVAddress() {
		return fullCVAddress;
	}

	public void setFullCVAddress(String fullCVAddress) {
		this.fullCVAddress = fullCVAddress;
	}
	
}
