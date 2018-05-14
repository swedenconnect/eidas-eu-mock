package se.swedenconnect.eidas.test.cef20demohub.data;

import lombok.Data;

@Data
public class RequestForm {

    private String nameIdentifier;
    private String requestedLoa;
    private String loaComparison;
    private String spType;
    private String citizenCountry;

    //Attributes
    private String personIdentifier;
    private String familyName;
    private String firstName;
    private String dateOfBirth;
    private String birthName;
    private String placeOfBirth;
    private String currentAddress;
    private String gender;

    private String legalPersonIdentifier;
    private String legalName;
    private String legalAddress;
    private String vatRegistration;
    private String taxReference;
    private String d2012_17_EUIdentifier;
    private String lei;
    private String eori;
    private String seed;
    private String sic;

    private String repr_personIdentifier;
    private String repr_familyName;
    private String repr_firstName;
    private String repr_dateOfBirth;
    private String repr_birthName;
    private String repr_placeOfBirth;
    private String repr_currentAddress;
    private String repr_gender;

    private String repr_legalPersonIdentifier;
    private String repr_legalName;
    private String repr_legalAddress;
    private String repr_vatRegistration;
    private String repr_taxReference;
    private String repr_d2012_17_EUIdentifier;
    private String repr_lei;
    private String repr_eori;
    private String repr_seed;
    private String repr_sic;

    //Required
    private String req_personIdentifier;
    private String req_familyName;
    private String req_firstName;
    private String req_dateOfBirth;
    private String req_birthName;
    private String req_placeOfBirth;
    private String req_currentAddress;
    private String req_gender;

    private String req_legalPersonIdentifier;
    private String req_legalName;
    private String req_legalAddress;
    private String req_vatRegistration;
    private String req_taxReference;
    private String req_d2012_17_EUIdentifier;
    private String req_lei;
    private String req_eori;
    private String req_seed;
    private String req_sic;

    private String req_repr_personIdentifier;
    private String req_repr_familyName;
    private String req_repr_firstName;
    private String req_repr_dateOfBirth;
    private String req_repr_birthName;
    private String req_repr_placeOfBirth;
    private String req_repr_currentAddress;
    private String req_repr_gender;

    private String req_repr_legalPersonIdentifier;
    private String req_repr_legalName;
    private String req_repr_legalAddress;
    private String req_repr_vatRegistration;
    private String req_repr_taxReference;
    private String req_repr_d2012_17_EUIdentifier;
    private String req_repr_lei;
    private String req_repr_eori;
    private String req_repr_seed;
    private String req_repr_sic;
}
