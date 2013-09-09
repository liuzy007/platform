
create table MIZAR_PARTNER(
  ID number,
  GMT_CREATED date, 
  GMT_MODIFIED date, 
  USERNAME varchar2(255), 
  PASSWORD varchar2(255), 
  ROLE varchar2(255), 
  DESCRIPTION varchar2(255),
  TYPE varchar2(255)
);
           
create sequence SEQ_MIZAR_PARTNER start with 1;
       
       
create table MIZAR_PARTNER_SRV_REF(
  ID number,
  PARTNER_ID number,
  SERVICE_ID number
);
           
create sequence SEQ_MIZAR_PARTNER_SRV_REF start with 1;


create table  MIZAR_SRV_INPUT_PARAM (
       ID number, 
    		CREATER varchar2(255), 
    		MODIFIER varchar2(255), 
    		GMT_CREATED date, 
    		GMT_MODIFIED date, 
    		SERVICE_ID number, 
    		PARAM_NAME varchar2(255), 
   			PARAM_TYPE varchar2(255), 
   			IS_NULLABLE number, 
   			PARAM_INDEX number
);
create sequence SEQ_MIZAR_SRV_INPUT_PARAM start with 1;


create table MIZAR_SERVICE
(
  ID                  NUMBER,
  CREATER             VARCHAR2(255),
  GMT_CREATED         DATE,
  GMT_MODIFIED        DATE,
  SERVICE_NAME        VARCHAR2(255),
  URL                 VARCHAR2(255),
  INVOKE_INTEFACE     VARCHAR2(255),
  INVOKE_METHOD       VARCHAR2(255),
  VERSION             VARCHAR2(255),
  STATUS              VARCHAR2(255),
  IS_VALIDATE_SIGN_IN NUMBER,
  IS_ENDORSE_SIGN_OUT NUMBER,
  IS_PUBLIC           NUMBER,
  DESCRIPTION         VARCHAR2(512),
  WEBSERVICEINTERFACE VARCHAR2(512),
  WEBSERVICEMETHOD    VARCHAR2(512)
);
create sequence SEQ_mizar_service start with 1;


