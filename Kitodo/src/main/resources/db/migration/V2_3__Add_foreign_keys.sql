--
-- Migration: Add foreign keys

-- 1. Add foreign keys
--

ALTER TABLE batch_x_process add constraint `FK_batch_x_process_batch_id`
foreign key (batch_id) REFERENCES batch(id);

ALTER TABLE batch_x_process add constraint `FK_batch_x_process_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE history add constraint `FK_history_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE process add constraint `FK_process_project_id`
foreign key (project_id) REFERENCES project(id);

ALTER TABLE process add constraint `FK_process_ruleset_id`
foreign key (ruleset_id) REFERENCES ruleset(id);

ALTER TABLE process add constraint `FK_process_docket_id`
foreign key (docket_id) REFERENCES docket(id);

ALTER TABLE processProperty add constraint `FK_processProperty_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE project_x_user add constraint `FK_project_x_user_project_id`
foreign key (project_id) REFERENCES project(id);

ALTER TABLE project_x_user add constraint `FK_project_x_user_user_id`
foreign key (user_id) REFERENCES user(id);

ALTER TABLE projectFileGroup add constraint `FK_projectFileGroup_project_id`
foreign key (project_id) REFERENCES project(id);

ALTER TABLE step add constraint `FK_step_processingUser_id`
foreign key (processingUser_id) REFERENCES user(id);

ALTER TABLE step add constraint `FK_step_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE step_x_user add constraint `FK_step_x_user_step_id`
foreign key (step_id) REFERENCES step(id);

ALTER TABLE step_x_user add constraint `FK_step_x_user_user_id`
foreign key (user_id) REFERENCES user(id);

ALTER TABLE step_x_userGroup add constraint `FK_step_x_userGroup_step_id`
foreign key (step_id) REFERENCES step(id);

ALTER TABLE step_x_userGroup add constraint `FK_step_x_userGroup_userGroup_id`
foreign key (userGroup_id) REFERENCES userGroup(id);

ALTER TABLE template add constraint `FK_template_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE templateProperty add constraint `FK_templateProperty_template_id`
foreign key (template_id) REFERENCES template(id);

ALTER TABLE user add constraint `FK_user_ldapGroup_id`
foreign key (ldapGroup_id) REFERENCES ldapGroup(id);

ALTER TABLE user_x_userGroup add constraint `FK_user_x_userGroup_user_id`
foreign key (user_id) REFERENCES user(id);

ALTER TABLE user_x_userGroup add constraint `FK_user_x_userGroup_userGroup_id`
foreign key (userGroup_id) REFERENCES userGroup(id);

ALTER TABLE userProperty add constraint `FK_userProperty_user_id`
foreign key (user_id) REFERENCES user(id);

ALTER TABLE workpiece add constraint `FK_workpiece_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE workpieceProperty add constraint `FK_workpieceProperty_workpiece_id`
foreign key (workpiece_id) REFERENCES workpiece(id);
