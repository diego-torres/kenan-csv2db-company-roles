/**
 * 
 */
package com.nowgroup.kenan.batch;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * @author dtorresf
 *
 */
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	// "file:/tmp/import/products-*"
	@Value("${csv.resource}")
	private Resource[] csvResource;

	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	@Autowired
	private DataSource dataSource;

	public MultiResourceItemReader<CompanyRole> multiResourceItemReader() {
		MultiResourceItemReader<CompanyRole> mri = new MultiResourceItemReader<>();
		mri.setResources(csvResource);
		mri.setDelegate(reader());
		return mri;
	}
	
	@Bean
	public FlatFileItemReader<CompanyRole> reader() {
		FlatFileItemReader<CompanyRole> reader = new FlatFileItemReader<>();
		//reader.setLinesToSkip(1);		
		reader.setLineMapper(new DefaultLineMapper<CompanyRole>() {
			{
				setLineTokenizer(new DelimitedLineTokenizer() {
					{
						setNames(new String[] { "companyId", "roleId" });
					}
				});
				setFieldSetMapper(new BeanWrapperFieldSetMapper<CompanyRole>() {
					{
						setTargetType(CompanyRole.class);
					}
				});
			}
		});
		return reader;
	}

	@Bean
	public CompanyRoleNewItemProcessor companyNewItemProcessor() {
		return new CompanyRoleNewItemProcessor();
	}

	@Bean
	public JdbcBatchItemWriter<CompanyRole> inserter() {
		JdbcBatchItemWriter<CompanyRole> writer = new JdbcBatchItemWriter<>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<CompanyRole>());
		writer.setSql("INSERT INTO cross_company_role (company_id, company_role_id) VALUES (:companyId, :roleId)");
		writer.setDataSource(dataSource);
		return writer;
	}

	@Bean
	public Job importUserJob() {
		return jobBuilderFactory
				.get("importUserJob")
				.incrementer(new RunIdIncrementer())
				.start(step1()).on("FAILED").end()
				.from(step1()).on("COMPLETED").to(step2()).on("FAILED").end()
				.from(step2()).on("COMPLETED").to(step3()).end()
				.build();
	}

	@Bean
	public CleanTableTasklet cleanTableTasklet() {
		CleanTableTasklet tasklet = new CleanTableTasklet();
		tasklet.setDataSource(dataSource);
		tasklet.setTableName("cross_company_role");
		return tasklet;
	}
	
	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1").tasklet(cleanTableTasklet()).build();
	}

	@Bean
	public Step step2() {
		return stepBuilderFactory.get("step2").<CompanyRole, CompanyRole>chunk(100)
				.reader(multiResourceItemReader())
				.processor(new ItemProcessor<CompanyRole, CompanyRole>(){
					@Override
					public CompanyRole process(CompanyRole item) throws Exception {
						// no transformation or filtering required
						return item;
					}})
				.writer(inserter()).build();
	}
	
	@Bean
	public Step step3() {
		return stepBuilderFactory.get("step3").tasklet(doneFileRenameTasklet()).build();
	}
	
	@Bean
	public DoneFileRenameTasklet doneFileRenameTasklet() {
		DoneFileRenameTasklet tasklet = new DoneFileRenameTasklet();
		tasklet.setResource(csvResource);
		return tasklet;
	}

}
