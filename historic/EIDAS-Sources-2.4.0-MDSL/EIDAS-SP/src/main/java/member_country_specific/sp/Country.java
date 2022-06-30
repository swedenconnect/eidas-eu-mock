/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package member_country_specific.sp;

public class Country {
	
	private int id;
	private String name;
	private String url;
	private String countrySelector;
	
	Country(int id, String name, String url, String countrySelector){
		this.id=id;
		this.name=name;
		this.url=url;
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
                + "\tCountry selector : " + getCountrySelector();
    }
}
