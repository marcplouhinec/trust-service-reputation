CREATE TABLE DOCUMENT (
  DOCUMENT_URL VARCHAR(2000) NOT NULL PRIMARY KEY,
  DOCUMENT_TYPE VARCHAR(32) NOT NULL,
  LANGUAGE_CODE VARCHAR(20) NOT NULL,
  PROVIDER_AGENCY_ID INT NOT NULL
);

CREATE TABLE AGENCY (
  AGENCY_ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  PARENT_AGENCY_ID INT NULL,
  AGENCY_TYPE VARCHAR(32) NOT NULL,
  REFERENCING_DOCUMENT_URL VARCHAR(2000) NULL,
  TERRITORY_CODE VARCHAR(20) NULL
);
ALTER TABLE AGENCY ADD FOREIGN KEY (PARENT_AGENCY_ID) REFERENCES AGENCY (AGENCY_ID);
ALTER TABLE AGENCY ADD FOREIGN KEY (REFERENCING_DOCUMENT_URL) REFERENCES DOCUMENT (DOCUMENT_URL);
ALTER TABLE DOCUMENT ADD FOREIGN KEY (PROVIDER_AGENCY_ID) REFERENCES AGENCY (AGENCY_ID);

CREATE TABLE AGENCY_NAME (
  AGENCY_NAME_ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  AGENCY_ID INT NOT NULL,
  NAME VARCHAR(500) NOT NULL,
  LANGUAGE_CODE VARCHAR(20) NOT NULL
);
ALTER TABLE AGENCY_NAME ADD FOREIGN KEY (AGENCY_ID) REFERENCES AGENCY (AGENCY_ID);



INSERT INTO AGENCY (AGENCY_ID, PARENT_AGENCY_ID, AGENCY_TYPE, REFERENCING_DOCUMENT_URL, TERRITORY_CODE)
VALUES (
  -1,
  NULL,
  'TRUST_SERVICE_LIST_OPERATOR',
  NULL,
  'EU'
);

INSERT INTO AGENCY_NAME (AGENCY_ID, NAME, LANGUAGE_CODE)
VALUES (
  -1,
  'European Commission',
  'en'
);

INSERT INTO DOCUMENT (DOCUMENT_URL, DOCUMENT_TYPE, LANGUAGE_CODE, PROVIDER_AGENCY_ID)
VALUES (
  'https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl-mp.xml',
  'TS_STATUS_LIST_XML',
  'EN',
  -1
);
