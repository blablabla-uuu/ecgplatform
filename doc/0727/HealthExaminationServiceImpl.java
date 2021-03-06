package com.ainia.ecgApi.service.health;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ainia.ecgApi.core.crud.BaseDao;
import com.ainia.ecgApi.core.crud.BaseServiceImpl;
import com.ainia.ecgApi.core.crud.Query;
import com.ainia.ecgApi.core.exception.ServiceException;
import com.ainia.ecgApi.core.security.AuthUser;
import com.ainia.ecgApi.core.security.AuthenticateService;
import com.ainia.ecgApi.dao.health.HealthExaminationDao;
import com.ainia.ecgApi.domain.health.HealthExamination;
import com.ainia.ecgApi.domain.health.HealthReply;
import com.ainia.ecgApi.domain.health.HealthRule;
import com.ainia.ecgApi.domain.sys.SystemConfig;
import com.ainia.ecgApi.domain.sys.User;
import com.ainia.ecgApi.domain.task.ExaminationTask;
import com.ainia.ecgApi.service.common.UploadService;
import com.ainia.ecgApi.service.common.UploadService.Type;
import com.ainia.ecgApi.service.sys.SystemConfigService;
import com.ainia.ecgApi.service.task.ExaminationTaskService;
import com.ainia.ecgApi.service.task.TaskService;
import com.ainia.ecgApi.utils.DataProcessor;
import com.ainia.ecgApi.utils.ECGChart;

/**
 * <p>
 * HealthExamination Service Impl
 * </p>
 * Copyright: Copyright (c) 2013 Company: HealthExaminationServiceImpl.java
 * 
 * @author pq
 * @createdDate 2013-07-07
 * @version 0.1
 */
