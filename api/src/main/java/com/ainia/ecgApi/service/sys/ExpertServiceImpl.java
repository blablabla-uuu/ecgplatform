package com.ainia.ecgApi.service.sys;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ainia.ecgApi.core.crud.BaseDao;
import com.ainia.ecgApi.core.crud.BaseServiceImpl;
import com.ainia.ecgApi.dao.sys.ExpertDao;
import com.ainia.ecgApi.domain.sys.Chief;
import com.ainia.ecgApi.domain.sys.Expert;

/**
 * <p>Expert ServiceImpl</p>
 * Copyright: Copyright (c) 2013
 * Company:   
 * ExpertServiceImpl.java
 * @author pq
 * @createdDate 2013-6-22
 * @version
 */
@Service
public class ExpertServiceImpl extends BaseServiceImpl<Expert , Long> implements
		ExpertService {
	@Autowired
	private ExpertDao expertDao;
	@Autowired
	private EmployeeService employeeService;

	@Override
	public BaseDao<Expert, Long> getBaseDao() {
		return expertDao;
	}

	public void setExpertDao(ExpertDao expertDao) {
		this.expertDao = expertDao;
	}

	@Override
	public Expert save(Expert domain) {
		return employeeService.save(domain);
	}

	@Override
	public List<Expert> save(Iterable domains) {
		return employeeService.save(domains);
	} 
}
