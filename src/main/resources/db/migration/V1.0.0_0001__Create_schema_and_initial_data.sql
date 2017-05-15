CREATE TABLE agency (
  AGENCY_ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  PARENT_AGENCY_ID INT NULL,
  AGENCY_TYPE VARCHAR(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  REFERENCED_BY_DOCUMENT_URL VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL,
  IS_STILL_REFERENCED_BY_DOCUMENT BOOLEAN NULL,
  TERRITORY_CODE VARCHAR(20) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL,
  X509_CERTIFICATE VARCHAR(4000) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL
);
ALTER TABLE agency ADD FOREIGN KEY (PARENT_AGENCY_ID) REFERENCES agency (AGENCY_ID);

CREATE TABLE document (
  DOCUMENT_ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  DOCUMENT_URL VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  DOCUMENT_TYPE VARCHAR(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  LANGUAGE_CODE VARCHAR(20) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  PROVIDED_BY_AGENCY_ID INT NOT NULL,
  REFERENCED_BY_DOCUMENT_TYPE VARCHAR(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL,
  IS_STILL_PROVIDED_BY_AGENCY BOOLEAN NOT NULL
);
ALTER TABLE document ADD FOREIGN KEY (PROVIDED_BY_AGENCY_ID) REFERENCES agency (AGENCY_ID);

CREATE TABLE agency_name (
  AGENCY_NAME_ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  AGENCY_ID INT NOT NULL,
  NAME VARCHAR(500) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  LANGUAGE_CODE VARCHAR(20) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL
);
ALTER TABLE agency_name ADD FOREIGN KEY (AGENCY_ID) REFERENCES agency (AGENCY_ID);

CREATE TABLE document_checking_result (
  DOCUMENT_CHECKING_RESULT_ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  DOCUMENT_URL VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  CHECKING_DATE DATETIME NOT NULL,
  IS_AVAILABLE BOOLEAN NOT NULL,
  IS_VALID BOOLEAN NOT NULL,
  SIZE_IN_BYTES INTEGER NOT NULL,
  DOWNLOAD_DURATION_IN_MILLIS INTEGER NOT NULL
);


INSERT INTO agency (AGENCY_ID, PARENT_AGENCY_ID, AGENCY_TYPE, REFERENCED_BY_DOCUMENT_URL, IS_STILL_REFERENCED_BY_DOCUMENT, TERRITORY_CODE)
VALUES (
  -1,
  NULL,
  'TRUST_SERVICE_LIST_OPERATOR',
  NULL,
  NULL,
  'EU'
);

INSERT INTO agency_name (AGENCY_ID, NAME, LANGUAGE_CODE)
VALUES (
  -1,
  'European Commission',
  'en'
);

INSERT INTO document (DOCUMENT_URL, DOCUMENT_TYPE, LANGUAGE_CODE, PROVIDED_BY_AGENCY_ID, IS_STILL_PROVIDED_BY_AGENCY)
VALUES (
  'https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl-mp.xml',
  'TS_STATUS_LIST_XML',
  'en',
  -1,
  TRUE
);