@Service
public class HealthExaminationServiceImpl extends
		BaseServiceImpl<HealthExamination, Long> implements
		HealthExaminationService {

	@Autowired
	private HealthExaminationDao healthExaminationDao;
	@Autowired
	private HealthReplyService healthReplyService;
	@Autowired
	private ExaminationTaskService examinationTaskService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private AuthenticateService authenticateService;
	@Autowired
	private UploadService uploadService;
	@Autowired
	private SystemConfigService systemConfigService;
	@Autowired
	private HealthRuleService healthRuleService;

	private ExecutorService executorService = Executors.newFixedThreadPool(3);

	@Override
	public BaseDao<HealthExamination, Long> getBaseDao() {
		return healthExaminationDao;
	}

	public void reply(Long id, HealthReply reply) {
		healthReplyService.create(reply);
	}

	public void upload(final HealthExamination examination,
			final byte[] uploadData) {

		final AuthUser authUser = authenticateService.getCurrentUser();
		if (authUser == null) {
			throw new ServiceException("authUser.error.notFound");
		}
		if (!User.class.getSimpleName().equals(authUser.getType())) {
			throw new ServiceException("examination.error.upload.notAllowed");
		}
		examination.setUserId(authUser.getId());
		examination.setUserName(authUser.getUsername());
		examination.setUserType(authUser.getType());
		examination.setTemp(37.5F);
		examination.setBodyTemp(37.5F);
		examination.setTestItem("ipad");
		examination.setHeartRhythm(1);
		examination.setBreath(50);
		examination.setBloodOxygen(6);

		this.create(examination);

		// 判断是否自动回复
		String config = systemConfigService
				.findByKey(SystemConfig.EXAMINATION_REPLY_AUTO);
		boolean isAuto = config == null ? false : Boolean.valueOf(config);

		ExaminationTask task = new ExaminationTask();
		task.setExaminationId(examination.getId());
		task.setAuto(isAuto);
		task.setUserId(authUser.getId());
		if (isAuto) {
			Query ruleQuery = new Query();
			ruleQuery.equals(HealthRule.USAGE.equals(HealthRule.Usage.filter));
			List<HealthRule> filters = healthRuleService.findAll(ruleQuery);
			if (filters != null) {
				for (HealthRule rule : filters) {
					if (rule.isMatch(examination)) {
						HealthReply reply = new HealthReply();
						reply.setExaminationId(examination.getId());
						rule.autoReply(reply);
						healthReplyService.create(reply);
						task.setAuto(true);
						if (examination.getLevel() == null) {
							examination.setLevel(rule.getLevel());
						} else if (examination.getLevel() < rule.getLevel()) {
							examination.setLevel(rule.getLevel());
						}
					}
				}
				taskService.complete(task);
				update(examination);
			} else {
				taskService.pending(task);
			}
		} else {
			taskService.pending(task);
		}

		executorService.execute(new Runnable() {

			public void run() {
				try {
					// save the file
					DataProcessor processor = new DataProcessor();
					processor.process(uploadData, uploadData.length);
					float maxDaolian = processor.getMaxDaolian();
					float[] daolian = null;
					
					// 生成文件相对路径
					// TODO 文件后缀名固定
					daolian = processor.getDaolian_i();
					String ecg1Path = "user/"
							+ String.valueOf(authUser.getId())
							+ "/examination/" + examination.getId()
							+ "/ecg1.jpg";
					
					byte[] ecg1 = ECGChart.createChart(
							daolian, maxDaolian, 0,
							daolian.length, (int)(daolian.length*0.756), (int)37.8*8);
					String ecg1Uri = uploadService.save(Type.heart_img,
							ecg1Path, ecg1);

					daolian = processor.getDaolian_ii();
					String ecg2Path = "user/"
							+ String.valueOf(authUser.getId())
							+ "/examination/" + examination.getId()
							+ "/ecg2.jpg";
					byte[] ecg2 = ECGChart.createChart(
							daolian, maxDaolian, 0,
							daolian.length, (int)(daolian.length*0.756), (int)37.8*8);
					String ecg2Uri = uploadService.save(Type.heart_img,
							ecg2Path, ecg2);

					daolian = processor.getDaolian_iii();
					String ecg3Path = "user/"
							+ String.valueOf(authUser.getId())
							+ "/examination/" + examination.getId()
							+ "/ecg3.jpg";
					byte[] ecg3 = ECGChart.createChart(
							daolian, maxDaolian, 0,
							daolian.length, (int)(daolian.length*0.756), (int)37.8*8);
					String ecg3Uri = uploadService.save(Type.heart_img,
							ecg3Path, ecg3);

					daolian = processor.getDaolian_avr();
					String ecg4Path = "user/"
							+ String.valueOf(authUser.getId())
							+ "/examination/" + examination.getId()
							+ "/ecg4.jpg";
					byte[] ecg4 = ECGChart.createChart(
							daolian, maxDaolian, 0,
							daolian.length, (int)(daolian.length*0.756), (int)37.8*8);
					String ecg4Uri = uploadService.save(Type.heart_img,
							ecg4Path, ecg4);

					daolian = processor.getDaolian_avl();
					String ecg5Path = "user/"
							+ String.valueOf(authUser.getId())
							+ "/examination/" + examination.getId()
							+ "/ecg5.jpg";
					byte[] ecg5 = ECGChart.createChart(
							daolian, maxDaolian, 0,
							daolian.length, (int)(daolian.length*0.756), (int)37.8*8);
					String ecg5Uri = uploadService.save(Type.heart_img,
							ecg5Path, ecg5);

					daolian = processor.getDaolian_avf();
					String ecg6Path = "user/"
							+ String.valueOf(authUser.getId())
							+ "/examination/" + examination.getId()
							+ "/ecg6.jpg";
					byte[] ecg6 = ECGChart.createChart(
							daolian, maxDaolian, 0,
							daolian.length, (int)(daolian.length*0.756), (int)37.8*8);
					String ecg6Uri = uploadService.save(Type.heart_img,
							ecg6Path, ecg6);

					daolian = processor.getDaolian_v();
					String ecg7Path = "user/"
							+ String.valueOf(authUser.getId())
							+ "/examination/" + examination.getId()
							+ "/ecg7.jpg";
					byte[] ecg7 = ECGChart.createChart(
							daolian, maxDaolian, 0,
							daolian.length, (int)(daolian.length*0.756), (int)37.8*8);
					String ecg7Uri = uploadService.save(Type.heart_img,
							ecg7Path, ecg7);

					// 存储原始文件
					String rawPath = "user/" + String.valueOf(authUser.getId())
							+ "/examination/" + examination.getId() + "/raw";
					String rawUri = uploadService.save(Type.heart_img, rawPath,
							uploadData);

					examination.setHeartData(rawUri);
					update(examination);
				} catch (Exception e) {
					e.printStackTrace();
					examination.setHasDataError(true);
					update(examination);
				}
			}

		});
	}

	public void setAuthenticateService(AuthenticateService authenticateService) {
		this.authenticateService = authenticateService;
	}

}
