package com.alifi.mizar.dao.impl;

import java.util.List;

import com.alifi.mizar.common.vo.Partner;
import com.alifi.mizar.dao.PartnerDao;

@SuppressWarnings("unchecked")
public class PartnerDaoImpl extends BaseDaoImpl implements PartnerDao {

    public int deleteById(int id) {
        return this.delete("partner.deleteById", id);
    }

    public int insert(Partner partner) {
        return this.insert("partner.insert", partner);
    }

    public List<Partner> list() {
        return this.getList("partner.list", null);
    }
    
    public Partner queryById(int id) {
        return (Partner) this.get("partner.queryById", id);
    }

}
