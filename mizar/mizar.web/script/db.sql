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


create table mizar_service(
      id number,
			creater varchar2(255), 
			gmt_created date, 
			gmt_modified date,
			service_name varchar2(255),
			url varchar2(255),
			invoke_inteface varchar2(255),
			invoke_method varchar2(255),
			version varchar2(255),
			status varchar2(255),
			is_validate_sign_in number,
			is_endorse_sign_out number,
			is_public number
);
create sequence SEQ_mizar_service start with 1;


