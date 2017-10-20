/**
 * 
 */
package com.nowgroup.kenan.batch;

import org.springframework.batch.item.ItemProcessor;

/**
 * @author dtorresf
 *
 */
public class CompanyRoleNewItemProcessor implements ItemProcessor<CompanyRole, CompanyRole> {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
	 */
	@Override
	public CompanyRole process(CompanyRole companyRole) throws Exception {
		return companyRole;
	}

}
