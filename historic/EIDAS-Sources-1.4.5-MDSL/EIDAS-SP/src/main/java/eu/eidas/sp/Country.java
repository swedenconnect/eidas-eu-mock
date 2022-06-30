/*
 * Copyright (c) 2016 by European Commission
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 * 
 */

package eu.eidas.sp;

public class Country {
	
	private int id;
	private String name;
	private String url;
	private String metadataUrl;
	private String countrySelector;
	
	Country(int id, String name, String url, String countrySelector, String metadataUrl){
		this.id=id;
		this.name=name;
		this.url=url;
		this.metadataUrl=metadataUrl;
		this.countrySelector=countrySelector;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMetadataUrl() {
		return metadataUrl;
	}
	public void setMetadataUrl(String metadataUrl) {
		this.metadataUrl = metadataUrl;
	}
	public String getCountrySelector() {
		return countrySelector;
	}

	public void setCountrySelector(String countrySelector) {
		this.countrySelector = countrySelector;
	}

    public String toString() {
        String spacer = " \n";
        return "Country id : " + getId() + spacer 
                + "\tCountry name : " + getName() + spacer 
                + "\tCountry url : " + getUrl() + spacer 
                + "\tCountry metadataUrl : " + getMetadataUrl() + spacer 
                + "\tCountry selector : " + getCountrySelector();
    }
}
