/**
 * 
 */
package com.nowgroup.kenan.batch;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * @author dtorresf
 *
 */
public class CleanTableTasklet implements Tasklet {
	private NamedParameterJdbcOperations namedParameterJdbcTemplate;
	private String tableName;

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.step.tasklet.Tasklet#execute(org.springframework.batch.core.StepContribution, org.springframework.batch.core.scope.context.ChunkContext)
	 */
	@Override
	public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {
		String sqlString = "DELETE FROM " + tableName;
		namedParameterJdbcTemplate.update(sqlString, new HashMap<String, String>());
		return RepeatStatus.FINISHED;
	}
	
	/**
	 * Public setter for the data source for injection purposes.
	 *
	 * @param dataSource {@link javax.sql.DataSource} to use for querying against
	 */
	public void setDataSource(DataSource dataSource) {
		if (namedParameterJdbcTemplate == null) {
			this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		} 
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

}
